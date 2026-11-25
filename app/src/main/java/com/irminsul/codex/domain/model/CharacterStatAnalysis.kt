package com.irminsul.codex.domain.model

/**
 * Availability of the source reconciliation report used to derive stat findings.
 * Stale data remains usable but is explicitly marked.
 */
enum class CharacterStatAnalysisAvailability {
    AVAILABLE,
    STALE,
    MISSING,
    UNAVAILABLE,
    FAILED
}

/**
 * An explainable arithmetic discrepancy from one source-reported stat triplet.
 * Source strings are retained exactly as supplied by the reconciler.
 */
data class CharacterStatInsight(
    val characterId: Int,
    val characterName: String?,
    val fieldPath: String,
    val propertyType: Int?,
    val statName: String?,
    val base: String?,
    val additive: String?,
    val reportedFinal: String?,
    val expectedFinal: String,
    val difference: String,
    val roundingTolerance: String,
    val explanation: String,
    val recommendedAction: String,
    val basedOnStaleData: Boolean
)

data class CharacterStatAnalysisReport(
    val availability: CharacterStatAnalysisAvailability,
    val analyzedCharacterCount: Int? = null,
    val matchCount: Int = 0,
    val withinToleranceCount: Int = 0,
    val mismatchCount: Int = 0,
    val notCheckedCount: Int = 0,
    val insights: List<CharacterStatInsight> = emptyList(),
    val failureReason: String? = null
) {
    val hasFindings: Boolean
        get() = insights.isNotEmpty()
}
