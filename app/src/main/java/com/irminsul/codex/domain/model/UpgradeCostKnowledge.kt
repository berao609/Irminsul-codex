package com.irminsul.codex.domain.model

import java.time.Instant

enum class UpgradeCostSubjectType { CHARACTER, WEAPON }

enum class UpgradeCostTrack {
    CHARACTER_LEVEL,
    CHARACTER_ASCENSION,
    TALENT_LEVEL,
    WEAPON_LEVEL,
    WEAPON_ASCENSION
}

data class UpgradeCostItem(
    val itemId: String,
    val quantity: Long
)

/**
 * One immutable, source-attributed cost segment for a single subject and
 * upgrade track. Revisions are new records that may supersede this record.
 */
data class UpgradeCostRecord(
    val schemaVersion: Int,
    val recordId: String,
    val revision: Int,
    val subjectType: UpgradeCostSubjectType,
    val subjectId: String,
    val track: UpgradeCostTrack,
    val fromLevel: Int,
    val toLevel: Int,
    val talentSlot: String? = null,
    val gameVersion: String,
    val costs: List<UpgradeCostItem>,
    val sourceReferences: List<KnowledgeSourceReference>,
    val retrievedAt: Instant,
    val validFrom: Instant? = null,
    val expiresAt: Instant? = null,
    val reviewState: KnowledgeReviewState,
    val reviewedAt: Instant? = null,
    val reviewNotes: String? = null,
    val supersedesRecordId: String? = null
)

enum class UpgradeCostRecordIssueCode {
    UNSUPPORTED_SCHEMA_VERSION,
    EMPTY_RECORD_ID,
    INVALID_REVISION,
    EMPTY_SUBJECT_ID,
    SUBJECT_TRACK_MISMATCH,
    INVALID_LEVEL_RANGE,
    INVALID_TALENT_SLOT,
    EMPTY_GAME_VERSION,
    EMPTY_COSTS,
    INVALID_COST_ITEM,
    DUPLICATE_COST_ITEM,
    NO_SOURCE_REFERENCES,
    UNKNOWN_SOURCE_REFERENCE,
    SOURCE_PERMISSION_NOT_APPROVED,
    SOURCE_ACTION_NOT_APPROVED,
    INVALID_SOURCE_URL,
    SOURCE_URL_HOST_MISMATCH,
    NOT_VERIFIED,
    VERIFIED_WITHOUT_REVIEW,
    INVALID_TIMESTAMP_ORDER,
    INVALID_SUPERSESSION
}

data class UpgradeCostRecordIssue(
    val code: UpgradeCostRecordIssueCode,
    val detail: String
)

object UpgradeCostRecordValidator {
    const val CURRENT_SCHEMA_VERSION = 1
    const val REQUIRED_SOURCE_ACTION = "material_cost_curation"

