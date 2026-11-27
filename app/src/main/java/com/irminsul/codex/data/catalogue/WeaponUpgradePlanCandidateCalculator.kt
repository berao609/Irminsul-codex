package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.WeaponExperienceCalculationRequest
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculationStatus
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculator
import com.irminsul.codex.domain.analysis.WeaponExperienceRarity

enum class WeaponUpgradePlanCandidateStatus {
    COMPLETE_CANDIDATE_DATA,
    INCOMPLETE,
    INVALID
}

data class WeaponUpgradePlanCandidateRequest(
    val weaponId: String,
    val gameVersion: String,
    val rarity: WeaponExperienceRarity,
    val currentLevel: Int,
    val currentExperienceWithinLevel: Long,
    val currentAscensionRank: Int,
    val targetLevel: Int
)

data class WeaponUpgradePlanCandidateRequirement(
    val itemId: String,
    val itemName: String,
    val quantity: Long
)

data class WeaponUpgradePlanCandidateResult(
    val status: WeaponUpgradePlanCandidateStatus,
    val request: WeaponUpgradePlanCandidateRequest,
    val requirements: List<WeaponUpgradePlanCandidateRequirement> = emptyList(),
    val usedAscensionCandidateIds: List<String> = emptyList(),
    val missingAscensionFromLevel: Int? = null,
    val issue: String? = null
)

/**
 * Orchestrates the existing EXP and ascension calculators. It does not repeat
 * either calculation and withholds every total when ascension candidate data
 * is incomplete. Mora is merged once by item ID across both result sets.
 */
object WeaponUpgradePlanCandidateCalculator {
    private val levelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

    fun calculate(
        request: WeaponUpgradePlanCandidateRequest,
        cumulativeExperienceByLevel: List<Long>,
        ascensionCandidates: List<GenshinDbUpgradeCostCandidate>
    ): WeaponUpgradePlanCandidateResult {
        fun result(
            status: WeaponUpgradePlanCandidateStatus,
            requirements: List<WeaponUpgradePlanCandidateRequirement> = emptyList(),
            used: List<String> = emptyList(),
            missing: Int? = null,
            issue: String? = null
        ) = WeaponUpgradePlanCandidateResult(
            status = status,
            request = request,
            requirements = requirements,
            usedAscensionCandidateIds = used,
            missingAscensionFromLevel = missing,
            issue = issue
        )

        if (request.weaponId.isBlank() || request.gameVersion.isBlank() ||
            request.currentAscensionRank !in 0..6 ||
            request.currentLevel !in 1..90 ||
            request.targetLevel !in request.currentLevel..90 ||
            request.currentLevel > levelCaps[request.currentAscensionRank]
        ) {
            return result(
                WeaponUpgradePlanCandidateStatus.INVALID,
                issue = "Weapon identity, rarity, levels, or current ascension rank is invalid."
            )
        }

        val ascension = WeaponAscensionCandidateCalculator.calculate(
            WeaponAscensionCandidateCalculationRequest(
                weaponId = request.weaponId,
                rarity = request.rarity.starCount,
                currentLevel = request.currentLevel,
                currentAscensionRank = request.currentAscensionRank,
                targetLevel = request.targetLevel,
                gameVersion = request.gameVersion
            ),
            ascensionCandidates
        )
        when (ascension.status) {
            WeaponAscensionCandidateCalculationStatus.COST_DATA_INCOMPLETE ->
                return result(
                    WeaponUpgradePlanCandidateStatus.INCOMPLETE,
                    used = ascension.usedCandidateIds,
                    missing = ascension.missingFromLevel,
                    issue = "Ascension material data is missing for a required stage."
                )
            WeaponAscensionCandidateCalculationStatus.INVALID_REQUEST,
            WeaponAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
            WeaponAscensionCandidateCalculationStatus.OVERLAPPING_SEGMENTS,
            WeaponAscensionCandidateCalculationStatus.QUANTITY_OVERFLOW ->
                return result(
                    WeaponUpgradePlanCandidateStatus.INVALID,
                    used = ascension.usedCandidateIds,
                    missing = ascension.missingFromLevel,
                    issue = ascension.issue ?: "Weapon ascension candidate data is invalid."
                )
            WeaponAscensionCandidateCalculationStatus.CANDIDATE_DATA,
            WeaponAscensionCandidateCalculationStatus.NO_ASCENSION_REQUIRED -> Unit
        }

        val currentCap = levelCaps[request.currentAscensionRank]
        val plannedCap = maxOf(currentCap, levelCaps.first { request.targetLevel <= it })
        val experience = WeaponExperienceCalculator.calculate(
            WeaponExperienceCalculationRequest(
                rarity = request.rarity,
                currentLevel = request.currentLevel,
                currentExperienceWithinLevel = request.currentExperienceWithinLevel,
                targetLevel = request.targetLevel,
                maximumUnlockedLevel = currentCap,
                targetMaximumUnlockedLevel = plannedCap
            ),
            cumulativeExperienceByLevel
        )
        if (experience.status != WeaponExperienceCalculationStatus.CANDIDATE_DATA) {
            return result(
                WeaponUpgradePlanCandidateStatus.INVALID,
                used = ascension.usedCandidateIds,
                issue = "Weapon EXP calculation is invalid: ${experience.issue}."
            )
        }

        val totals = linkedMapOf<String, Pair<String, Long>>()
        fun add(itemId: String, itemName: String, quantity: Long): Boolean {
            if (itemId.isBlank() || itemName.isBlank() || quantity <= 0L) return false
            val existing = totals[itemId]
            if (existing != null && existing.first != itemName) return false
            val next = try {
                Math.addExact(existing?.second ?: 0L, quantity)
            } catch (_: ArithmeticException) {
                return false
            }
            totals[itemId] = (existing?.first ?: itemName) to next
            return true
        }

        experience.materials.forEach { material ->
            val name = when (material.itemId) {
                WeaponExperienceCalculator.MYSTIC_ENHANCEMENT_ORE_ITEM_ID -> "Mystic Enhancement Ore"
                WeaponExperienceCalculator.FINE_ENHANCEMENT_ORE_ITEM_ID -> "Fine Enhancement Ore"
                WeaponExperienceCalculator.ENHANCEMENT_ORE_ITEM_ID -> "Enhancement Ore"
                WeaponExperienceCalculator.MORA_ITEM_ID -> "Mora"
                else -> material.itemId
            }
            if (!add(material.itemId, name, material.quantity)) {
                return result(
                    WeaponUpgradePlanCandidateStatus.INVALID,
                    used = ascension.usedCandidateIds,
                    issue = "Material names conflict or a combined quantity exceeds the supported range."
                )
            }
        }
        ascension.materials.forEach { material ->
            if (!add(material.itemId, material.itemName, material.quantity)) {
                return result(
                    WeaponUpgradePlanCandidateStatus.INVALID,
                    used = ascension.usedCandidateIds,
                    issue = "Material names conflict or a combined quantity exceeds the supported range."
                )
            }
        }

        return result(
            status = WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA,
            requirements = totals.map { (id, value) ->
                WeaponUpgradePlanCandidateRequirement(id, value.first, value.second)
            },
            used = ascension.usedCandidateIds
        )
    }
}
