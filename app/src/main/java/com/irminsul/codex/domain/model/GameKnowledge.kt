package com.irminsul.codex.domain.model

import java.time.Instant

enum class KnowledgeSubjectType { CHARACTER, WEAPON, ARTIFACT, TEAM, MECHANIC, GAME_MODE, OTHER }
enum class KnowledgeClaimType { ROLE_CAPABILITY, TEAM_SYNERGY, BUILD_GUIDANCE, WEAPON_COMPARISON, MECHANIC, MODE_RELEVANCE, USAGE_OBSERVATION }
enum class KnowledgeEvidenceClass { THEORYCRAFTING, EDITORIAL_EVALUATION, CALCULATED_COMPARISON, OBSERVED_USAGE, GAME_REFERENCE, CREATOR_DEMONSTRATION }
enum class KnowledgeReviewState { CANDIDATE, VERIFIED, REJECTED, SUPERSEDED, EXPIRED_REVIEW_REQUIRED }
enum class KnowledgeConfidenceState { SUPPORTED, LIMITED, CONFLICTED, UNKNOWN }
enum class KnowledgeSourcePlatform { HOYOLAB, YOUTUBE, X, TIKTOK, FACEBOOK, KQM, PRYDWEN, YSHELPER, HXG, OTHER }
enum class KnowledgeAccessMethod { OFFICIAL_API, LICENSED_DATASET, EXPLICIT_PERMISSION, MANUAL_CURATION, PUBLIC_METADATA, LINK_ONLY }
enum class KnowledgePermissionState { APPROVED, RESTRICTED, PENDING_REVIEW, NOT_APPROVED, UNKNOWN }
enum class UsageObservationStatus { PRELIMINARY, UPDATING, FINALIZED, QUALIFIED }

data class KnowledgeSourceRegistryEntry(
    val sourceId: String,
    val platform: KnowledgeSourcePlatform,
    val publisherName: String,
    val canonicalSourceUrl: String,
    val accessMethod: KnowledgeAccessMethod,
    val permissionState: KnowledgePermissionState,
    val attributionRequirements: String?,
    val allowedActions: Set<String>,
    val retentionAndDeletionPolicy: String?,
    val rateLimitOrQuotaNotes: String?,
    val termsLastCheckedAt: Instant?
)

data class KnowledgeSourceReference(
    val sourceId: String,
    val canonicalUrl: String,
    val publisherLabel: String? = null
)

data class KnowledgeGameContext(
    val gameVersion: String? = null,
    val patchPhase: String? = null,
    val gameMode: String? = null,
    val difficulty: String? = null,
    val encounter: String? = null,
    val observationPeriod: String? = null
)

data class UsageObservationContext(
    val gameMode: String?,
    val cycleOrVersion: String?,
    val difficulty: String?,
    val encounter: String?,
    val sampleSize: Int?,
    val numerator: Int?,
    val denominator: Int?,
    val denominatorPopulation: String?,
    val ownershipContext: String?,
    val collectionStartedAt: Instant?,
    val collectionEndedAt: Instant?,
    val methodology: String?,
    val status: UsageObservationStatus
)

data class KnowledgeClaim(
    val summary: String,
    val attributes: Map<String, String> = emptyMap()
)

/**
 * A compact, source-attributed claim. Revisions should be represented by a new
 * record/version; existing records are not mutated in place.
 */
data class GameKnowledgeRecord(
    val schemaVersion: Int,
    val recordId: String,
    val subjectType: KnowledgeSubjectType,
    val subjectId: String,
    val claimType: KnowledgeClaimType,
    val claim: KnowledgeClaim,
    val evidenceClass: KnowledgeEvidenceClass,
    val sourceReferences: List<KnowledgeSourceReference>,
    val gameContext: KnowledgeGameContext,
    val publishedAt: Instant?,
    val observedAt: Instant?,
    val retrievedAt: Instant,
    val validFrom: Instant?,
    val expiresAt: Instant?,
    val reviewState: KnowledgeReviewState,
    val reviewedAt: Instant?,
    val reviewNotes: String?,
    val confidenceState: KnowledgeConfidenceState,
    val confidenceReason: String?,
    val usageObservation: UsageObservationContext? = null
)

enum class KnowledgeRecordIssueCode {
    UNSUPPORTED_SCHEMA_VERSION,
    EMPTY_RECORD_ID,
    EMPTY_SUBJECT_ID,
    EMPTY_CLAIM,
    NO_SOURCE_REFERENCES,
    UNKNOWN_SOURCE_REFERENCE,
    INVALID_SOURCE_URL,
    VERIFIED_WITHOUT_REVIEW,
    INVALID_TIMESTAMP_ORDER,
    INVALID_USAGE_OBSERVATION,
    USAGE_CLAIM_WITHOUT_USAGE_CONTEXT,
    UNEXPECTED_USAGE_CONTEXT
}

