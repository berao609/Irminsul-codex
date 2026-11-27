package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState

data class WeaponAscensionPhaseCost(
    /** Ascension rank unlocked by this cost, from 1 through 6. */
    val ascensionLevel: Int,
    val costs: List<WeaponExperienceMaterialRequirement>
)

data class WeaponAscensionCostCandidate(
    val weaponId: Int,
    val rarity: Int,
    val phaseCosts: List<WeaponAscensionPhaseCost>,
    val sourceLabel: String,
    val sourceUrl: String,
    val sourceLicense: String,
    val retrievedOn: String,
    val reviewState: KnowledgeReviewState = KnowledgeReviewState.CANDIDATE,
    val permissionState: KnowledgePermissionState = KnowledgePermissionState.PENDING_REVIEW
)

data class WeaponAscensionCostRequest(
    val weaponId: Int,
    val rarity: Int,
    val currentLevel: Int,
    val targetLevel: Int,
    /** Current promote_level value from the account snapshot. */
    val currentAscensionLevel: Int
)

enum class WeaponAscensionCostStatus {
    CANDIDATE_DATA,
    COST_DATA_INCOMPLETE,
    INVALID_REQUEST,
    INVALID_CANDIDATE,
    QUANTITY_OVERFLOW
}

enum class WeaponAscensionCostIssue {
    UNSUPPORTED_RARITY,
    INVALID_LEVEL_RANGE,
    INVALID_ASCENSION_LEVEL,
    TARGET_BELOW_CURRENT_LEVEL,
    WEAPON_ID_MISMATCH,
    RARITY_MISMATCH,
    INVALID_SOURCE_PROVENANCE,
    INVALID_PHASE,
    INVALID_COST_ITEM,
    DUPLICATE_COST_ITEM,
    DUPLICATE_PHASE,
    MISSING_PHASE
}

data class WeaponAscensionCostResult(
    val status: WeaponAscensionCostStatus,
    val request: WeaponAscensionCostRequest,
    val requirements: List<WeaponExperienceMaterialRequirement> = emptyList(),
    val requiredAscensionLevels: List<Int> = emptyList(),
    val missingAscensionLevel: Int? = null,
    val issue: WeaponAscensionCostIssue? = null
)

/**
 * Calculates exact ascension-phase costs for one weapon ID.
 *
 * A weapon's ascension materials are weapon-specific. This calculator accepts
 * an explicit candidate cost table and never substitutes generic rarity costs.
 * Candidate data is deliberately kept separate from the trusted upgrade-cost
 * registry until source permission and review are approved.
 */
object WeaponAscensionCostCalculator {
    private val levelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

