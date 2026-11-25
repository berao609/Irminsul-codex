package com.irminsul.codex.domain.model

enum class AccountGapAnalysisAvailability { AVAILABLE, STALE, MISSING, UNAVAILABLE, FAILED }

enum class AccountGapType {
    INVALID_ROSTER_ENTRY,
    DUPLICATE_CHARACTER_ID,
    WEAPON_DATA_GAP,
    ARTIFACT_DATA_GAP,
    ARTIFACT_SLOT_GAP,
    EQUIPMENT_INTEGRITY_ISSUE
}

enum class AccountGapConfidence { CONFIRMED, DATA_REVIEW }

data class AccountGapFinding(
    val findingId: String,
    val type: AccountGapType,
    val confidence: AccountGapConfidence,
    val characterId: Int? = null,
    val characterName: String? = null,
    val title: String,
    val explanation: String,
    val evidence: List<String>,
    val basedOnStaleData: Boolean
)

data class AccountGapAnalysisReport(
    val availability: AccountGapAnalysisAvailability,
    val assessedCharacterCount: Int? = null,
    val confirmedGapCount: Int = 0,
    val dataReviewCount: Int = 0,
    val findings: List<AccountGapFinding> = emptyList(),
    val failureReason: String? = null
) {
    val findingCount: Int get() = findings.size
}
