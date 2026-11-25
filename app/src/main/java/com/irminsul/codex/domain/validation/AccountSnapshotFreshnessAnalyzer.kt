package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.model.AccountSnapshotFreshnessReport
import com.irminsul.codex.domain.model.SectionFreshnessAssessment
import com.irminsul.codex.domain.model.SectionFreshnessStatus
import com.irminsul.codex.domain.model.SnapshotPayloadState
import java.time.Duration
import java.time.Instant

/**
 * Assesses section age against caller-supplied freshness thresholds.
 * No default threshold is assumed because section update cadence differs.
 */
object AccountSnapshotFreshnessAnalyzer {
    fun assess(
        snapshot: AccountSnapshot,
        thresholds: Map<String, Duration> = emptyMap(),
        assessedAt: Instant = snapshot.metadata.exportedAt
    ): AccountSnapshotFreshnessReport {
        require(thresholds.values.all { !it.isNegative }) {
            "Freshness thresholds must not be negative."
        }
        val sections = snapshot.sectionCoverage.sections.map { coverage ->
            val retrievedAt = coverage.retrievedAt
            val threshold = thresholds[coverage.sectionName]
            val age = retrievedAt?.let { Duration.between(it, assessedAt) }
            val status = when {
                retrievedAt == null -> SectionFreshnessStatus.UNKNOWN
                age!!.isNegative -> SectionFreshnessStatus.INVALID_TIMESTAMP
                coverage.payloadState == SnapshotPayloadState.STALE -> SectionFreshnessStatus.STALE
                threshold == null -> SectionFreshnessStatus.UNKNOWN
                age > threshold -> SectionFreshnessStatus.STALE
                else -> SectionFreshnessStatus.FRESH
            }
            SectionFreshnessAssessment(
                sectionName = coverage.sectionName,
                status = status,
                retrievedAt = retrievedAt,
                assessedAt = assessedAt,
                ageSeconds = age?.takeUnless { it.isNegative }?.seconds,
                thresholdSeconds = threshold?.seconds
            )
        }
        return AccountSnapshotFreshnessReport(assessedAt, sections)
    }
}
