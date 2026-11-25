package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.CalculatedMaterialRequirement
import com.irminsul.codex.domain.analysis.ManualMaterialInventory
import com.irminsul.codex.domain.analysis.MaterialInventoryEntryState
import com.irminsul.codex.domain.analysis.UpgradeCostCalculationRequest
import com.irminsul.codex.domain.analysis.UpgradeCostCalculationStatus
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
                    iconUrl = "https://example.org/mora.png"
                )
            )
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
        val display = MaterialCatalogue(emptyList()).display(
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
        val duplicate = MaterialCatalogueEntry("202", "Mora", MaterialCategory.CURRENCY)
        assertThrows(IllegalArgumentException::class.java) {
            MaterialCatalogue(listOf(duplicate, duplicate.copy(displayName = "Duplicate")))
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
            catalogue = MaterialCatalogue(emptyList()),
            inventory = null
        )

        assertEquals(UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE, result.calculation.status)
        assertEquals(request, result.calculation.request)
        assertEquals(emptyList<MaterialRequirementDisplay>(), result.materials)
    }
}
