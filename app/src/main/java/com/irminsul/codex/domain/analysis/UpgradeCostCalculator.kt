package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.KnowledgeSourceRegistryEntry
import com.irminsul.codex.domain.model.UpgradeCostRecord
import com.irminsul.codex.domain.model.UpgradeCostRecordValidator
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import java.time.Instant

data class UpgradeCostCalculationRequest(
    val subjectType: UpgradeCostSubjectType,
    val subjectId: String,
    val track: UpgradeCostTrack,
    val fromLevel: Int,
    val toLevel: Int,
    val gameVersion: String,
    val talentSlot: String? = null
)

data class ManualMaterialInventoryEntry(
    val itemId: String,
    val quantity: Long
)

/** User-entered inventory. It is not account-retrieved or externally verified. */
data class ManualMaterialInventory(
    val entries: List<ManualMaterialInventoryEntry>,
    val updatedAt: Instant
)

enum class ManualInventoryIssueCode {
    EMPTY_ITEM_ID,
    NEGATIVE_QUANTITY,
    DUPLICATE_ITEM_ID
}

data class ManualInventoryIssue(
    val code: ManualInventoryIssueCode,
    val itemId: String
)

object ManualMaterialInventoryValidator {
    fun validate(inventory: ManualMaterialInventory): List<ManualInventoryIssue> {
        val issues = mutableListOf<ManualInventoryIssue>()
        inventory.entries.forEach { entry ->
            if (entry.itemId.isBlank()) {
                issues += ManualInventoryIssue(ManualInventoryIssueCode.EMPTY_ITEM_ID, entry.itemId)
            }
            if (entry.quantity < 0L) {
                issues += ManualInventoryIssue(ManualInventoryIssueCode.NEGATIVE_QUANTITY, entry.itemId)
            }
        }
        inventory.entries.groupBy { it.itemId }.filterValues { it.size > 1 }.keys.forEach { itemId ->
            issues += ManualInventoryIssue(ManualInventoryIssueCode.DUPLICATE_ITEM_ID, itemId)
        }
        return issues
    }
}

enum class UpgradeCostCalculationStatus {
    COMPLETE,
    INVALID_REQUEST,
    COST_DATA_INCOMPLETE,
    INVALID_COST_DATA,
    OVERLAPPING_COST_SEGMENTS,
    QUANTITY_OVERFLOW
}

enum class MaterialInventoryEntryState {
    NOT_ENTERED,
    ENTERED
}

data class CalculatedMaterialRequirement(
    val itemId: String,
    val requiredQuantity: Long,
    val inventoryState: MaterialInventoryEntryState,
    val ownedQuantity: Long? = null,
    val remainingQuantity: Long? = null
)

data class UpgradeCostCalculationResult(
    val status: UpgradeCostCalculationStatus,
    val request: UpgradeCostCalculationRequest,
    val requirements: List<CalculatedMaterialRequirement> = emptyList(),
    val usedRecordIds: List<String> = emptyList(),
    val missingFromLevel: Int? = null,
    val invalidRecordIds: List<String> = emptyList(),
    val inventoryIssues: List<ManualInventoryIssue> = emptyList(),
    val evaluatedAt: Instant
)

/**
 * Aggregates only complete, contiguous, verified cost segments for one exact
 * subject, track, talent slot, and game version. It never estimates a missing
 * segment or interprets it as zero.
 */
