package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*

/**
 * Aggregates already-validated M49 character evidence across the observed
 * owned roster. It does not infer missing claims, team synergy, or investment
 * priorities.
 */
object RoleCapabilityCoverageAssessor {
    const val OUTPUT_SCHEMA_VERSION = 1

    fun assess(
        knowledge: OwnedCharacterCapabilityKnowledge
    ): RoleCapabilityCoverageAssessment {
        val usableRoster = knowledge.rosterState == InvestmentFactState.AVAILABLE ||
            knowledge.rosterState == InvestmentFactState.STALE
        val ambiguousProfiles = knowledge.profiles.filter { it.ownershipAmbiguous }
        val profiles = if (usableRoster) knowledge.profiles.filterNot { it.ownershipAmbiguous } else emptyList()

        val roles = CharacterCombatRole.entries.map { role ->
            val evidence = profiles.mapNotNull { profile ->
                profile.roles.firstOrNull { it.role == role }?.let { profile.characterId to it }
            }
            val supporting = evidence.filter { it.second.state == CapabilityEvidenceState.SUPPORTED }
            val conflicted = evidence.filter { it.second.state == CapabilityEvidenceState.CONFLICTED }
            val contradicted = evidence.filter { it.second.state == CapabilityEvidenceState.CONTRADICTED }
            RoleCoverageResult(
                role = role,
                state = state(
                    usableRoster, knowledge.rosterIsPartial, supporting.isNotEmpty(),
                    conflicted.isNotEmpty(), evidence.isNotEmpty()
                ),
                supportingCharacterIds = supporting.map { it.first }.distinct().sorted(),
                conflictedCharacterIds = conflicted.map { it.first }.distinct().sorted(),
                contradictedCharacterIds = contradicted.map { it.first }.distinct().sorted(),
                supportingRecordIds = supporting.flatMap { it.second.supportingRecordIds }.distinct().sorted(),
                contradictingRecordIds = evidence.flatMap { it.second.contradictingRecordIds }.distinct().sorted()
            )
        }

        val capabilities = CharacterCombatCapability.entries.map { capability ->
            val evidence = profiles.mapNotNull { profile ->
                profile.capabilities.firstOrNull { it.capability == capability }?.let { profile.characterId to it }
            }
            val supporting = evidence.filter { it.second.state == CapabilityEvidenceState.SUPPORTED }
            val conflicted = evidence.filter { it.second.state == CapabilityEvidenceState.CONFLICTED }
            val contradicted = evidence.filter { it.second.state == CapabilityEvidenceState.CONTRADICTED }
            CapabilityCoverageResult(
                capability = capability,
                state = state(
                    usableRoster, knowledge.rosterIsPartial, supporting.isNotEmpty(),
                    conflicted.isNotEmpty(), evidence.isNotEmpty()
                ),
                supportingCharacterIds = supporting.map { it.first }.distinct().sorted(),
                conflictedCharacterIds = conflicted.map { it.first }.distinct().sorted(),
                contradictedCharacterIds = contradicted.map { it.first }.distinct().sorted(),
                supportingRecordIds = supporting.flatMap { it.second.supportingRecordIds }.distinct().sorted(),
                contradictingRecordIds = evidence.flatMap { it.second.contradictingRecordIds }.distinct().sorted()
            )
        }

        return RoleCapabilityCoverageAssessment(
            schemaVersion = OUTPUT_SCHEMA_VERSION,
            evaluatedAt = knowledge.evaluatedAt,
            context = knowledge.context,
            rosterState = knowledge.rosterState,
            rosterIsPartial = knowledge.rosterIsPartial || ambiguousProfiles.isNotEmpty(),
            roles = roles,
            capabilities = capabilities,
            rejectedClaims = knowledge.rejectedClaims
        )
    }

    private fun state(
        usableRoster: Boolean,
        rosterIsPartial: Boolean,
        hasSupport: Boolean,
        hasConflict: Boolean,
        hasAnyEvidence: Boolean
    ): RosterCoverageState = when {
        !usableRoster -> RosterCoverageState.UNKNOWN
        hasSupport && hasConflict -> RosterCoverageState.COVERED_WITH_CONFLICTS
        hasSupport -> RosterCoverageState.COVERED
        hasConflict -> RosterCoverageState.CONFLICTED
        rosterIsPartial -> RosterCoverageState.INCOMPLETE
        hasAnyEvidence -> RosterCoverageState.NOT_ESTABLISHED
        else -> RosterCoverageState.NOT_ESTABLISHED
    }
}
