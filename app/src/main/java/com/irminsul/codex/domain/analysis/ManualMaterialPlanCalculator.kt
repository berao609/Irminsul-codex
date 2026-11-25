package com.irminsul.codex.domain.analysis

enum class ManualPlanCalculationStatus {
    COMPLETE,
    REQUIRED_NOT_ENTERED,
    INVENTORY_NOT_ENTERED,
    INVALID_QUANTITY
}

data class ManualPlanCalculation(
    val status: ManualPlanCalculationStatus,
    val requiredQuantity: Long? = null,
    val ownedQuantity: Long? = null,
    val remainingQuantity: Long? = null
)

/**
 * Calculates a player-authored material plan row. This is intentionally
 * separate from verified upgrade-cost records: both quantities are manual.
 */
object ManualMaterialPlanCalculator {
    fun calculate(requiredInput: String, ownedInput: String): ManualPlanCalculation {
        if (requiredInput.isBlank()) {
            return ManualPlanCalculation(ManualPlanCalculationStatus.REQUIRED_NOT_ENTERED)
        }
        val required = requiredInput.trim().toLongOrNull()
            ?: return ManualPlanCalculation(ManualPlanCalculationStatus.INVALID_QUANTITY)
        if (required < 0L) {
            return ManualPlanCalculation(ManualPlanCalculationStatus.INVALID_QUANTITY)
        }
        if (ownedInput.isBlank()) {
            return ManualPlanCalculation(
                status = ManualPlanCalculationStatus.INVENTORY_NOT_ENTERED,
                requiredQuantity = required
            )
        }
        val owned = ownedInput.trim().toLongOrNull()
            ?: return ManualPlanCalculation(ManualPlanCalculationStatus.INVALID_QUANTITY)
        if (owned < 0L) {
            return ManualPlanCalculation(ManualPlanCalculationStatus.INVALID_QUANTITY)
        }
        return ManualPlanCalculation(
            status = ManualPlanCalculationStatus.COMPLETE,
            requiredQuantity = required,
            ownedQuantity = owned,
            remainingQuantity = (required - owned).coerceAtLeast(0L)
        )
    }
}
