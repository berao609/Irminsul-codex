package com.irminsul.codex.domain.model

/**
 * Composes existing deterministic account-analysis outputs for a future account
 * overview. Source reports remain intact and authoritative; this model is not a score.
 */
data class AccountIntelligenceOverview(
    val characterAssessment: UnifiedCharacterAssessmentReport,
    val goalAwareDevelopment: GoalAwareCharacterDevelopmentReport,
    val accountGaps: AccountGapAnalysisReport,
    val dataConfidence: AccountDataConfidenceSummary
) {
    val selectedGoal: CharacterDevelopmentGoal
        get() = goalAwareDevelopment.selectedGoal

    val assessedCharacterCount: Int?
        get() = characterAssessment.assessedCharacterCount

    val needsAttentionCount: Int
        get() = characterAssessment.needsAttentionCount

    val reviewCharacterCount: Int
        get() = characterAssessment.reviewCount

    val clearCharacterCount: Int
        get() = characterAssessment.clearCount

    val recommendationCount: Int
        get() = goalAwareDevelopment.recommendationCount

    val affectedCharacterCount: Int
        get() = goalAwareDevelopment.affectedCharacterCount

    val confirmedAccountGapCount: Int
        get() = accountGaps.confirmedGapCount

    val accountDataReviewCount: Int
        get() = accountGaps.dataReviewCount

    val accountSectionCount: Int
        get() = dataConfidence.sectionCount

    val presentAccountSectionCount: Int
        get() = dataConfidence.presentSectionCount

    val staleAccountSectionCount: Int
        get() = dataConfidence.staleCoverageSectionCount

    val missingAccountSectionCount: Int
        get() = dataConfidence.missingSectionCount

    val unavailableAccountSectionCount: Int
        get() = dataConfidence.unavailableSectionCount

    val failedAccountSectionCount: Int
        get() = dataConfidence.failedSectionCount

    val freshAccountSectionCount: Int?
        get() = dataConfidence.freshSectionCount

    val staleFreshnessSectionCount: Int?
        get() = dataConfidence.staleFreshnessCount

    val unknownFreshnessSectionCount: Int?
        get() = dataConfidence.unknownFreshnessCount

    val invalidTimestampSectionCount: Int?
        get() = dataConfidence.invalidTimestampCount

    val hasIncompleteAccountData: Boolean
        get() = dataConfidence.hasIncompleteSectionData

    val hasUnassessedFreshness: Boolean
        get() = !dataConfidence.freshnessAssessed

    val hasAccountCoverageInconsistencies: Boolean
        get() = dataConfidence.hasCoverageInconsistencies

    val hasDataConfidenceConcerns: Boolean
        get() = dataConfidence.hasDataConfidenceConcerns

    val hasStaleEvidence: Boolean
        get() =
            characterAssessment.availability == UnifiedCharacterAssessmentAvailability.STALE ||
                characterAssessment.assessments.any { it.basedOnStaleData } ||
                goalAwareDevelopment.availability == CharacterDevelopmentRecommendationAvailability.STALE ||
                goalAwareDevelopment.recommendations.any { it.recommendation.basedOnStaleData } ||
                accountGaps.availability == AccountGapAnalysisAvailability.STALE ||
                accountGaps.findings.any { it.basedOnStaleData } ||
                dataConfidence.hasStaleSections

    /**
     * True when at least one analysis source is missing, unavailable, or failed.
     * STALE is reported separately and is not silently treated as missing.
     */
    val hasUnverifiedSources: Boolean
        get() =
            characterAssessment.availability in setOf(
                UnifiedCharacterAssessmentAvailability.MISSING,
                UnifiedCharacterAssessmentAvailability.UNAVAILABLE,
                UnifiedCharacterAssessmentAvailability.FAILED
            ) ||
                goalAwareDevelopment.availability in setOf(
                    CharacterDevelopmentRecommendationAvailability.MISSING,
                    CharacterDevelopmentRecommendationAvailability.UNAVAILABLE,
                    CharacterDevelopmentRecommendationAvailability.FAILED
                ) ||
                accountGaps.availability in setOf(
                    AccountGapAnalysisAvailability.MISSING,
                    AccountGapAnalysisAvailability.UNAVAILABLE,
                    AccountGapAnalysisAvailability.FAILED
                )
}
