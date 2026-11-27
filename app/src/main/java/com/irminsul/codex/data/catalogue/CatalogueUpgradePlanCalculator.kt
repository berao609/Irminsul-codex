package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.ManualMaterialInventory
import com.irminsul.codex.domain.analysis.UpgradePlanCalculationRequest
import com.irminsul.codex.domain.analysis.UpgradePlanCalculationResult
import com.irminsul.codex.domain.analysis.UpgradePlanCalculator
import com.irminsul.codex.domain.model.KnowledgeSourceRegistryEntry
import com.irminsul.codex.domain.model.UpgradeCostRecord
import java.time.Instant

data class CatalogueUpgradePlanCalculation(
    val calculation: UpgradePlanCalculationResult,
    val materials: List<MaterialRequirementDisplay>
)

/**
 * Calculates every requested character/weapon track together, then resolves
 * the resulting material IDs through reviewed catalogue metadata.
 *
 * Unknown IDs remain explicit and incomplete calculations never expose partial
 * material totals as if they were a complete plan.
 */
object CatalogueUpgradePlanCalculator {
    fun calculate(
        request: UpgradePlanCalculationRequest,
        records: List<UpgradeCostRecord>,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>,
        evaluatedAt: Instant,
        catalogue: MaterialCatalogue,
        inventory: ManualMaterialInventory? = null
    ): CatalogueUpgradePlanCalculation {
        val calculation = UpgradePlanCalculator.calculate(
            request = request,
            records = records,
            sourceRegistry = sourceRegistry,
            evaluatedAt = evaluatedAt,
            inventory = inventory
        )
        return CatalogueUpgradePlanCalculation(
            calculation = calculation,
            materials = catalogue.displayAll(calculation.requirements)
        )
    }
}
