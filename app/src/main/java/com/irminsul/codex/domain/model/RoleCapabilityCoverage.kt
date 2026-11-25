package com.irminsul.codex.domain.model

import java.time.Instant

enum class RosterCoverageState {
    COVERED,
    COVERED_WITH_CONFLICTS,
    CONFLICTED,
    NOT_ESTABLISHED,
    INCOMPLETE,
    UNKNOWN
}

data class RoleCoverageResult(
    val role: CharacterCombatRole,
    val state: RosterCoverageState,
    val supportingCharacterIds: List<Int>,
    val conflictedCharacterIds: List<Int>,
    val contradictedCharacterIds: List<Int>,
    val supportingRecordIds: List<String>,
    val contradictingRecordIds: List<String>
)

data class CapabilityCoverageResult(
    val capability: CharacterCombatCapability,
    val state: RosterCoverageState,
    val supportingCharacterIds: List<Int>,
    val conflictedCharacterIds: List<Int>,
    val contradictedCharacterIds: List<Int>,
    val supportingRecordIds: List<String>,
    val contradictingRecordIds: List<String>
)

/**
 * Deterministic roster-level aggregation of M49's per-character evidence.
 * NOT_ESTABLISHED means no affirmative evidence was found; it does not prove
 * that the roster lacks the role or capability.
 */
data class RoleCapabilityCoverageAssessment(
    val schemaVersion: Int,
    val evaluatedAt: Instant,
    val context: CharacterCapabilityQueryContext,
    val rosterState: InvestmentFactState,
    val rosterIsPartial: Boolean,
    val roles: List<RoleCoverageResult>,
    val capabilities: List<CapabilityCoverageResult>,
    val rejectedClaims: List<CharacterCapabilityKnowledgeIssue>
)
