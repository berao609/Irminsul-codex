package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.CharacterExperienceCalculationRequest
import com.irminsul.codex.domain.analysis.CharacterExperienceCalculationStatus
import com.irminsul.codex.domain.analysis.CharacterExperienceCalculator

enum class CharacterUpgradePlanCandidateStatus {
    COMPLETE_CANDIDATE_DATA,
    INCOMPLETE,
    INVALID
}

data class CharacterUpgradePlanCandidateRequest(
    val characterId: String,
    val gameVersion: String,
    val currentLevel: Int,
    val currentExperienceWithinLevel: Long,
    val currentAscensionRank: Int,
    val targetLevel: Int
)

data class CharacterUpgradePlanCandidateRequirement(
    val itemId: String,
    val itemName: String,
    val quantity: Long
)

data class CharacterUpgradePlanCandidateResult(
    val status: CharacterUpgradePlanCandidateStatus,
    val request: CharacterUpgradePlanCandidateRequest,
    val requirements: List<CharacterUpgradePlanCandidateRequirement> = emptyList(),
    val usedAscensionCandidateIds: List<String> = emptyList(),
    val missingAscensionFromLevel: Int? = null,
    val issue: String? = null
)

/** Combines character EXP and ascension candidate costs, merging Mora exactly once. */
object CharacterUpgradePlanCandidateCalculator {
    private val levelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

    fun calculate(
        request: CharacterUpgradePlanCandidateRequest,
        cumulativeExperienceByLevel: List<Long>,
        ascensionCandidates: List<GenshinDbUpgradeCostCandidate>
    ): CharacterUpgradePlanCandidateResult {
        fun result(
            status: CharacterUpgradePlanCandidateStatus,
            requirements: List<CharacterUpgradePlanCandidateRequirement> = emptyList(),
            used: List<String> = emptyList(),
            missing: Int? = null,
            issue: String? = null
        ) = CharacterUpgradePlanCandidateResult(status, request, requirements, used, missing, issue)

        if (request.characterId.isBlank() || request.gameVersion.isBlank() ||
            request.currentAscensionRank !in 0..6 || request.currentLevel !in 1..90 ||
            request.targetLevel !in request.currentLevel..90 ||
            request.currentLevel > levelCaps[request.currentAscensionRank]
        ) {
            return result(
                CharacterUpgradePlanCandidateStatus.INVALID,
                issue = "Character identity, levels, or current ascension rank is invalid."
            )
        }

        val ascension = CharacterAscensionCandidateCalculator.calculate(
            CharacterAscensionCandidateCalculationRequest(
                characterId = request.characterId,
                currentLevel = request.currentLevel,
                currentAscensionRank = request.currentAscensionRank,
                targetLevel = request.targetLevel,
                gameVersion = request.gameVersion
            ),
            ascensionCandidates
        )
        when (ascension.status) {
            CharacterAscensionCandidateCalculationStatus.COST_DATA_INCOMPLETE ->
                return result(
                    CharacterUpgradePlanCandidateStatus.INCOMPLETE,
                    used = ascension.usedCandidateIds,
                    missing = ascension.missingFromLevel,
                    issue = "Character ascension material data is missing for a required stage."
                )
            CharacterAscensionCandidateCalculationStatus.INVALID_REQUEST,
            CharacterAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
            CharacterAscensionCandidateCalculationStatus.OVERLAPPING_SEGMENTS,
            CharacterAscensionCandidateCalculationStatus.QUANTITY_OVERFLOW ->
                return result(
                    CharacterUpgradePlanCandidateStatus.INVALID,
                    used = ascension.usedCandidateIds,
                    missing = ascension.missingFromLevel,
                    issue = ascension.issue ?: "Character ascension candidate data is invalid."
                )
            CharacterAscensionCandidateCalculationStatus.CANDIDATE_DATA,
            CharacterAscensionCandidateCalculationStatus.NO_ASCENSION_REQUIRED -> Unit
        }

        val currentCap = levelCaps[request.currentAscensionRank]
        val plannedCap = maxOf(currentCap, levelCaps.first { request.targetLevel <= it })
        val experience = CharacterExperienceCalculator.calculate(
            CharacterExperienceCalculationRequest(
                currentLevel = request.currentLevel,
                currentExperienceWithinLevel = request.currentExperienceWithinLevel,
                targetLevel = request.targetLevel,
                maximumUnlockedLevel = currentCap,
                targetMaximumUnlockedLevel = plannedCap
            ),
            cumulativeExperienceByLevel
        )
        if (experience.status != CharacterExperienceCalculationStatus.CANDIDATE_DATA) {
            return result(
                CharacterUpgradePlanCandidateStatus.INVALID,
                used = ascension.usedCandidateIds,
                issue = "Character EXP calculation is invalid: ${experience.issue}."
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
                CharacterExperienceCalculator.HEROS_WIT_ITEM_ID -> "Hero's Wit"
                CharacterExperienceCalculator.ADVENTURERS_EXPERIENCE_ITEM_ID -> "Adventurer's Experience"
                CharacterExperienceCalculator.WANDERERS_ADVICE_ITEM_ID -> "Wanderer's Advice"
                CharacterExperienceCalculator.MORA_ITEM_ID -> "Mora"
                else -> material.itemId
            }
            if (!add(material.itemId, name, material.quantity)) {
                return result(
                    CharacterUpgradePlanCandidateStatus.INVALID,
                    used = ascension.usedCandidateIds,
                    issue = "Material names conflict or a combined quantity exceeds the supported range."
                )
            }
        }
        ascension.materials.forEach { material ->
            if (!add(material.itemId, material.itemName, material.quantity)) {
                return result(
                    CharacterUpgradePlanCandidateStatus.INVALID,
                    used = ascension.usedCandidateIds,
                    issue = "Material names conflict or a combined quantity exceeds the supported range."
                )
            }
        }

        return result(
            CharacterUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA,
            requirements = totals.map { (id, value) ->
                CharacterUpgradePlanCandidateRequirement(id, value.first, value.second)
            },
            used = ascension.usedCandidateIds
        )
    }
}
