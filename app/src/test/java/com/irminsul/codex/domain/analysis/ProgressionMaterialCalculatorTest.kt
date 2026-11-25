package com.irminsul.codex.domain.analysis

import org.junit.Assert.*
import org.junit.Test

class ProgressionMaterialCalculatorTest {

    @Test fun aggregatesCostsAcrossUpgradeStepsByStableMaterialId() {
        val result = ProgressionMaterialCalculator.calculate(
            currentLevel = 1,
            targetLevel = 3,
            costSteps = listOf(
                step(2, cost(100, "Mora", 10), cost(200, "Slime Condensate", 2)),
                step(3, cost(100, "Mora", 20), cost(200, "Slime Condensate", 3))
            )
        )
        assertEquals(ProgressionCalculationStatus.COMPLETE, result.status)
        assertEquals(30L, result.materials.single { it.materialId == 100 }.requiredQuantity)
        assertEquals(5L, result.materials.single { it.materialId == 200 }.requiredQuantity)
    }

    @Test fun inventoryDistinguishesUnknownFromExplicitZero() {
        val steps = listOf(step(2, cost(100, "Mora", 50), cost(200, "Slime Condensate", 4)))
        val result = ProgressionMaterialCalculator.calculate(1, 2, steps, mapOf(100 to 0L))
        val mora = result.materials.single { it.materialId == 100 }
        val slime = result.materials.single { it.materialId == 200 }
        assertEquals(0L, mora.ownedQuantity)
        assertEquals(50L, mora.missingQuantity)
        assertNull(slime.ownedQuantity)
        assertNull(slime.missingQuantity)
    }

    @Test fun inventoryAboveRequirementHasZeroMissing() {
        val result = ProgressionMaterialCalculator.calculate(
            1, 2, listOf(step(2, cost(100, "Mora", 50))), mapOf(100 to 80L)
        )
        assertEquals(0L, result.materials.single().missingQuantity)
    }

    @Test fun missingIntermediateCostStepIsNotTreatedAsFree() {
        val result = ProgressionMaterialCalculator.calculate(
            1, 3, listOf(step(2, cost(100, "Mora", 10)))
        )
        assertEquals(ProgressionCalculationStatus.MISSING_COST_DATA, result.status)
        assertEquals(listOf(3), result.missingTargetLevels)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun sameCurrentAndTargetLevelNeedsNoCostRecords() {
        val result = ProgressionMaterialCalculator.calculate(90, 90, emptyList())
        assertEquals(ProgressionCalculationStatus.COMPLETE, result.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun rejectsInvalidRangesAndInventory() {
        assertEquals(
            ProgressionCalculationStatus.INVALID_LEVEL_RANGE,
            ProgressionMaterialCalculator.calculate(3, 2, emptyList()).status
        )
        assertEquals(
            ProgressionCalculationStatus.INVALID_LEVEL_RANGE,
            ProgressionMaterialCalculator.calculate(-1, 2, emptyList()).status
        )
        assertEquals(
            ProgressionCalculationStatus.INVALID_COST_DATA,
            ProgressionMaterialCalculator.calculate(1, 2, listOf(step(2, cost(100, "Mora", 1))), mapOf(100 to -1L)).status
        )
    }

    @Test fun rejectsMalformedCostsDuplicateStepsAndConflictingNames() {
        assertEquals(
            ProgressionCalculationStatus.INVALID_COST_DATA,
            ProgressionMaterialCalculator.calculate(1, 2, listOf(step(2, cost(100, "", 1)))).status
        )
        assertEquals(
            ProgressionCalculationStatus.INVALID_COST_DATA,
            ProgressionMaterialCalculator.calculate(1, 2, listOf(step(2), step(2))).status
        )
        assertEquals(
            ProgressionCalculationStatus.INVALID_COST_DATA,
            ProgressionMaterialCalculator.calculate(
                1, 3, listOf(
                    step(2, cost(100, "Mora", 1)),
                    step(3, cost(100, "Different Item", 1))
                )
            ).status
        )
    }

    @Test fun detectsLongOverflowInsteadOfReturningCorruptTotals() {
        val result = ProgressionMaterialCalculator.calculate(
            1, 3, listOf(
                step(2, cost(100, "Mora", Long.MAX_VALUE)),
                step(3, cost(100, "Mora", 1))
            )
        )
        assertEquals(ProgressionCalculationStatus.QUANTITY_OVERFLOW, result.status)
    }

    private fun step(level: Int, vararg materials: UpgradeMaterialCost) =
        UpgradeCostStep(level, materials.toList())

    private fun cost(id: Int, name: String, quantity: Long) =
        UpgradeMaterialCost(id, name, quantity)
}