data class KnowledgeRecordIssue(
    val code: KnowledgeRecordIssueCode,
    val detail: String
)

/** Structural validation only. It does not grant source permission or verify a claim. */
object GameKnowledgeRecordValidator {
    const val CURRENT_SCHEMA_VERSION = 1

    fun validate(
        record: GameKnowledgeRecord,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>
    ): List<KnowledgeRecordIssue> {
        val issues = mutableListOf<KnowledgeRecordIssue>()
        fun issue(code: KnowledgeRecordIssueCode, detail: String) {
            issues += KnowledgeRecordIssue(code, detail)
        }

        if (record.schemaVersion != CURRENT_SCHEMA_VERSION) {
            issue(KnowledgeRecordIssueCode.UNSUPPORTED_SCHEMA_VERSION, "Unsupported schema version: ${record.schemaVersion}")
        }
        if (record.recordId.isBlank()) issue(KnowledgeRecordIssueCode.EMPTY_RECORD_ID, "Record ID is blank.")
        if (record.subjectId.isBlank()) issue(KnowledgeRecordIssueCode.EMPTY_SUBJECT_ID, "Subject ID is blank.")
        if (record.claim.summary.isBlank()) issue(KnowledgeRecordIssueCode.EMPTY_CLAIM, "Claim summary is blank.")
        if (record.sourceReferences.isEmpty()) {
            issue(KnowledgeRecordIssueCode.NO_SOURCE_REFERENCES, "At least one source reference is required.")
        }

        val sourcesById = sourceRegistry.associateBy { it.sourceId }
        record.sourceReferences.forEach { reference ->
            if (reference.sourceId !in sourcesById) {
                issue(KnowledgeRecordIssueCode.UNKNOWN_SOURCE_REFERENCE, "Unknown source registry ID: ${reference.sourceId}")
            }
            val validHttpsUrl = runCatching {
                val uri = java.net.URI(reference.canonicalUrl)
                uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
            }.getOrDefault(false)
            if (!validHttpsUrl) {
                issue(KnowledgeRecordIssueCode.INVALID_SOURCE_URL, "Source URL must be an absolute HTTPS URL.")
            }
        }

        if (record.reviewState == KnowledgeReviewState.VERIFIED &&
            (record.reviewedAt == null || record.reviewNotes.isNullOrBlank())
        ) {
            issue(KnowledgeRecordIssueCode.VERIFIED_WITHOUT_REVIEW, "Verified records require a review timestamp and review notes.")
        }
        if ((record.publishedAt != null && record.publishedAt.isAfter(record.retrievedAt)) ||
            (record.observedAt != null && record.observedAt.isAfter(record.retrievedAt)) ||
            (record.validFrom != null && record.expiresAt != null && record.expiresAt.isBefore(record.validFrom)) ||
            (record.reviewedAt != null && record.reviewedAt.isAfter(record.retrievedAt))
        ) {
            issue(KnowledgeRecordIssueCode.INVALID_TIMESTAMP_ORDER, "Knowledge timestamps are inconsistent.")
        }

        if (record.claimType == KnowledgeClaimType.USAGE_OBSERVATION && record.usageObservation == null) {
            issue(KnowledgeRecordIssueCode.USAGE_CLAIM_WITHOUT_USAGE_CONTEXT, "Usage claims require explicit observation metadata.")
        }
        if (record.claimType != KnowledgeClaimType.USAGE_OBSERVATION && record.usageObservation != null) {
            issue(KnowledgeRecordIssueCode.UNEXPECTED_USAGE_CONTEXT, "Usage metadata is only valid for usage-observation claims.")
        }
        record.usageObservation?.let { usage ->
            val invalid = (usage.sampleSize != null && usage.sampleSize < 0) ||
                (usage.numerator != null && usage.numerator < 0) ||
                (usage.denominator != null && usage.denominator < 0) ||
                (usage.numerator != null && usage.denominator != null && usage.numerator > usage.denominator) ||
                (usage.denominator != null && usage.sampleSize != null && usage.denominator > usage.sampleSize) ||
                (usage.collectionStartedAt != null && usage.collectionEndedAt != null &&
                    usage.collectionEndedAt.isBefore(usage.collectionStartedAt))
            if (invalid) {
                issue(KnowledgeRecordIssueCode.INVALID_USAGE_OBSERVATION, "Usage counts or collection dates are inconsistent.")
            }
        }
        return issues
    }
}
