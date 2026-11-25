package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.CalculatedMaterialRequirement
import com.irminsul.codex.domain.analysis.ManualMaterialInventory
import com.irminsul.codex.domain.analysis.ManualMaterialInventoryEntry
import com.irminsul.codex.domain.analysis.MaterialInventoryEntryState
import com.irminsul.codex.domain.analysis.UpgradeCostCalculationRequest
import com.irminsul.codex.domain.analysis.UpgradeCostCalculationStatus
import com.irminsul.codex.domain.model.KnowledgeAccessMethod
import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import com.irminsul.codex.domain.model.KnowledgeSourcePlatform
import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.KnowledgeSourceRegistryEntry
import com.irminsul.codex.domain.model.UpgradeCostItem
import com.irminsul.codex.domain.model.UpgradeCostRecord
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.Instant

class MaterialCatalogueTest {
    @Test
    fun resolvesReviewedMetadataWithoutChangingCalculatedQuantities() {
        val catalogue = MaterialCatalogue(
            listOf(
                MaterialCatalogueEntry(
                    itemId = "202",
                    displayName = "Mora",
                    category = MaterialCategory.CURRENCY,
                    rarity = 3,
                    iconUrl = "https://example.org/mora.png",
                    sourceReferences = listOf(KnowledgeSourceReference("approved-test", "https://example.org/materials/mora")),
                    retrievedAt = Instant.parse("2026-11-25T00:00:00Z"),
                    reviewState = KnowledgeReviewState.VERIFIED,
                    reviewedAt = Instant.parse("2026-11-25T00:00:00Z"),
                    reviewNotes = "Manually cross-checked against an approved source."
                )
            ),
            approvedRegistry()
        )

        val display = catalogue.display(
            CalculatedMaterialRequirement(
                itemId = "202",
                requiredQuantity = 120_000L,
                inventoryState = MaterialInventoryEntryState.ENTERED,
                ownedQuantity = 50_000L,
                remainingQuantity = 70_000L
            )
        )

        assertEquals("Mora", display.displayName)
        assertEquals(MaterialCategory.CURRENCY, display.category)
        assertEquals(120_000L, display.requiredQuantity)
        assertEquals(50_000L, display.ownedQuantity)
        assertEquals(70_000L, display.remainingQuantity)
    }

    @Test
    fun unknownMaterialRemainsExplicitAndDoesNotTrustCandidateName() {
        val display = MaterialCatalogue(emptyList(), emptyList()).display(
            CalculatedMaterialRequirement(
                itemId = "9999",
                requiredQuantity = 4L,
                inventoryState = MaterialInventoryEntryState.NOT_ENTERED
            )
        )

        assertEquals("Unknown material (9999)", display.displayName)
        assertEquals(MaterialCategory.UNKNOWN, display.category)
        assertNull(display.iconUrl)
        assertNull(display.ownedQuantity)
    }

    @Test
    fun rejectsDuplicateCatalogueIds() {
        val duplicate = MaterialCatalogueEntry(
            itemId = "202",
            displayName = "Mora",
            category = MaterialCategory.CURRENCY,
            sourceReferences = listOf(KnowledgeSourceReference("approved-test", "https://example.org/materials/mora")),
            retrievedAt = Instant.parse("2026-11-25T00:00:00Z"),
            reviewState = KnowledgeReviewState.VERIFIED,
            reviewedAt = Instant.parse("2026-11-25T00:00:00Z"),
            reviewNotes = "Reviewed."
        )
        assertThrows(IllegalArgumentException::class.java) {
            MaterialCatalogue(listOf(duplicate, duplicate.copy(displayName = "Duplicate")), approvedRegistry())
        }
    }

    @Test
    fun facadeDelegatesToExistingCalculatorAndPreservesMissingCostStatus() {
        val request = UpgradeCostCalculationRequest(
            subjectType = UpgradeCostSubjectType.CHARACTER,
            subjectId = "10000002",
            track = UpgradeCostTrack.CHARACTER_ASCENSION,
            fromLevel = 1,
            toLevel = 20,
            gameVersion = "6.2"
        )
        val result = CatalogueUpgradeCostCalculator.calculate(
            request = request,
            records = emptyList(),
            sourceRegistry = emptyList(),
            evaluatedAt = Instant.parse("2026-11-26T00:00:00Z"),
            catalogue = MaterialCatalogue(emptyList(), emptyList()),
            inventory = null
        )

        assertEquals(UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE, result.calculation.status)
        assertEquals(request, result.calculation.request)
        assertEquals(emptyList<MaterialRequirementDisplay>(), result.materials)
    }

