package com.irminsul.codex.data.catalogue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EnhancementMaterialInventoryCalculatorTest {
    @Test fun unknownInventoryDoesNotPretendTheOwnedQuantityIsZero() {
        assertNull(EnhancementMaterialInventoryCalculator.remainingQuantity(125L, null))
    }

    @Test fun explicitZeroLeavesTheFullRequirementRemaining() {
        assertEquals(125L, EnhancementMaterialInventoryCalculator.remainingQuantity(125L, 0L))
    }

    @Test fun enteredInventoryIsSubtractedAndClampedAtZero() {
        assertEquals(45L, EnhancementMaterialInventoryCalculator.remainingQuantity(125L, 80L))
        assertEquals(0L, EnhancementMaterialInventoryCalculator.remainingQuantity(125L, 150L))
    }

    @Test fun negativeQuantitiesAreRejected() {
        assertIllegalArgument { EnhancementMaterialInventoryCalculator.remainingQuantity(-1L, 0L) }
        assertIllegalArgument { EnhancementMaterialInventoryCalculator.remainingQuantity(1L, -1L) }
    }

    private fun assertIllegalArgument(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Negative quantity should be rejected.")
        } catch (_: IllegalArgumentException) {
        }
    }
}
