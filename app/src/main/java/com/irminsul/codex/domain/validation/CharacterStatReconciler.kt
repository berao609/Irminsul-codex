package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*
import java.math.BigDecimal

/**
 * Reconciles source-reported stat triplets without changing their original string values.
 *
 * The tolerance is the sum of the half-unit rounding intervals for base, additive,
 * and final values. Each source string is treated at its reported decimal precision.
 * This allows independently rounded displayed inputs to reconcile without hiding
 * discrepancies larger than the combined precision can explain.
 */
object CharacterStatReconciler {
    fun reconcile(characters: List<CharacterSnapshot>): CharacterStatReconciliationReport =
        CharacterStatReconciliationReport(characters.map(::reconcileCharacter))

    private fun reconcileCharacter(character: CharacterSnapshot): CharacterStatReconciliation {
        val results = mutableListOf<StatReconciliationResult>()
        appendCollection(character, character.selectedProperties, "selectedProperties", results)
        appendCollection(character, character.baseProperties, "baseProperties", results)
        appendCollection(character, character.extraProperties, "extraProperties", results)
        appendCollection(character, character.elementProperties, "elementProperties", results)

        when (val weapon = character.weapon) {
            is DataValue.Available -> appendWeapon(character, weapon.value, results)
            is DataValue.Stale -> appendWeapon(character, weapon.value, results)
            else -> Unit
        }
        return CharacterStatReconciliation(character.id, results)
    }

    private fun appendWeapon(
        character: CharacterSnapshot,
        weapon: WeaponSnapshot,
        results: MutableList<StatReconciliationResult>
    ) {
        weapon.mainProperty?.let {
            results += reconcileStat(character.id, "weapon.mainProperty", it)
        }
        weapon.subProperty?.let {
            results += reconcileStat(character.id, "weapon.subProperty", it)
        }
    }

    private fun appendCollection(
        character: CharacterSnapshot,
        stats: List<StatValueSnapshot>,
        field: String,
        results: MutableList<StatReconciliationResult>
    ) {
        stats.forEachIndexed { index, stat ->
            results += reconcileStat(character.id, "$field[$index]", stat)
        }
    }

    private fun reconcileStat(
        characterId: Int,
        path: String,
        stat: StatValueSnapshot
    ): StatReconciliationResult {
        fun notChecked(reason: String) = StatReconciliationResult(
            characterId = characterId,
            fieldPath = path,
            propertyType = stat.propertyType,
            statName = stat.statName,
            base = stat.base,
            add = stat.add,
            final = stat.final,
            expectedFinal = null,
            difference = null,
            roundingTolerance = null,
            status = StatReconciliationStatus.NOT_CHECKED,
            reason = reason
        )

        val baseRaw = stat.base ?: return notChecked("Base value is missing.")
        val addRaw = stat.add ?: return notChecked("Additive value is missing.")
        val finalRaw = stat.final ?: return notChecked("Final value is missing.")
        val base = parseValue(baseRaw) ?: return notChecked("Base value is not a supported decimal value.")
        val add = parseValue(addRaw) ?: return notChecked("Additive value is not a supported decimal value.")
        val final = parseValue(finalRaw) ?: return notChecked("Final value is not a supported decimal value.")
        if (base.isPercent != add.isPercent || base.isPercent != final.isPercent) {
            return notChecked("Base, additive, and final values use inconsistent units.")
        }

        val expected = base.amount.add(add.amount)
        val difference = final.amount.subtract(expected)
        val tolerance = roundingHalfUnit(base.amount)
            .add(roundingHalfUnit(add.amount))
            .add(roundingHalfUnit(final.amount))
        val status = when {
            difference.compareTo(BigDecimal.ZERO) == 0 -> StatReconciliationStatus.MATCH
            difference.abs().compareTo(tolerance) <= 0 -> StatReconciliationStatus.WITHIN_ROUNDING_TOLERANCE
            else -> StatReconciliationStatus.MISMATCH
        }

        return StatReconciliationResult(
            characterId = characterId,
            fieldPath = path,
            propertyType = stat.propertyType,
            statName = stat.statName,
            base = baseRaw,
            add = addRaw,
            final = finalRaw,
            expectedFinal = canonical(expected),
            difference = canonical(difference),
            roundingTolerance = canonical(tolerance),
            status = status
        )
    }

    private fun roundingHalfUnit(value: BigDecimal): BigDecimal =
        BigDecimal("0.5").movePointLeft(value.scale().coerceAtLeast(0))

    private data class ParsedValue(val amount: BigDecimal, val isPercent: Boolean)

    private fun parseValue(raw: String): ParsedValue? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val isPercent = trimmed.endsWith("%")
        val numeric = if (isPercent) trimmed.dropLast(1).trim() else trimmed
        if (numeric.isEmpty()) return null
        return try {
            ParsedValue(BigDecimal(numeric), isPercent)
        } catch (_: NumberFormatException) {
            null
        }
    }

    private fun canonical(value: BigDecimal): String =
        value.stripTrailingZeros().toPlainString().ifEmpty { "0" }
}
