package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*

/**
 * Combines build validation, stat reconciliation, and explicit data-completeness
 * evidence. This is an assessment, not a score or combat-strength ranking.
 */
object UnifiedCharacterAssessmentAnalyzer {
    fun assess(
        build: CharacterBuildAnalysisReport,
        stats: CharacterStatAnalysisReport,
        characters: DataValue<List<CharacterSnapshot>>
    ): UnifiedCharacterAssessmentReport {
        val buildOk = build.availability in setOf(
            CharacterBuildAnalysisAvailability.AVAILABLE,
            CharacterBuildAnalysisAvailability.STALE
        )
        val statsOk = stats.availability in setOf(
            CharacterStatAnalysisAvailability.AVAILABLE,
            CharacterStatAnalysisAvailability.STALE
        )
        val characterList = when (characters) {
            is DataValue.Available -> characters.value
            is DataValue.Stale -> characters.value
            else -> emptyList()
        }
        if (!buildOk && !statsOk && characterList.isEmpty()) {
            val state = when {
                build.availability == CharacterBuildAnalysisAvailability.FAILED ||
                    stats.availability == CharacterStatAnalysisAvailability.FAILED ->
                    UnifiedCharacterAssessmentAvailability.FAILED
                build.availability == CharacterBuildAnalysisAvailability.UNAVAILABLE ||
                    stats.availability == CharacterStatAnalysisAvailability.UNAVAILABLE ->
                    UnifiedCharacterAssessmentAvailability.UNAVAILABLE
                else -> UnifiedCharacterAssessmentAvailability.MISSING
            }
            return UnifiedCharacterAssessmentReport(
                availability = state,
                failureReason = build.failureReason ?: stats.failureReason
            )
        }

        val staleCharacters = characters is DataValue.Stale
        val characterById = characterList.associateBy { it.id }
        val buildById = build.insights.groupBy { it.characterId }
        val statById = stats.insights.groupBy { it.characterId }
        val ids = (characterList.map { it.id } + buildById.keys + statById.keys).distinct().sorted()

        val assessments = ids.map { id ->
            val character = characterById[id]
            val buildFindings = buildById[id].orEmpty().sortedWith(
                compareBy<CharacterBuildInsight> { if (it.severity == CharacterBuildInsightSeverity.ERROR) 0 else 1 }
                    .thenBy { it.fieldPath }.thenBy { it.code }
            )
            val statFindings = statById[id].orEmpty().sortedWith(
                compareBy<CharacterStatInsight> { it.fieldPath }.thenBy { it.statName.orEmpty() }
            )
            val completeness = completeness(character, build.availability, stats.availability)
            val errors = buildFindings.count { it.severity == CharacterBuildInsightSeverity.ERROR }
            val warnings = buildFindings.count { it.severity == CharacterBuildInsightSeverity.WARNING }
            val mismatches = statFindings.size
            val attention = when {
                errors > 0 || mismatches > 0 -> CharacterAttentionStatus.NEEDS_ATTENTION
                warnings > 0 || completeness.level != CharacterCompletenessLevel.COMPLETE ||
                    completeness.unverifiedAnalyses.isNotEmpty() ->
                    CharacterAttentionStatus.REVIEW
                else -> CharacterAttentionStatus.CLEAR
            }
            UnifiedCharacterAssessment(
                characterId = id,
                characterName = character?.name ?: buildFindings.firstNotNullOfOrNull { it.characterName }
                    ?: statFindings.firstNotNullOfOrNull { it.characterName },
                attentionStatus = attention,
                buildErrors = errors,
                buildWarnings = warnings,
                statMismatches = mismatches,
                completeness = completeness,
                buildFindings = buildFindings,
                statFindings = statFindings,
                basedOnStaleData = staleCharacters || buildFindings.any { it.basedOnStaleData } ||
                    statFindings.any { it.basedOnStaleData }
            )
        }.sortedWith(
            compareBy<UnifiedCharacterAssessment> { statusOrder(it.attentionStatus) }
                .thenByDescending { it.buildErrors }
                .thenByDescending { it.statMismatches }
                .thenByDescending { it.buildWarnings }
                .thenBy { it.characterId }
        )

        val availability = when {
            staleCharacters || build.availability == CharacterBuildAnalysisAvailability.STALE ||
                stats.availability == CharacterStatAnalysisAvailability.STALE ->
                UnifiedCharacterAssessmentAvailability.STALE
            else -> UnifiedCharacterAssessmentAvailability.AVAILABLE
        }
        return UnifiedCharacterAssessmentReport(
            availability = availability,
            assessedCharacterCount = assessments.size,
            needsAttentionCount = assessments.count { it.attentionStatus == CharacterAttentionStatus.NEEDS_ATTENTION },
            reviewCount = assessments.count { it.attentionStatus == CharacterAttentionStatus.REVIEW },
            clearCount = assessments.count { it.attentionStatus == CharacterAttentionStatus.CLEAR },
            assessments = assessments
        )
    }

