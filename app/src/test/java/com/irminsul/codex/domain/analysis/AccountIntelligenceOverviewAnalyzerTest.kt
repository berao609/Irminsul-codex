package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class AccountIntelligenceOverviewAnalyzerTest {
    private fun assessment(
        availability: UnifiedCharacterAssessmentAvailability = UnifiedCharacterAssessmentAvailability.AVAILABLE,
        assessedCount: Int? = 2,
        needsAttention: Int = 1,
        review: Int = 1,
        clear: Int = 0
    ) = UnifiedCharacterAssessmentReport(
        availability = availability,
        assessedCharacterCount = assessedCount,
        needsAttentionCount = needsAttention,
        reviewCount = review,
        clearCount = clear
    )

    private fun recommendation(
        stale: Boolean = false
    ) = CharacterDevelopmentRecommendation(
        recommendationId = "character-100-build-errors",
        characterId = 100,
        characterName = "Example",
        type = CharacterDevelopmentRecommendationType.BUILD_ERRORS,
        priority = CharacterDevelopmentRecommendationPriority.P1,
        title = "Resolve build errors",
        action = "Review the reported build issues",
        reason = "A verified build error exists",
        evidence = listOf("source finding"),
        basedOnStaleData = stale
    )

    private fun prioritization(
        availability: CharacterDevelopmentRecommendationAvailability = CharacterDevelopmentRecommendationAvailability.AVAILABLE,
        stale: Boolean = false
    ) = GoalAwareCharacterDevelopmentReport(
        availability = availability,
        selectedGoal = CharacterDevelopmentGoal.BUILD_RELIABILITY,
        recommendationCount = 1,
        affectedCharacterCount = 1,
        recommendations = listOf(
            GoalAwareCharacterRecommendation(
                recommendation = recommendation(stale),
                goalPriority = 1,
                prioritizationReason = "Build reliability prioritizes confirmed validation errors."
            )
        )
    )

    private fun gaps(
        availability: AccountGapAnalysisAvailability = AccountGapAnalysisAvailability.AVAILABLE,
        stale: Boolean = false
    ) = AccountGapAnalysisReport(
        availability = availability,
        assessedCharacterCount = 2,
        confirmedGapCount = 1,
        dataReviewCount = 1,
        findings = listOf(
            AccountGapFinding(
                findingId = "100-artifact-slots",
                type = AccountGapType.ARTIFACT_SLOT_GAP,
                confidence = AccountGapConfidence.CONFIRMED,
                characterId = 100,
                characterName = "Example",
                title = "Artifact slots are unoccupied",
                explanation = "The retrieved equipment data identifies unoccupied slots.",
                evidence = listOf("Unoccupied slots: 4, 5"),
                basedOnStaleData = stale
            )
        )
    )

    @Test fun composesReportsAndExposesTheirSummaryCounts() {
        val sourceAssessment = assessment()
        val sourcePrioritization = prioritization()
        val sourceGaps = gaps()

        val result = AccountIntelligenceOverviewAnalyzer.compose(
            sourceAssessment, sourcePrioritization, sourceGaps
        )

        assertSame(sourceAssessment, result.characterAssessment)
        assertSame(sourcePrioritization, result.goalAwareDevelopment)
        assertSame(sourceGaps, result.accountGaps)
        assertEquals(CharacterDevelopmentGoal.BUILD_RELIABILITY, result.selectedGoal)
        assertEquals(2, result.assessedCharacterCount)
        assertEquals(1, result.needsAttentionCount)
        assertEquals(1, result.reviewCharacterCount)
        assertEquals(0, result.clearCharacterCount)
        assertEquals(1, result.recommendationCount)
        assertEquals(1, result.affectedCharacterCount)
        assertEquals(1, result.confirmedAccountGapCount)
        assertEquals(1, result.accountDataReviewCount)
        assertFalse(result.hasStaleEvidence)
        assertFalse(result.hasUnverifiedSources)
    }

    @Test fun staleAvailabilityAndStaleFindingsAreSurfacedSeparatelyFromMissingData() {
        val staleAssessment = assessment(UnifiedCharacterAssessmentAvailability.STALE)
        val stalePrioritization = prioritization(
            CharacterDevelopmentRecommendationAvailability.STALE, stale = true
        )
        val staleGaps = gaps(AccountGapAnalysisAvailability.STALE, stale = true)

        val result = AccountIntelligenceOverviewAnalyzer.compose(
            staleAssessment, stalePrioritization, staleGaps
        )

        assertTrue(result.hasStaleEvidence)
        assertFalse(result.hasUnverifiedSources)
        assertEquals(UnifiedCharacterAssessmentAvailability.STALE, result.characterAssessment.availability)
        assertEquals(CharacterDevelopmentRecommendationAvailability.STALE, result.goalAwareDevelopment.availability)
        assertEquals(AccountGapAnalysisAvailability.STALE, result.accountGaps.availability)
    }

    @Test fun missingUnavailableAndFailedSourcesRemainExplicit() {
        val result = AccountIntelligenceOverviewAnalyzer.compose(
            assessment(UnifiedCharacterAssessmentAvailability.MISSING, null, 0, 0, 0),
            prioritization(CharacterDevelopmentRecommendationAvailability.UNAVAILABLE),
            gaps(AccountGapAnalysisAvailability.FAILED)
        )

        assertTrue(result.hasUnverifiedSources)
        assertFalse(result.hasStaleEvidence)
        assertEquals(UnifiedCharacterAssessmentAvailability.MISSING, result.characterAssessment.availability)
        assertEquals(CharacterDevelopmentRecommendationAvailability.UNAVAILABLE, result.goalAwareDevelopment.availability)
        assertEquals(AccountGapAnalysisAvailability.FAILED, result.accountGaps.availability)
    }

    @Test fun availableEmptyReportsProduceZeroCountsWithoutInventedFindings() {
        val result = AccountIntelligenceOverviewAnalyzer.compose(
            UnifiedCharacterAssessmentReport(
                availability = UnifiedCharacterAssessmentAvailability.AVAILABLE,
                assessedCharacterCount = 0
            ),
            GoalAwareCharacterDevelopmentReport(
                availability = CharacterDevelopmentRecommendationAvailability.AVAILABLE,
                selectedGoal = CharacterDevelopmentGoal.BALANCED_DEVELOPMENT
            ),
            AccountGapAnalysisReport(
                availability = AccountGapAnalysisAvailability.AVAILABLE,
                assessedCharacterCount = 0
            )
        )

        assertEquals(0, result.assessedCharacterCount)
        assertEquals(0, result.recommendationCount)
        assertEquals(0, result.confirmedAccountGapCount)
        assertEquals(0, result.accountDataReviewCount)
        assertFalse(result.hasStaleEvidence)
        assertFalse(result.hasUnverifiedSources)
    }

    @Test fun oneUnverifiedSourceDoesNotEraseOtherAvailableEvidence() {
        val availableAssessment = assessment()
        val availablePrioritization = prioritization()
        val failedGaps = gaps(AccountGapAnalysisAvailability.FAILED)

        val result = AccountIntelligenceOverviewAnalyzer.compose(
            availableAssessment, availablePrioritization, failedGaps
        )

        assertTrue(result.hasUnverifiedSources)
        assertEquals(2, result.assessedCharacterCount)
        assertEquals(1, result.recommendationCount)
        assertEquals(1, result.needsAttentionCount)
        assertSame(availableAssessment, result.characterAssessment)
        assertSame(availablePrioritization, result.goalAwareDevelopment)
        assertSame(failedGaps, result.accountGaps)
    }
}
