package com.irminsul.codex.domain.analysis

enum class WeaponExperienceRarity(val starCount: Int, val maximumLevel: Int) {
    FOUR_STAR(starCount = 4, maximumLevel = 90),
    FIVE_STAR(starCount = 5, maximumLevel = 90)
}

enum class WeaponExperienceCalculationStatus { CANDIDATE_DATA, INVALID_REQUEST }

enum class WeaponExperienceCalculationIssue {
    INVALID_RARITY,
    INVALID_LEVEL_RANGE,
    TARGET_EXCEEDS_ASCENSION_CAP,
    INVALID_CURRENT_EXPERIENCE,
    INVALID_PROGRESSION_DATA
}

data class WeaponExperienceCalculationRequest(
    val rarity: WeaponExperienceRarity,
    val currentLevel: Int,
    val currentExperienceWithinLevel: Long,
    val targetLevel: Int,
    /** Highest level currently unlocked; future ascensions may be included in the plan. */
    val maximumUnlockedLevel: Int,
    /** Highest level the plan intends to unlock after paying required ascension costs. */
    val targetMaximumUnlockedLevel: Int = maximumUnlockedLevel
)

data class WeaponExperienceMaterialRequirement(
    val itemId: String,
    val quantity: Long
)

data class WeaponExperienceCalculationResult(
    val status: WeaponExperienceCalculationStatus,
    val request: WeaponExperienceCalculationRequest,
    val requiredExperience: Long = 0L,
    val grantedExperience: Long = 0L,
    val wastedExperience: Long = 0L,
    val moraRequired: Long = 0L,
    val materials: List<WeaponExperienceMaterialRequirement> = emptyList(),
    val issue: WeaponExperienceCalculationIssue? = null
)

/**
 * Calculates ore-only weapon enhancement costs for 4-star and 5-star weapons.
 * Each ascension phase is rounded independently because EXP cannot carry past
 * a weapon's current level cap. Results remain candidate data until source
 * permission and human review are complete.
 */
object WeaponExperienceCalculator {
    const val MORA_ITEM_ID = "202"
    const val ENHANCEMENT_ORE_ITEM_ID = "104011"
    const val FINE_ENHANCEMENT_ORE_ITEM_ID = "104012"
    const val MYSTIC_ENHANCEMENT_ORE_ITEM_ID = "104013"

    const val ENHANCEMENT_ORE_EXP = 400L
    const val FINE_ENHANCEMENT_ORE_EXP = 2_000L
    const val MYSTIC_ENHANCEMENT_ORE_EXP = 10_000L
    private const val MORA_PER_TEN_EXP = 1L

    private val ascensionPhaseEnds = listOf(20, 40, 50, 60, 70, 80, 90)

