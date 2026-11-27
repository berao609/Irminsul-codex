package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.WeaponExperienceCalculationRequest
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculationStatus
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculator
import com.irminsul.codex.domain.analysis.WeaponExperienceRarity
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack

enum class WeaponUpgradePlanCandidateStatus { COMPLETE_CANDIDATE_DATA, INCOMPLETE, INVALID }

data class WeaponUpgradePlanCandidateRequest(
    val weaponId: String,
    val gameVersion: String,
    val rarity: WeaponExperienceRarity,
    val currentLevel: Int,
    val currentExperienceWithinLevel: Long,
    val currentAscensionLevel: Int,
    val targetLevel: Int
)

data class WeaponUpgradePlanCandidateRequirement(
    val itemId: String,
    val itemName: String,
    val quantity: Long
)

data class WeaponUpgradePlanCandidateResult(
    val status: WeaponUpgradePlanCandidateStatus,
    val experience: com.irminsul.codex.domain.analysis.WeaponExperienceCalculationResult?,
    val requirements: List<WeaponUpgradePlanCandidateRequirement> = emptyList(),
    val missingAscensionFromLevels: List<Int> = emptyList(),
    val invalidCandidateIds: List<String> = emptyList()
)

/**
 * Combines ore-only EXP requirements with source-attributed Genshin-DB
 * ascension candidates. All output remains candidate data, not trusted costs.
 */
object WeaponUpgradePlanCandidateCalculator {
    private val ascensionRanges = listOf(
        20 to 40, 40 to 50, 50 to 60, 60 to 70, 70 to 80, 80 to 90
    )
    private val levelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

    fun calculate(
        request: WeaponUpgradePlanCandidateRequest,
        cumulativeExperienceByLevel: List<Long>,
        ascensionCandidates: List<GenshinDbUpgradeCostCandidate>
    ): WeaponUpgradePlanCandidateResult {
        fun invalid(
            exp: com.irminsul.codex.domain.analysis.WeaponExperienceCalculationResult? = null,
            ids: List<String> = emptyList()
        ) = WeaponUpgradePlanCandidateResult(
            status = WeaponUpgradePlanCandidateStatus.INVALID,
            experience = exp,
            invalidCandidateIds = ids
        )

        if (request.weaponId.isBlank() || request.gameVersion.isBlank() ||
            request.currentAscensionLevel !in 0..6 ||
            request.currentLevel !in 1..90 ||
            request.targetLevel !in request.currentLevel..90 ||
            request.currentLevel > levelCaps[request.currentAscensionLevel]
        ) return invalid()

        val expResult = WeaponExperienceCalculator.calculate(
            WeaponExperienceCalculationRequest(
                rarity = request.rarity,
                currentLevel = request.currentLevel,
                currentExperienceWithinLevel = request.currentExperienceWithinLevel,
                targetLevel = request.targetLevel,
                maximumUnlockedLevel = levelCaps[request.currentAscensionLevel]
            ),
            cumulativeExperienceByLevel
        )
        if (expResult.status != WeaponExperienceCalculationStatus.CANDIDATE_DATA) {
            return invalid(expResult)
        }

        val neededRanges = ascensionRanges.drop(request.currentAscensionLevel)
            .filter { (from, _) -> request.targetLevel > from }
        val matching = ascensionCandidates.filter {
            it.subjectType == UpgradeCostSubjectType.WEAPON &&
                it.subjectId == request.weaponId &&
                it.track == UpgradeCostTrack.WEAPON_ASCENSION &&
                it.talentSlot == null &&
                it.gameVersion == request.gameVersion
        }
        val duplicateRanges = matching.groupBy { it.fromLevel to it.toLevel }
            .filterValues { it.size > 1 }.keys
        val invalidIds = matching.filter {
            (it.fromLevel to it.toLevel) in duplicateRanges ||
                (it.fromLevel to it.toLevel) !in ascensionRanges ||
                it.costs.any { cost -> cost.itemId.isBlank() || cost.itemName.isBlank() || cost.quantity <= 0L }
        }.map { it.candidateId }
        if (invalidIds.isNotEmpty()) return invalid(expResult, invalidIds)

        val byRange = matching.associateBy { it.fromLevel to it.toLevel }
        val missing = neededRanges.filterNot { byRange.containsKey(it) }.map { it.first }
        val totals = linkedMapOf<String, Pair<String, Long>>()
        fun add(itemId: String, itemName: String, quantity: Long): Boolean {
            val old = totals[itemId]
            val next = try {
                Math.addExact(old?.second ?: 0L, quantity)
            } catch (_: ArithmeticException) {
                return false
            }
            totals[itemId] = (old?.first ?: itemName) to next
            return true
        }

        expResult.materials.forEach { material ->
            val name = when (material.itemId) {
                WeaponExperienceCalculator.MYSTIC_ENHANCEMENT_ORE_ITEM_ID -> "Mystic Enhancement Ore"
                WeaponExperienceCalculator.FINE_ENHANCEMENT_ORE_ITEM_ID -> "Fine Enhancement Ore"
                WeaponExperienceCalculator.ENHANCEMENT_ORE_ITEM_ID -> "Enhancement Ore"
                WeaponExperienceCalculator.MORA_ITEM_ID -> "Mora"
                else -> material.itemId
            }
            if (!add(material.itemId, name, material.quantity)) return invalid(expResult)
        }
        neededRanges.mapNotNull(byRange::get).forEach { candidate ->
            candidate.costs.forEach { cost ->
                if (!add(cost.itemId, cost.itemName, cost.quantity)) return invalid(expResult)
            }
        }

        return WeaponUpgradePlanCandidateResult(
            status = if (missing.isEmpty()) WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA
                else WeaponUpgradePlanCandidateStatus.INCOMPLETE,
            experience = expResult,
            requirements = totals.map { (id, value) ->
                WeaponUpgradePlanCandidateRequirement(id, value.first, value.second)
            },
            missingAscensionFromLevels = missing
        )
    }
}
