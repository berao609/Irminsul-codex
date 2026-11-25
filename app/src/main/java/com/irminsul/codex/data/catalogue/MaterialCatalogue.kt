package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.ManualMaterialInventory
import com.irminsul.codex.domain.analysis.UpgradeCostCalculationRequest
import com.irminsul.codex.domain.analysis.UpgradeCostCalculationResult
import com.irminsul.codex.domain.analysis.UpgradeCostCalculator
import com.irminsul.codex.domain.analysis.CalculatedMaterialRequirement
import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.KnowledgeSourceRegistryEntry
import com.irminsul.codex.domain.model.UpgradeCostRecord
import com.irminsul.codex.domain.model.UpgradeCostRecordValidator
import java.net.URI
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
    val iconUrl: String? = null,
    val sourceReferences: List<KnowledgeSourceReference>,
    val retrievedAt: Instant,
    val reviewState: KnowledgeReviewState,
    val reviewedAt: Instant?,
    val reviewNotes: String?
)

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
class MaterialCatalogue(
    entries: Collection<MaterialCatalogueEntry>,
    sourceRegistry: List<KnowledgeSourceRegistryEntry>
) {
    private val entriesById: Map<String, MaterialCatalogueEntry>

    init {
        require(entries.map { it.itemId }.distinct().size == entries.size) {
            "Material catalogue item IDs must be unique."
        }
        entries.forEach { entry ->
            require(validateEntry(entry, sourceRegistry).isEmpty()) {
                "Material catalogue entry ${entry.itemId} has unapproved or incomplete provenance."
            }
        }
        entriesById = entries.associateBy { it.itemId }
    }

    private fun validateEntry(
        entry: MaterialCatalogueEntry,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>
    ): List<String> {
        val issues = mutableListOf<String>()
        if (entry.itemId.isBlank() || entry.displayName.isBlank()) issues += "Missing material identity."
        if (entry.rarity != null && entry.rarity !in 1..5) issues += "Invalid rarity."
        if (entry.iconUrl != null && !isHttps(entry.iconUrl)) issues += "Invalid icon URL."
        if (entry.sourceReferences.isEmpty()) issues += "Missing source references."
        if (entry.sourceReferences.map { it.sourceId }.distinct().size != entry.sourceReferences.size) {
            issues += "Duplicate source references."
        }
        val sources = sourceRegistry.associateBy { it.sourceId }
        entry.sourceReferences.forEach { reference ->
            val source = sources[reference.sourceId]
            if (source == null) {
                issues += "Unknown source: ${reference.sourceId}."
            } else {
                if (source.permissionState != KnowledgePermissionState.APPROVED ||
                    UpgradeCostRecordValidator.REQUIRED_SOURCE_ACTION !in source.allowedActions
                ) {
                    issues += "Source ${reference.sourceId} is not approved for material-cost curation."
                }
                val referenceHost = runCatching { URI(reference.canonicalUrl).host }.getOrNull()
                val sourceHost = runCatching { URI(source.canonicalSourceUrl).host }.getOrNull()
                if (referenceHost == null || sourceHost == null ||
                    !referenceHost.equals(sourceHost, ignoreCase = true) ||
                    !isHttps(source.canonicalSourceUrl)
                ) {
                    issues += "Source host mismatch or non-HTTPS registry URL: ${reference.sourceId}."
                }
            }
            if (!isHttps(reference.canonicalUrl)) issues += "Source reference must use HTTPS."
        }
        if (entry.reviewState != KnowledgeReviewState.VERIFIED ||
            entry.reviewedAt == null || entry.reviewNotes.isNullOrBlank()
        ) {
            issues += "Material metadata requires explicit human verification."
        }
        if (entry.reviewedAt != null && entry.reviewedAt.isAfter(entry.retrievedAt)) {
            issues += "Review timestamp is later than retrieval."
        }
        return issues
    }

    private fun isHttps(value: String): Boolean = runCatching {
        val uri = URI(value)
        uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }.getOrDefault(false)

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
