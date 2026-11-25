package com.irminsul.codex.domain.analysis

/**
 * One material cost entry for a single upgrade step.
 *
 * [materialId] must be the stable in-game item ID from the selected catalogue.
 * A missing inventory entry is intentionally different from an explicit zero.
 */
data class UpgradeMaterialCost(
    val materialId: Int,
    val materialName: String,
    val quantity: Long
)

/**
 * Costs applied when a progression target reaches [targetLevel].
 *
 * Examples:
 * - Character/weapon ascension: target levels 20, 40, 50, 60, 70, 80.
 * - Talent upgrades: target levels 2 through 10.
 */
data class UpgradeCostStep(
    val targetLevel: Int,
    val materials: List<UpgradeMaterialCost>
)

data class CalculatedMaterialRequirement(
    val materialId: Int,
    val materialName: String,
    val requiredQuantity: Long,
    /** Null means inventory has not been entered for this material. */
    val ownedQuantity: Long?,
    /** Null when owned quantity is unknown. */
    val missingQuantity: Long?
)

enum class ProgressionCalculationStatus {
    COMPLETE,
    INVALID_LEVEL_RANGE,
    MISSING_COST_DATA,
    INVALID_COST_DATA,
    QUANTITY_OVERFLOW
}

data class ProgressionMaterialCalculation(
    val status: ProgressionCalculationStatus,
    val materials: List<CalculatedMaterialRequirement> = emptyList(),
    val missingTargetLevels: List<Int> = emptyList()
)

/**
 * Deterministic aggregation engine for versioned, normalized game catalogue costs.
 *
 * This class does not invent costs. Every level step between current and target
 * must have an explicit catalogue record, including a valid empty record when
 * the source confirms that a step has no material cost.
 */
object ProgressionMaterialCalculator {

    fun calculate(
        currentLevel: Int,
        targetLevel: Int,
        costSteps: List<UpgradeCostStep>,
        inventoryByMaterialId: Map<Int, Long> = emptyMap()
    ): ProgressionMaterialCalculation {
        if (currentLevel < 0 || targetLevel < 0 || targetLevel < currentLevel) {
            return ProgressionMaterialCalculation(
                status = ProgressionCalculationStatus.INVALID_LEVEL_RANGE
            )
        }
        if (inventoryByMaterialId.any { (id, quantity) -> id <= 0 || quantity < 0L }) {
            return ProgressionMaterialCalculation(
                status = ProgressionCalculationStatus.INVALID_COST_DATA
            )
        }

        val stepsByLevel = costSteps.groupBy { it.targetLevel }
        val requiredLevels = if (targetLevel == currentLevel) {
            emptyList()
        } else {
            (currentLevel + 1..targetLevel).toList()
        }
        val missingLevels = requiredLevels.filter { stepsByLevel[it].isNullOrEmpty() }
        if (missingLevels.isNotEmpty()) {
            return ProgressionMaterialCalculation(
                status = ProgressionCalculationStatus.MISSING_COST_DATA,
                missingTargetLevels = missingLevels
            )
        }

        val totals = linkedMapOf<Int, Pair<String, Long>>()
        for (level in requiredLevels) {
            val steps = stepsByLevel[level].orEmpty()
            if (steps.size != 1) {
                return ProgressionMaterialCalculation(
                    status = ProgressionCalculationStatus.INVALID_COST_DATA
                )
            }
            for (cost in steps.single().materials) {
                if (cost.materialId <= 0 || cost.materialName.isBlank() || cost.quantity <= 0L) {
                    return ProgressionMaterialCalculation(
                        status = ProgressionCalculationStatus.INVALID_COST_DATA
                    )
                }
                val existing = totals[cost.materialId]
                if (existing != null && !existing.first.equals(cost.materialName, ignoreCase = true)) {
                    return ProgressionMaterialCalculation(
                        status = ProgressionCalculationStatus.INVALID_COST_DATA
                    )
                }
                val sum = try {
                    Math.addExact(existing?.second ?: 0L, cost.quantity)
                } catch (_: ArithmeticException) {
                    return ProgressionMaterialCalculation(
                        status = ProgressionCalculationStatus.QUANTITY_OVERFLOW
                    )
                }
                totals[cost.materialId] = (existing?.first ?: cost.materialName) to sum
            }
        }

        val result = totals.map { (id, value) ->
            val owned = inventoryByMaterialId[id]
            val missing = owned?.let { (value.second - it).coerceAtLeast(0L) }
            CalculatedMaterialRequirement(
                materialId = id,
                materialName = value.first,
                requiredQuantity = value.second,
                ownedQuantity = owned,
                missingQuantity = missing
            )
        }
        return ProgressionMaterialCalculation(
            status = ProgressionCalculationStatus.COMPLETE,
            materials = result
        )
    }
}
