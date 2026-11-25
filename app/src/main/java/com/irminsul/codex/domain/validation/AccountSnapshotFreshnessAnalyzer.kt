package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.model.AccountSnapshotFreshnessReport
import com.irminsul.codex.domain.model.SectionFreshnessAssessment
import com.irminsul.codex.domain.model.SectionFreshnessStatus
import com.irminsul.codex.domain.model.SectionFreshnessTimestampSource
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
            val lastSuccessfulRetrievedAt = coverage.lastSuccessfulRetrievedAt
            val threshold = thresholds[coverage.sectionName]
            // A current payload uses its current retrieval time. When no current payload
            // is available, assess age from the last known successful retrieval instead.
            val timestampSource = when {
                coverage.payloadState == SnapshotPayloadState.PRESENT && retrievedAt != null ->
                    SectionFreshnessTimestampSource.CURRENT_RETRIEVAL
                lastSuccessfulRetrievedAt != null ->
                    SectionFreshnessTimestampSource.LAST_SUCCESSFUL_RETRIEVAL
                retrievedAt != null ->
                    SectionFreshnessTimestampSource.CURRENT_RETRIEVAL
                else -> SectionFreshnessTimestampSource.NONE
            }
            val referenceAt = when (timestampSource) {
                SectionFreshnessTimestampSource.CURRENT_RETRIEVAL -> retrievedAt
                SectionFreshnessTimestampSource.LAST_SUCCESSFUL_RETRIEVAL -> lastSuccessfulRetrievedAt
                SectionFreshnessTimestampSource.NONE -> null
            }
            val age = referenceAt?.let { Duration.between(it, assessedAt) }
            val status = when {
                referenceAt == null -> SectionFreshnessStatus.UNKNOWN
                age!!.isNegative -> SectionFreshnessStatus.INVALID_TIMESTAMP
                coverage.payloadState == SnapshotPayloadState.STALE -> SectionFreshnessStatus.STALE
                threshold == null -> SectionFreshnessStatus.UNKNOWN
                age > threshold -> SectionFreshnessStatus.STALE
                else -> SectionFreshnessStatus.FRESH
            }
            SectionFreshnessAssessment(
                sectionName = coverage.sectionName,
                status = status,
                payloadState = coverage.payloadState,
                retrievedAt = retrievedAt,
                lastSuccessfulRetrievedAt = lastSuccessfulRetrievedAt,
                referenceAt = referenceAt,
                timestampSource = timestampSource,
                assessedAt = assessedAt,
                ageSeconds = age?.takeUnless { it.isNegative }?.seconds,
                thresholdSeconds = threshold?.seconds
            )
        }
        return AccountSnapshotFreshnessReport(assessedAt, sections)
    }
}
