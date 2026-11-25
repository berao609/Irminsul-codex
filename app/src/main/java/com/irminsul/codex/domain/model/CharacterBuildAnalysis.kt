package com.irminsul.codex.domain.model

/**
 * Describes whether deterministic character-build validation data is usable.
 * Missing and unavailable inputs remain distinct from a valid empty report.
 */
enum class CharacterBuildAnalysisAvailability {
    AVAILABLE,
    STALE,
    MISSING,
    UNAVAILABLE,
    FAILED
}

enum class CharacterBuildInsightSeverity {
    ERROR,
    WARNING
}

/**
 * A traceable finding derived from one existing build-validation issue.
 * It never changes or repairs the source account data.
 */
data class CharacterBuildInsight(
    val characterId: Int,
    val characterName: String?,
    val code: String,
    val severity: CharacterBuildInsightSeverity,
    val fieldPath: String,
    val message: String,
    val recommendedAction: String,
    val basedOnStaleData: Boolean
)

data class CharacterBuildAnalysisReport(
    val availability: CharacterBuildAnalysisAvailability,
    val analyzedCharacterCount: Int? = null,
    val insights: List<CharacterBuildInsight> = emptyList(),
    val failureReason: String? = null
) {
    val errorCount: Int
        get() = insights.count { it.severity == CharacterBuildInsightSeverity.ERROR }

    val warningCount: Int
        get() = insights.count { it.severity == CharacterBuildInsightSeverity.WARNING }

    val hasFindings: Boolean
        get() = insights.isNotEmpty()
}
