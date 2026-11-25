package com.irminsul.codex.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GameKnowledgeRecordValidatorTest {
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
        retentionAndDeletionPolicy = "Store claim summary and canonical link only",
        rateLimitOrQuotaNotes = null,
        termsLastCheckedAt = now
    )

    private fun record(
        claimType: KnowledgeClaimType = KnowledgeClaimType.TEAM_SYNERGY,
        usage: UsageObservationContext? = null,
        reviewState: KnowledgeReviewState = KnowledgeReviewState.VERIFIED
    ) = GameKnowledgeRecord(
        schemaVersion = 1,
        recordId = "record-001",
        subjectType = KnowledgeSubjectType.CHARACTER,
        subjectId = "10000002",
        claimType = claimType,
        claim = KnowledgeClaim("Supports the listed team composition", mapOf("role" to "support")),
        evidenceClass = KnowledgeEvidenceClass.THEORYCRAFTING,
        sourceReferences = listOf(KnowledgeSourceReference("kqm", "https://keqingmains.com/example/")),
        gameContext = KnowledgeGameContext(gameVersion = "6.2", gameMode = "Spiral Abyss"),
        publishedAt = now.minusSeconds(600),
        observedAt = null,
        retrievedAt = now,
        validFrom = now.minusSeconds(300),
        expiresAt = now.plusSeconds(86400),
        reviewState = reviewState,
        reviewedAt = if (reviewState == KnowledgeReviewState.VERIFIED) now.minusSeconds(30) else null,
        reviewNotes = if (reviewState == KnowledgeReviewState.VERIFIED) "Checked against the cited source and patch context." else null,
        confidenceState = KnowledgeConfidenceState.SUPPORTED,
        confidenceReason = "Claim is directly supported by the cited source.",
        usageObservation = usage
    )

    @Test fun validVerifiedRecordPreservesSourceAndReviewProvenance() {
        val result = GameKnowledgeRecordValidator.validate(record(), listOf(source))
        assertTrue(result.toString(), result.isEmpty())
    }

    @Test fun unknownSourceAndNonHttpsUrlAreRejected() {
        val invalid = record().copy(
            sourceReferences = listOf(KnowledgeSourceReference("missing", "http://example.com/claim"))
        )
        val codes = GameKnowledgeRecordValidator.validate(invalid, listOf(source)).map { it.code }
        assertTrue(KnowledgeRecordIssueCode.UNKNOWN_SOURCE_REFERENCE in codes)
        assertTrue(KnowledgeRecordIssueCode.INVALID_SOURCE_URL in codes)
    }

    @Test fun verifiedRecordRequiresReviewTimestampAndNotes() {
        val invalid = record().copy(reviewedAt = null, reviewNotes = " ")
        assertTrue(
            GameKnowledgeRecordValidator.validate(invalid, listOf(source))
                .any { it.code == KnowledgeRecordIssueCode.VERIFIED_WITHOUT_REVIEW }
        )
    }

    @Test fun usageObservationRequiresDenominatorAndMethodologyToRemainExplicitWhenKnown() {
        val usage = UsageObservationContext(
            gameMode = "Spiral Abyss",
            cycleOrVersion = "6.2 second half",
            difficulty = "Floor 12",
            encounter = "Single-target boss",
            sampleSize = 1000,
            numerator = 250,
            denominator = 500,
            denominatorPopulation = "Accounts that cleared Floor 12 and own the character",
            ownershipContext = "Ownership-filtered",
            collectionStartedAt = now.minusSeconds(3600),
            collectionEndedAt = now.minusSeconds(60),
            methodology = "Submitted clear data",
            status = UsageObservationStatus.UPDATING
        )
        val valid = record(KnowledgeClaimType.USAGE_OBSERVATION, usage)
        assertTrue(GameKnowledgeRecordValidator.validate(valid, listOf(source)).isEmpty())
        assertEquals("Accounts that cleared Floor 12 and own the character", valid.usageObservation?.denominatorPopulation)
        assertEquals(500, valid.usageObservation?.denominator)
    }

    @Test fun usageObservationRejectsImpossibleCountsAndMissingContext() {
        val usage = UsageObservationContext(
            gameMode = "Abyss", cycleOrVersion = null, difficulty = null, encounter = null,
            sampleSize = 10, numerator = 8, denominator = 5, denominatorPopulation = null,
            ownershipContext = null, collectionStartedAt = now, collectionEndedAt = now.minusSeconds(1),
            methodology = null, status = UsageObservationStatus.PRELIMINARY
        )
        val invalid = record(KnowledgeClaimType.USAGE_OBSERVATION, usage)
        assertTrue(
            GameKnowledgeRecordValidator.validate(invalid, listOf(source))
                .any { it.code == KnowledgeRecordIssueCode.INVALID_USAGE_OBSERVATION }
        )
        val missing = record(KnowledgeClaimType.USAGE_OBSERVATION, null)
        assertTrue(
            GameKnowledgeRecordValidator.validate(missing, listOf(source))
                .any { it.code == KnowledgeRecordIssueCode.USAGE_CLAIM_WITHOUT_USAGE_CONTEXT }
        )
    }

    @Test fun candidateRecordIsNotAutomaticallyInvalidOrTrusted() {
        val candidate = record(reviewState = KnowledgeReviewState.CANDIDATE)
        assertTrue(GameKnowledgeRecordValidator.validate(candidate, listOf(source)).isEmpty())
        assertEquals(KnowledgeReviewState.CANDIDATE, candidate.reviewState)
    }
}
