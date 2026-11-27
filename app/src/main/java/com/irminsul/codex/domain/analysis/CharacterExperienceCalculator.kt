package com.irminsul.codex.domain.analysis

/**
 * Deterministic Character EXP calculation. It deliberately returns CANDIDATE_DATA
 * because the current level curve has not yet passed the application's source
 * permission and human verification workflow.
 */
enum class CharacterExperienceCalculationStatus { CANDIDATE_DATA, INVALID_REQUEST }

enum class CharacterExperienceCalculationIssue {
    INVALID_LEVEL_RANGE,
    TARGET_EXCEEDS_ASCENSION_CAP,
    INVALID_CURRENT_EXPERIENCE,
    INVALID_PROGRESSION_DATA
}

data class CharacterExperienceCalculationRequest(
    val currentLevel: Int,
    val currentExperienceWithinLevel: Long,
    val targetLevel: Int,
    /** Highest level currently unlocked by the character's ascension state. */
    val maximumUnlockedLevel: Int
)

data class CharacterExperienceMaterialRequirement(
    val itemId: String,
    val quantity: Long
)

data class CharacterExperienceCalculationResult(
    val status: CharacterExperienceCalculationStatus,
    val request: CharacterExperienceCalculationRequest,
    val requiredExperience: Long = 0L,
    val grantedExperience: Long = 0L,
    val wastedExperience: Long = 0L,
    val moraRequired: Long = 0L,
    val materials: List<CharacterExperienceMaterialRequirement> = emptyList(),
    val issue: CharacterExperienceCalculationIssue? = null
)

object CharacterExperienceCalculator {
    const val MORA_ITEM_ID = "202"
    const val WANDERERS_ADVICE_ITEM_ID = "104001"
    const val ADVENTURERS_EXPERIENCE_ITEM_ID = "104002"
    const val HEROS_WIT_ITEM_ID = "104003"

    const val WANDERERS_ADVICE_EXP = 1_000L
    const val ADVENTURERS_EXPERIENCE_EXP = 5_000L
    const val HEROS_WIT_EXP = 20_000L
    private const val MORA_PER_EXPERIENCE_DIVISOR = 5L

    private val supportedAscensionCaps = setOf(20, 40, 50, 60, 70, 80, 90)

    fun calculate(
        request: CharacterExperienceCalculationRequest,
        cumulativeExperienceByLevel: List<Long>
    ): CharacterExperienceCalculationResult {
        fun invalid(issue: CharacterExperienceCalculationIssue) =
            CharacterExperienceCalculationResult(
                status = CharacterExperienceCalculationStatus.INVALID_REQUEST,
                request = request,
                issue = issue
            )

        if (request.currentLevel !in 1..90 || request.targetLevel !in 1..90 ||
            request.targetLevel < request.currentLevel ||
            request.maximumUnlockedLevel !in supportedAscensionCaps ||
            request.currentLevel > request.maximumUnlockedLevel
        ) {
            return invalid(CharacterExperienceCalculationIssue.INVALID_LEVEL_RANGE)
        }
        if (request.targetLevel > request.maximumUnlockedLevel) {
            return invalid(CharacterExperienceCalculationIssue.TARGET_EXCEEDS_ASCENSION_CAP)
        }
        if (cumulativeExperienceByLevel.size != 90 ||
            cumulativeExperienceByLevel.firstOrNull() != 0L ||
            cumulativeExperienceByLevel.any { it < 0L } ||
            cumulativeExperienceByLevel.zipWithNext().any { (a, b) -> b <= a }
        ) {
            return invalid(CharacterExperienceCalculationIssue.INVALID_PROGRESSION_DATA)
        }

        val currentTotal = cumulativeExperienceByLevel[request.currentLevel - 1]
        val nextLevelTotal = if (request.currentLevel == 90) currentTotal
            else cumulativeExperienceByLevel[request.currentLevel]
        val currentLevelCapacity = nextLevelTotal - currentTotal
        if (request.currentExperienceWithinLevel < 0L ||
            request.currentExperienceWithinLevel >= currentLevelCapacity
        ) {
            return invalid(CharacterExperienceCalculationIssue.INVALID_CURRENT_EXPERIENCE)
        }

        val targetTotal = cumulativeExperienceByLevel[request.targetLevel - 1]
        val alreadyEarned = currentTotal + request.currentExperienceWithinLevel
        val required = (targetTotal - alreadyEarned).coerceAtLeast(0L)
        val granted = if (required == 0L) 0L else ((required + 999L) / 1_000L) * 1_000L

        var remainder = granted
        val herosWit = remainder / HEROS_WIT_EXP
        remainder %= HEROS_WIT_EXP
        val adventurersExperience = remainder / ADVENTURERS_EXPERIENCE_EXP
        remainder %= ADVENTURERS_EXPERIENCE_EXP
        val wanderersAdvice = remainder / WANDERERS_ADVICE_EXP

        val materials = buildList {
            if (herosWit > 0L) add(CharacterExperienceMaterialRequirement(HEROS_WIT_ITEM_ID, herosWit))
            if (adventurersExperience > 0L) {
                add(CharacterExperienceMaterialRequirement(ADVENTURERS_EXPERIENCE_ITEM_ID, adventurersExperience))
            }
            if (wanderersAdvice > 0L) {
                add(CharacterExperienceMaterialRequirement(WANDERERS_ADVICE_ITEM_ID, wanderersAdvice))
            }
            val mora = granted / MORA_PER_EXPERIENCE_DIVISOR
            if (mora > 0L) add(CharacterExperienceMaterialRequirement(MORA_ITEM_ID, mora))
        }

        return CharacterExperienceCalculationResult(
            status = CharacterExperienceCalculationStatus.CANDIDATE_DATA,
            request = request,
            requiredExperience = required,
            grantedExperience = granted,
            wastedExperience = granted - required,
            moraRequired = granted / MORA_PER_EXPERIENCE_DIVISOR,
            materials = materials
        )
    }
}
