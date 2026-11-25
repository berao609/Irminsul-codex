package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.AccountDataConfidenceSummary
import com.irminsul.codex.domain.model.AccountSnapshotCoverageReport
import com.irminsul.codex.domain.model.AccountSnapshotFreshnessReport

/**
 * Summarizes existing section-coverage and freshness evidence without merging
 * their distinct states or inferring that unknown data is stale.
 */
object AccountDataConfidenceAnalyzer {
    fun summarize(
        coverage: AccountSnapshotCoverageReport,
        freshness: AccountSnapshotFreshnessReport?
    ): AccountDataConfidenceSummary = AccountDataConfidenceSummary(
        coverage = coverage,
        freshness = freshness
    )
}
