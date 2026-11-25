package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.ManualMaterialInventory
import com.irminsul.codex.domain.analysis.UpgradeCostCalculationRequest
import com.irminsul.codex.domain.analysis.UpgradeCostCalculationResult
import com.irminsul.codex.domain.analysis.UpgradeCostCalculator
import com.irminsul.codex.domain.analysis.CalculatedMaterialRequirement
import com.irminsul.codex.domain.model.KnowledgeSourceRegistryEntry
import com.irminsul.codex.domain.model.UpgradeCostRecord
import java.time.Instant

/**
 * Presentation metadata for a material. Entries must come from a source whose
 * reuse rights and attribution have been reviewed. No game data is bundled by
 * this type or inferred from an upgrade-cost candidate.
 */
enum class MaterialCategory {
    CURRENCY,
    CHARACTER_EXP,
    WEAPON_EXP,
    CHARACTER_ASCENSION,
    WEAPON_ASCENSION,
    TALENT,
    WEEKLY_BOSS,
    LOCAL_SPECIALTY,
    ENEMY_DROP,
    CRAFTING,
    UNKNOWN
}

data class MaterialCatalogueEntry(
    val itemId: String,
    val displayName: String,
    val category: MaterialCategory,
    val rarity: Int? = null,
    val iconUrl: String? = null
) {
    init {
        require(itemId.isNotBlank())
        require(displayName.isNotBlank())
        require(rarity == null || rarity in 1..5)
        require(iconUrl == null || iconUrl.startsWith("https://"))
    }
}

data class MaterialRequirementDisplay(
    val itemId: String,
    val displayName: String,
    val category: MaterialCategory,
    val rarity: Int?,
    val iconUrl: String?,
    val requiredQuantity: Long,
    val ownedQuantity: Long?,
    val remainingQuantity: Long?
)

/**
 * Immutable metadata lookup. Unknown IDs remain visible as explicit unknowns;
 * names from untrusted cost feeds are never silently promoted to catalogue data.
 */
class MaterialCatalogue(entries: Collection<MaterialCatalogueEntry>) {
    private val entriesById: Map<String, MaterialCatalogueEntry>

    init {
        require(entries.map { it.itemId }.distinct().size == entries.size) {
            "Material catalogue item IDs must be unique."
        }
        entriesById = entries.associateBy { it.itemId }
    }

    fun find(itemId: String): MaterialCatalogueEntry? = entriesById[itemId]

    fun display(requirement: CalculatedMaterialRequirement): MaterialRequirementDisplay {
        val entry = find(requirement.itemId)
        return MaterialRequirementDisplay(
            itemId = requirement.itemId,
            displayName = entry?.displayName ?: "Unknown material (${requirement.itemId})",
            category = entry?.category ?: MaterialCategory.UNKNOWN,
            rarity = entry?.rarity,
            iconUrl = entry?.iconUrl,
            requiredQuantity = requirement.requiredQuantity,
            ownedQuantity = requirement.ownedQuantity,
            remainingQuantity = requirement.remainingQuantity
        )
    }

    fun displayAll(requirements: List<CalculatedMaterialRequirement>): List<MaterialRequirementDisplay> =
        requirements.map(::display)
}

data class CatalogueUpgradeCostCalculation(
    val calculation: UpgradeCostCalculationResult,
    val materials: List<MaterialRequirementDisplay>
)

/**
 * Joins trusted calculation output to reviewed display metadata. The existing
 * UpgradeCostCalculator remains the sole arithmetic and trust-validation path.
 */
object CatalogueUpgradeCostCalculator {
    fun calculate(
        request: UpgradeCostCalculationRequest,
        records: List<UpgradeCostRecord>,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>,
        evaluatedAt: Instant,
        catalogue: MaterialCatalogue,
        inventory: ManualMaterialInventory? = null
    ): CatalogueUpgradeCostCalculation {
        val calculation = UpgradeCostCalculator.calculate(
            request = request,
            records = records,
            sourceRegistry = sourceRegistry,
            evaluatedAt = evaluatedAt,
            inventory = inventory
        )
        return CatalogueUpgradeCostCalculation(
            calculation = calculation,
            materials = catalogue.displayAll(calculation.requirements)
        )
    }
}
