package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.KnowledgeSourceRegistryEntry
import com.irminsul.codex.domain.model.UpgradeCostRecord
import java.time.Instant

data class UpgradePlanCalculationRequest(
    val tracks: List<UpgradeCostCalculationRequest>
)

enum class UpgradePlanCalculationStatus {
    COMPLETE,
    INVALID_REQUEST,
    COST_DATA_INCOMPLETE,
    INVALID_COST_DATA,
    OVERLAPPING_COST_SEGMENTS,
    QUANTITY_OVERFLOW
}

data class UpgradePlanCalculationResult(
    val status: UpgradePlanCalculationStatus,
    val trackResults: List<UpgradeCostCalculationResult>,
    val requirements: List<CalculatedMaterialRequirement> = emptyList(),
    val missingTracks: List<UpgradeCostCalculationRequest> = emptyList(),
    val evaluatedAt: Instant
)

/**
 * Calculates a complete character or weapon upgrade plan across independent
 * level, ascension, and talent tracks. Partial totals are deliberately withheld
 * when any requested track lacks verified contiguous cost data.
 */
object UpgradePlanCalculator {
    fun calculate(
        request: UpgradePlanCalculationRequest,
        records: List<UpgradeCostRecord>,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>,
        evaluatedAt: Instant,
        inventory: ManualMaterialInventory? = null
    ): UpgradePlanCalculationResult {
        fun result(
            status: UpgradePlanCalculationStatus,
            trackResults: List<UpgradeCostCalculationResult> = emptyList(),
            requirements: List<CalculatedMaterialRequirement> = emptyList(),
            missing: List<UpgradeCostCalculationRequest> = emptyList()
        ) = UpgradePlanCalculationResult(status, trackResults, requirements, missing, evaluatedAt)

        if (request.tracks.isEmpty()) return result(UpgradePlanCalculationStatus.INVALID_REQUEST)

        val duplicateTracks = request.tracks.groupBy {
            listOf(it.subjectType.name, it.subjectId, it.track.name, it.talentSlot.orEmpty(), it.gameVersion)
        }.values.any { it.size > 1 }
        if (duplicateTracks) return result(UpgradePlanCalculationStatus.INVALID_REQUEST)

        val calculated = request.tracks.map {
            UpgradeCostCalculator.calculate(it, records, sourceRegistry, evaluatedAt, inventory)
        }
        val failed = calculated.filter { it.status != UpgradeCostCalculationStatus.COMPLETE }
        if (failed.isNotEmpty()) {
            val status = when {
                failed.any { it.status == UpgradeCostCalculationStatus.INVALID_REQUEST } ->
                    UpgradePlanCalculationStatus.INVALID_REQUEST
                failed.any { it.status == UpgradeCostCalculationStatus.INVALID_COST_DATA } ->
                    UpgradePlanCalculationStatus.INVALID_COST_DATA
                failed.any { it.status == UpgradeCostCalculationStatus.OVERLAPPING_COST_SEGMENTS } ->
                    UpgradePlanCalculationStatus.OVERLAPPING_COST_SEGMENTS
                failed.any { it.status == UpgradeCostCalculationStatus.QUANTITY_OVERFLOW } ->
                    UpgradePlanCalculationStatus.QUANTITY_OVERFLOW
                else -> UpgradePlanCalculationStatus.COST_DATA_INCOMPLETE
            }
            return result(
                status = status,
                trackResults = calculated,
                missing = request.tracks.filterIndexed { index, _ ->
                    calculated[index].status == UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE
                }
            )
        }

        val totals = linkedMapOf<String, Long>()
        try {
            calculated.flatMap { it.requirements }.forEach { requirement ->
                totals[requirement.itemId] = Math.addExact(
                    totals[requirement.itemId] ?: 0L,
                    requirement.requiredQuantity
                )
            }
        } catch (_: ArithmeticException) {
            return result(UpgradePlanCalculationStatus.QUANTITY_OVERFLOW, calculated)
        }

        val inventoryIssues = inventory?.let(ManualMaterialInventoryValidator::validate).orEmpty()
        val ownedById = inventory?.takeIf { inventoryIssues.isEmpty() }
            ?.entries?.associate { it.itemId to it.quantity }.orEmpty()
        val requirements = totals.map { (itemId, required) ->
            val owned = ownedById[itemId]
            CalculatedMaterialRequirement(
                itemId = itemId,
                requiredQuantity = required,
                inventoryState = if (owned == null) MaterialInventoryEntryState.NOT_ENTERED
                    else MaterialInventoryEntryState.ENTERED,
                ownedQuantity = owned,
                remainingQuantity = owned?.let { (required - it).coerceAtLeast(0L) }
            )
        }
        return result(
            UpgradePlanCalculationStatus.COMPLETE,
            trackResults = calculated,
            requirements = requirements
        )
    }
}
