package com.irminsul.codex.domain.model

import java.time.Instant

/**
 * A deliberately small vocabulary for account-relative capability reasoning.
 * These are curated game-knowledge assertions, never inferred from account stats.
 */
enum class CharacterCombatRole {
    ON_FIELD_DAMAGE_DEALER,
    OFF_FIELD_DAMAGE_DEALER,
    SUPPORT,
    ENABLER,
    DRIVER,
    HEALER,
    SHIELDER,
    BUFFER,
    DEBUFFER,
    BATTERY,
    CROWD_CONTROL
}

enum class CharacterCombatCapability {
    ON_FIELD_DAMAGE,
    OFF_FIELD_DAMAGE,
    ELEMENTAL_APPLICATION,
    REACTION_TRIGGERING,
    REACTION_ENABLING,
    HEALING,
    SHIELDING,
    DAMAGE_BUFFING,
    DEFENSE_BUFFING,
    RESISTANCE_SHREDDING,
    ENERGY_GENERATION,
    CROWD_CONTROL,
    GROUPING,
    INTERRUPTION_RESISTANCE,
    DAMAGE_MITIGATION,
    ELEMENTAL_INFUSION
}

enum class CharacterCapabilityAssertion { AFFIRMS, REFUTES }

data class CharacterCapabilityKnowledgeClaim(
    val schemaVersion: Int,
    val claimId: String,
    val characterId: Int,
    val sourceRecordId: String,
    val assertion: CharacterCapabilityAssertion,
    val roles: Set<CharacterCombatRole> = emptySet(),
    val capabilities: Set<CharacterCombatCapability> = emptySet(),
    val conditions: List<String> = emptyList()
)

data class CharacterCapabilityQueryContext(
    val gameVersion: String? = null,
    val patchPhase: String? = null,
    val gameMode: String? = null,
    val difficulty: String? = null,
    val encounter: String? = null
)

enum class CharacterCapabilityIssueCode {
    UNSUPPORTED_SCHEMA_VERSION,
    EMPTY_CLAIM_ID,
    INVALID_CHARACTER_ID,
    EMPTY_SOURCE_RECORD_ID,
    EMPTY_ASSERTION,
    TYPED_PAYLOAD_MISMATCH,
    DUPLICATE_CLAIM_ID,
    DUPLICATE_SOURCE_RECORD_ID,
    UNKNOWN_SOURCE_RECORD,
    WRONG_SUBJECT_TYPE,
    SUBJECT_ID_MISMATCH,
    WRONG_CLAIM_TYPE,
    UNTRUSTED_KNOWLEDGE_RECORD,
    SOURCE_ACTION_NOT_APPROVED,
    USAGE_EVIDENCE_NOT_ALLOWED,
    KNOWLEDGE_CONTEXT_MISMATCH
}

data class CharacterCapabilityKnowledgeIssue(
    val claimId: String,
    val code: CharacterCapabilityIssueCode,
    val detail: String
)

data class CharacterRoleEvidence(
    val role: CharacterCombatRole,
    val state: CapabilityEvidenceState,
    val supportingRecordIds: List<String>,
    val contradictingRecordIds: List<String>
)

data class CharacterCapabilityEvidence(
    val capability: CharacterCombatCapability,
    val state: CapabilityEvidenceState,
    val supportingRecordIds: List<String>,
    val contradictingRecordIds: List<String>
)

enum class CapabilityEvidenceState { SUPPORTED, CONTRADICTED, CONFLICTED }

data class OwnedCharacterCapabilityProfile(
    val characterId: Int,
    val ownershipState: InvestmentFactState,
    val ownershipAmbiguous: Boolean,
    val roles: List<CharacterRoleEvidence>,
    val capabilities: List<CharacterCapabilityEvidence>,
    val supportingClaimIds: List<String>
)

data class OwnedCharacterCapabilityKnowledge(
    val schemaVersion: Int,
    val evaluatedAt: Instant,
    val context: CharacterCapabilityQueryContext,
    val rosterState: InvestmentFactState,
    val rosterIsPartial: Boolean,
    val profiles: List<OwnedCharacterCapabilityProfile>,
    val rejectedClaims: List<CharacterCapabilityKnowledgeIssue>
)

/**
 * Validates typed character-role/capability assertions against their canonical
 * GameKnowledgeRecord. The generic record remains the authority for source,
 * review, confidence, patch/mode context, and validity timestamps.
 */
object CharacterCapabilityKnowledgeValidator {
    const val CURRENT_SCHEMA_VERSION = 1

