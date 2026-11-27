package com.irminsul.codex.data.catalogue

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GenshinDbProgressionCandidateImporterTest {
    private val retrievedAt = Instant.parse("2026-11-27T00:00:00Z")
    private val sourceId = "genshin-db-7.2"

    private fun ascensionCosts(): JSONObject {
        val result = JSONObject()
        listOf("ascend1", "ascend2", "ascend3", "ascend4", "ascend5", "ascend6")
            .forEachIndexed { index, key ->
                result.put(key, JSONArray().put(
                    JSONObject().put("id", 202).put("name", "Mora").put("count", (index + 1) * 1000)
                ))
            }
        return result
    }

    private fun talentCosts(): JSONObject {
        val result = JSONObject()
        (2..10).forEach { level ->
            result.put("lvl$level", JSONArray().put(
                JSONObject().put("id", 202).put("name", "Mora").put("count", level * 1000)
            ))
        }
        return result
    }

    @Test fun importsCharacterAscensionAndAllThreeTalentTracksAsCandidates() {
        val character = JSONObject().put("id", 10000038).put("name", "Albedo").put("costs", ascensionCosts())
        val talents = JSONObject().put("id", 3801).put("name", "Albedo").put("costs", talentCosts())

        val result = GenshinDbProgressionCandidateImporter.importCharacter(
            character = character,
            talents = talents,
            gameVersion = "7.2",
            sourceId = sourceId,
            characterSourceUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/characters/albedo.json",
            talentSourceUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/talents/albedo.json",
            retrievedAt = retrievedAt
        )

        assertEquals(GenshinDbProgressionImportStatus.PARTIAL, result.status)
        assertEquals("10000038", result.subjectId)
        assertEquals("Albedo", result.subjectName)
        assertEquals(33, result.candidates.size)
        assertEquals(6, result.candidates.count { it.track == com.irminsul.codex.domain.model.UpgradeCostTrack.CHARACTER_ASCENSION })
        assertEquals(27, result.candidates.count { it.track == com.irminsul.codex.domain.model.UpgradeCostTrack.TALENT_LEVEL })
        listOf("normal", "skill", "burst").forEach { slot ->
            assertEquals(9, result.candidates.count { it.talentSlot == slot })
        }
        assertEquals(setOf("normal", "skill", "burst"), result.candidates.mapNotNull { it.talentSlot }.toSet())
        assertEquals(20, result.candidates.first().fromLevel)
        assertEquals(90, result.candidates.filter { it.track == com.irminsul.codex.domain.model.UpgradeCostTrack.CHARACTER_ASCENSION }.last().toLevel)
        assertEquals(listOf("character_level_experience"), result.missingComponents)
        assertTrue(result.invalidComponents.isEmpty())
    }

    @Test fun importsWeaponAscensionAndExplicitlyReportsMissingWeaponExperience() {
        val weapon = JSONObject().put("id", 15502).put("name", "Amos' Bow").put("costs", ascensionCosts())
        val result = GenshinDbProgressionCandidateImporter.importWeapon(
            weapon = weapon,
            gameVersion = "7.2",
            sourceId = sourceId,
            weaponSourceUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/weapons/amosbow.json",
            retrievedAt = retrievedAt
        )

        assertEquals(GenshinDbProgressionImportStatus.PARTIAL, result.status)
        assertEquals("15502", result.subjectId)
        assertEquals("Amos' Bow", result.subjectName)
        assertEquals(6, result.candidates.size)
        assertTrue(result.candidates.all { it.track == com.irminsul.codex.domain.model.UpgradeCostTrack.WEAPON_ASCENSION })
        assertEquals(listOf("weapon_level_experience"), result.missingComponents)
    }

    @Test fun missingTalentStagesDoNotEraseValidCharacterAscensionCandidates() {
        val incompleteTalents = JSONObject().put("costs", JSONObject().put("lvl2", JSONArray()))
        val result = GenshinDbProgressionCandidateImporter.importCharacter(
            character = JSONObject().put("id", 10000038).put("name", "Albedo").put("costs", ascensionCosts()),
            talents = incompleteTalents,
            gameVersion = "7.2",
            sourceId = sourceId,
            characterSourceUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/characters/albedo.json",
            talentSourceUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/talents/albedo.json",
            retrievedAt = retrievedAt
        )
        assertEquals(GenshinDbProgressionImportStatus.PARTIAL, result.status)
        assertEquals(6, result.candidates.size)
        assertTrue(result.missingComponents.any { it.startsWith("talent_costs_") })
    }

    @Test fun malformedEntityIdentityIsReportedWithoutCandidates() {
        val result = GenshinDbProgressionCandidateImporter.importWeapon(
            weapon = JSONObject().put("id", "not-numeric").put("name", "Broken weapon"),
            gameVersion = "7.2",
            sourceId = sourceId,
            weaponSourceUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/weapons/broken.json",
            retrievedAt = retrievedAt
        )
        assertEquals(GenshinDbProgressionImportStatus.INVALID_DATA, result.status)
        assertNull(result.subjectId)
        assertTrue(result.candidates.isEmpty())
        assertEquals(listOf("weapon_id"), result.invalidComponents)
    }
}