object UpgradeCostCalculator {
    fun calculate(
        request: UpgradeCostCalculationRequest,
        records: List<UpgradeCostRecord>,
        sourceRegistry: List<KnowledgeSourceRegistryEntry>,
        evaluatedAt: Instant,
        inventory: ManualMaterialInventory? = null
    ): UpgradeCostCalculationResult {
        val inventoryIssues = inventory?.let(ManualMaterialInventoryValidator::validate).orEmpty()
        fun result(
            status: UpgradeCostCalculationStatus,
            requirements: List<CalculatedMaterialRequirement> = emptyList(),
            used: List<String> = emptyList(),
            missing: Int? = null,
            invalid: List<String> = emptyList()
        ) = UpgradeCostCalculationResult(
            status = status,
            request = request,
            requirements = requirements,
            usedRecordIds = used,
            missingFromLevel = missing,
            invalidRecordIds = invalid,
            inventoryIssues = inventoryIssues,
            evaluatedAt = evaluatedAt
        )

        val expectedSubject = when (request.track) {
            UpgradeCostTrack.CHARACTER_LEVEL,
            UpgradeCostTrack.CHARACTER_ASCENSION,
            UpgradeCostTrack.TALENT_LEVEL -> UpgradeCostSubjectType.CHARACTER
            UpgradeCostTrack.WEAPON_LEVEL,
            UpgradeCostTrack.WEAPON_ASCENSION -> UpgradeCostSubjectType.WEAPON
        }
        if (request.subjectId.isBlank() || request.gameVersion.isBlank() ||
            request.fromLevel < 1 || request.toLevel <= request.fromLevel ||
            request.subjectType != expectedSubject ||
            (request.track == UpgradeCostTrack.TALENT_LEVEL && request.talentSlot.isNullOrBlank()) ||
            (request.track != UpgradeCostTrack.TALENT_LEVEL && request.talentSlot != null)
        ) {
            return result(UpgradeCostCalculationStatus.INVALID_REQUEST)
        }

        val matching = records.filter {
            it.subjectType == request.subjectType &&
                it.subjectId == request.subjectId &&
                it.track == request.track &&
                it.talentSlot == request.talentSlot &&
                it.gameVersion == request.gameVersion &&
                it.fromLevel < request.toLevel &&
                it.toLevel > request.fromLevel
        }.sortedWith(compareBy<UpgradeCostRecord> { it.fromLevel }.thenBy { it.toLevel })

        if (matching.isEmpty()) {
            return result(UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE, missing = request.fromLevel)
        }

        val invalid = matching.filter {
            !UpgradeCostRecordValidator.isApplicableAt(it, request.gameVersion, evaluatedAt, sourceRegistry)
        }.map { it.recordId }
        if (invalid.isNotEmpty()) {
            return result(UpgradeCostCalculationStatus.INVALID_COST_DATA, invalid = invalid)
        }

        val totals = linkedMapOf<String, Long>()
        val usedRecordIds = mutableListOf<String>()
        var cursor = request.fromLevel
        for (record in matching) {
            if (record.fromLevel != cursor || record.toLevel > request.toLevel) {
                val overlap = record.fromLevel < cursor
                return result(
                    if (overlap) UpgradeCostCalculationStatus.OVERLAPPING_COST_SEGMENTS
                    else UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE,
                    used = usedRecordIds,
                    missing = if (overlap) null else cursor
                )
            }
            try {
                record.costs.forEach { item ->
                    totals[item.itemId] = Math.addExact(totals[item.itemId] ?: 0L, item.quantity)
                }
            } catch (_: ArithmeticException) {
                return result(UpgradeCostCalculationStatus.QUANTITY_OVERFLOW, used = usedRecordIds)
            }
            usedRecordIds += record.recordId
            cursor = record.toLevel
            if (cursor == request.toLevel) break
        }

        if (cursor != request.toLevel) {
            return result(
                UpgradeCostCalculationStatus.COST_DATA_INCOMPLETE,
                used = usedRecordIds,
                missing = cursor
            )
        }

        val validInventory = inventory?.takeIf { inventoryIssues.isEmpty() }
            ?.entries?.associate { it.itemId to it.quantity }
        val requirements = totals.entries.map { (itemId, required) ->
            val owned = validInventory?.get(itemId)
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
            UpgradeCostCalculationStatus.COMPLETE,
            requirements = requirements,
            used = usedRecordIds
        )
    }
}