    fun validate(
        claims: List<CharacterCapabilityKnowledgeClaim>,
        records: List<GameKnowledgeRecord>,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>,
        evaluatedAt: Instant,
        context: CharacterCapabilityQueryContext = CharacterCapabilityQueryContext()
    ): List<CharacterCapabilityKnowledgeIssue> {
        val issues = mutableListOf<CharacterCapabilityKnowledgeIssue>()
        fun issue(claim: CharacterCapabilityKnowledgeClaim, code: CharacterCapabilityIssueCode, detail: String) {
            issues += CharacterCapabilityKnowledgeIssue(claim.claimId, code, detail)
        }

        val duplicateClaimIds = claims.filter { it.claimId.isNotBlank() }
            .groupingBy { it.claimId }.eachCount().filterValues { it > 1 }.keys
        val duplicateRecordIds = records.filter { it.recordId.isNotBlank() }
            .groupingBy { it.recordId }.eachCount().filterValues { it > 1 }.keys
        val recordsById = records.groupBy { it.recordId }

        claims.forEach { claim ->
            if (claim.schemaVersion != CURRENT_SCHEMA_VERSION) {
                issue(claim, CharacterCapabilityIssueCode.UNSUPPORTED_SCHEMA_VERSION, "Unsupported capability-claim schema version.")
            }
            if (claim.claimId.isBlank()) {
                issue(claim, CharacterCapabilityIssueCode.EMPTY_CLAIM_ID, "Claim ID is blank.")
            }
            if (claim.characterId <= 0) {
                issue(claim, CharacterCapabilityIssueCode.INVALID_CHARACTER_ID, "Character ID must be positive.")
            }
            if (claim.sourceRecordId.isBlank()) {
                issue(claim, CharacterCapabilityIssueCode.EMPTY_SOURCE_RECORD_ID, "Source knowledge-record ID is blank.")
            }
            if (claim.roles.isEmpty() && claim.capabilities.isEmpty()) {
                issue(claim, CharacterCapabilityIssueCode.EMPTY_ASSERTION, "A claim must explicitly assert at least one role or capability.")
            }
            if (claim.claimId in duplicateClaimIds) {
                issue(claim, CharacterCapabilityIssueCode.DUPLICATE_CLAIM_ID, "Claim IDs must be unique.")
            }
            if (claim.sourceRecordId in duplicateRecordIds) {
                issue(claim, CharacterCapabilityIssueCode.DUPLICATE_SOURCE_RECORD_ID, "The referenced knowledge-record ID is ambiguous.")
            }

            val matchingRecords = recordsById[claim.sourceRecordId].orEmpty()
            if (matchingRecords.isEmpty()) {
                issue(claim, CharacterCapabilityIssueCode.UNKNOWN_SOURCE_RECORD, "Referenced GameKnowledgeRecord does not exist.")
                return@forEach
            }
            if (matchingRecords.size != 1) return@forEach
            val record = matchingRecords.single()

            if (record.subjectType != KnowledgeSubjectType.CHARACTER) {
                issue(claim, CharacterCapabilityIssueCode.WRONG_SUBJECT_TYPE, "Capability claims must reference a character knowledge record.")
            }
            if (record.subjectId != claim.characterId.toString()) {
                issue(claim, CharacterCapabilityIssueCode.SUBJECT_ID_MISMATCH, "Typed claim character ID does not match its source record.")
            }
            if (record.claimType != KnowledgeClaimType.ROLE_CAPABILITY) {
                issue(claim, CharacterCapabilityIssueCode.WRONG_CLAIM_TYPE, "Source record must be a ROLE_CAPABILITY claim.")
            }
            val expectedAttributes = mapOf(
                "assertion" to claim.assertion.name,
                "roles" to claim.roles.map { it.name }.sorted().joinToString(","),
                "capabilities" to claim.capabilities.map { it.name }.sorted().joinToString(","),
                "conditions" to claim.conditions.joinToString("|")
            )
            if (expectedAttributes.any { (key, value) -> record.claim.attributes[key] != value }) {
                issue(claim, CharacterCapabilityIssueCode.TYPED_PAYLOAD_MISMATCH, "Typed assertion must exactly match the canonical source record attributes.")
            }

            val structurallyTrusted = record.reviewState == KnowledgeReviewState.VERIFIED &&
                record.confidenceState == KnowledgeConfidenceState.SUPPORTED &&
                (record.validFrom == null || !record.validFrom.isAfter(evaluatedAt)) &&
                (record.expiresAt == null || record.expiresAt.isAfter(evaluatedAt)) &&
                GameKnowledgeRecordValidator.validate(record, sourceRegistry).isEmpty()
            if (!structurallyTrusted) {
                issue(claim, CharacterCapabilityIssueCode.UNTRUSTED_KNOWLEDGE_RECORD, "Source record is not verified, supported, permitted, structurally valid, and current.")
            }
            val sourceEntries = sourceRegistry.associateBy { it.sourceId }
            val permitsManualCuration = record.sourceReferences.all { reference ->
                sourceEntries[reference.sourceId]?.allowedActions?.contains("manual_claim_curation") == true
            }
            if (!permitsManualCuration) {
                issue(
                    claim,
                    CharacterCapabilityIssueCode.SOURCE_ACTION_NOT_APPROVED,
                    "Every referenced source must explicitly permit manual capability-claim curation."
                )
            }

            if (record.evidenceClass == KnowledgeEvidenceClass.OBSERVED_USAGE ||
                record.claimType == KnowledgeClaimType.USAGE_OBSERVATION
            ) {
                issue(claim, CharacterCapabilityIssueCode.USAGE_EVIDENCE_NOT_ALLOWED, "Observed usage cannot establish a character role or capability.")
            }
            if (!context.matches(record.gameContext)) {
                issue(claim, CharacterCapabilityIssueCode.KNOWLEDGE_CONTEXT_MISMATCH, "Source record does not explicitly match the requested game context.")
            }
        }
        return issues
    }

