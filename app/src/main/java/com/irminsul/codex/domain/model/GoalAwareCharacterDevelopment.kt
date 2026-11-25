package com.irminsul.codex.domain.model

/**
 * Explicit player-selected objective used only to order existing recommendations.
 * It never changes the evidence or base P1-P4 classification.
 */
enum class CharacterDevelopmentGoal {
    BALANCED_DEVELOPMENT,
    BUILD_RELIABILITY,
    STAT_INTEGRITY,
    DATA_CONFIDENCE
}

data class GoalAwareCharacterRecommendation(
    val recommendation: CharacterDevelopmentRecommendation,
    /** One-based position within the selected goal's priority tier. */
    val goalPriority: Int,
    val prioritizationReason: String
)

data class GoalAwareCharacterDevelopmentReport(
    val availability: CharacterDevelopmentRecommendationAvailability,
    val selectedGoal: CharacterDevelopmentGoal,
    val recommendationCount: Int = 0,
    val affectedCharacterCount: Int = 0,
    val recommendations: List<GoalAwareCharacterRecommendation> = emptyList(),
    val failureReason: String? = null
) {
    val hasRecommendations: Boolean get() = recommendations.isNotEmpty()
}
