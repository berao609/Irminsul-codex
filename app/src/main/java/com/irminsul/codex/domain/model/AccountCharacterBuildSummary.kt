package com.irminsul.codex.domain.model

/**
 * Rule-based attention category for a character with one or more build findings.
 * This is not a build score or a judgment of combat strength.
 */
enum class CharacterBuildAttentionLevel {
    NEEDS_ATTENTION,
    REVIEW
}

/**
 * Account-level grouping of the original, traceable findings for one character.
 */
data class CharacterBuildPrioritySummary(
    val characterId: Int,
    val characterName: String?,
    val attentionLevel: CharacterBuildAttentionLevel,
    val errorCount: Int,
    val warningCount: Int,
    val findings: List<CharacterBuildInsight>
)

data class AccountCharacterBuildSummary(
    val availability: CharacterBuildAnalysisAvailability,
    val analyzedCharacterCount: Int? = null,
    val affectedCharacterCount: Int = 0,
    val errorCount: Int = 0,
    val warningCount: Int = 0,
    val prioritizedCharacters: List<CharacterBuildPrioritySummary> = emptyList(),
    val failureReason: String? = null
) {
    val hasFindings: Boolean
        get() = affectedCharacterCount > 0
}