    private fun CharacterCapabilityQueryContext.matches(source: KnowledgeGameContext): Boolean =
        (gameVersion == null || source.gameVersion == gameVersion) &&
            (patchPhase == null || source.patchPhase == patchPhase) &&
            (gameMode == null || source.gameMode == gameMode) &&
            (difficulty == null || source.difficulty == difficulty) &&
            (encounter == null || source.encounter == encounter)
}

/**
 * Joins only characters present in the retrieved roster to explicitly typed,
 * trusted capability assertions. Absence from the roster is never emitted as
 * an unowned-character conclusion.
 */
object OwnedCharacterCapabilityJoiner {
    const val OUTPUT_SCHEMA_VERSION = 1

    fun join(
        ownedFacts: OwnedCharacterInvestmentFacts,
        claims: List<CharacterCapabilityKnowledgeClaim>,
        records: List<GameKnowledgeRecord>,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>,
        evaluatedAt: Instant,
        context: CharacterCapabilityQueryContext = CharacterCapabilityQueryContext()
    ): OwnedCharacterCapabilityKnowledge {
        val issues = CharacterCapabilityKnowledgeValidator.validate(
            claims, records, sourceRegistry, evaluatedAt, context
        )
        val invalidClaimIds = issues.mapTo(mutableSetOf()) { it.claimId }
        val accepted = claims.filter { it.claimId !in invalidClaimIds }
        val duplicateCharacterIds = ownedFacts.roster.collectionIntegrity.duplicateCharacterIds.toSet()

        val profiles = if (ownedFacts.roster.state == InvestmentFactState.AVAILABLE ||
            ownedFacts.roster.state == InvestmentFactState.STALE
        ) {
            ownedFacts.roster.characters.map { character ->
                val ambiguous = character.characterId in duplicateCharacterIds
                val matching = if (ambiguous) emptyList() else accepted.filter {
                    it.characterId == character.characterId
                }
                val roleEvidence = CharacterCombatRole.entries.mapNotNull { role ->
                    val assertions = matching.filter { role in it.roles }
                    evidence(assertions)?.let { (state, supporting, contradicting) ->
                        CharacterRoleEvidence(role, state, supporting, contradicting)
                    }
                }
                val capabilityEvidence = CharacterCombatCapability.entries.mapNotNull { capability ->
                    val assertions = matching.filter { capability in it.capabilities }
                    evidence(assertions)?.let { (state, supporting, contradicting) ->
                        CharacterCapabilityEvidence(capability, state, supporting, contradicting)
                    }
                }
                OwnedCharacterCapabilityProfile(
                    characterId = character.characterId,
                    ownershipState = ownedFacts.roster.state,
                    ownershipAmbiguous = ambiguous,
                    roles = roleEvidence,
                    capabilities = capabilityEvidence,
                    supportingClaimIds = matching.map { it.claimId }.distinct().sorted()
                )
            }
        } else {
            emptyList()
        }

        return OwnedCharacterCapabilityKnowledge(
            schemaVersion = OUTPUT_SCHEMA_VERSION,
            evaluatedAt = evaluatedAt,
            context = context,
            rosterState = ownedFacts.roster.state,
            rosterIsPartial = ownedFacts.roster.isPartial,
            profiles = profiles,
            rejectedClaims = issues
        )
    }

    private data class EvidenceParts(
        val state: CapabilityEvidenceState,
        val supporting: List<String>,
        val contradicting: List<String>
    )

    private fun evidence(
        assertions: List<CharacterCapabilityKnowledgeClaim>
    ): EvidenceParts? {
        if (assertions.isEmpty()) return null
        val supporting = assertions.filter { it.assertion == CharacterCapabilityAssertion.AFFIRMS }
            .map { it.sourceRecordId }.distinct().sorted()
        val contradicting = assertions.filter { it.assertion == CharacterCapabilityAssertion.REFUTES }
            .map { it.sourceRecordId }.distinct().sorted()
        val state = when {
            supporting.isNotEmpty() && contradicting.isNotEmpty() -> CapabilityEvidenceState.CONFLICTED
            supporting.isNotEmpty() -> CapabilityEvidenceState.SUPPORTED
            else -> CapabilityEvidenceState.CONTRADICTED
        }
        return EvidenceParts(state, supporting, contradicting)
    }
}
