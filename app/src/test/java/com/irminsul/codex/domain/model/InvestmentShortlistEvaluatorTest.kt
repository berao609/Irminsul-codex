package com.irminsul.codex.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class InvestmentShortlistEvaluatorTest {
    private val now = Instant.parse("2026-11-25T12:00:00Z")
    private val source = KnowledgeSourceRegistryEntry(
        sourceId = "source",
        platform = KnowledgeSourcePlatform.OTHER,
        publisherName = "Reviewed publisher",
        canonicalSourceUrl = "https://example.com/",
        accessMethod = KnowledgeAccessMethod.MANUAL_CURATION,
        permissionState = KnowledgePermissionState.APPROVED,
        attributionRequirements = null,
        allowedActions = setOf("manual_claim_curation"),
        retentionAndDeletionPolicy = null,
        rateLimitOrQuotaNotes = null,
        termsLastCheckedAt = now
    )

    private fun knowledge(
        id: String = "trusted",
        claimType: KnowledgeClaimType = KnowledgeClaimType.TEAM_SYNERGY,
        review: KnowledgeReviewState = KnowledgeReviewState.VERIFIED,
        confidence: KnowledgeConfidenceState = KnowledgeConfidenceState.SUPPORTED
    ) = GameKnowledgeRecord(
        schemaVersion = 1, recordId = id,
        subjectType = KnowledgeSubjectType.CHARACTER, subjectId = "10000002",
        claimType = claimType, claim = KnowledgeClaim("Verified synergy claim"),
        evidenceClass = if (claimType == KnowledgeClaimType.USAGE_OBSERVATION) KnowledgeEvidenceClass.OBSERVED_USAGE else KnowledgeEvidenceClass.THEORYCRAFTING,
        sourceReferences = listOf(KnowledgeSourceReference("source", "https://example.com/claim")),
        gameContext = KnowledgeGameContext(gameVersion = "6.2"),
        publishedAt = now.minusSeconds(100), observedAt = null, retrievedAt = now,
        validFrom = null, expiresAt = null, reviewState = review,
        reviewedAt = if (review == KnowledgeReviewState.VERIFIED) now.minusSeconds(10) else null,
        reviewNotes = if (review == KnowledgeReviewState.VERIFIED) "Reviewed." else null,
        confidenceState = confidence, confidenceReason = "Evidence assessment.",
        usageObservation = if (claimType == KnowledgeClaimType.USAGE_OBSERVATION) UsageObservationContext(
            gameMode = "Abyss", cycleOrVersion = "6.2", difficulty = null, encounter = null,
            sampleSize = 100, numerator = 50, denominator = 100, denominatorPopulation = "All submitted clears",
            ownershipContext = null, collectionStartedAt = null, collectionEndedAt = null,
            methodology = "Submitted data", status = UsageObservationStatus.FINALIZED
        ) else null
    )

    private fun candidate(
        id: Int,
        benefit: InvestmentAccountBenefit = InvestmentAccountBenefit.ROLE_GAP,
        synergy: InvestmentEvidenceState = InvestmentEvidenceState.SUPPORTED,
        patch: InvestmentEvidenceState = InvestmentEvidenceState.SUPPORTED,
        redundancy: InvestmentRedundancyState = InvestmentRedundancyState.NOT_REDUNDANT,
        evidence: List<String> = listOf("trusted"),
        sufficient: Boolean = false
    ) = InvestmentCandidateAssessment(
        characterId = id, characterName = "Character$id", accountBenefit = benefit,
        teamSynergy = synergy, patchRelevance = patch, redundancy = redundancy,
        alreadySufficient = sufficient, ownedTeamIds = listOf("team-a"),
        investmentRequirements = listOf("Level and talent materials"),
        supportingKnowledgeRecordIds = evidence
    )

    private fun evaluate(candidates: List<InvestmentCandidateAssessment>, records: List<GameKnowledgeRecord> = listOf(knowledge())) =
        InvestmentShortlistEvaluator.evaluate(candidates, records, now, "snapshot-2026-11-25")

    @Test fun supportedRoleGapRanksHighAndTeamImprovementRanksWorthInvesting() {
        val result = evaluate(listOf(candidate(1), candidate(2, benefit = InvestmentAccountBenefit.TEAM_IMPROVEMENT)))
        assertEquals(
            listOf(InvestmentPriorityBand.HIGH_PRIORITY, InvestmentPriorityBand.WORTH_INVESTING),
            result.recommendations.map { it.priority }
        )
        assertEquals("snapshot-2026-11-25", result.accountSnapshotReference)
        assertEquals(InvestmentShortlistEvaluator.RULE_SET_VERSION, result.ruleSetVersion)
    }

    @Test fun unknownOrNoAccountBenefitDoesNotFillTheShortlist() {
        val result = evaluate(listOf(
            candidate(1, benefit = InvestmentAccountBenefit.UNKNOWN),
            candidate(2, benefit = InvestmentAccountBenefit.NONE)
        ))
        assertTrue(result.recommendations.isEmpty())
    }

    @Test fun candidateRejectedAndUnsupportedRecordsCannotSupportInvestmentPriority() {
        val records = listOf(
            knowledge("candidate", review = KnowledgeReviewState.CANDIDATE),
            knowledge("rejected", review = KnowledgeReviewState.REJECTED),
            knowledge("limited", confidence = KnowledgeConfidenceState.LIMITED)
        )
        val result = evaluate(listOf(candidate(1, evidence = listOf("candidate", "rejected", "limited"))), records)
        assertTrue(result.recommendations.isEmpty())
    }

    @Test fun usageRateEvidenceAloneCannotPromoteACharacter() {
        val result = evaluate(
            listOf(candidate(1, evidence = listOf("usage"))),
            listOf(knowledge("usage", claimType = KnowledgeClaimType.USAGE_OBSERVATION))
        )
        assertTrue(result.recommendations.isEmpty())
    }

    @Test fun usageEvidenceMayAccompanyIndependentTrustedEvidenceButDoesNotReplaceIt() {
        val result = evaluate(
            listOf(candidate(1, evidence = listOf("trusted", "usage"))),
            listOf(knowledge(), knowledge("usage", claimType = KnowledgeClaimType.USAGE_OBSERVATION))
        )
        assertEquals(1, result.recommendations.size)
        assertEquals(listOf("trusted", "usage"), result.recommendations.single().supportingKnowledgeRecordIds)
    }

    @Test fun uncertainPatchOrSynergyProducesSituationalPriority() {
        val result = evaluate(listOf(candidate(1, patch = InvestmentEvidenceState.UNKNOWN)))
        assertEquals(InvestmentPriorityBand.SITUATIONAL, result.recommendations.single().priority)
        assertTrue(InvestmentReasonCode.MATERIAL_FACTORS_UNKNOWN in result.recommendations.single().reasonCodes)
    }

    @Test fun confirmedRedundancyLowersPriorityWithoutCallingCharacterBad() {
        val result = evaluate(listOf(candidate(1, redundancy = InvestmentRedundancyState.REDUNDANT)))
        val recommendation = result.recommendations.single()
        assertEquals(InvestmentPriorityBand.LOW_PRIORITY_FOR_NOW, recommendation.priority)
        assertTrue(recommendation.explanation.contains("other investments may offer more account value"))
    }

    @Test fun alreadySufficientIsExplicitAndMissingEvidenceDoesNotCreateFindings() {
        val result = evaluate(listOf(
            candidate(1, sufficient = true, evidence = emptyList()),
            candidate(2, evidence = emptyList())
        ))
        assertEquals(listOf(InvestmentPriorityBand.ALREADY_SUFFICIENT), result.recommendations.map { it.priority })
    }

    @Test fun orderingIsStableAndDeterministic() {
        val first = evaluate(listOf(candidate(9, benefit = InvestmentAccountBenefit.TEAM_IMPROVEMENT), candidate(2)))
        val second = evaluate(listOf(candidate(2), candidate(9, benefit = InvestmentAccountBenefit.TEAM_IMPROVEMENT)))
        assertEquals(first.recommendations, second.recommendations)
    }
}
