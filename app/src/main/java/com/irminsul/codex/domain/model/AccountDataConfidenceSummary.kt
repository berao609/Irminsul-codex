package com.irminsul.codex.domain.model

/**
 * Preserves account section coverage and freshness as separate evidence sources.
 * Counts are read from the source reports; unknown freshness is not treated as stale.
 */
data class AccountDataConfidenceSummary(
    val coverage: AccountSnapshotCoverageReport,
    val freshness: AccountSnapshotFreshnessReport?
) {
    val sectionCount: Int
        get() = coverage.sectionCount

    val presentSectionCount: Int
        get() = coverage.presentCount

    val staleCoverageSectionCount: Int
        get() = coverage.staleCount

    val missingSectionCount: Int
        get() = coverage.missingCount

    val unavailableSectionCount: Int
        get() = coverage.unavailableCount

    val failedSectionCount: Int
        get() = coverage.failedCount

    val coverageIssueCount: Int
        get() = coverage.issueCount

    val freshnessAssessed: Boolean
        get() = freshness != null

    val freshSectionCount: Int?
        get() = freshness?.freshCount

    val staleFreshnessCount: Int?
        get() = freshness?.staleCount

    val unknownFreshnessCount: Int?
        get() = freshness?.unknownCount

    val invalidTimestampCount: Int?
        get() = freshness?.invalidTimestampCount

    val hasStaleSections: Boolean
        get() = staleCoverageSectionCount > 0 || (staleFreshnessCount ?: 0) > 0

    val hasIncompleteSectionData: Boolean
        get() = missingSectionCount > 0 || unavailableSectionCount > 0 || failedSectionCount > 0

    val hasFreshnessUncertainty: Boolean
        get() = freshness == null ||
            (unknownFreshnessCount ?: 0) > 0 ||
            (invalidTimestampCount ?: 0) > 0

    val hasCoverageInconsistencies: Boolean
        get() = coverageIssueCount > 0

    val hasDataConfidenceConcerns: Boolean
        get() = hasStaleSections || hasIncompleteSectionData ||
            hasFreshnessUncertainty || hasCoverageInconsistencies
}
