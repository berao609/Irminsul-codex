package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class GoalAwareCharacterDevelopmentAnalyzerTest {
    private fun recommendation(
        id: Int,
        type: CharacterDevelopmentRecommendationType,
        priority: CharacterDevelopmentRecommendationPriority
    ) = CharacterDevelopmentRecommendation(
        recommendationId = "$id-${type.name}",
        characterId = id,
        characterName = "Character$id",
        type = type,
        priority = priority,
        title = "Title",
        action = "Action",
        reason = "Original reason",
        evidence = listOf("source evidence"),
        basedOnStaleData = false
    )

    private fun report() = CharacterDevelopmentRecommendationReport(
        availability = CharacterDevelopmentRecommendationAvailability.AVAILABLE,
        recommendations = listOf(
            recommendation(4, CharacterDevelopmentRecommendationType.DATA_REVIEW, CharacterDevelopmentRecommendationPriority.P4),
            recommendation(3, CharacterDevelopmentRecommendationType.BUILD_WARNINGS, CharacterDevelopmentRecommendationPriority.P3),
            recommendation(2, CharacterDevelopmentRecommendationType.STAT_MISMATCHES, CharacterDevelopmentRecommendationPriority.P2),
            recommendation(1, CharacterDevelopmentRecommendationType.BUILD_ERRORS, CharacterDevelopmentRecommendationPriority.P1)
        )
    )

    @Test fun balancedGoalPreservesDefaultM25Ordering() {
        val source = report()
        val result = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            source, CharacterDevelopmentGoal.BALANCED_DEVELOPMENT
        )
        assertEquals(
            listOf(
                CharacterDevelopmentRecommendationType.BUILD_ERRORS,
                CharacterDevelopmentRecommendationType.STAT_MISMATCHES,
                CharacterDevelopmentRecommendationType.BUILD_WARNINGS,
                CharacterDevelopmentRecommendationType.DATA_REVIEW
            ),
            result.recommendations.map { it.recommendation.type }
        )
        assertEquals(listOf(1, 2, 3, 4), result.recommendations.map { it.goalPriority })
    }

    @Test fun buildReliabilityPrioritizesBuildErrorsThenWarnings() {
        val result = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            report(), CharacterDevelopmentGoal.BUILD_RELIABILITY
        )
        assertEquals(
            listOf(
                CharacterDevelopmentRecommendationType.BUILD_ERRORS,
                CharacterDevelopmentRecommendationType.BUILD_WARNINGS,
                CharacterDevelopmentRecommendationType.STAT_MISMATCHES,
                CharacterDevelopmentRecommendationType.DATA_REVIEW
            ),
            result.recommendations.map { it.recommendation.type }
        )
    }

    @Test fun statIntegrityPrioritizesStatDiscrepancies() {
        val result = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            report(), CharacterDevelopmentGoal.STAT_INTEGRITY
        )
        assertEquals(
            CharacterDevelopmentRecommendationType.STAT_MISMATCHES,
            result.recommendations.first().recommendation.type
        )
        assertEquals(1, result.recommendations.first().goalPriority)
    }

    @Test fun dataConfidencePrioritizesMissingAndUnverifiedInputs() {
        val result = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            report(), CharacterDevelopmentGoal.DATA_CONFIDENCE
        )
        assertEquals(
            CharacterDevelopmentRecommendationType.DATA_REVIEW,
            result.recommendations.first().recommendation.type
        )
        assertTrue(result.recommendations.first().prioritizationReason.contains("missing or unverified"))
    }

    @Test fun goalChangesOrderingWithoutChangingBasePriorityOrEvidence() {
        val source = report()
        val original = source.recommendations.associateBy { it.recommendationId }
        val result = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            source, CharacterDevelopmentGoal.DATA_CONFIDENCE
        )
        result.recommendations.forEach { ranked ->
            val before = original.getValue(ranked.recommendation.recommendationId)
            assertEquals(before.priority, ranked.recommendation.priority)
            assertEquals(before.evidence, ranked.recommendation.evidence)
            assertEquals(before.action, ranked.recommendation.action)
            assertEquals(before.reason, ranked.recommendation.reason)
        }
    }

    @Test fun tiesAreOrderedDeterministicallyByCharacterId() {
        val source = CharacterDevelopmentRecommendationReport(
            availability = CharacterDevelopmentRecommendationAvailability.AVAILABLE,
            recommendations = listOf(
                recommendation(9, CharacterDevelopmentRecommendationType.STAT_MISMATCHES, CharacterDevelopmentRecommendationPriority.P2),
                recommendation(2, CharacterDevelopmentRecommendationType.STAT_MISMATCHES, CharacterDevelopmentRecommendationPriority.P2)
            )
        )
        val result = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            source, CharacterDevelopmentGoal.STAT_INTEGRITY
        )
        assertEquals(listOf(2, 9), result.recommendations.map { it.recommendation.characterId })
    }

    @Test fun unavailableSourceStateAndFailureReasonArePreserved() {
        val source = CharacterDevelopmentRecommendationReport(
            availability = CharacterDevelopmentRecommendationAvailability.UNAVAILABLE,
            failureReason = "Assessment source unavailable"
        )
        val result = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            source, CharacterDevelopmentGoal.DATA_CONFIDENCE
        )
        assertEquals(CharacterDevelopmentRecommendationAvailability.UNAVAILABLE, result.availability)
        assertEquals(CharacterDevelopmentGoal.DATA_CONFIDENCE, result.selectedGoal)
        assertEquals("Assessment source unavailable", result.failureReason)
        assertTrue(result.recommendations.isEmpty())
        assertEquals(0, result.recommendationCount)
        assertEquals(0, result.affectedCharacterCount)
    }

    @Test fun staleAvailabilityAndStaleEvidenceRemainUnchanged() {
        val staleRecommendation = recommendation(
            7, CharacterDevelopmentRecommendationType.DATA_REVIEW,
            CharacterDevelopmentRecommendationPriority.P4
        ).copy(basedOnStaleData = true)
        val source = CharacterDevelopmentRecommendationReport(
            availability = CharacterDevelopmentRecommendationAvailability.STALE,
            recommendations = listOf(staleRecommendation)
        )
        val result = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            source, CharacterDevelopmentGoal.DATA_CONFIDENCE
        )
        assertEquals(CharacterDevelopmentRecommendationAvailability.STALE, result.availability)
        assertTrue(result.recommendations.single().recommendation.basedOnStaleData)
    }
}