    private fun completeness(
        character: CharacterSnapshot?,
        buildAvailability: CharacterBuildAnalysisAvailability,
        statAvailability: CharacterStatAnalysisAvailability
    ): CharacterCompletenessEvidence {
        val missing = mutableListOf<String>()
        val unverified = mutableListOf<String>()
        if (buildAvailability !in setOf(CharacterBuildAnalysisAvailability.AVAILABLE, CharacterBuildAnalysisAvailability.STALE)) {
            unverified += when (buildAvailability) {
                CharacterBuildAnalysisAvailability.MISSING -> "Build validation missing"
                CharacterBuildAnalysisAvailability.UNAVAILABLE -> "Build validation unavailable"
                CharacterBuildAnalysisAvailability.FAILED -> "Build validation failed"
                else -> "Build validation not verified"
            }
        }
        if (statAvailability !in setOf(CharacterStatAnalysisAvailability.AVAILABLE, CharacterStatAnalysisAvailability.STALE)) {
            unverified += when (statAvailability) {
                CharacterStatAnalysisAvailability.MISSING -> "Stat reconciliation missing"
                CharacterStatAnalysisAvailability.UNAVAILABLE -> "Stat reconciliation unavailable"
                CharacterStatAnalysisAvailability.FAILED -> "Stat reconciliation failed"
                else -> "Stat reconciliation not verified"
            }
        }
        if (character == null) {
            missing += "Character details are unavailable."
        } else {
            if (character.level == null) missing += "Character level"
            if (character.rarity == null) missing += "Character rarity"
            when (character.weapon) {
                DataValue.Missing -> missing += "Weapon data missing"
                DataValue.Unavailable -> missing += "Weapon data unavailable"
                is DataValue.Failed -> missing += "Weapon retrieval failed"
                is DataValue.Stale -> missing += "Weapon data stale"
                is DataValue.Available -> Unit
            }
            when (character.artifacts) {
                DataValue.Missing -> missing += "Artifact data missing"
                DataValue.Unavailable -> missing += "Artifact data unavailable"
                is DataValue.Failed -> missing += "Artifact retrieval failed"
                is DataValue.Stale -> missing += "Artifact data stale"
                is DataValue.Available -> if (character.artifacts.value.size < 5) {
                    missing += "Artifact collection has fewer than five entries"
                }
            }
            if (character.selectedProperties.isEmpty() && character.baseProperties.isEmpty() &&
                character.extraProperties.isEmpty() && character.elementProperties.isEmpty()) {
                missing += "Character stat collections"
            }
        }
        val level = when {
            missing.isEmpty() -> CharacterCompletenessLevel.COMPLETE
            character == null || character.level == null || character.rarity == null ->
                CharacterCompletenessLevel.INSUFFICIENT
            else -> CharacterCompletenessLevel.PARTIAL
        }
        return CharacterCompletenessEvidence(missing, level, unverified)
    }

    private fun statusOrder(status: CharacterAttentionStatus): Int = when (status) {
        CharacterAttentionStatus.NEEDS_ATTENTION -> 0
        CharacterAttentionStatus.REVIEW -> 1
        CharacterAttentionStatus.CLEAR -> 2
    }
}
