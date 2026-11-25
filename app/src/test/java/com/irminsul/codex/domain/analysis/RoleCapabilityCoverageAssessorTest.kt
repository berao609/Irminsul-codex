package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class RoleCapabilityCoverageAssessorTest {
    private val now = Instant.parse("2026-11-25T12:00:00Z")
    private val context = CharacterCapabilityQueryContext(gameVersion = "6.2", gameMode = "Spiral Abyss")

    private fun profile(
        id: Int,
        ambiguous: Boolean = false,
        roles: List<CharacterRoleEvidence> = emptyList(),
        capabilities: List<CharacterCapabilityEvidence> = emptyList()
    ) = OwnedCharacterCapabilityProfile(
        characterId = id,
        ownershipState = InvestmentFactState.AVAILABLE,
        ownershipAmbiguous = ambiguous,
        roles = roles,
        capabilities = capabilities,
        supportingClaimIds = emptyList()
    )

    private fun knowledge(
        rosterState: InvestmentFactState = InvestmentFactState.AVAILABLE,
        partial: Boolean = false,
        profiles: List<OwnedCharacterCapabilityProfile> = emptyList(),
        issues: List<CharacterCapabilityKnowledgeIssue> = emptyList()
    ) = OwnedCharacterCapabilityKnowledge(
        schemaVersion = 1,
        evaluatedAt = now,
        context = context,
        rosterState = rosterState,
        rosterIsPartial = partial,
        profiles = profiles,
        rejectedClaims = issues
    )

    @Test fun aggregatesSupportedRoleAndCapabilityAcrossOwnedCharacters() {
        val first = profile(
            10000002,
            roles = listOf(CharacterRoleEvidence(
                CharacterCombatRole.HEALER, CapabilityEvidenceState.SUPPORTED,
                listOf("role-record"), emptyList()
            )),
            capabilities = listOf(CharacterCapabilityEvidence(
                CharacterCombatCapability.HEALING, CapabilityEvidenceState.SUPPORTED,
                listOf("heal-record"), emptyList()
            ))
        )
        val second = profile(
            10000003,
            roles = listOf(CharacterRoleEvidence(
                CharacterCombatRole.HEALER, CapabilityEvidenceState.SUPPORTED,
                listOf("second-role-record"), emptyList()
            ))
        )

        val result = RoleCapabilityCoverageAssessor.assess(knowledge(profiles = listOf(first, second)))

        val healer = result.roles.single { it.role == CharacterCombatRole.HEALER }
        assertEquals(RosterCoverageState.COVERED, healer.state)
        assertEquals(listOf(10000002, 10000003), healer.supportingCharacterIds)
        assertEquals(listOf("role-record", "second-role-record"), healer.supportingRecordIds)
        val healing = result.capabilities.single { it.capability == CharacterCombatCapability.HEALING }
        assertEquals(RosterCoverageState.COVERED, healing.state)
        assertEquals(listOf(10000002), healing.supportingCharacterIds)
    }

    @Test fun preservesConflictsWithoutDiscardingIndependentCoverage() {
        val supported = profile(
            10000002,
            roles = listOf(CharacterRoleEvidence(
                CharacterCombatRole.SUPPORT, CapabilityEvidenceState.SUPPORTED,
                listOf("positive"), emptyList()
            ))
        )
        val conflicted = profile(
            10000003,
            roles = listOf(CharacterRoleEvidence(
                CharacterCombatRole.SUPPORT, CapabilityEvidenceState.CONFLICTED,
                listOf("conflict-positive"), listOf("conflict-negative")
            ))
        )

        val result = RoleCapabilityCoverageAssessor.assess(knowledge(profiles = listOf(supported, conflicted)))
        val support = result.roles.single { it.role == CharacterCombatRole.SUPPORT }

        assertEquals(RosterCoverageState.COVERED_WITH_CONFLICTS, support.state)
        assertEquals(listOf(10000002), support.supportingCharacterIds)
        assertEquals(listOf(10000003), support.conflictedCharacterIds)
        assertEquals(listOf("conflict-negative"), support.contradictingRecordIds)
    }

    @Test fun conflictWithoutAnySupportingCharacterRemainsConflicted() {
        val conflicted = profile(
            10000002,
            roles = listOf(CharacterRoleEvidence(
                CharacterCombatRole.BATTERY, CapabilityEvidenceState.CONFLICTED,
                listOf("yes"), listOf("no")
            ))
        )
        val result = RoleCapabilityCoverageAssessor.assess(knowledge(profiles = listOf(conflicted)))
        assertEquals(
            RosterCoverageState.CONFLICTED,
            result.roles.single { it.role == CharacterCombatRole.BATTERY }.state
        )
    }

    @Test fun noAffirmativeEvidenceIsNotReportedAsAProvenRosterGap() {
        val contradicted = profile(
            10000002,
            roles = listOf(CharacterRoleEvidence(
                CharacterCombatRole.SHIELDER, CapabilityEvidenceState.CONTRADICTED,
                emptyList(), listOf("refutation")
            ))
        )
        val result = RoleCapabilityCoverageAssessor.assess(knowledge(profiles = listOf(contradicted)))
        val shielder = result.roles.single { it.role == CharacterCombatRole.SHIELDER }

        assertEquals(RosterCoverageState.NOT_ESTABLISHED, shielder.state)
        assertTrue(shielder.supportingCharacterIds.isEmpty())
        assertEquals(listOf(10000002), shielder.contradictedCharacterIds)
        assertEquals(
            RosterCoverageState.NOT_ESTABLISHED,
            result.roles.single { it.role == CharacterCombatRole.HEALER }.state
        )
    }

    @Test fun partialRosterWithoutPositiveEvidenceIsIncomplete() {
        val result = RoleCapabilityCoverageAssessor.assess(
            knowledge(partial = true, profiles = listOf(profile(10000002)))
        )
        assertEquals(
            RosterCoverageState.INCOMPLETE,
            result.roles.single { it.role == CharacterCombatRole.HEALER }.state
        )
        assertTrue(result.rosterIsPartial)
    }


    @Test fun staleRosterWithoutPositiveEvidenceIsIncomplete() {
        val result = RoleCapabilityCoverageAssessor.assess(
            knowledge(rosterState = InvestmentFactState.STALE, profiles = listOf(profile(10000002)))
        )
        assertEquals(
            RosterCoverageState.INCOMPLETE,
            result.roles.single { it.role == CharacterCombatRole.HEALER }.state
        )
    }

    @Test fun unavailableRosterProducesUnknownCoverage() {
        val result = RoleCapabilityCoverageAssessor.assess(
            knowledge(rosterState = InvestmentFactState.UNAVAILABLE)
        )
        assertEquals(RosterCoverageState.UNKNOWN, result.roles.first().state)
        assertEquals(RosterCoverageState.UNKNOWN, result.capabilities.first().state)
        assertTrue(result.roles.all { it.supportingCharacterIds.isEmpty() })
    }

    @Test fun ambiguousDuplicateProfilesAreExcludedAndMarkCoveragePartial() {
        val ambiguous = profile(
            10000002,
            ambiguous = true,
            roles = listOf(CharacterRoleEvidence(
                CharacterCombatRole.HEALER, CapabilityEvidenceState.SUPPORTED,
                listOf("ambiguous-record"), emptyList()
            ))
        )
        val result = RoleCapabilityCoverageAssessor.assess(knowledge(profiles = listOf(ambiguous)))
        val healer = result.roles.single { it.role == CharacterCombatRole.HEALER }

        assertEquals(RosterCoverageState.INCOMPLETE, healer.state)
        assertTrue(healer.supportingCharacterIds.isEmpty())
        assertTrue(result.rosterIsPartial)
    }

    @Test fun contextAndRejectedClaimEvidenceArePreserved() {
        val issue = CharacterCapabilityKnowledgeIssue(
            "claim-bad",
            CharacterCapabilityIssueCode.KNOWLEDGE_CONTEXT_MISMATCH,
            "The source did not match the requested context."
        )
        val result = RoleCapabilityCoverageAssessor.assess(knowledge(issues = listOf(issue)))
        assertEquals(context, result.context)
        assertEquals(now, result.evaluatedAt)
        assertEquals(listOf(issue), result.rejectedClaims)
    }
}
