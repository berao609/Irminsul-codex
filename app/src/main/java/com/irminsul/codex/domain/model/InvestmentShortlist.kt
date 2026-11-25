package com.irminsul.codex.domain.model

import java.time.Instant

enum class InvestmentPriorityBand {
    HIGH_PRIORITY,
    WORTH_INVESTING,
    SITUATIONAL,
    LOW_PRIORITY_FOR_NOW,
    ALREADY_SUFFICIENT
}

enum class InvestmentReasonCode {
    ADDRESSES_ROLE_GAP,
    IMPROVES_OWNED_TEAM,
    SUPPORTED_TEAM_SYNERGY,
    CURRENT_CONTEXT_SUPPORTED,
    PATCH_CONTEXT_LIMITED,
    REDUNDANT_WITH_OWNED_CHARACTER,
    ALREADY_SUFFICIENT,
    MATERIAL_FACTORS_UNKNOWN,
    EVIDENCE_CONFLICT
}

enum class InvestmentAccountBenefit { ROLE_GAP, TEAM_IMPROVEMENT, NONE, UNKNOWN }
enum class InvestmentEvidenceState { SUPPORTED, LIMITED, CONFLICTED, UNKNOWN }
enum class InvestmentRedundancyState { NOT_REDUNDANT, REDUNDANT, CONFLICTED, UNKNOWN }

data class InvestmentCandidateAssessment(
    val characterId: Int,
    val characterName: String,
    val accountBenefit: InvestmentAccountBenefit,
    val teamSynergy: InvestmentEvidenceState,
    val patchRelevance: InvestmentEvidenceState,
    val redundancy: InvestmentRedundancyState,
    val alreadySufficient: Boolean = false,
    val ownedTeamIds: List<String> = emptyList(),
    val investmentRequirements: List<String> = emptyList(),
    val unknownFactors: List<String> = emptyList(),
    val conflictingFactors: List<String> = emptyList(),
    val supportingKnowledgeRecordIds: List<String> = emptyList()
)

data class InvestmentRecommendation(
    val characterId: Int,
    val characterName: String,
    val priority: InvestmentPriorityBand,
    val reasonCodes: List<InvestmentReasonCode>,
    val accountBenefit: InvestmentAccountBenefit,
    val ownedTeamIds: List<String>,
    val investmentRequirements: List<String>,
    val supportingKnowledgeRecordIds: List<String>,
    val unknownFactors: List<String>,
    val conflictingFactors: List<String>,
    val explanation: String
)

data class InvestmentShortlist(
    val ruleSetVersion: String,
    val evaluatedAt: Instant,
    val accountSnapshotReference: String,
    val recommendations: List<InvestmentRecommendation>
)

/**
 * A small explicit-rule contract, not a scoring model. Only verified, supported,
 * non-usage claims can independently support a priority decision.
 */
object InvestmentShortlistEvaluator {
    const val RULE_SET_VERSION = "1"

