package com.irminsul.codex.domain.model

enum class CharacterDevelopmentRecommendationAvailability {
    AVAILABLE,
    STALE,
    MISSING,
    UNAVAILABLE,
    FAILED
}

enum class CharacterDevelopmentRecommendationType {
    BUILD_ERRORS,
    STAT_MISMATCHES,
    BUILD_WARNINGS,
    DATA_REVIEW
}

enum class CharacterDevelopmentRecommendationPriority {
    P1,
    P2,
    P3,
    P4
}

/**
 * A deterministic, evidence-backed next action derived from a unified character
 * assessment. It does not estimate costs, recommend farming, or rank by meta.
 */
data class CharacterDevelopmentRecommendation(
    val recommendationId: String,
    val characterId: Int,
    val characterName: String?,
    val type: CharacterDevelopmentRecommendationType,
    val priority: CharacterDevelopmentRecommendationPriority,
    val title: String,
    val action: String,
    val reason: String,
    val evidence: List<String>,
    val basedOnStaleData: Boolean
)

data class CharacterDevelopmentRecommendationReport(
    val availability: CharacterDevelopmentRecommendationAvailability,
    val recommendationCount: Int = 0,
    val affectedCharacterCount: Int = 0,
    val recommendations: List<CharacterDevelopmentRecommendation> = emptyList(),
    val failureReason: String? = null
) {
    val hasRecommendations: Boolean get() = recommendations.isNotEmpty()
}