    fun calculate(
        request: WeaponExperienceCalculationRequest,
        cumulativeExperienceByLevel: List<Long>
    ): WeaponExperienceCalculationResult {
        fun invalid(issue: WeaponExperienceCalculationIssue) =
            WeaponExperienceCalculationResult(
                status = WeaponExperienceCalculationStatus.INVALID_REQUEST,
                request = request,
                issue = issue
            )

        if (request.currentLevel !in 1..request.rarity.maximumLevel ||
            request.targetLevel !in 1..request.rarity.maximumLevel ||
            request.targetLevel < request.currentLevel ||
            request.maximumUnlockedLevel !in ascensionPhaseEnds ||
            request.targetMaximumUnlockedLevel !in ascensionPhaseEnds ||
            request.targetMaximumUnlockedLevel < request.maximumUnlockedLevel ||
            request.currentLevel > request.maximumUnlockedLevel
        ) {
            return invalid(WeaponExperienceCalculationIssue.INVALID_LEVEL_RANGE)
        }
        if (request.targetLevel > request.targetMaximumUnlockedLevel) {
            return invalid(WeaponExperienceCalculationIssue.TARGET_EXCEEDS_ASCENSION_CAP)
        }
        if (cumulativeExperienceByLevel.size != request.rarity.maximumLevel ||
            cumulativeExperienceByLevel.firstOrNull() != 0L ||
            cumulativeExperienceByLevel.any { it < 0L } ||
            cumulativeExperienceByLevel.zipWithNext().any { (a, b) -> b <= a }
        ) {
            return invalid(WeaponExperienceCalculationIssue.INVALID_PROGRESSION_DATA)
        }

        val currentTotal = cumulativeExperienceByLevel[request.currentLevel - 1]
        val nextLevelTotal = if (request.currentLevel == request.rarity.maximumLevel) currentTotal
            else cumulativeExperienceByLevel[request.currentLevel]
        val currentLevelCapacity = nextLevelTotal - currentTotal
        val currentExperienceIsValid = if (request.currentLevel == request.maximumUnlockedLevel) {
            request.currentExperienceWithinLevel == 0L
        } else {
            request.currentExperienceWithinLevel >= 0L &&
                request.currentExperienceWithinLevel < currentLevelCapacity
        }
        if (!currentExperienceIsValid) {
            return invalid(WeaponExperienceCalculationIssue.INVALID_CURRENT_EXPERIENCE)
        }

        var cursorLevel = request.currentLevel
        var experienceWithinCursorLevel = request.currentExperienceWithinLevel
        var totalRequiredExperience = 0L
        var totalGrantedExperience = 0L
        var totalMysticOre = 0L
        var totalFineOre = 0L
        var totalEnhancementOre = 0L

        while (cursorLevel < request.targetLevel) {
            val phaseEnd = ascensionPhaseEnds.first { it > cursorLevel }
            val segmentTarget = minOf(request.targetLevel, phaseEnd)
            val segmentStartTotal = cumulativeExperienceByLevel[cursorLevel - 1] +
                experienceWithinCursorLevel
            val segmentTargetTotal = cumulativeExperienceByLevel[segmentTarget - 1]
            val segmentRequired = (segmentTargetTotal - segmentStartTotal).coerceAtLeast(0L)
            val segmentGranted = if (segmentRequired == 0L) 0L
                else ((segmentRequired + ENHANCEMENT_ORE_EXP - 1L) / ENHANCEMENT_ORE_EXP) *
                    ENHANCEMENT_ORE_EXP

            // All ore values are multiples of 400 EXP. Greedy decomposition
            // gives the fewest items for the granted amount without exceeding it.
            val oreUnits = segmentGranted / ENHANCEMENT_ORE_EXP
            totalMysticOre += oreUnits / (MYSTIC_ENHANCEMENT_ORE_EXP / ENHANCEMENT_ORE_EXP)
            val afterMystic = oreUnits % (MYSTIC_ENHANCEMENT_ORE_EXP / ENHANCEMENT_ORE_EXP)
            totalFineOre += afterMystic / (FINE_ENHANCEMENT_ORE_EXP / ENHANCEMENT_ORE_EXP)
            totalEnhancementOre += afterMystic % (FINE_ENHANCEMENT_ORE_EXP / ENHANCEMENT_ORE_EXP)

            totalRequiredExperience += segmentRequired
            totalGrantedExperience += segmentGranted
            cursorLevel = segmentTarget
            experienceWithinCursorLevel = 0L
        }

        val moraRequired = totalGrantedExperience / (10L / MORA_PER_TEN_EXP)
        val materials = buildList {
            if (totalMysticOre > 0L) {
                add(WeaponExperienceMaterialRequirement(MYSTIC_ENHANCEMENT_ORE_ITEM_ID, totalMysticOre))
            }
            if (totalFineOre > 0L) {
                add(WeaponExperienceMaterialRequirement(FINE_ENHANCEMENT_ORE_ITEM_ID, totalFineOre))
            }
            if (totalEnhancementOre > 0L) {
                add(WeaponExperienceMaterialRequirement(ENHANCEMENT_ORE_ITEM_ID, totalEnhancementOre))
            }
            if (moraRequired > 0L) {
                add(WeaponExperienceMaterialRequirement(MORA_ITEM_ID, moraRequired))
            }
        }

        return WeaponExperienceCalculationResult(
            status = WeaponExperienceCalculationStatus.CANDIDATE_DATA,
            request = request,
            requiredExperience = totalRequiredExperience,
            grantedExperience = totalGrantedExperience,
            wastedExperience = totalGrantedExperience - totalRequiredExperience,
            moraRequired = moraRequired,
            materials = materials
        )
    }
}
