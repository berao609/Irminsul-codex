package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class UpgradeCostCalculatorTest {
    private val evaluatedAt = Instant.parse("2026-12-01T00:00:00Z")
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
        termsLastCheckedAt = evaluatedAt
    )
    private val registry = listOf(source)

    private fun record(
        id: String,
        from: Int,
        to: Int,
        costs: List<UpgradeCostItem> = listOf(UpgradeCostItem("mora", 100)),
        track: UpgradeCostTrack = UpgradeCostTrack.CHARACTER_ASCENSION,
        talentSlot: String? = null,
        gameVersion: String = "6.2",
        reviewState: KnowledgeReviewState = KnowledgeReviewState.VERIFIED,
        expiresAt: Instant? = Instant.parse("2027-01-01T00:00:00Z")
    ) = UpgradeCostRecord(
        schemaVersion = 1,
        recordId = id,
        revision = 1,
        subjectType = UpgradeCostSubjectType.CHARACTER,
        subjectId = "10000002",
        track = track,
        fromLevel = from,
        toLevel = to,
        talentSlot = talentSlot,
        gameVersion = gameVersion,
        costs = costs,
        sourceReferences = listOf(
            KnowledgeSourceReference("cost-source", "https://data.example.org/genshin/costs/$id")
        ),
        retrievedAt = evaluatedAt,
        validFrom = Instant.parse("2026-11-01T00:00:00Z"),
        expiresAt = expiresAt,
        reviewState = reviewState,
        reviewedAt = evaluatedAt,
        reviewNotes = "Checked against source."
    )

    private fun request(
        from: Int = 1,
        to: Int = 40,
        track: UpgradeCostTrack = UpgradeCostTrack.CHARACTER_ASCENSION,
        talentSlot: String? = null
    ) = UpgradeCostCalculationRequest(
        subjectType = UpgradeCostSubjectType.CHARACTER,
        subjectId = "10000002",
        track = track,
        fromLevel = from,
        toLevel = to,
        gameVersion = "6.2",
        talentSlot = talentSlot
    )

    @Test fun aggregatesCompleteContiguousSegmentsByMaterialId() {
        val result = UpgradeCostCalculator.calculate(
            request(),
            listOf(
                record("a", 1, 20, listOf(UpgradeCostItem("mora", 100), UpgradeCostItem("ore", 2))),
                record("b", 20, 40, listOf(UpgradeCostItem("mora", 300), UpgradeCostItem("boss-drop", 3)))
            ),
            registry,
            evaluatedAt
        )
        assertEquals(UpgradeCostCalculationStatus.COMPLETE, result.status)
        assertEquals(listOf("a", "b"), result.usedRecordIds)
        assertEquals(
            mapOf("mora" to 400L, "ore" to 2L, "boss-drop" to 3L),
            result.requirements.associate { it.itemId to it.requiredQuantity }
        )
    }

    @Test fun missingSegmentNeverBecomesZeroCost() {
        val result = UpgradeCostCalculator.calculate(
            request(),
            listOf(record("a", 1, 20)),
            registry,
            evaluatedAt
        )
        assertEquals(UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE, result.status)
        assertEquals(20, result.missingFromLevel)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun detectsOverlappingAndDuplicateRevisionSegments() {
        val result = UpgradeCostCalculator.calculate(
            request(),
            listOf(record("a", 1, 30), record("b", 20, 40)),
            registry,
            evaluatedAt
        )
        assertEquals(UpgradeCostCalculationStatus.OVERLAPPING_COST_SEGMENTS, result.status)
    }

    @Test fun rejectsUnverifiedAndExpiredMatchingRecords() {
        val unverified = record("candidate", 1, 40, reviewState = KnowledgeReviewState.CANDIDATE)
        val expired = record(
            "expired", 1, 40,
            expiresAt = Instant.parse("2026-11-30T00:00:00Z")
        )
        assertEquals(
            UpgradeCostCalculationStatus.INVALID_COST_DATA,
            UpgradeCostCalculator.calculate(request(), listOf(unverified), registry, evaluatedAt).status
        )
        assertEquals(
            UpgradeCostCalculationStatus.INVALID_COST_DATA,
            UpgradeCostCalculator.calculate(request(), listOf(expired), registry, evaluatedAt).status
        )
    }

    @Test fun requiresExactGameVersionAndSubjectTrack() {
        val wrongVersion = record("wrong-version", 1, 40, gameVersion = "6.1")
        assertEquals(
            UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE,
            UpgradeCostCalculator.calculate(request(), listOf(wrongVersion), registry, evaluatedAt).status
        )
        val wrongTrack = record("wrong-track", 1, 40, track = UpgradeCostTrack.CHARACTER_LEVEL)
        assertEquals(
            UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE,
            UpgradeCostCalculator.calculate(request(), listOf(wrongTrack), registry, evaluatedAt).status
        )
    }

    @Test fun manualInventoryDistinguishesNotEnteredFromExplicitZero() {
        val cost = listOf(record("all", 1, 40, listOf(
            UpgradeCostItem("mora", 100),
            UpgradeCostItem("ore", 5)
        )))
        val withoutInventory = UpgradeCostCalculator.calculate(request(), cost, registry, evaluatedAt)
        assertTrue(withoutInventory.requirements.all {
            it.inventoryState == MaterialInventoryEntryState.NOT_ENTERED &&
                it.ownedQuantity == null && it.remainingQuantity == null
        })

        val manual = ManualMaterialInventory(
            entries = listOf(
                ManualMaterialInventoryEntry("mora", 25),
                ManualMaterialInventoryEntry("ore", 0)
            ),
            updatedAt = evaluatedAt
        )
        val withInventory = UpgradeCostCalculator.calculate(request(), cost, registry, evaluatedAt, manual)
        assertEquals(UpgradeCostCalculationStatus.COMPLETE, withInventory.status)
        val byId = withInventory.requirements.associateBy { it.itemId }
        assertEquals(25L, byId.getValue("mora").ownedQuantity)
        assertEquals(75L, byId.getValue("mora").remainingQuantity)
        assertEquals(0L, byId.getValue("ore").ownedQuantity)
        assertEquals(5L, byId.getValue("ore").remainingQuantity)
    }

    @Test fun inventoryAboveRequirementHasZeroRemainingAndMissingItemsStayNotEntered() {
        val cost = listOf(record("all", 1, 40, listOf(
            UpgradeCostItem("mora", 100),
            UpgradeCostItem("ore", 5)
        )))
        val manual = ManualMaterialInventory(
            entries = listOf(ManualMaterialInventoryEntry("mora", 500)),
            updatedAt = evaluatedAt
        )
        val result = UpgradeCostCalculator.calculate(request(), cost, registry, evaluatedAt, manual)
        val byId = result.requirements.associateBy { it.itemId }
        assertEquals(0L, byId.getValue("mora").remainingQuantity)
        assertEquals(MaterialInventoryEntryState.NOT_ENTERED, byId.getValue("ore").inventoryState)
        assertNull(byId.getValue("ore").remainingQuantity)
    }

    @Test fun invalidManualInventoryDoesNotCorruptRequiredTotalsOrProduceRemainingTotals() {
        val cost = listOf(record("all", 1, 40))
        val manual = ManualMaterialInventory(
            entries = listOf(
                ManualMaterialInventoryEntry("mora", -1),
                ManualMaterialInventoryEntry("mora", 50)
            ),
            updatedAt = evaluatedAt
        )
        val result = UpgradeCostCalculator.calculate(request(), cost, registry, evaluatedAt, manual)
        assertEquals(UpgradeCostCalculationStatus.COMPLETE, result.status)
        assertEquals(100L, result.requirements.single().requiredQuantity)
        assertTrue(result.inventoryIssues.isNotEmpty())
        assertNull(result.requirements.single().ownedQuantity)
        assertNull(result.requirements.single().remainingQuantity)
    }

    @Test fun detectsQuantityOverflowInsteadOfWrapping() {
        val result = UpgradeCostCalculator.calculate(
            request(),
            listOf(
                record("a", 1, 20, listOf(UpgradeCostItem("mora", Long.MAX_VALUE))),
                record("b", 20, 40, listOf(UpgradeCostItem("mora", 1)))
            ),
            registry,
            evaluatedAt
        )
        assertEquals(UpgradeCostCalculationStatus.QUANTITY_OVERFLOW, result.status)
    }

    @Test fun rejectsInvalidRequestsAndRequiresTalentSlot() {
        val invalidRange = request(from = 40, to = 40)
        assertEquals(
            UpgradeCostCalculationStatus.INVALID_REQUEST,
            UpgradeCostCalculator.calculate(invalidRange, emptyList(), registry, evaluatedAt).status
        )
        val talentWithoutSlot = request(track = UpgradeCostTrack.TALENT_LEVEL)
        assertEquals(
            UpgradeCostCalculationStatus.INVALID_REQUEST,
            UpgradeCostCalculator.calculate(talentWithoutSlot, emptyList(), registry, evaluatedAt).status
        )
        val talent = record(
            "talent", 1, 2,
            track = UpgradeCostTrack.TALENT_LEVEL,
            talentSlot = "elemental-skill"
        )
        val validTalentRequest = request(from = 1, to = 2, track = UpgradeCostTrack.TALENT_LEVEL, talentSlot = "elemental-skill")
        assertEquals(
            UpgradeCostCalculationStatus.COMPLETE,
            UpgradeCostCalculator.calculate(validTalentRequest, listOf(talent), registry, evaluatedAt).status
        )
    }
}
