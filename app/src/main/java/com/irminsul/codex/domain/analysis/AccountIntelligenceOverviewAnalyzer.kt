package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.AccountDataConfidenceSummary
import com.irminsul.codex.domain.model.AccountGapAnalysisReport
import com.irminsul.codex.domain.model.AccountIntelligenceOverview
import com.irminsul.codex.domain.model.GoalAwareCharacterDevelopmentReport
import com.irminsul.codex.domain.model.UnifiedCharacterAssessmentReport

/**
 * Composes existing deterministic reports without recalculating, filtering, or
 * reclassifying their findings. Availability and evidence remain source-specific.
 */
object AccountIntelligenceOverviewAnalyzer {
    fun compose(
        characterAssessment: UnifiedCharacterAssessmentReport,
        goalAwareDevelopment: GoalAwareCharacterDevelopmentReport,
        accountGaps: AccountGapAnalysisReport,
        dataConfidence: AccountDataConfidenceSummary
    ): AccountIntelligenceOverview = AccountIntelligenceOverview(
        characterAssessment = characterAssessment,
        goalAwareDevelopment = goalAwareDevelopment,
        accountGaps = accountGaps,
        dataConfidence = dataConfidence
    )
}
