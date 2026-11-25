package com.irminsul.codex.domain.analysis

import org.junit.Assert.*
import org.junit.Test

class ManualMaterialPlanCalculatorTest {
    @Test fun calculatesRemainingQuantity() {
        val result = ManualMaterialPlanCalculator.calculate("100", "25")
        assertEquals(ManualPlanCalculationStatus.COMPLETE, result.status)
        assertEquals(100L, result.requiredQuantity)
        assertEquals(25L, result.ownedQuantity)
        assertEquals(75L, result.remainingQuantity)
    }

    @Test fun explicitZeroIsDifferentFromInventoryNotEntered() {
        val zero = ManualMaterialPlanCalculator.calculate("100", "0")
        assertEquals(ManualPlanCalculationStatus.COMPLETE, zero.status)
        assertEquals(100L, zero.remainingQuantity)

        val notEntered = ManualMaterialPlanCalculator.calculate("100", "")
        assertEquals(ManualPlanCalculationStatus.INVENTORY_NOT_ENTERED, notEntered.status)
        assertEquals(100L, notEntered.requiredQuantity)
        assertNull(notEntered.ownedQuantity)
        assertNull(notEntered.remainingQuantity)
    }

    @Test fun inventoryAboveRequirementHasNoNegativeRemainder() {
        val result = ManualMaterialPlanCalculator.calculate("100", "150")
        assertEquals(0L, result.remainingQuantity)
    }

    @Test fun missingRequiredQuantityIsNotZero() {
        assertEquals(
            ManualPlanCalculationStatus.REQUIRED_NOT_ENTERED,
            ManualMaterialPlanCalculator.calculate("", "10").status
        )
    }

    @Test fun rejectsNegativeMalformedAndOverflowingQuantities() {
        listOf("-1", "abc", "9223372036854775808").forEach { value ->
            assertEquals(
                ManualPlanCalculationStatus.INVALID_QUANTITY,
                ManualMaterialPlanCalculator.calculate(value, "0").status
            )
        }
        assertEquals(
            ManualPlanCalculationStatus.INVALID_QUANTITY,
            ManualMaterialPlanCalculator.calculate("10", "-1").status
        )
    }
}