    fun calculate(
        request: WeaponAscensionCostRequest,
        candidate: WeaponAscensionCostCandidate?
    ): WeaponAscensionCostResult {
        fun result(
            status: WeaponAscensionCostStatus,
            requirements: List<WeaponExperienceMaterialRequirement> = emptyList(),
            requiredLevels: List<Int> = emptyList(),
            missing: Int? = null,
            issue: WeaponAscensionCostIssue? = null
        ) = WeaponAscensionCostResult(status, request, requirements, requiredLevels, missing, issue)

        if (request.rarity != 4 && request.rarity != 5) {
            return result(WeaponAscensionCostStatus.INVALID_REQUEST, issue = WeaponAscensionCostIssue.UNSUPPORTED_RARITY)
        }
        if (request.currentAscensionLevel !in 0..6) {
            return result(WeaponAscensionCostStatus.INVALID_REQUEST, issue = WeaponAscensionCostIssue.INVALID_ASCENSION_LEVEL)
        }
        val currentCap = levelCaps[request.currentAscensionLevel]
        if (request.currentLevel !in 1..90 || request.targetLevel !in 1..90 ||
            request.currentLevel > currentCap
        ) {
            return result(WeaponAscensionCostStatus.INVALID_REQUEST, issue = WeaponAscensionCostIssue.INVALID_LEVEL_RANGE)
        }
        if (request.targetLevel < request.currentLevel) {
            return result(WeaponAscensionCostStatus.INVALID_REQUEST, issue = WeaponAscensionCostIssue.TARGET_BELOW_CURRENT_LEVEL)
        }

        val targetAscensionLevel = levelCaps.indexOfFirst { request.targetLevel <= it }
        if (targetAscensionLevel < 0) {
            return result(WeaponAscensionCostStatus.INVALID_REQUEST, issue = WeaponAscensionCostIssue.INVALID_LEVEL_RANGE)
        }
        val requiredLevels = (request.currentAscensionLevel + 1..targetAscensionLevel).toList()
        if (requiredLevels.isEmpty()) {
            return result(WeaponAscensionCostStatus.CANDIDATE_DATA, requiredLevels = emptyList())
        }

        if (candidate == null) {
            return result(
                WeaponAscensionCostStatus.COST_DATA_INCOMPLETE,
                requiredLevels = requiredLevels,
                missing = requiredLevels.first()
            )
        }
        if (candidate.weaponId != request.weaponId) {
            return result(WeaponAscensionCostStatus.INVALID_CANDIDATE, requiredLevels = requiredLevels, issue = WeaponAscensionCostIssue.WEAPON_ID_MISMATCH)
        }
        if (candidate.rarity != request.rarity) {
            return result(WeaponAscensionCostStatus.INVALID_CANDIDATE, requiredLevels = requiredLevels, issue = WeaponAscensionCostIssue.RARITY_MISMATCH)
        }
        val validUrl = runCatching {
            val uri = java.net.URI(candidate.sourceUrl)
            uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
        }.getOrDefault(false)
        if (candidate.sourceLabel.isBlank() || !validUrl || candidate.sourceLicense.isBlank() || candidate.retrievedOn.isBlank()) {
            return result(WeaponAscensionCostStatus.INVALID_CANDIDATE, requiredLevels = requiredLevels, issue = WeaponAscensionCostIssue.INVALID_SOURCE_PROVENANCE)
        }

        val phasesByLevel = candidate.phaseCosts.groupBy { it.ascensionLevel }
        if (phasesByLevel.values.any { it.size > 1 }) {
            return result(WeaponAscensionCostStatus.INVALID_CANDIDATE, requiredLevels = requiredLevels, issue = WeaponAscensionCostIssue.DUPLICATE_PHASE)
        }
        val invalidPhase = candidate.phaseCosts.firstOrNull { it.ascensionLevel !in 1..6 || it.costs.isEmpty() }
        if (invalidPhase != null) {
            return result(WeaponAscensionCostStatus.INVALID_CANDIDATE, requiredLevels = requiredLevels, issue = WeaponAscensionCostIssue.INVALID_PHASE)
        }
        val invalidItem = candidate.phaseCosts.firstOrNull { phase ->
            phase.costs.any { it.itemId.isBlank() || it.quantity <= 0L }
        }
        if (invalidItem != null) {
            return result(WeaponAscensionCostStatus.INVALID_CANDIDATE, requiredLevels = requiredLevels, issue = WeaponAscensionCostIssue.INVALID_COST_ITEM)
        }
        val duplicateItems = candidate.phaseCosts.firstOrNull { phase ->
            phase.costs.map { it.itemId }.distinct().size != phase.costs.size
        }
        if (duplicateItems != null) {
            return result(WeaponAscensionCostStatus.INVALID_CANDIDATE, requiredLevels = requiredLevels, issue = WeaponAscensionCostIssue.DUPLICATE_COST_ITEM)
        }

        val missing = requiredLevels.firstOrNull { phasesByLevel[it].isNullOrEmpty() }
        if (missing != null) {
            return result(
                WeaponAscensionCostStatus.COST_DATA_INCOMPLETE,
                requiredLevels = requiredLevels,
                missing = missing
            )
        }

        val totals = linkedMapOf<String, Long>()
        val names = linkedMapOf<String, String>()
        try {
            requiredLevels.forEach { level ->
                phasesByLevel.getValue(level).single().costs.forEach { item ->
                    if (item.itemId.isBlank() || item.quantity <= 0L ||
                        (item.itemName != null && item.itemName.isBlank())
                    ) {
                        return result(
                            WeaponAscensionCostStatus.INVALID_CANDIDATE,
                            requiredLevels = requiredLevels,
                            issue = WeaponAscensionCostIssue.INVALID_COST_ITEM
                        )
                    }
                    val itemName = item.itemName
                    if (itemName != null) {
                        val existingName = names[item.itemId]
                        if (existingName != null && existingName != itemName) {
                            return result(
                                WeaponAscensionCostStatus.INVALID_CANDIDATE,
                                requiredLevels = requiredLevels,
                                issue = WeaponAscensionCostIssue.INVALID_COST_ITEM
                            )
                        }
                        names[item.itemId] = itemName
                    }
                    totals[item.itemId] = Math.addExact(totals[item.itemId] ?: 0L, item.quantity)
                }
            }
        } catch (_: ArithmeticException) {
            return result(WeaponAscensionCostStatus.QUANTITY_OVERFLOW, requiredLevels = requiredLevels)
        }

        return result(
            WeaponAscensionCostStatus.CANDIDATE_DATA,
            requirements = totals.map { (id, quantity) ->
                WeaponExperienceMaterialRequirement(id, quantity, names[id])
            },
            requiredLevels = requiredLevels
        )
    }
}
