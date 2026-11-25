package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CharacterBuildValidatorTest {
    private fun validCharacter(
        weapon: DataValue<WeaponSnapshot> = DataValue.Available(
            WeaponSnapshot(1, "Weapon", null, 1, 5, 90, 6, null, null, 1,
                StatValueSnapshot(2000, "ATK", "100", "20", "120", null, null), null),
            null
        ),
        artifacts: DataValue<List<ArtifactSnapshot>> = DataValue.Available(emptyList(), null)
    ) = CharacterSnapshot(
        id = 10000002, name = "Test", level = 90, rarity = 5, element = "Wind",
        friendshipLevel = 10, activeConstellationCount = 6, weapon = weapon,
        artifacts = artifacts
    )

    private fun issueCodes(character: CharacterSnapshot): Set<String> =
        CharacterBuildValidator.validate(listOf(character)).characters.single().issues.map { it.code }.toSet()

    @Test fun validCharacterWeaponAndEmptyArtifactCollectionHaveNoIssues() {
        val report = CharacterBuildValidator.validate(listOf(validCharacter()))
        assertEquals(0, report.totalErrors)
        assertEquals(0, report.totalWarnings)
        assertEquals(1, report.validCharacterCount)
    }

    @Test fun reportsCharacterAndWeaponValuesOutsideSupportedRanges() {
        val weapon = WeaponSnapshot(1, "Weapon", null, 1, 6, 91, 7, null, null, 0, null, null)
        val character = validCharacter(weapon = DataValue.Available(weapon, null)).copy(
            level = 0, rarity = 6, friendshipLevel = 11, activeConstellationCount = 7
        )
        val codes = issueCodes(character)
        assertTrue(codes.contains("character.level.out_of_range"))
        assertTrue(codes.contains("character.rarity.out_of_range"))
        assertTrue(codes.contains("character.friendship.out_of_range"))
        assertTrue(codes.contains("character.constellations.out_of_range"))
        assertTrue(codes.contains("weapon.level.out_of_range"))
        assertTrue(codes.contains("weapon.rarity.out_of_range"))
        assertTrue(codes.contains("weapon.ascension.out_of_range"))
        assertTrue(codes.contains("weapon.refinement.out_of_range"))
    }

    @Test fun validatesArtifactLevelRaritySlotAndMainSubstatRelationships() {
        val artifact = ArtifactSnapshot(
            id = 9, name = "Artifact", icon = null, slot = 8, slotName = null,
            rarity = 6, level = 21, set = null,
            mainProperty = StatValueSnapshot(1, "HP", null, null, null, "46.6%", null),
            subProperties = listOf(
                StatValueSnapshot(2, "ATK", null, null, null, "5.8", 2),
                StatValueSnapshot(2, "ATK duplicate", null, null, null, "3.5", 1),
                StatValueSnapshot(1, "HP collision", null, null, null, "100", 1)
            )
        )
        val codes = issueCodes(validCharacter(artifacts = DataValue.Available(listOf(artifact), null)))
        assertTrue(codes.contains("artifact.level.out_of_range"))
        assertTrue(codes.contains("artifact.rarity.out_of_range"))
        assertTrue(codes.contains("artifact.slot.out_of_range"))
        assertTrue(codes.contains("artifact.substat.duplicate"))
        assertTrue(codes.contains("artifact.mainstat.substat.conflict"))
    }

    @Test fun detectsMalformedStatValuesAndNegativeRollCounts() {
        val weapon = WeaponSnapshot(
            1, "Weapon", null, 1, 5, 90, 6, null, null, 1,
            StatValueSnapshot(null, "ATK", "not-a-number", null, null, null, null),
            null
        )
        val codes = issueCodes(validCharacter(weapon = DataValue.Available(weapon, null)))
        assertTrue(codes.contains("stat.property_type.missing"))
        assertTrue(codes.contains("stat.value.invalid"))
    }

    @Test fun missingEquipmentIsWarningAndNotAValidEmptyOrFabricatedRecord() {
        val result = CharacterBuildValidator.validate(listOf(validCharacter(
            weapon = DataValue.Missing,
            artifacts = DataValue.Missing
        ))).characters.single()
        assertEquals(0, result.errorCount)
        assertTrue(result.warningCount >= 2)
        assertTrue(result.issues.any { it.code == "weapon.missing" })
        assertTrue(result.issues.any { it.code == "artifacts.missing" })
    }

    @Test fun malformedEquipmentStateIsAnError() {
        val codes = issueCodes(validCharacter(
            weapon = DataValue.Failed("bad weapon"),
            artifacts = DataValue.Failed("bad artifacts")
        ))
        assertTrue(codes.contains("weapon.failed"))
        assertTrue(codes.contains("artifacts.failed"))
    }

    @Test fun reportsDuplicateAndMalformedArtifactEntriesFromIntegrityDiagnostics() {
        val integrity = EquipmentIntegritySnapshot(
            artifactSourceEntryCount = 3,
            mappedArtifactCount = 2,
            invalidArtifactEntryIndexes = listOf(2),
            duplicateArtifactIds = listOf(9),
            duplicateArtifactSlots = listOf(1),
            invalidArtifactSlots = listOf(8),
            invalidSubPropertyEntryIndexes = listOf(0)
        )
        val character = validCharacter(
            artifacts = DataValue.Available(emptyList(), null)
        ).copy(equipmentIntegrity = integrity)
        val codes = issueCodes(character)
        assertTrue(codes.contains("artifact.entry.invalid"))
        assertTrue(codes.contains("artifact.id.duplicate"))
        assertTrue(codes.contains("artifact.slot.duplicate"))
        assertTrue(codes.contains("artifact.slot.invalid"))
        assertTrue(codes.contains("artifact.substat.invalid"))
    }

    @Test fun percentageStringsAreAcceptedAsNumericValues() {
        val stat = StatValueSnapshot(1, "HP", null, null, null, "+46.6%", 0)
        val artifact = ArtifactSnapshot(1, "Artifact", null, 1, "Flower", 5, 20, null,
            stat, emptyList())
        val report = CharacterBuildValidator.validate(listOf(validCharacter(
            artifacts = DataValue.Available(listOf(artifact), null)
        )))
        assertEquals(0, report.totalErrors)
    }
}
