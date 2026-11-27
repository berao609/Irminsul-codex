package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class UpgradePlanCalculatorTest {
    private val now = Instant.parse("2026-11-27T00:00:00Z")
    private val source = KnowledgeSourceRegistryEntry(
        sourceId = "source",
        platform = KnowledgeSourcePlatform.OTHER,
        publisherName = "Test source",
        canonicalSourceUrl = "https://costs.example.test/data",
        accessMethod = KnowledgeAccessMethod.MANUAL_CURATION,
        permissionState = KnowledgePermissionState.APPROVED,
        attributionRequirements = "Test attribution",
        allowedActions = setOf(UpgradeCostRecordValidator.REQUIRED_SOURCE_ACTION),
        retentionAndDeletionPolicy = "Reviewed facts only",
        rateLimitOrQuotaNotes = null,
        termsLastCheckedAt = now
    )
    private val registry = listOf(source)

    private fun record(
        id: String,
        track: UpgradeCostTrack,
        slot: String? = null,
        item: String,
        quantity: Long
    ) = UpgradeCostRecord(
        schemaVersion = 1,
        recordId = id,
        revision = 1,
        subjectType = UpgradeCostSubjectType.CHARACTER,
        subjectId = "10000002",
        track = track,
        fromLevel = 1,
        toLevel = 2,
        talentSlot = slot,
        gameVersion = "6.2",
        costs = listOf(UpgradeCostItem(item, quantity)),
        sourceReferences = listOf(KnowledgeSourceReference("source", "https://costs.example.test/data/$id")),
        retrievedAt = now,
        validFrom = now,
        expiresAt = Instant.parse("2027-01-01T00:00:00Z"),
        reviewState = KnowledgeReviewState.VERIFIED,
        reviewedAt = now,
        reviewNotes = "Verified test fixture."
    )

    private fun request(track: UpgradeCostTrack, slot: String? = null) =
        UpgradeCostCalculationRequest(
            subjectType = UpgradeCostSubjectType.CHARACTER,
            subjectId = "10000002",
            track = track,
            fromLevel = 1,
            toLevel = 2,
            gameVersion = "6.2",
            talentSlot = slot
        )

    @Test fun combinesCharacterLevelAndTalentCostsIntoOneMaterialList() {
        val result = UpgradePlanCalculator.calculate(
            UpgradePlanCalculationRequest(listOf(
                request(UpgradeCostTrack.CHARACTER_LEVEL),
                request(UpgradeCostTrack.CHARACTER_ASCENSION),
                request(UpgradeCostTrack.TALENT_LEVEL, "normal")
            )),
            listOf(
                record("level", UpgradeCostTrack.CHARACTER_LEVEL, item = "mora", quantity = 100),
                record("ascension", UpgradeCostTrack.CHARACTER_ASCENSION, item = "mora", quantity = 200),
                record("talent", UpgradeCostTrack.TALENT_LEVEL, "normal", "book", 3)
            ),
            registry,
            now
        )
        assertEquals(UpgradePlanCalculationStatus.COMPLETE, result.status)
        assertEquals(mapOf("mora" to 300L, "book" to 3L),
            result.requirements.associate { it.itemId to it.requiredQuantity })
    }

    @Test fun withholdsAllTotalsWhenAnyRequestedTrackIsMissing() {
        val result = UpgradePlanCalculator.calculate(
            UpgradePlanCalculationRequest(listOf(
                request(UpgradeCostTrack.CHARACTER_LEVEL),
                request(UpgradeCostTrack.CHARACTER_ASCENSION)
            )),
            listOf(record("level", UpgradeCostTrack.CHARACTER_LEVEL, item = "mora", quantity = 100)),
            registry,
            now
        )
        assertEquals(UpgradePlanCalculationStatus.COST_DATA_INCOMPLETE, result.status)
        assertTrue(result.requirements.isEmpty())
        assertEquals(1, result.missingTracks.size)
    }

    @Test fun rejectsDuplicateTrackRequests() {
        val track = request(UpgradeCostTrack.CHARACTER_LEVEL)
        val result = UpgradePlanCalculator.calculate(
            UpgradePlanCalculationRequest(listOf(track, track)),
            emptyList(),
            registry,
            now
        )
        assertEquals(UpgradePlanCalculationStatus.INVALID_REQUEST, result.status)
    }

    @Test fun calculatesOwnedAndRemainingOnceAfterAggregatingDuplicateMaterials() {
        val result = UpgradePlanCalculator.calculate(
            UpgradePlanCalculationRequest(listOf(
                request(UpgradeCostTrack.CHARACTER_LEVEL),
                request(UpgradeCostTrack.CHARACTER_ASCENSION)
            )),
            listOf(
                record("level", UpgradeCostTrack.CHARACTER_LEVEL, item = "mora", quantity = 100),
                record("ascension", UpgradeCostTrack.CHARACTER_ASCENSION, item = "mora", quantity = 200)
            ),
            registry,
            now,
            ManualMaterialInventory(listOf(ManualMaterialInventoryEntry("mora", 250)), now)
        )
        assertEquals(UpgradePlanCalculationStatus.COMPLETE, result.status)
        assertEquals(300L, result.requirements.single().requiredQuantity)
        assertEquals(250L, result.requirements.single().ownedQuantity)
        assertEquals(50L, result.requirements.single().remainingQuantity)
    }
}
