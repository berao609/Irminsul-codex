package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*
import java.time.Duration
import java.time.Instant
import org.junit.Assert.*
import org.junit.Test

class AccountSnapshotFreshnessAnalyzerTest {
    private val exportedAt = Instant.parse("2026-11-25T08:00:00Z")

    private fun snapshot(retrievedAt: Instant?): AccountSnapshot {
        val payload = if (retrievedAt == null) DataValue.Missing
            else DataValue.Available(StructuredData.ObjectValue(emptyMap()), retrievedAt)
        val metadata = retrievedAt?.let {
            mapOf("account_card" to AccountSectionMetadata(SectionStatus.SUCCESS, "test", it, null))
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
            sections = metadata
        ).let { base ->
            base.copy(sectionCoverage = AccountSnapshotCoverageAnalyzer.assess(base))
        }
    }

    @Test fun appliesCallerProvidedThresholdWithoutInventingDefaults() {
        val data = snapshot(exportedAt.minusSeconds(3600))
        val unconfigured = AccountSnapshotFreshnessAnalyzer.assess(data)
        assertEquals(SectionFreshnessStatus.UNKNOWN, unconfigured.sections.first().status)
        assertEquals(3600L, unconfigured.sections.first().ageSeconds)

        val configured = AccountSnapshotFreshnessAnalyzer.assess(
            data,
            mapOf("account_card" to Duration.ofMinutes(30))
        )
        assertEquals(SectionFreshnessStatus.STALE, configured.sections.first().status)
        assertEquals(1800L, configured.sections.first().thresholdSeconds)
    }

    @Test fun distinguishesFreshMissingAndFutureTimestamps() {
        val fresh = AccountSnapshotFreshnessAnalyzer.assess(
            snapshot(exportedAt.minusSeconds(60)),
            mapOf("account_card" to Duration.ofMinutes(5))
        )
        assertEquals(SectionFreshnessStatus.FRESH, fresh.sections.first().status)

        val missing = AccountSnapshotFreshnessAnalyzer.assess(snapshot(null))
        assertEquals(SectionFreshnessStatus.UNKNOWN, missing.sections.first().status)

        val future = AccountSnapshotFreshnessAnalyzer.assess(
            snapshot(exportedAt.plusSeconds(1)),
            mapOf("account_card" to Duration.ofHours(1))
        )
        assertEquals(SectionFreshnessStatus.INVALID_TIMESTAMP, future.sections.first().status)
        assertNull(future.sections.first().ageSeconds)
    }

    @Test fun preservesExplicitStaleStateAndRejectsNegativeThresholds() {
        val base = snapshot(exportedAt.minusSeconds(1))
        val stale = base.copy(
            accountCard = DataValue.Stale(
                StructuredData.ObjectValue(emptyMap()),
                exportedAt.minusSeconds(1)
            )
        ).let { it.copy(sectionCoverage = AccountSnapshotCoverageAnalyzer.assess(it)) }
        val report = AccountSnapshotFreshnessAnalyzer.assess(stale)
        assertEquals(SectionFreshnessStatus.STALE, report.sections.first().status)

        try {
            AccountSnapshotFreshnessAnalyzer.assess(
                base,
                mapOf("account_card" to Duration.ofSeconds(-1))
            )
            fail("Negative thresholds must be rejected.")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }
}
