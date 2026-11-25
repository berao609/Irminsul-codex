package com.irminsul.codex.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CharacterCapabilityKnowledgeTest {
    private val now = Instant.parse("2026-11-25T12:00:00Z")
    private val source = KnowledgeSourceRegistryEntry(
        sourceId = "kqm",
        platform = KnowledgeSourcePlatform.KQM,
        publisherName = "KeqingMains",
        canonicalSourceUrl = "https://keqingmains.com/",
        accessMethod = KnowledgeAccessMethod.MANUAL_CURATION,
        permissionState = KnowledgePermissionState.APPROVED,
        attributionRequirements = "Link to original guide",
        allowedActions = setOf("manual_claim_curation"),
        retentionAndDeletionPolicy = "Store concise claim and canonical link only",
        rateLimitOrQuotaNotes = null,
        termsLastCheckedAt = now
    )

    private fun record(
        id: String = "record-10000002",
        characterId: Int = 10000002,
        subjectType: KnowledgeSubjectType = KnowledgeSubjectType.CHARACTER,
        claimType: KnowledgeClaimType = KnowledgeClaimType.ROLE_CAPABILITY,
        evidenceClass: KnowledgeEvidenceClass = KnowledgeEvidenceClass.THEORYCRAFTING,
        reviewState: KnowledgeReviewState = KnowledgeReviewState.VERIFIED,
        confidence: KnowledgeConfidenceState = KnowledgeConfidenceState.SUPPORTED,
        gameVersion: String? = "6.2",
        gameMode: String? = "Spiral Abyss",
        expiresAt: Instant? = now.plusSeconds(86_400),
        assertion: CharacterCapabilityAssertion = CharacterCapabilityAssertion.AFFIRMS,
        roles: Set<CharacterCombatRole> = setOf(CharacterCombatRole.OFF_FIELD_DAMAGE_DEALER),
        capabilities: Set<CharacterCombatCapability> = setOf(CharacterCombatCapability.OFF_FIELD_DAMAGE),
        conditions: List<String> = emptyList()
    ) = GameKnowledgeRecord(
        schemaVersion = 1,
        recordId = id,
        subjectType = subjectType,
        subjectId = characterId.toString(),
        claimType = claimType,
        claim = KnowledgeClaim(
            "Curated character role and capability assertion.",
            mapOf(
                "assertion" to assertion.name,
                "roles" to roles.map { it.name }.sorted().joinToString(","),
                "capabilities" to capabilities.map { it.name }.sorted().joinToString(","),
                "conditions" to conditions.joinToString("|")
            )
        ),
        evidenceClass = evidenceClass,
        sourceReferences = listOf(KnowledgeSourceReference("kqm", "https://keqingmains.com/example/")),
        gameContext = KnowledgeGameContext(gameVersion = gameVersion, gameMode = gameMode),
        publishedAt = now.minusSeconds(600),
        observedAt = null,
        retrievedAt = now,
        validFrom = now.minusSeconds(300),
        expiresAt = expiresAt,
        reviewState = reviewState,
        reviewedAt = if (reviewState == KnowledgeReviewState.VERIFIED) now.minusSeconds(30) else null,
        reviewNotes = if (reviewState == KnowledgeReviewState.VERIFIED) "Checked against the cited source and applicable game context." else null,
        confidenceState = confidence,
        confidenceReason = "Explicitly supported by the cited source."
    )

    private fun claim(
        id: String = "claim-1",
        characterId: Int = 10000002,
        recordId: String = "record-10000002",
        assertion: CharacterCapabilityAssertion = CharacterCapabilityAssertion.AFFIRMS,
        roles: Set<CharacterCombatRole> = setOf(CharacterCombatRole.OFF_FIELD_DAMAGE_DEALER),
        capabilities: Set<CharacterCombatCapability> = setOf(CharacterCombatCapability.OFF_FIELD_DAMAGE),
        conditions: List<String> = emptyList()
    ) = CharacterCapabilityKnowledgeClaim(
        schemaVersion = 1,
        claimId = id,
        characterId = characterId,
        sourceRecordId = recordId,
        assertion = assertion,
        roles = roles,
        capabilities = capabilities,
        conditions = conditions
    )

    private fun ownedFacts(
        state: InvestmentFactState = InvestmentFactState.AVAILABLE,
        characterIds: List<Int> = listOf(10000002),
        duplicates: List<Int> = emptyList(),
        partial: Boolean = false
    ): OwnedCharacterInvestmentFacts {
        val characters = characterIds.map { id ->
            OwnedCharacterInvestmentFact(
                characterId = id,
                name = InvestmentObservedFact(InvestmentFactState.AVAILABLE, "Character $id", now),
                level = InvestmentObservedFact(InvestmentFactState.AVAILABLE, 90, now),
                rarity = InvestmentObservedFact(InvestmentFactState.AVAILABLE, 5, now),
                element = InvestmentObservedFact(InvestmentFactState.AVAILABLE, "Anemo", now),
                activeConstellationCount = InvestmentObservedFact(InvestmentFactState.AVAILABLE, 0, now),
                weapon = InvestmentObservedFact(InvestmentFactState.MISSING),
                artifacts = InvestmentObservedFact(InvestmentFactState.MISSING),
                artifactIntegrity = InvestmentObservedFact(InvestmentFactState.MISSING),
                skills = InvestmentObservedFact(InvestmentFactState.NOT_REPORTED),
                constellations = InvestmentObservedFact(InvestmentFactState.NOT_REPORTED)
            )
        }
        return OwnedCharacterInvestmentFacts(
            snapshotExportedAt = now,
            roster = OwnedCharacterInvestmentRosterFacts(
                state = state,
                observedAt = now,
                characters = characters,
                collectionIntegrity = CharacterCollectionIntegrity(
                    sourceEntryCount = characterIds.size + if (partial) 1 else 0,
                    mappedEntryCount = characterIds.size,
                    duplicateCharacterIds = duplicates
                )
            )
        )
    }

    private val context = CharacterCapabilityQueryContext(
        gameVersion = "6.2",
        gameMode = "Spiral Abyss"
    )

    @Test fun joinsOnlyVerifiedTypedClaimsToCharactersActuallyPresentInRoster() {
        val owned = ownedFacts()
        val sourceRecord = record()
        val unownedRecord = record(id = "record-10000003", characterId = 10000003)
        val result = OwnedCharacterCapabilityJoiner.join(
            owned,
            listOf(claim(), claim(id = "claim-unowned", characterId = 10000003, recordId = "record-10000003")),
            listOf(sourceRecord, unownedRecord),
            listOf(source),
            now,
            context
        )

        assertEquals(InvestmentFactState.AVAILABLE, result.rosterState)
        assertEquals(1, result.profiles.size)
        assertEquals(10000002, result.profiles.single().characterId)
        assertEquals(CapabilityEvidenceState.SUPPORTED, result.profiles.single().roles.single().state)
        assertEquals(CharacterCombatRole.OFF_FIELD_DAMAGE_DEALER, result.profiles.single().roles.single().role)
        assertEquals(CharacterCombatCapability.OFF_FIELD_DAMAGE, result.profiles.single().capabilities.single().capability)
        assertFalse(result.profiles.single().ownershipAmbiguous)
        assertTrue(result.rejectedClaims.isEmpty())
    }

    @Test fun conflictingAffirmingAndRefutingClaimsRemainExplicitlyConflicted() {
        val first = record(id = "record-positive")
        val second = record(
            id = "record-negative",
            assertion = CharacterCapabilityAssertion.REFUTES
        )
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(),
            listOf(
                claim(id = "positive", recordId = first.recordId),
                claim(
                    id = "negative",
                    recordId = second.recordId,
                    assertion = CharacterCapabilityAssertion.REFUTES
                )
            ),
            listOf(first, second),
            listOf(source),
            now,
            context
        )
        assertEquals(CapabilityEvidenceState.CONFLICTED, result.profiles.single().roles.single().state)
        assertEquals(listOf("record-positive"), result.profiles.single().roles.single().supportingRecordIds)
        assertEquals(listOf("record-negative"), result.profiles.single().roles.single().contradictingRecordIds)
    }

    @Test fun candidateOrLimitedKnowledgeCannotJoinAsTrustedCapability() {
        val candidate = record(reviewState = KnowledgeReviewState.CANDIDATE)
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(), listOf(claim()), listOf(candidate), listOf(source), now, context
        )
        assertTrue(result.profiles.single().roles.isEmpty())
        assertTrue(result.rejectedClaims.any {
            it.code == CharacterCapabilityIssueCode.UNTRUSTED_KNOWLEDGE_RECORD
        })
    }

    @Test fun approvedSourceMustAlsoPermitManualCapabilityCuration() {
        val linkOnlySource = source.copy(
            permissionState = KnowledgePermissionState.APPROVED,
            allowedActions = setOf("canonical_link")
        )
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(),
            listOf(claim()),
            listOf(record()),
            listOf(linkOnlySource),
            now,
            context
        )
        assertTrue(result.profiles.single().roles.isEmpty())
        assertTrue(result.rejectedClaims.any {
            it.code == CharacterCapabilityIssueCode.SOURCE_ACTION_NOT_APPROVED
        })
    }

    @Test fun observedUsageCannotEstablishRoleOrCapability() {
        val usage = record(evidenceClass = KnowledgeEvidenceClass.OBSERVED_USAGE)
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(), listOf(claim()), listOf(usage), listOf(source), now, context
        )
        assertTrue(result.profiles.single().roles.isEmpty())
        assertTrue(result.rejectedClaims.any {
            it.code == CharacterCapabilityIssueCode.USAGE_EVIDENCE_NOT_ALLOWED
        })
    }

    @Test fun expiredOrWrongPatchAndModeKnowledgeIsRejected() {
        val expired = record(expiresAt = now)
        val wrongVersion = record(id = "wrong-version", gameVersion = "6.1")
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(),
            listOf(claim(), claim(id = "wrong-version-claim", recordId = "wrong-version")),
            listOf(expired, wrongVersion),
            listOf(source),
            now,
            context
        )
        assertEquals(2, result.rejectedClaims.size)
        assertTrue(result.profiles.single().roles.isEmpty())
        assertTrue(result.rejectedClaims.all {
            it.code == CharacterCapabilityIssueCode.UNTRUSTED_KNOWLEDGE_RECORD ||
                it.code == CharacterCapabilityIssueCode.KNOWLEDGE_CONTEXT_MISMATCH
        })
    }

    @Test fun sourcePermissionAndSubjectIdentityAreRequired() {
        val restricted = source.copy(permissionState = KnowledgePermissionState.PENDING_REVIEW)
        val mismatchedSubject = record(id = "mismatch", characterId = 10000003)
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(),
            listOf(claim(), claim(id = "mismatch-claim", recordId = "mismatch")),
            listOf(record(), mismatchedSubject),
            listOf(restricted),
            now,
            context
        )
        assertTrue(result.profiles.single().roles.isEmpty())
        assertTrue(result.rejectedClaims.any {
            it.code == CharacterCapabilityIssueCode.UNTRUSTED_KNOWLEDGE_RECORD
        })
        assertTrue(result.rejectedClaims.any {
            it.code == CharacterCapabilityIssueCode.SUBJECT_ID_MISMATCH
        })
    }

    @Test fun typedClaimMustMatchTheCanonicalSourceRecordPayload() {
        val mismatchedRecord = record(
            roles = setOf(CharacterCombatRole.SUPPORT),
            capabilities = setOf(CharacterCombatCapability.HEALING)
        )
        val result = CharacterCapabilityKnowledgeValidator.validate(
            listOf(claim()),
            listOf(mismatchedRecord),
            listOf(source),
            now,
            context
        )
        assertTrue(result.any { it.code == CharacterCapabilityIssueCode.TYPED_PAYLOAD_MISMATCH })
    }

    @Test fun emptyAndInvalidTypedAssertionsAreRejected() {
        val empty = claim(roles = emptySet(), capabilities = emptySet())
        val invalid = claim(id = "bad", characterId = 0)
        val result = CharacterCapabilityKnowledgeValidator.validate(
            listOf(empty, invalid),
            listOf(record()),
            listOf(source),
            now,
            context
        )
        assertTrue(result.any { it.code == CharacterCapabilityIssueCode.EMPTY_ASSERTION })
        assertTrue(result.any { it.code == CharacterCapabilityIssueCode.INVALID_CHARACTER_ID })
    }

    @Test fun staleAndPartialRosterStatesArePreservedForObservedCharacters() {
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(
                state = InvestmentFactState.STALE,
                characterIds = listOf(10000002),
                partial = true
            ),
            listOf(claim()),
            listOf(record()),
            listOf(source),
            now,
            context
        )
        assertEquals(InvestmentFactState.STALE, result.rosterState)
        assertTrue(result.rosterIsPartial)
        assertEquals(InvestmentFactState.STALE, result.profiles.single().ownershipState)
        assertTrue(result.profiles.single().roles.isNotEmpty())
    }

    @Test fun duplicateOwnedCharacterIdsAreNotJoinedAmbiguously() {
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(
                characterIds = listOf(10000002, 10000002),
                duplicates = listOf(10000002)
            ),
            listOf(claim()),
            listOf(record()),
            listOf(source),
            now,
            context
        )
        assertEquals(2, result.profiles.size)
        assertTrue(result.profiles.all { it.ownershipAmbiguous })
        assertTrue(result.profiles.all { it.roles.isEmpty() && it.capabilities.isEmpty() })
    }

    @Test fun unavailableRosterProducesNoOwnershipProfiles() {
        val result = OwnedCharacterCapabilityJoiner.join(
            ownedFacts(state = InvestmentFactState.UNAVAILABLE),
            listOf(claim()),
            listOf(record()),
            listOf(source),
            now,
            context
        )
        assertEquals(InvestmentFactState.UNAVAILABLE, result.rosterState)
        assertTrue(result.profiles.isEmpty())
    }
}
