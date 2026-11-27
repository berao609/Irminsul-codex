package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.*
import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CatalogueUpgradePlanCalculatorTest {
    private val now = Instant.parse("2026-11-27T00:00:00Z")
    private val registry = listOf(
        KnowledgeSourceRegistryEntry(
            sourceId = "approved",
            platform = KnowledgeSourcePlatform.OTHER,
            publisherName = "Reviewed test source",
            canonicalSourceUrl = "https://example.org/costs",
            accessMethod = KnowledgeAccessMethod.MANUAL_CURATION,
            permissionState = KnowledgePermissionState.APPROVED,
            attributionRequirements = "Attribution",
            allowedActions = setOf(UpgradeCostRecordValidator.REQUIRED_SOURCE_ACTION),
            retentionAndDeletionPolicy = "Reviewed records only",
            rateLimitOrQuotaNotes = null,
            termsLastCheckedAt = now
        )
    )
    private val catalogue = MaterialCatalogue(
        listOf(
            MaterialCatalogueEntry(
                itemId = "202",
                displayName = "Mora",
                category = MaterialCategory.CURRENCY,
                sourceReferences = listOf(KnowledgeSourceReference("approved", "https://example.org/costs/mora")),
                retrievedAt = now,
                reviewState = KnowledgeReviewState.VERIFIED,
                reviewedAt = now,
                reviewNotes = "Identity verified."
            )
        ),
        registry
    )

    private fun record(id: String, track: UpgradeCostTrack, itemId: String, quantity: Long) =
        UpgradeCostRecord(
            schemaVersion = 1,
            recordId = id,
            revision = 1,
            subjectType = UpgradeCostSubjectType.CHARACTER,
            subjectId = "10000002",
            track = track,
            fromLevel = 1,
            toLevel = 2,
            gameVersion = "6.2",
            costs = listOf(UpgradeCostItem(itemId, quantity)),
            sourceReferences = listOf(KnowledgeSourceReference("approved", "https://example.org/costs/$id")),
            retrievedAt = now,
            reviewState = KnowledgeReviewState.VERIFIED,
            reviewedAt = now,
            reviewNotes = "Verified test data."
        )

    private fun request(track: UpgradeCostTrack) = UpgradeCostCalculationRequest(
        subjectType = UpgradeCostSubjectType.CHARACTER,
        subjectId = "10000002",
        track = track,
        fromLevel = 1,
        toLevel = 2,
        gameVersion = "6.2"
    )

    @Test fun aggregatesTracksAndResolvesReviewedMaterialNames() {
        val result = CatalogueUpgradePlanCalculator.calculate(
            UpgradePlanCalculationRequest(listOf(
                request(UpgradeCostTrack.CHARACTER_LEVEL),
                request(UpgradeCostTrack.CHARACTER_ASCENSION)
            )),
            listOf(
                record("level", UpgradeCostTrack.CHARACTER_LEVEL, "202", 100),
                record("ascension", UpgradeCostTrack.CHARACTER_ASCENSION, "202", 200)
            ),
            registry,
            now,
            catalogue,
            ManualMaterialInventory(listOf(ManualMaterialInventoryEntry("202", 50)), now)
        )

        assertEquals(UpgradePlanCalculationStatus.COMPLETE, result.calculation.status)
        assertEquals(1, result.materials.size)
        assertEquals("Mora", result.materials.single().displayName)
        assertEquals(300L, result.materials.single().requiredQuantity)
        assertEquals(50L, result.materials.single().ownedQuantity)
        assertEquals(250L, result.materials.single().remainingQuantity)
    }

    @Test fun incompleteTrackDoesNotDisplayPartialTotals() {
        val result = CatalogueUpgradePlanCalculator.calculate(
            UpgradePlanCalculationRequest(listOf(
                request(UpgradeCostTrack.CHARACTER_LEVEL),
                request(UpgradeCostTrack.CHARACTER_ASCENSION)
            )),
            listOf(record("level", UpgradeCostTrack.CHARACTER_LEVEL, "202", 100)),
            registry,
            now,
            catalogue
        )

        assertEquals(UpgradePlanCalculationStatus.COST_DATA_INCOMPLETE, result.calculation.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun unknownMaterialIdIsShownAsUnknownRatherThanTrustingCandidateName() {
        val result = CatalogueUpgradePlanCalculator.calculate(
            UpgradePlanCalculationRequest(listOf(request(UpgradeCostTrack.CHARACTER_LEVEL))),
            listOf(record("level", UpgradeCostTrack.CHARACTER_LEVEL, "9999", 4)),
            registry,
            now,
            catalogue
        )

        assertEquals("Unknown material (9999)", result.materials.single().displayName)
        assertEquals(MaterialCategory.UNKNOWN, result.materials.single().category)
    }
}