    @Test
    fun rejectsCatalogueMetadataFromUnapprovedSource() {
        val entry = MaterialCatalogueEntry(
            itemId = "202",
            displayName = "Mora",
            category = MaterialCategory.CURRENCY,
            sourceReferences = listOf(KnowledgeSourceReference("pending-test", "https://example.org/materials/mora")),
            retrievedAt = Instant.parse("2026-11-25T00:00:00Z"),
            reviewState = KnowledgeReviewState.VERIFIED,
            reviewedAt = Instant.parse("2026-11-25T00:00:00Z"),
            reviewNotes = "Reviewed."
        )
        assertThrows(IllegalArgumentException::class.java) {
            MaterialCatalogue(listOf(entry), listOf(approvedRegistry().single().copy(
                sourceId = "pending-test",
                permissionState = KnowledgePermissionState.PENDING_REVIEW
            )))
        }
    }

    @Test
    fun verifiedCostRecordFlowsThroughExistingCalculatorAndCatalogue() {
        val now = Instant.parse("2026-11-26T00:00:00Z")
        val registry = approvedRegistry()
        val record = UpgradeCostRecord(
            schemaVersion = 1,
            recordId = "character-10000002-ascension-1-20",
            revision = 1,
            subjectType = UpgradeCostSubjectType.CHARACTER,
            subjectId = "10000002",
            track = UpgradeCostTrack.CHARACTER_ASCENSION,
            fromLevel = 1,
            toLevel = 20,
            gameVersion = "6.2",
            costs = listOf(UpgradeCostItem(itemId = "202", quantity = 20_000L)),
            sourceReferences = listOf(
                KnowledgeSourceReference("approved-test", "https://example.org/materials/character-ascension")
            ),
            retrievedAt = now,
            reviewState = KnowledgeReviewState.VERIFIED,
            reviewedAt = now,
            reviewNotes = "Verified against the approved source."
        )
        val catalogue = MaterialCatalogue(
            listOf(
                MaterialCatalogueEntry(
                    itemId = "202",
                    displayName = "Mora",
                    category = MaterialCategory.CURRENCY,
                    sourceReferences = listOf(
                        KnowledgeSourceReference("approved-test", "https://example.org/materials/mora")
                    ),
                    retrievedAt = now,
                    reviewState = KnowledgeReviewState.VERIFIED,
                    reviewedAt = now,
                    reviewNotes = "Material identity and category manually verified."
                )
            ),
            registry
        )
        val result = CatalogueUpgradeCostCalculator.calculate(
            request = UpgradeCostCalculationRequest(
                subjectType = UpgradeCostSubjectType.CHARACTER,
                subjectId = "10000002",
                track = UpgradeCostTrack.CHARACTER_ASCENSION,
                fromLevel = 1,
                toLevel = 20,
                gameVersion = "6.2"
            ),
            records = listOf(record),
            sourceRegistry = registry,
            evaluatedAt = now,
            catalogue = catalogue,
            inventory = ManualMaterialInventory(
                entries = listOf(ManualMaterialInventoryEntry("202", 5_000L)),
                updatedAt = now
            )
        )

        assertEquals(UpgradeCostCalculationStatus.COMPLETE, result.calculation.status)
        assertEquals(1, result.materials.size)
        assertEquals("Mora", result.materials.single().displayName)
        assertEquals(20_000L, result.materials.single().requiredQuantity)
        assertEquals(5_000L, result.materials.single().ownedQuantity)
        assertEquals(15_000L, result.materials.single().remainingQuantity)
    }

    private fun approvedRegistry() = listOf(
        KnowledgeSourceRegistryEntry(
            sourceId = "approved-test",
            platform = KnowledgeSourcePlatform.OTHER,
            publisherName = "Test publisher",
            canonicalSourceUrl = "https://example.org/catalogue",
            accessMethod = KnowledgeAccessMethod.PUBLIC_METADATA,
            permissionState = KnowledgePermissionState.APPROVED,
            attributionRequirements = "Test attribution",
            allowedActions = setOf("material_cost_curation"),
            retentionAndDeletionPolicy = "Test policy",
            rateLimitOrQuotaNotes = null,
            termsLastCheckedAt = Instant.parse("2026-11-25T00:00:00Z")
        )
    )
}
