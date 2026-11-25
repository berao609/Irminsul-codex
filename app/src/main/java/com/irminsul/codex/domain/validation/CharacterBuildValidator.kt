package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*
import java.math.BigDecimal

/**
 * Deterministic structural and range validation for the supplied character build data.
 * This reports issues only; it never repairs or changes source values.
 */
object CharacterBuildValidator {
    fun validate(characters: List<CharacterSnapshot>): CharacterBuildValidationReport =
        CharacterBuildValidationReport(characters.map(::validateCharacter))

    private fun validateCharacter(character: CharacterSnapshot): CharacterBuildValidation {
        val issues = mutableListOf<BuildValidationIssue>()
        val root = "character[${character.id}]"

        checkRange(character.level, 1..90, "$root.level", "character.level", issues)
        checkRange(character.rarity, 1..5, "$root.rarity", "character.rarity", issues)
        checkRange(character.friendshipLevel, 0..10, "$root.friendshipLevel", "character.friendship", issues)
        checkRange(character.activeConstellationCount, 0..6, "$root.activeConstellationCount", "character.constellations", issues)

        when (val weapon = character.weapon) {
            DataValue.Missing -> warning(issues, "weapon.missing", "$root.weapon", "Weapon data is missing from this character record.")
            DataValue.Unavailable -> warning(issues, "weapon.unavailable", "$root.weapon", "Weapon data was unavailable.")
            is DataValue.Failed -> error(issues, "weapon.failed", "$root.weapon", weapon.reason)
            is DataValue.Available -> validateWeapon(weapon.value, "$root.weapon", issues)
            is DataValue.Stale -> {
                warning(issues, "weapon.stale", "$root.weapon", "Weapon data is stale.")
                validateWeapon(weapon.value, "$root.weapon", issues)
            }
        }

        when (val artifacts = character.artifacts) {
            DataValue.Missing -> warning(issues, "artifacts.missing", "$root.artifacts", "Artifact collection data is missing.")
            DataValue.Unavailable -> warning(issues, "artifacts.unavailable", "$root.artifacts", "Artifact collection data was unavailable.")
            is DataValue.Failed -> error(issues, "artifacts.failed", "$root.artifacts", artifacts.reason)
            is DataValue.Available -> validateArtifacts(artifacts.value, character.equipmentIntegrity, "$root.artifacts", issues)
            is DataValue.Stale -> {
                warning(issues, "artifacts.stale", "$root.artifacts", "Artifact collection data is stale.")
                validateArtifacts(artifacts.value, character.equipmentIntegrity, "$root.artifacts", issues)
            }
        }

        validateStatCollection(character.selectedProperties, "$root.selectedProperties", issues)
        validateStatCollection(character.baseProperties, "$root.baseProperties", issues)
        validateStatCollection(character.extraProperties, "$root.extraProperties", issues)
        validateStatCollection(character.elementProperties, "$root.elementProperties", issues)

        return CharacterBuildValidation(character.id, issues)
    }

    private fun validateWeapon(weapon: WeaponSnapshot, path: String, issues: MutableList<BuildValidationIssue>) {
        checkRange(weapon.level, 1..90, "$path.level", "weapon.level", issues)
        checkRange(weapon.rarity, 1..5, "$path.rarity", "weapon.rarity", issues)
        checkRange(weapon.ascensionLevel, 0..6, "$path.ascensionLevel", "weapon.ascension", issues)
        checkRange(weapon.refinementLevel, 1..5, "$path.refinementLevel", "weapon.refinement", issues)
        weapon.mainProperty?.let { validateStat(it, "$path.mainProperty", true, issues) }
        weapon.subProperty?.let { validateStat(it, "$path.subProperty", true, issues) }
    }

