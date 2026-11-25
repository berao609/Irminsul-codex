package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*

/**
 * Aggregates M21 character-build findings into an account-level priority overview.
 *
 * Priority is based only on existing validation severity:
 * errors require attention before warning-only findings. No score, meta ranking,
 * or combat-strength inference is produced.
 */
object CharacterBuildAccountSummaryAnalyzer {
    fun summarize(report: CharacterBuildAnalysisReport): AccountCharacterBuildSummary {
        if (report.availability !in setOf(
                CharacterBuildAnalysisAvailability.AVAILABLE,
                CharacterBuildAnalysisAvailability.STALE
            )
        ) {
            return AccountCharacterBuildSummary(
                availability = report.availability,
                failureReason = report.failureReason
            )
        }

        val prioritized = report.insights
            .groupBy { it.characterId }
            .map { (characterId, findings) ->
                val errors = findings.count {
                    it.severity == CharacterBuildInsightSeverity.ERROR
                }
                val warnings = findings.count {
                    it.severity == CharacterBuildInsightSeverity.WARNING
                }
                val orderedFindings = findings.sortedWith(
                    compareBy<CharacterBuildInsight> {
                        if (it.severity == CharacterBuildInsightSeverity.ERROR) 0 else 1
                    }
                        .thenBy { it.fieldPath }
                        .thenBy { it.code }
                )

                CharacterBuildPrioritySummary(
                    characterId = characterId,
                    characterName = findings.firstNotNullOfOrNull { it.characterName },
                    attentionLevel = if (errors > 0) {
                        CharacterBuildAttentionLevel.NEEDS_ATTENTION
                    } else {
                        CharacterBuildAttentionLevel.REVIEW
                    },
                    errorCount = errors,
                    warningCount = warnings,
                    findings = orderedFindings
                )
            }
            .sortedWith(
                compareByDescending<CharacterBuildPrioritySummary> { it.errorCount }
                    .thenByDescending { it.warningCount }
                    .thenBy { it.characterId }
            )

        return AccountCharacterBuildSummary(
            availability = report.availability,
            analyzedCharacterCount = report.analyzedCharacterCount,
            affectedCharacterCount = prioritized.size,
            errorCount = report.errorCount,
            warningCount = report.warningCount,
            prioritizedCharacters = prioritized,
            failureReason = report.failureReason
        )
    }
}
