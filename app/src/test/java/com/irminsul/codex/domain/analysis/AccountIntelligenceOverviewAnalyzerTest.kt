package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class AccountIntelligenceOverviewAnalyzerTest {
    private val now = Instant.parse("2026-11-25T12:00:00Z")

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

    private fun recommendation(stale: Boolean = false) = CharacterDevelopmentRecommendation(
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

    private fun coverageSection(
        name: String,
        state: SnapshotPayloadState,
        issues: List<SnapshotCoverageIssue> = emptyList()
    ) = AccountSectionCoverage(
        sectionName = name,
        declaredStatus = SectionStatus.SUCCESS,
        payloadState = state,
        source = "account_export",
        retrievedAt = now,
        lastSuccessfulRetrievedAt = now,
        issues = issues
    )

    private fun freshnessSection(
        name: String,
        status: SectionFreshnessStatus
    ) = SectionFreshnessAssessment(
        sectionName = name,
        status = status,
        payloadState = when (status) {
            SectionFreshnessStatus.FRESH -> SnapshotPayloadState.PRESENT
            SectionFreshnessStatus.STALE -> SnapshotPayloadState.STALE
            SectionFreshnessStatus.UNKNOWN -> SnapshotPayloadState.UNAVAILABLE
            SectionFreshnessStatus.INVALID_TIMESTAMP -> SnapshotPayloadState.PRESENT
        },
        retrievedAt = if (status == SectionFreshnessStatus.INVALID_TIMESTAMP) null else now,
        lastSuccessfulRetrievedAt = null,
        referenceAt = now,
        timestampSource = if (status == SectionFreshnessStatus.UNKNOWN) {
            SectionFreshnessTimestampSource.NONE
        } else {
            SectionFreshnessTimestampSource.CURRENT_RETRIEVAL
        },
        assessedAt = now,
        ageSeconds = null,
        thresholdSeconds = null
    )

    private fun confidence(
        states: List<SnapshotPayloadState> = listOf(SnapshotPayloadState.PRESENT),
        freshnessStatuses: List<SectionFreshnessStatus>? = listOf(SectionFreshnessStatus.FRESH),
        issues: List<SnapshotCoverageIssue> = emptyList()
    ): AccountDataConfidenceSummary {
        val coverage = AccountSnapshotCoverageReport(
            states.mapIndexed { index, state ->
                coverageSection("section-$index", state, issues.filter { it.sectionName == "section-$index" })
            }
        )
        val freshness = freshnessStatuses?.let { statuses ->
            AccountSnapshotFreshnessReport(
                assessedAt = now,
                sections = statuses.mapIndexed { index, status ->
                    freshnessSection("section-$index", status)
                }
            )
        }
        return AccountDataConfidenceAnalyzer.summarize(coverage, freshness)
    }

    private fun compose(
        characterAssessment: UnifiedCharacterAssessmentReport = assessment(),
        goalAwareDevelopment: GoalAwareCharacterDevelopmentReport = prioritization(),
        accountGaps: AccountGapAnalysisReport = gaps(),
        dataConfidence: AccountDataConfidenceSummary = confidence()
    ) = AccountIntelligenceOverviewAnalyzer.compose(
        characterAssessment,
        goalAwareDevelopment,
        accountGaps,
        dataConfidence
    )

    @Test fun composesAllFourReportsAndExposesTheirSummaryCounts() {
        val sourceAssessment = assessment()
        val sourcePrioritization = prioritization()
        val sourceGaps = gaps()
        val sourceConfidence = confidence(
            states = listOf(SnapshotPayloadState.PRESENT, SnapshotPayloadState.PRESENT),
            freshnessStatuses = listOf(SectionFreshnessStatus.FRESH, SectionFreshnessStatus.FRESH)
        )

        val result = compose(
            sourceAssessment, sourcePrioritization, sourceGaps, sourceConfidence
        )

        assertSame(sourceAssessment, result.characterAssessment)
        assertSame(sourcePrioritization, result.goalAwareDevelopment)
        assertSame(sourceGaps, result.accountGaps)
        assertSame(sourceConfidence, result.dataConfidence)
        assertEquals(CharacterDevelopmentGoal.BUILD_RELIABILITY, result.selectedGoal)
        assertEquals(2, result.assessedCharacterCount)
        assertEquals(1, result.needsAttentionCount)
        assertEquals(1, result.reviewCharacterCount)
        assertEquals(0, result.clearCharacterCount)
        assertEquals(1, result.recommendationCount)
        assertEquals(1, result.affectedCharacterCount)
        assertEquals(1, result.confirmedAccountGapCount)
        assertEquals(1, result.accountDataReviewCount)
        assertEquals(2, result.accountSectionCount)
        assertEquals(2, result.presentAccountSectionCount)
        assertEquals(2, result.freshAccountSectionCount)
        assertFalse(result.hasStaleEvidence)
        assertFalse(result.hasUnverifiedSources)
        assertFalse(result.hasDataConfidenceConcerns)
    }

    @Test fun staleAccountDataIsIncludedWithoutChangingItsSourceReport() {
        val sourceConfidence = confidence(
            states = listOf(SnapshotPayloadState.STALE),
            freshnessStatuses = listOf(SectionFreshnessStatus.STALE)
        )

        val result = compose(dataConfidence = sourceConfidence)

        assertSame(sourceConfidence, result.dataConfidence)
        assertTrue(result.hasStaleEvidence)
        assertTrue(result.hasDataConfidenceConcerns)
        assertEquals(1, result.staleAccountSectionCount)
        assertEquals(1, result.staleFreshnessSectionCount)
        assertFalse(result.hasUnverifiedSources)
    }

    @Test fun unknownFreshnessCreatesUncertaintyButNotStaleEvidence() {
        val result = compose(
            dataConfidence = confidence(
                freshnessStatuses = listOf(SectionFreshnessStatus.UNKNOWN)
            )
        )

        assertEquals(1, result.unknownFreshnessSectionCount)
        assertEquals(0, result.staleFreshnessSectionCount)
        assertTrue(result.hasDataConfidenceConcerns)
        assertFalse(result.hasStaleEvidence)
    }

    @Test fun missingFreshnessAssessmentRemainsUnassessed() {
        val result = compose(dataConfidence = confidence(freshnessStatuses = null))

        assertFalse(result.dataConfidence.freshnessAssessed)
        assertTrue(result.hasUnassessedFreshness)
        assertTrue(result.hasDataConfidenceConcerns)
        assertFalse(result.hasStaleEvidence)
    }

    @Test fun incompleteCoverageAndCoverageIssuesRemainVisible() {
        val issue = SnapshotCoverageIssue(
            code = SnapshotCoverageIssueCode.SUCCESS_WITHOUT_PAYLOAD,
            sectionName = "section-0"
        )
        val result = compose(
            dataConfidence = confidence(
                states = listOf(
                    SnapshotPayloadState.MISSING,
                    SnapshotPayloadState.UNAVAILABLE,
                    SnapshotPayloadState.FAILED
                ),
                issues = listOf(issue)
            )
        )

        assertEquals(1, result.missingAccountSectionCount)
        assertEquals(1, result.unavailableAccountSectionCount)
        assertEquals(1, result.failedAccountSectionCount)
        assertTrue(result.hasIncompleteAccountData)
        assertTrue(result.hasAccountCoverageInconsistencies)
        assertTrue(result.hasDataConfidenceConcerns)
        assertFalse(result.hasStaleEvidence)
    }

    @Test fun staleAnalysisReportsRemainSeparateFromUnverifiedSources() {
        val result = compose(
            characterAssessment = assessment(UnifiedCharacterAssessmentAvailability.STALE),
            goalAwareDevelopment = prioritization(
                CharacterDevelopmentRecommendationAvailability.STALE, stale = true
            ),
            accountGaps = gaps(AccountGapAnalysisAvailability.STALE, stale = true)
        )

        assertTrue(result.hasStaleEvidence)
        assertFalse(result.hasUnverifiedSources)
        assertEquals(UnifiedCharacterAssessmentAvailability.STALE, result.characterAssessment.availability)
        assertEquals(CharacterDevelopmentRecommendationAvailability.STALE, result.goalAwareDevelopment.availability)
        assertEquals(AccountGapAnalysisAvailability.STALE, result.accountGaps.availability)
    }

    @Test fun missingUnavailableAndFailedAnalysisSourcesRemainExplicit() {
        val result = compose(
            characterAssessment = assessment(UnifiedCharacterAssessmentAvailability.MISSING, null, 0, 0, 0),
            goalAwareDevelopment = prioritization(CharacterDevelopmentRecommendationAvailability.UNAVAILABLE),
            accountGaps = gaps(AccountGapAnalysisAvailability.FAILED)
        )

        assertTrue(result.hasUnverifiedSources)
        assertFalse(result.hasStaleEvidence)
        assertEquals(UnifiedCharacterAssessmentAvailability.MISSING, result.characterAssessment.availability)
        assertEquals(CharacterDevelopmentRecommendationAvailability.UNAVAILABLE, result.goalAwareDevelopment.availability)
        assertEquals(AccountGapAnalysisAvailability.FAILED, result.accountGaps.availability)
    }

    @Test fun availableEmptyReportsProduceZeroCountsWithoutInventedFindings() {
        val result = compose(
            characterAssessment = UnifiedCharacterAssessmentReport(
                availability = UnifiedCharacterAssessmentAvailability.AVAILABLE,
                assessedCharacterCount = 0
            ),
            goalAwareDevelopment = GoalAwareCharacterDevelopmentReport(
                availability = CharacterDevelopmentRecommendationAvailability.AVAILABLE,
                selectedGoal = CharacterDevelopmentGoal.BALANCED_DEVELOPMENT
            ),
            accountGaps = AccountGapAnalysisReport(
                availability = AccountGapAnalysisAvailability.AVAILABLE,
                assessedCharacterCount = 0
            ),
            dataConfidence = confidence(
                states = emptyList(),
                freshnessStatuses = emptyList()
            )
        )

        assertEquals(0, result.assessedCharacterCount)
        assertEquals(0, result.recommendationCount)
        assertEquals(0, result.confirmedAccountGapCount)
        assertEquals(0, result.accountDataReviewCount)
        assertEquals(0, result.accountSectionCount)
        assertEquals(0, result.freshAccountSectionCount)
        assertFalse(result.hasStaleEvidence)
        assertFalse(result.hasUnverifiedSources)
        assertFalse(result.hasDataConfidenceConcerns)
    }

    @Test fun oneUnverifiedAnalysisSourceDoesNotEraseOtherAvailableEvidence() {
        val availableAssessment = assessment()
        val availablePrioritization = prioritization()
        val failedGaps = gaps(AccountGapAnalysisAvailability.FAILED)

        val result = compose(
            characterAssessment = availableAssessment,
            goalAwareDevelopment = availablePrioritization,
            accountGaps = failedGaps
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
