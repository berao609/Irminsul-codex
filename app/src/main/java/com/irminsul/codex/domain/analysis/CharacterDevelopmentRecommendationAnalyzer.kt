package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*

/**
 * Turns unified character assessments into deterministic, evidence-backed actions.
 * Only confirmed findings create build/stat actions. Missing analysis sources
 * create review actions rather than being interpreted as zero findings.
 */
object CharacterDevelopmentRecommendationAnalyzer {
    fun recommend(report: UnifiedCharacterAssessmentReport): CharacterDevelopmentRecommendationReport {
        val availability = when (report.availability) {
            UnifiedCharacterAssessmentAvailability.AVAILABLE ->
                CharacterDevelopmentRecommendationAvailability.AVAILABLE
            UnifiedCharacterAssessmentAvailability.STALE ->
                CharacterDevelopmentRecommendationAvailability.STALE
            UnifiedCharacterAssessmentAvailability.MISSING ->
                CharacterDevelopmentRecommendationAvailability.MISSING
            UnifiedCharacterAssessmentAvailability.UNAVAILABLE ->
                CharacterDevelopmentRecommendationAvailability.UNAVAILABLE
            UnifiedCharacterAssessmentAvailability.FAILED ->
                CharacterDevelopmentRecommendationAvailability.FAILED
        }
        if (report.availability !in setOf(
                UnifiedCharacterAssessmentAvailability.AVAILABLE,
                UnifiedCharacterAssessmentAvailability.STALE
            )) {
            return CharacterDevelopmentRecommendationReport(
                availability = availability,
                failureReason = report.failureReason
            )
        }

        val staleReport = report.availability == UnifiedCharacterAssessmentAvailability.STALE
        val recommendations = report.assessments.flatMap { assessment ->
            val stale = staleReport || assessment.basedOnStaleData
            buildList {
                val errors = assessment.buildFindings.filter {
                    it.severity == CharacterBuildInsightSeverity.ERROR
                }
                if (errors.isNotEmpty()) add(
                    recommendation(
                        assessment, CharacterDevelopmentRecommendationType.BUILD_ERRORS,
                        CharacterDevelopmentRecommendationPriority.P1,
                        "Resolve confirmed build errors",
                        "Correct the flagged build entries, then refresh the account data.",
                        "${errors.size} build error(s) were detected by deterministic validation.",
                        errors.map { "${it.code} at ${it.fieldPath}: ${it.message}" },
                        stale
                    )
                )

                if (assessment.statFindings.isNotEmpty()) add(
                    recommendation(
                        assessment, CharacterDevelopmentRecommendationType.STAT_MISMATCHES,
                        CharacterDevelopmentRecommendationPriority.P2,
                        "Investigate stat discrepancies",
                        "Verify the displayed base, additive, and final values in-game, then refresh the account data.",
                        "${assessment.statFindings.size} stat reconciliation mismatch(es) were detected.",
                        assessment.statFindings.map {
                            "${it.fieldPath} (${it.statName ?: "unnamed stat"}): reported ${it.reportedFinal}, expected ${it.expectedFinal}, difference ${it.difference}, tolerance ${it.roundingTolerance}"
                        },
                        stale || assessment.statFindings.any { it.basedOnStaleData }
                    )
                )

                val warnings = assessment.buildFindings.filter {
                    it.severity == CharacterBuildInsightSeverity.WARNING
                }
                if (warnings.isNotEmpty()) add(
                    recommendation(
                        assessment, CharacterDevelopmentRecommendationType.BUILD_WARNINGS,
                        CharacterDevelopmentRecommendationPriority.P3,
                        "Review build warnings",
                        "Review the flagged entries and confirm them against the in-game profile.",
                        "${warnings.size} build warning(s) were detected.",
                        warnings.map { "${it.code} at ${it.fieldPath}: ${it.message}" },
                        stale
                    )
                )

                val missing = assessment.completeness.missingOrUnverifiedComponents
                val unverified = assessment.completeness.unverifiedAnalyses
                if (missing.isNotEmpty() || unverified.isNotEmpty()) {
                    val evidence = missing.map { "Character data gap: $it" } +
                        unverified.map { "Analysis not verified: $it" }
                    add(
                        recommendation(
                            assessment, CharacterDevelopmentRecommendationType.DATA_REVIEW,
                            CharacterDevelopmentRecommendationPriority.P4,
                            "Complete or verify character data",
                            "Refresh the account data and make the listed components or analysis sources available before drawing stronger conclusions.",
                            "Some character details or required analysis inputs are incomplete or unverified.",
                            evidence,
                            stale
                        )
                    )
                }
            }
        }.sortedWith(
            compareBy<CharacterDevelopmentRecommendation> { priorityOrder(it.priority) }
                .thenBy { it.characterId }
                .thenBy { it.type.ordinal }
        )

        return CharacterDevelopmentRecommendationReport(
            availability = availability,
            recommendationCount = recommendations.size,
            affectedCharacterCount = recommendations.map { it.characterId }.distinct().size,
            recommendations = recommendations
        )
    }

    private fun recommendation(
        assessment: UnifiedCharacterAssessment,
        type: CharacterDevelopmentRecommendationType,
        priority: CharacterDevelopmentRecommendationPriority,
        title: String,
        action: String,
        reason: String,
        evidence: List<String>,
        stale: Boolean
    ) = CharacterDevelopmentRecommendation(
        recommendationId = "${assessment.characterId}-${type.name}",
        characterId = assessment.characterId,
        characterName = assessment.characterName,
        type = type,
        priority = priority,
        title = title,
        action = action,
        reason = reason,
        evidence = evidence,
        basedOnStaleData = stale
    )

    private fun priorityOrder(priority: CharacterDevelopmentRecommendationPriority): Int = when (priority) {
        CharacterDevelopmentRecommendationPriority.P1 -> 0
        CharacterDevelopmentRecommendationPriority.P2 -> 1
        CharacterDevelopmentRecommendationPriority.P3 -> 2
        CharacterDevelopmentRecommendationPriority.P4 -> 3
    }
}