    fun validate(
        record: UpgradeCostRecord,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>
    ): List<UpgradeCostRecordIssue> {
        val issues = mutableListOf<UpgradeCostRecordIssue>()
        fun issue(code: UpgradeCostRecordIssueCode, detail: String) {
            issues += UpgradeCostRecordIssue(code, detail)
        }

        if (record.schemaVersion != CURRENT_SCHEMA_VERSION) {
            issue(UpgradeCostRecordIssueCode.UNSUPPORTED_SCHEMA_VERSION, "Unsupported upgrade-cost schema version.")
        }
        if (record.recordId.isBlank()) issue(UpgradeCostRecordIssueCode.EMPTY_RECORD_ID, "Record ID is blank.")
        if (record.revision < 1) issue(UpgradeCostRecordIssueCode.INVALID_REVISION, "Revision must be positive.")
        if (record.subjectId.isBlank()) issue(UpgradeCostRecordIssueCode.EMPTY_SUBJECT_ID, "Subject ID is blank.")
        val expectedSubject = when (record.track) {
            UpgradeCostTrack.CHARACTER_LEVEL,
            UpgradeCostTrack.CHARACTER_ASCENSION,
            UpgradeCostTrack.TALENT_LEVEL -> UpgradeCostSubjectType.CHARACTER
            UpgradeCostTrack.WEAPON_LEVEL,
            UpgradeCostTrack.WEAPON_ASCENSION -> UpgradeCostSubjectType.WEAPON
        }
        if (record.subjectType != expectedSubject) {
            issue(UpgradeCostRecordIssueCode.SUBJECT_TRACK_MISMATCH, "Upgrade track does not match the subject type.")
        }
        if (record.fromLevel < 1 || record.toLevel <= record.fromLevel) {
            issue(UpgradeCostRecordIssueCode.INVALID_LEVEL_RANGE, "Upgrade range must move from a positive level to a higher level.")
        }
        if (record.track == UpgradeCostTrack.TALENT_LEVEL && record.talentSlot.isNullOrBlank()) {
            issue(UpgradeCostRecordIssueCode.INVALID_TALENT_SLOT, "Talent upgrades require an explicit talent slot.")
        }
        if (record.track != UpgradeCostTrack.TALENT_LEVEL && record.talentSlot != null) {
            issue(UpgradeCostRecordIssueCode.INVALID_TALENT_SLOT, "Talent slot is only valid for talent upgrades.")
        }
        if (record.gameVersion.isBlank()) issue(UpgradeCostRecordIssueCode.EMPTY_GAME_VERSION, "Game version is required.")
        if (record.costs.isEmpty()) issue(UpgradeCostRecordIssueCode.EMPTY_COSTS, "At least one cost item is required.")
        record.costs.forEach { item ->
            if (item.itemId.isBlank() || item.quantity <= 0L) {
                issue(UpgradeCostRecordIssueCode.INVALID_COST_ITEM, "Cost item IDs must be nonblank and quantities positive.")
            }
        }
        if (record.costs.map { it.itemId }.distinct().size != record.costs.size) {
            issue(UpgradeCostRecordIssueCode.DUPLICATE_COST_ITEM, "Each cost item may appear only once per record.")
        }
        if (record.sourceReferences.isEmpty()) {
            issue(UpgradeCostRecordIssueCode.NO_SOURCE_REFERENCES, "At least one source reference is required.")
        }
        if (record.sourceReferences.map { it.sourceId }.distinct().size != record.sourceReferences.size) {
            issue(UpgradeCostRecordIssueCode.UNKNOWN_SOURCE_REFERENCE, "Duplicate source references are not allowed.")
        }

        val sourcesById = sourceRegistry.associateBy { it.sourceId }
        record.sourceReferences.forEach { reference ->
            val source = sourcesById[reference.sourceId]
            if (source == null) {
                issue(UpgradeCostRecordIssueCode.UNKNOWN_SOURCE_REFERENCE, "Unknown source registry ID: ${reference.sourceId}")
            } else {
                if (source.permissionState != KnowledgePermissionState.APPROVED) {
                    issue(UpgradeCostRecordIssueCode.SOURCE_PERMISSION_NOT_APPROVED, "Source ${reference.sourceId} is not approved for trusted cost curation.")
                }
                if (REQUIRED_SOURCE_ACTION !in source.allowedActions) {
                    issue(UpgradeCostRecordIssueCode.SOURCE_ACTION_NOT_APPROVED, "Source ${reference.sourceId} does not permit material-cost curation.")
                }
                val referenceHost = runCatching { java.net.URI(reference.canonicalUrl).host }.getOrNull()
                val registryHost = runCatching { java.net.URI(source.canonicalSourceUrl).host }.getOrNull()
                if (referenceHost != null && registryHost != null && !referenceHost.equals(registryHost, ignoreCase = true)) {
                    issue(UpgradeCostRecordIssueCode.SOURCE_URL_HOST_MISMATCH, "Source URL host does not match its registry entry.")
                }
            }
            val validHttpsUrl = runCatching {
                val uri = java.net.URI(reference.canonicalUrl)
                uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
            }.getOrDefault(false)
            if (!validHttpsUrl) {
                issue(UpgradeCostRecordIssueCode.INVALID_SOURCE_URL, "Source URL must be an absolute HTTPS URL.")
            }
        }

        if (record.reviewState != KnowledgeReviewState.VERIFIED) {
            issue(UpgradeCostRecordIssueCode.NOT_VERIFIED, "Only verified cost records may enter trusted calculations.")
        }
        if (record.reviewState == KnowledgeReviewState.VERIFIED &&
            (record.reviewedAt == null || record.reviewNotes.isNullOrBlank())
        ) {
            issue(UpgradeCostRecordIssueCode.VERIFIED_WITHOUT_REVIEW, "Verified records require a review timestamp and review notes.")
        }
        if ((record.validFrom != null && record.validFrom.isAfter(record.retrievedAt)) ||
            (record.validFrom != null && record.expiresAt != null && record.expiresAt.isBefore(record.validFrom)) ||
            (record.reviewedAt != null && record.reviewedAt.isAfter(record.retrievedAt))
        ) {
            issue(UpgradeCostRecordIssueCode.INVALID_TIMESTAMP_ORDER, "Upgrade-cost timestamps are inconsistent.")
        }
        if (record.supersedesRecordId != null &&
            (record.supersedesRecordId.isBlank() || record.supersedesRecordId == record.recordId)
        ) {
            issue(UpgradeCostRecordIssueCode.INVALID_SUPERSESSION, "A record cannot supersede itself or a blank record ID.")
        }
        return issues
    }

    fun isApplicableAt(
        record: UpgradeCostRecord,
        gameVersion: String,
        evaluatedAt: Instant,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>
    ): Boolean {
        if (record.gameVersion != gameVersion) return false
        if (record.validFrom != null && evaluatedAt.isBefore(record.validFrom)) return false
        if (record.expiresAt != null && !evaluatedAt.isBefore(record.expiresAt)) return false
        return validate(record, sourceRegistry).isEmpty()
    }
}
