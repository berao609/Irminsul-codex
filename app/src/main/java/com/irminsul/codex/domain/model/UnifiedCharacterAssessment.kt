package com.irminsul.codex.domain.model

enum class UnifiedCharacterAssessmentAvailability { AVAILABLE, STALE, MISSING, UNAVAILABLE, FAILED }
enum class CharacterCompletenessLevel { COMPLETE, PARTIAL, INSUFFICIENT }
enum class CharacterAttentionStatus { NEEDS_ATTENTION, REVIEW, CLEAR }

data class CharacterCompletenessEvidence(
    val missingOrUnverifiedComponents: List<String>,
    val level: CharacterCompletenessLevel
)

data class UnifiedCharacterAssessment(
    val characterId: Int,
    val characterName: String?,
    val attentionStatus: CharacterAttentionStatus,
    val buildErrors: Int,
    val buildWarnings: Int,
    val statMismatches: Int,
    val completeness: CharacterCompletenessEvidence,
    val buildFindings: List<CharacterBuildInsight>,
    val statFindings: List<CharacterStatInsight>,
    val basedOnStaleData: Boolean
)

data class UnifiedCharacterAssessmentReport(
    val availability: UnifiedCharacterAssessmentAvailability,
    val assessedCharacterCount: Int? = null,
    val needsAttentionCount: Int = 0,
    val reviewCount: Int = 0,
    val clearCount: Int = 0,
    val assessments: List<UnifiedCharacterAssessment> = emptyList(),
    val failureReason: String? = null
) {
    val hasAssessments: Boolean get() = assessments.isNotEmpty()
}