    private fun validateArtifacts(
        artifacts: List<ArtifactSnapshot>,
        integrity: EquipmentIntegritySnapshot,
        path: String,
        issues: MutableList<BuildValidationIssue>
    ) {
        integrity.invalidArtifactEntryIndexes.forEach {
            error(issues, "artifact.entry.invalid", "$path[$it]", "Artifact source entry could not be mapped because its shape or identifier was invalid.")
        }
        integrity.duplicateArtifactIds.forEach {
            error(issues, "artifact.id.duplicate", path, "Artifact identifier $it appears more than once.")
        }
        integrity.duplicateArtifactSlots.forEach {
            error(issues, "artifact.slot.duplicate", path, "More than one artifact uses slot $it.")
        }
        integrity.invalidArtifactSlots.forEach {
            error(issues, "artifact.slot.invalid", path, "Artifact slot value $it is outside the supported range 1 through 5.")
        }
        integrity.invalidSubPropertyEntryIndexes.forEach {
            error(issues, "artifact.substat.invalid", "$path[$it].subProperties", "One or more substat entries have an invalid shape or missing property type.")
        }

        artifacts.forEachIndexed { index, artifact ->
            val itemPath = "$path[$index]"
            checkRange(artifact.level, 0..20, "$itemPath.level", "artifact.level", issues)
            checkRange(artifact.rarity, 1..5, "$itemPath.rarity", "artifact.rarity", issues)
            if (artifact.slot == null) {
                error(issues, "artifact.slot.missing", "$itemPath.slot", "Artifact slot is missing.")
            } else {
                checkRange(artifact.slot, 1..5, "$itemPath.slot", "artifact.slot", issues)
            }
            val main = artifact.mainProperty
            if (main == null) {
                error(issues, "artifact.mainstat.missing", "$itemPath.mainProperty", "Artifact main stat is missing.")
            } else {
                validateStat(main, "$itemPath.mainProperty", true, issues)
            }
            if (artifact.subProperties.size > 4) {
                error(issues, "artifact.substat.count", "$itemPath.subProperties", "Artifact has more than four substats.")
            }
            val subTypes = artifact.subProperties.mapNotNull { it.propertyType }
            subTypes.groupingBy { it }.eachCount().filterValues { it > 1 }.keys.forEach {
                error(issues, "artifact.substat.duplicate", "$itemPath.subProperties", "Substat property type $it appears more than once.")
            }
            if (main?.propertyType != null && main.propertyType in subTypes) {
                error(issues, "artifact.mainstat.substat.conflict", "$itemPath", "The main stat property type is repeated among the substats.")
            }
            artifact.subProperties.forEachIndexed { statIndex, stat ->
                validateStat(stat, "$itemPath.subProperties[$statIndex]", true, issues)
            }
        }
    }

    private fun validateStatCollection(
        stats: List<StatValueSnapshot>,
        path: String,
        issues: MutableList<BuildValidationIssue>
    ) {
        stats.mapNotNull { it.propertyType }.groupingBy { it }.eachCount()
            .filterValues { it > 1 }.keys.forEach {
                error(issues, "stat.property.duplicate", path, "Property type $it appears more than once in this stat collection.")
            }
        stats.forEachIndexed { index, stat ->
            validateStat(stat, "$path[$index]", false, issues)
        }
    }

    private fun validateStat(
        stat: StatValueSnapshot,
        path: String,
        requireNumericValue: Boolean,
        issues: MutableList<BuildValidationIssue>
    ) {
        if (stat.propertyType == null) {
            error(issues, "stat.property_type.missing", "$path.propertyType", "Stat property type is missing or invalid.")
        }
        val values = listOf("base" to stat.base, "add" to stat.add, "final" to stat.final, "value" to stat.value)
        var foundNumeric = false
        values.forEach { (field, raw) ->
            if (raw != null) {
                if (parseDecimal(raw) == null) {
                    error(issues, "stat.value.invalid", "$path.$field", "Stat value '$raw' is not a valid numeric value.")
                } else {
                    foundNumeric = true
                }
            }
        }
        if (requireNumericValue && !foundNumeric) {
            error(issues, "stat.value.missing", path, "Stat has no usable numeric value.")
        }
        if (stat.rolls != null && stat.rolls < 0) {
            error(issues, "stat.rolls.negative", "$path.rolls", "Stat roll count cannot be negative.")
        }
    }

    private fun parseDecimal(raw: String): BigDecimal? {
        val normalized = raw.trim().removeSuffix("%").trim().removePrefix("+")
        if (normalized.isEmpty()) return null
        return try { BigDecimal(normalized) } catch (_: NumberFormatException) { null }
    }

    private fun checkRange(
        value: Int?,
        range: IntRange,
        path: String,
        code: String,
        issues: MutableList<BuildValidationIssue>
    ) {
        if (value == null) {
            warning(issues, "$code.missing", path, "Value is missing; it cannot be range-validated.")
        } else if (value !in range) {
            error(issues, "$code.out_of_range", path, "Value $value is outside the supported range ${range.first} through ${range.last}.")
        }
    }

    private fun warning(issues: MutableList<BuildValidationIssue>, code: String, path: String, message: String) {
        issues += BuildValidationIssue(code, BuildIssueSeverity.WARNING, path, message)
    }

    private fun error(issues: MutableList<BuildValidationIssue>, code: String, path: String, message: String) {
        issues += BuildValidationIssue(code, BuildIssueSeverity.ERROR, path, message)
    }
}
