package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class AccountDataConfidenceAnalyzerTest {
    private val now = Instant.parse("2026-11-25T12:00:00Z")

    private fun section(
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

    private fun freshness(
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

    @Test fun summarizesCoverageAndFreshnessCountsWithoutChangingSourceReports() {
        val coverage = AccountSnapshotCoverageReport(
            listOf(
                section("characters", SnapshotPayloadState.PRESENT),
                section("profile", SnapshotPayloadState.STALE),
                section("abyss", SnapshotPayloadState.MISSING),
                section("theater", SnapshotPayloadState.UNAVAILABLE),
                section("details", SnapshotPayloadState.FAILED)
            )
        )
        val freshness = AccountSnapshotFreshnessReport(
            assessedAt = now,
            sections = listOf(
                freshness("characters", SectionFreshnessStatus.FRESH),
                freshness("profile", SectionFreshnessStatus.STALE),
                freshness("abyss", SectionFreshnessStatus.UNKNOWN),
                freshness("theater", SectionFreshnessStatus.INVALID_TIMESTAMP)
            )
        )

        val result = AccountDataConfidenceAnalyzer.summarize(coverage, freshness)

        assertSame(coverage, result.coverage)
        assertSame(freshness, result.freshness)
        assertEquals(5, result.sectionCount)
        assertEquals(1, result.presentSectionCount)
        assertEquals(1, result.staleCoverageSectionCount)
        assertEquals(1, result.missingSectionCount)
        assertEquals(1, result.unavailableSectionCount)
        assertEquals(1, result.failedSectionCount)
        assertEquals(1, result.freshSectionCount)
        assertEquals(1, result.staleFreshnessCount)
        assertEquals(1, result.unknownFreshnessCount)
        assertEquals(1, result.invalidTimestampCount)
        assertTrue(result.hasStaleSections)
        assertTrue(result.hasIncompleteSectionData)
        assertTrue(result.hasFreshnessUncertainty)
        assertTrue(result.hasDataConfidenceConcerns)
    }

    @Test fun coverageInconsistenciesRemainExplicit() {
        val issue = SnapshotCoverageIssue(
            code = SnapshotCoverageIssueCode.SUCCESS_WITHOUT_PAYLOAD,
            sectionName = "characters"
        )
        val coverage = AccountSnapshotCoverageReport(
            listOf(section("characters", SnapshotPayloadState.MISSING, listOf(issue)))
        )

        val result = AccountDataConfidenceAnalyzer.summarize(coverage, null)

        assertEquals(1, result.coverageIssueCount)
        assertTrue(result.hasCoverageInconsistencies)
        assertTrue(result.hasFreshnessUncertainty)
        assertTrue(result.hasDataConfidenceConcerns)
    }

    @Test fun unknownFreshnessIsNotCountedAsStale() {
        val coverage = AccountSnapshotCoverageReport(
            listOf(section("characters", SnapshotPayloadState.PRESENT))
        )
        val freshness = AccountSnapshotFreshnessReport(
            assessedAt = now,
            sections = listOf(freshness("characters", SectionFreshnessStatus.UNKNOWN))
        )

        val result = AccountDataConfidenceAnalyzer.summarize(coverage, freshness)

        assertEquals(0, result.staleCoverageSectionCount)
        assertEquals(0, result.staleFreshnessCount)
        assertEquals(1, result.unknownFreshnessCount)
        assertFalse(result.hasStaleSections)
        assertTrue(result.hasFreshnessUncertainty)
    }

    @Test fun unavailableFreshnessAssessmentIsNotPresentedAsVerified() {
        val coverage = AccountSnapshotCoverageReport(
            listOf(section("characters", SnapshotPayloadState.PRESENT))
        )

        val result = AccountDataConfidenceAnalyzer.summarize(coverage, null)

        assertFalse(result.freshnessAssessed)
        assertNull(result.freshSectionCount)
        assertNull(result.unknownFreshnessCount)
        assertTrue(result.hasFreshnessUncertainty)
        assertTrue(result.hasDataConfidenceConcerns)
    }

    @Test fun fullyPresentConsistentAndFreshSectionsHaveNoConfidenceConcerns() {
        val coverage = AccountSnapshotCoverageReport(
            listOf(
                section("characters", SnapshotPayloadState.PRESENT),
                section("profile", SnapshotPayloadState.PRESENT)
            )
        )
        val freshness = AccountSnapshotFreshnessReport(
            assessedAt = now,
            sections = listOf(
                freshness("characters", SectionFreshnessStatus.FRESH),
                freshness("profile", SectionFreshnessStatus.FRESH)
            )
        )

        val result = AccountDataConfidenceAnalyzer.summarize(coverage, freshness)

        assertEquals(2, result.presentSectionCount)
        assertEquals(2, result.freshSectionCount)
        assertEquals(0, result.coverageIssueCount)
        assertFalse(result.hasStaleSections)
        assertFalse(result.hasIncompleteSectionData)
        assertFalse(result.hasFreshnessUncertainty)
        assertFalse(result.hasCoverageInconsistencies)
        assertFalse(result.hasDataConfidenceConcerns)
    }
}