    fun evaluate(
        candidates: List<InvestmentCandidateAssessment>,
        knowledgeRecords: List<GameKnowledgeRecord>,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>,
        evaluatedAt: Instant,
        accountSnapshotReference: String
    ): InvestmentShortlist {
        val trustedRecords = knowledgeRecords.filter { record ->
            record.reviewState == KnowledgeReviewState.VERIFIED &&
                record.confidenceState == KnowledgeConfidenceState.SUPPORTED &&
                GameKnowledgeRecordValidator.validate(record, sourceRegistry).isEmpty()
        }
        val trustedDecisionEvidence = trustedRecords
            .filter { it.claimType != KnowledgeClaimType.USAGE_OBSERVATION }
            .associateBy { it.recordId }
        val trustedUsageEvidenceIds = trustedRecords
            .filter { it.claimType == KnowledgeClaimType.USAGE_OBSERVATION }
            .mapTo(mutableSetOf()) { it.recordId }

        val recommendations = candidates.mapNotNull { candidate ->
            val evidenceIds = candidate.supportingKnowledgeRecordIds
                .distinct()
                .filter { it in trustedDecisionEvidence || it in trustedUsageEvidenceIds }
            if (candidate.alreadySufficient) {
                return@mapNotNull InvestmentRecommendation(
                    characterId = candidate.characterId,
                    characterName = candidate.characterName,
                    priority = InvestmentPriorityBand.ALREADY_SUFFICIENT,
                    reasonCodes = listOf(InvestmentReasonCode.ALREADY_SUFFICIENT),
                    accountBenefit = candidate.accountBenefit,
                    ownedTeamIds = candidate.ownedTeamIds,
                    investmentRequirements = emptyList(),
                    supportingKnowledgeRecordIds = evidenceIds,
                    unknownFactors = candidate.unknownFactors,
                    conflictingFactors = candidate.conflictingFactors,
                    explanation = "Current investment is sufficient; no further investment is recommended now."
                )
            }

            if (candidate.accountBenefit == InvestmentAccountBenefit.NONE ||
                candidate.accountBenefit == InvestmentAccountBenefit.UNKNOWN ||
                evidenceIds.isEmpty()
            ) return@mapNotNull null

            val reasons = mutableListOf<InvestmentReasonCode>()
            when (candidate.accountBenefit) {
                InvestmentAccountBenefit.ROLE_GAP -> reasons += InvestmentReasonCode.ADDRESSES_ROLE_GAP
                InvestmentAccountBenefit.TEAM_IMPROVEMENT -> reasons += InvestmentReasonCode.IMPROVES_OWNED_TEAM
                else -> Unit
            }
            if (candidate.teamSynergy == InvestmentEvidenceState.SUPPORTED) {
                reasons += InvestmentReasonCode.SUPPORTED_TEAM_SYNERGY
            }
            if (candidate.patchRelevance == InvestmentEvidenceState.SUPPORTED) {
                reasons += InvestmentReasonCode.CURRENT_CONTEXT_SUPPORTED
            }
            if (candidate.patchRelevance == InvestmentEvidenceState.LIMITED) {
                reasons += InvestmentReasonCode.PATCH_CONTEXT_LIMITED
            }
            if (candidate.redundancy == InvestmentRedundancyState.REDUNDANT) {
                reasons += InvestmentReasonCode.REDUNDANT_WITH_OWNED_CHARACTER
            }
            if (candidate.unknownFactors.isNotEmpty() ||
                candidate.teamSynergy == InvestmentEvidenceState.UNKNOWN ||
                candidate.patchRelevance == InvestmentEvidenceState.UNKNOWN ||
                candidate.redundancy == InvestmentRedundancyState.UNKNOWN
            ) reasons += InvestmentReasonCode.MATERIAL_FACTORS_UNKNOWN
            if (candidate.conflictingFactors.isNotEmpty() ||
                candidate.teamSynergy == InvestmentEvidenceState.CONFLICTED ||
                candidate.patchRelevance == InvestmentEvidenceState.CONFLICTED ||
                candidate.redundancy == InvestmentRedundancyState.CONFLICTED
            ) reasons += InvestmentReasonCode.EVIDENCE_CONFLICT

            val priority = when {
                candidate.redundancy == InvestmentRedundancyState.REDUNDANT ->
                    InvestmentPriorityBand.LOW_PRIORITY_FOR_NOW
                candidate.teamSynergy != InvestmentEvidenceState.SUPPORTED ||
                    candidate.patchRelevance != InvestmentEvidenceState.SUPPORTED ||
                    candidate.redundancy != InvestmentRedundancyState.NOT_REDUNDANT ->
                    InvestmentPriorityBand.SITUATIONAL
                candidate.accountBenefit == InvestmentAccountBenefit.ROLE_GAP ->
                    InvestmentPriorityBand.HIGH_PRIORITY
                else -> InvestmentPriorityBand.WORTH_INVESTING
            }

            val explanation = when (priority) {
                InvestmentPriorityBand.HIGH_PRIORITY -> "Addresses an identified account role gap with supported team and current-context evidence."
                InvestmentPriorityBand.WORTH_INVESTING -> "Improves an owned team with supported synergy and current-context evidence."
                InvestmentPriorityBand.SITUATIONAL -> "Potential account value exists, but one or more material factors remain limited, unknown, or conflicted."
                InvestmentPriorityBand.LOW_PRIORITY_FOR_NOW -> "An owned character already provides a sufficiently overlapping role; other investments may offer more account value."
                InvestmentPriorityBand.ALREADY_SUFFICIENT -> "Current investment is sufficient."
            }

            InvestmentRecommendation(
                characterId = candidate.characterId,
                characterName = candidate.characterName,
                priority = priority,
                reasonCodes = reasons.distinct(),
                accountBenefit = candidate.accountBenefit,
                ownedTeamIds = candidate.ownedTeamIds,
                investmentRequirements = candidate.investmentRequirements,
                supportingKnowledgeRecordIds = evidenceIds,
                unknownFactors = candidate.unknownFactors,
                conflictingFactors = candidate.conflictingFactors,
                explanation = explanation
            )
        }.sortedWith(
            compareBy<InvestmentRecommendation> { it.priority.ordinal }
                .thenBy { it.characterName.lowercase() }
                .thenBy { it.characterId }
        )

        return InvestmentShortlist(
            ruleSetVersion = RULE_SET_VERSION,
            evaluatedAt = evaluatedAt,
            accountSnapshotReference = accountSnapshotReference,
            recommendations = recommendations
        )
    }
}
