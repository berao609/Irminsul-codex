package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*

/**
 * Reorders existing evidence-backed recommendations for an explicit player goal.
 * Each objective has a distinct category order. Base priority, evidence, actions,
 * and freshness are never modified. Objectives cannot surface findings that do not exist.
 */
object GoalAwareCharacterDevelopmentAnalyzer {
    fun prioritize(
        report: CharacterDevelopmentRecommendationReport,
        goal: CharacterDevelopmentGoal
    ): GoalAwareCharacterDevelopmentReport {
        val ordered = report.recommendations
            .sortedWith(
                compareBy<CharacterDevelopmentRecommendation> { goalRank(it.type, goal) }
                    .thenBy { it.characterId }
                    .thenBy { it.priority.ordinal }
                    .thenBy { it.type.ordinal }
                    .thenBy { it.recommendationId }
            )
            .map { recommendation ->
                GoalAwareCharacterRecommendation(
                    recommendation = recommendation,
                    goalPriority = goalRank(recommendation.type, goal) + 1,
                    prioritizationReason = reasonFor(recommendation.type, goal)
                )
            }

        return GoalAwareCharacterDevelopmentReport(
            availability = report.availability,
            selectedGoal = goal,
            recommendationCount = ordered.size,
            affectedCharacterCount = ordered.map { it.recommendation.characterId }.distinct().size,
            recommendations = ordered,
            failureReason = report.failureReason
        )
    }

    private fun goalRank(
        type: CharacterDevelopmentRecommendationType,
        goal: CharacterDevelopmentGoal
    ): Int = when (goal) {
        CharacterDevelopmentGoal.BALANCED_DEVELOPMENT -> when (type) {
            CharacterDevelopmentRecommendationType.BUILD_ERRORS -> 0
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> 1
            CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> 2
            CharacterDevelopmentRecommendationType.DATA_REVIEW -> 3
        }
        CharacterDevelopmentGoal.BUILD_RELIABILITY -> when (type) {
            CharacterDevelopmentRecommendationType.BUILD_ERRORS -> 0
            CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> 1
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> 2
            CharacterDevelopmentRecommendationType.DATA_REVIEW -> 3
        }
        CharacterDevelopmentGoal.STAT_INTEGRITY -> when (type) {
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> 0
            CharacterDevelopmentRecommendationType.BUILD_ERRORS -> 1
            CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> 2
            CharacterDevelopmentRecommendationType.DATA_REVIEW -> 3
        }
        CharacterDevelopmentGoal.DATA_CONFIDENCE -> when (type) {
            CharacterDevelopmentRecommendationType.DATA_REVIEW -> 0
            CharacterDevelopmentRecommendationType.BUILD_ERRORS -> 1
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> 2
            CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> 3
        }
    }

    private fun reasonFor(
        type: CharacterDevelopmentRecommendationType,
        goal: CharacterDevelopmentGoal
    ): String = when (goal) {
        CharacterDevelopmentGoal.BALANCED_DEVELOPMENT -> when (type) {
            CharacterDevelopmentRecommendationType.BUILD_ERRORS -> "Balanced development addresses confirmed build errors first."
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> "Balanced development next verifies numerical inconsistencies."
            CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> "Balanced development then reviews potential build issues."
            CharacterDevelopmentRecommendationType.DATA_REVIEW -> "Balanced development also resolves data gaps before stronger conclusions."
        }
        CharacterDevelopmentGoal.BUILD_RELIABILITY -> when (type) {
            CharacterDevelopmentRecommendationType.BUILD_ERRORS -> "Build reliability prioritizes confirmed validation errors."
            CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> "Build reliability reviews warnings before unrelated analysis."
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> "Stat discrepancies remain visible but follow build reliability issues."
            CharacterDevelopmentRecommendationType.DATA_REVIEW -> "Data gaps remain visible but follow verified build findings."
        }
        CharacterDevelopmentGoal.STAT_INTEGRITY -> when (type) {
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> "Stat integrity prioritizes confirmed reconciliation discrepancies."
            CharacterDevelopmentRecommendationType.BUILD_ERRORS -> "Confirmed build errors follow the selected stat-integrity objective."
            CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> "Build warnings remain secondary to stat integrity."
            CharacterDevelopmentRecommendationType.DATA_REVIEW -> "Data gaps remain visible after confirmed stat and build findings."
        }
        CharacterDevelopmentGoal.DATA_CONFIDENCE -> when (type) {
            CharacterDevelopmentRecommendationType.DATA_REVIEW -> "Data confidence first resolves missing or unverified inputs."
            CharacterDevelopmentRecommendationType.BUILD_ERRORS -> "Confirmed build errors remain important after data-confidence gaps."
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> "Stat discrepancies follow data-confidence and build-error actions."
            CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> "Build warnings remain visible after higher-confidence priorities."
        }
    }
}
