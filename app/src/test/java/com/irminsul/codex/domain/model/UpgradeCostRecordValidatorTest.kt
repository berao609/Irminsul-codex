package com.irminsul.codex.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class UpgradeCostRecordValidatorTest {
    private val retrieved = Instant.parse("2026-11-25T10:00:00Z")
    private val source = KnowledgeSourceRegistryEntry(
        sourceId = "cost-source",
        platform = KnowledgeSourcePlatform.OTHER,
        publisherName = "Test Publisher",
        canonicalSourceUrl = "https://data.example.org/genshin",
        accessMethod = KnowledgeAccessMethod.EXPLICIT_PERMISSION,
        permissionState = KnowledgePermissionState.APPROVED,
        attributionRequirements = "Attribution required",
        allowedActions = setOf(UpgradeCostRecordValidator.REQUIRED_SOURCE_ACTION),
        retentionAndDeletionPolicy = "Retain reviewed cost facts only",
        rateLimitOrQuotaNotes = null,
        termsLastCheckedAt = retrieved
    )

    private fun record(
        track: UpgradeCostTrack = UpgradeCostTrack.CHARACTER_ASCENSION,
        subjectType: UpgradeCostSubjectType = UpgradeCostSubjectType.CHARACTER,
        subjectId: String = "character-10000002",
        from: Int = 20,
        to: Int = 40,
        talentSlot: String? = null,
        gameVersion: String = "6.2",
        costs: List<UpgradeCostItem> = listOf(UpgradeCostItem("mora", 20000)),
        sources: List<KnowledgeSourceReference> = listOf(
            KnowledgeSourceReference("cost-source", "https://data.example.org/genshin/character/10000002")
        ),
        reviewState: KnowledgeReviewState = KnowledgeReviewState.VERIFIED,
        validFrom: Instant? = Instant.parse("2026-11-01T00:00:00Z"),
        expiresAt: Instant? = Instant.parse("2027-01-01T00:00:00Z"),
        supersedes: String? = null
    ) = UpgradeCostRecord(
        schemaVersion = 1,
        recordId = "cost-record-1",
        revision = 1,
        subjectType = subjectType,
        subjectId = subjectId,
        track = track,
        fromLevel = from,
        toLevel = to,
        talentSlot = talentSlot,
        gameVersion = gameVersion,
        costs = costs,
        sourceReferences = sources,
        retrievedAt = retrieved,
        validFrom = validFrom,
        expiresAt = expiresAt,
        reviewState = reviewState,
        reviewedAt = retrieved,
        reviewNotes = "Compared against the cited source and checked by a reviewer.",
        supersedesRecordId = supersedes
    )

    @Test fun acceptsVerifiedPermissionBoundVersionedCostRecord() {
        assertTrue(UpgradeCostRecordValidator.validate(record(), listOf(source)).isEmpty())
    }

    @Test fun requiresExplicitMaterialCostCurationPermission() {
        val linkOnly = source.copy(allowedActions = setOf("canonical_link"))
        val issues = UpgradeCostRecordValidator.validate(record(), listOf(linkOnly))
        assertTrue(issues.any { it.code == UpgradeCostRecordIssueCode.SOURCE_ACTION_NOT_APPROVED })
    }

    @Test fun rejectsUnapprovedSourceEvenWhenActionIsListed() {
        val pending = source.copy(permissionState = KnowledgePermissionState.PENDING_REVIEW)
        val issues = UpgradeCostRecordValidator.validate(record(), listOf(pending))
        assertTrue(issues.any { it.code == UpgradeCostRecordIssueCode.SOURCE_PERMISSION_NOT_APPROVED })
    }

    @Test fun candidateCostRecordsCannotEnterTrustedCalculations() {
        val issues = UpgradeCostRecordValidator.validate(
            record(reviewState = KnowledgeReviewState.CANDIDATE),
            listOf(source)
        )
        assertTrue(issues.any { it.code == UpgradeCostRecordIssueCode.NOT_VERIFIED })
    }

    @Test fun requiresReviewMetadataForVerifiedRecords() {
        val unreviewed = record().copy(reviewedAt = null, reviewNotes = null)
        val issues = UpgradeCostRecordValidator.validate(unreviewed, listOf(source))
        assertTrue(issues.any { it.code == UpgradeCostRecordIssueCode.VERIFIED_WITHOUT_REVIEW })
    }

    @Test fun rejectsSubjectAndTrackMismatch() {
        val invalid = record(track = UpgradeCostTrack.WEAPON_ASCENSION)
        val issues = UpgradeCostRecordValidator.validate(invalid, listOf(source))
        assertTrue(issues.any { it.code == UpgradeCostRecordIssueCode.SUBJECT_TRACK_MISMATCH })
    }

    @Test fun requiresTalentSlotOnlyForTalentTrack() {
        val missing = record(track = UpgradeCostTrack.TALENT_LEVEL)
        val unexpected = record(talentSlot = "normal-attack")
        assertTrue(UpgradeCostRecordValidator.validate(missing, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.INVALID_TALENT_SLOT
        })
        assertTrue(UpgradeCostRecordValidator.validate(unexpected, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.INVALID_TALENT_SLOT
        })
    }

    @Test fun rejectsEmptyZeroNegativeAndDuplicateCosts() {
        val empty = record(costs = emptyList())
        val invalid = record(costs = listOf(
            UpgradeCostItem("mora", 0),
            UpgradeCostItem("hero-wit", -1)
        ))
        val duplicate = record(costs = listOf(
            UpgradeCostItem("mora", 10),
            UpgradeCostItem("mora", 20)
        ))
        assertTrue(UpgradeCostRecordValidator.validate(empty, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.EMPTY_COSTS
        })
        assertTrue(UpgradeCostRecordValidator.validate(invalid, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.INVALID_COST_ITEM
        })
        assertTrue(UpgradeCostRecordValidator.validate(duplicate, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.DUPLICATE_COST_ITEM
        })
    }

    @Test fun rejectsSourceHostMismatchAndUnknownSource() {
        val wrongHost = record(sources = listOf(
            KnowledgeSourceReference("cost-source", "https://other.example.net/costs")
        ))
        val unknown = record(sources = listOf(
            KnowledgeSourceReference("missing", "https://data.example.org/costs")
        ))
        assertTrue(UpgradeCostRecordValidator.validate(wrongHost, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.SOURCE_URL_HOST_MISMATCH
        })
        assertTrue(UpgradeCostRecordValidator.validate(unknown, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.UNKNOWN_SOURCE_REFERENCE
        })
    }

    @Test fun requiresExactGameVersionAndValidityWindow() {
        val valid = record()
        assertTrue(UpgradeCostRecordValidator.isApplicableAt(
            valid, "6.2", Instant.parse("2026-12-01T00:00:00Z"), listOf(source)
        ))
        assertFalse(UpgradeCostRecordValidator.isApplicableAt(
            valid, "6.3", Instant.parse("2026-12-01T00:00:00Z"), listOf(source)
        ))
        assertFalse(UpgradeCostRecordValidator.isApplicableAt(
            valid, "6.2", Instant.parse("2027-01-01T00:00:00Z"), listOf(source)
        ))
        assertFalse(UpgradeCostRecordValidator.isApplicableAt(
            valid, "6.2", Instant.parse("2026-10-31T23:59:59Z"), listOf(source)
        ))
    }

    @Test fun requiresStableRevisionAndValidSupersession() {
        val invalidRevision = record().copy(revision = 0)
        val selfSuperseding = record().copy(supersedesRecordId = "cost-record-1")
        assertTrue(UpgradeCostRecordValidator.validate(invalidRevision, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.INVALID_REVISION
        })
        assertTrue(UpgradeCostRecordValidator.validate(selfSuperseding, listOf(source)).any {
            it.code == UpgradeCostRecordIssueCode.INVALID_SUPERSESSION
        })
    }

    @Test fun acceptsWeaponAndCharacterTalentCostShapes() {
        val weapon = record(
            track = UpgradeCostTrack.WEAPON_ASCENSION,
            subjectType = UpgradeCostSubjectType.WEAPON,
            subjectId = "weapon-10001"
        )
        val talent = record(
            track = UpgradeCostTrack.TALENT_LEVEL,
            talentSlot = "elemental-skill",
            from = 1,
            to = 2
        )
        assertTrue(UpgradeCostRecordValidator.validate(weapon, listOf(source)).isEmpty())
        assertTrue(UpgradeCostRecordValidator.validate(talent, listOf(source)).isEmpty())
    }
}
