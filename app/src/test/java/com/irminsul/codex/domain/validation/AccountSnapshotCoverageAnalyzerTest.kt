package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class AccountSnapshotCoverageAnalyzerTest {
    private val exportedAt = Instant.parse("2026-11-25T08:00:00Z")

    private fun snapshot(
        payload: DataValue<StructuredData> = DataValue.Missing,
        status: SectionStatus? = null
    ): AccountSnapshot {
        val sections = status?.let {
            mapOf("account_card" to AccountSectionMetadata(it, "test-source", exportedAt, null))
        } ?: emptyMap()
        return AccountSnapshot(
            identity = AccountIdentity("800000001", "os_asia"),
            metadata = SnapshotMetadata(exportedAt, 1),
            overallStatus = SectionStatus.SUCCESS,
            dailyNote = DataValue.Missing,
            characters = DataValue.Missing,
            accountCard = payload,
            characterRosterSource = DataValue.Missing,
            characterDetailSource = DataValue.Missing,
            profileAndExploration = DataValue.Missing,
            spiralAbyssCurrent = DataValue.Missing,
            spiralAbyssPrevious = DataValue.Missing,
            imaginariumTheater = DataValue.Missing,
            sections = sections
        )
    }

    @Test fun reportsSectionPayloadStatesAndPreservesMetadata() {
        val report = AccountSnapshotCoverageAnalyzer.assess(snapshot(
            payload = DataValue.Available(StructuredData.ObjectValue(emptyMap()), exportedAt),
            status = SectionStatus.SUCCESS
        ))
        val card = report.sections.first()
        assertEquals(9, report.sectionCount)
        assertEquals("account_card", card.sectionName)
        assertEquals(SectionStatus.SUCCESS, card.declaredStatus)
        assertEquals(SnapshotPayloadState.PRESENT, card.payloadState)
        assertEquals("test-source", card.source)
        assertEquals(exportedAt, card.retrievedAt)
        assertTrue(report.isConsistent)
    }

    @Test fun detectsSuccessfulSectionWithoutPayload() {
        val report = AccountSnapshotCoverageAnalyzer.assess(snapshot(status = SectionStatus.SUCCESS))
        val card = report.sections.first()
        assertEquals(SnapshotPayloadState.MISSING, card.payloadState)
        assertEquals(SnapshotCoverageIssueCode.SUCCESS_WITHOUT_PAYLOAD, card.issues.single().code)
        assertFalse(report.isConsistent)
    }

    @Test fun detectsPayloadWithoutMetadataAndSkippedPayloadContradiction() {
        val untracked = AccountSnapshotCoverageAnalyzer.assess(snapshot(
            payload = DataValue.Available(StructuredData.NullValue, null)
        ))
        assertEquals(SnapshotCoverageIssueCode.PAYLOAD_WITHOUT_METADATA, untracked.sections.first().issues.single().code)

        val skipped = AccountSnapshotCoverageAnalyzer.assess(snapshot(
            payload = DataValue.Available(StructuredData.ObjectValue(emptyMap()), null),
            status = SectionStatus.SKIPPED
        ))
        assertEquals(SnapshotCoverageIssueCode.SKIPPED_WITH_PAYLOAD, skipped.sections.first().issues.single().code)
    }

    @Test fun distinguishesUnavailableFailedAndStalePayloadStates() {
        val modified = snapshot().copy(
            accountCard = DataValue.Unavailable,
            profileAndExploration = DataValue.Failed("request failed"),
            dailyNote = DataValue.Stale(DailyNoteSnapshot(
                DataValue.Missing, DataValue.Missing, DataValue.Missing, DataValue.Missing,
                DataValue.Missing, DataValue.Missing, DataValue.Missing, DataValue.Missing
            ), exportedAt)
        )
        val report = AccountSnapshotCoverageAnalyzer.assess(modified)
        assertEquals(1, report.unavailableCount)
        assertEquals(1, report.failedCount)
        assertEquals(1, report.staleCount)
    }
}
