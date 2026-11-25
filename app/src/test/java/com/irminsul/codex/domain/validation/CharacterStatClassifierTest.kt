package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CharacterStatClassifierTest {
    private fun stat(type: Int, name: String? = null) =
        StatValueSnapshot(type, name, "1", "2", "3", null, null)

    @Test fun classifiesCharacterAndEquippedArtifactStats() {
        val artifact = ArtifactSnapshot(10, "Artifact", null, 1, "Flower", 5, 20, null,
            stat(2), listOf(stat(23)))
        val character = CharacterSnapshot(
            id = 10000002, name = "Test", level = 90, rarity = 5, element = "Pyro",
            friendshipLevel = 10, activeConstellationCount = 0,
            artifacts = DataValue.Available(listOf(artifact), null),
            selectedProperties = listOf(stat(3)),
            baseProperties = listOf(stat(1)),
            extraProperties = listOf(stat(20)),
            elementProperties = listOf(stat(40))
        )
        val report = CharacterStatClassifier.classify(listOf(character))
        assertEquals(5, report.classifiedCount)
        assertEquals(0, report.unknownCount)
        assertEquals(0, report.conflictCount)
        assertEquals("artifacts[0].mainProperty", report.characters.single().results[4].fieldPath)
    }

    @Test fun unknownPropertiesDoNotBecomeGuessedCategories() {
        val character = CharacterSnapshot(
            1, null, null, null, null, null, null,
            selectedProperties = listOf(stat(987654, "FIGHT_PROP_FUTURE_STAT"))
        )
        val report = CharacterStatClassifier.classify(listOf(character))
        assertEquals(1, report.unknownCount)
        assertEquals(0, report.classifiedCount)
    }

    @Test fun staleEquipmentCanBeClassifiedButFailedEquipmentIsNotInvented() {
        val weapon = WeaponSnapshot(1, null, null, null, null, null, null, null, null, null,
            stat(4), null)
        val stale = CharacterSnapshot(
            1, null, null, null, null, null, null,
            weapon = DataValue.Stale(weapon, java.time.Instant.parse("2026-11-25T00:00:00Z"))
        )
        val failed = stale.copy(weapon = DataValue.Failed("bad"))
        assertEquals(1, CharacterStatClassifier.classify(listOf(stale)).classifiedCount)
        assertEquals(0, CharacterStatClassifier.classify(listOf(failed)).classifiedCount)
    }
}
