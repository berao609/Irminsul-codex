package com.irminsul.codex.data.catalogue

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class BundledCharacterProgressionCandidateRepositoryTest {
    private fun stageCosts(keyPrefix: String, count: Int): JSONObject {
        val costs = JSONObject()
        for (index in 1..count) {
            costs.put(
                "$keyPrefix$index",
                JSONArray().put(
                    JSONObject().put("id", 202).put("name", "Mora").put("count", 100 * index)
                )
            )
        }
        return costs
    }

    private fun record(
        slug: String = "amber",
        id: Int = 10000021,
        includeTalents: Boolean = true
    ): JSONObject {
        val character = JSONObject()
            .put("id", id)
            .put("name", "Amber")
            .put("costs", stageCosts("ascend", 6))
        val record = JSONObject()
            .put("slug", slug)
            .put("character", character)
        if (includeTalents) {
            record.put(
                "talents",
                JSONObject().put("costs", stageCosts("lvl", 9))
            )
        } else {
            record.put("talents", JSONObject.NULL)
        }
        return record
    }

    private fun chunk(vararg records: JSONObject) = JSONObject()
        .put("schemaVersion", 1)
        .put("gameVersion", BundledCharacterProgressionCandidateRepository.GAME_VERSION)
        .put("sourceCommit", BundledCharacterProgressionCandidateRepository.SOURCE_COMMIT)
        .put("characters", JSONArray().apply { records.forEach(::put) })

    @Test fun importsAscensionAndAllThreeIndependentTalentSlots() {
        val result = BundledCharacterProgressionCandidateRepository.parseCandidates(
            listOf(chunk(record())),
            "10000021"
        )!!

        assertEquals(GenshinDbProgressionImportStatus.PARTIAL, result.status)
        assertEquals("10000021", result.subjectId)
        assertEquals("Amber", result.subjectName)
        assertEquals(33, result.candidates.size)
        assertEquals(
            setOf("normal", "skill", "burst"),
            result.candidates.filter { it.track == com.irminsul.codex.domain.model.UpgradeCostTrack.TALENT_LEVEL }
                .mapNotNull { it.talentSlot }.toSet()
        )
        assertTrue(result.candidates.filter {
            it.track == com.irminsul.codex.domain.model.UpgradeCostTrack.TALENT_LEVEL
        }.all { it.fromLevel in 1..9 && it.toLevel == it.fromLevel + 1 })
        assertTrue(result.candidates.all {
            it.gameVersion == "7.2" &&
                it.sourceReference.canonicalUrl.contains(
                    "/blob/${BundledCharacterProgressionCandidateRepository.SOURCE_COMMIT}/"
                )
        })
        assertTrue(result.missingComponents.contains("character_level_experience"))
    }

    @Test fun missingTravelerTalentDataRemainsExplicitlyIncomplete() {
        val result = BundledCharacterProgressionCandidateRepository.parseCandidates(
            listOf(chunk(record(slug = "aether", id = 10000005, includeTalents = false))),
            "10000005"
        )!!

        assertEquals(GenshinDbProgressionImportStatus.PARTIAL, result.status)
        assertTrue(result.candidates.none {
            it.track == com.irminsul.codex.domain.model.UpgradeCostTrack.TALENT_LEVEL
        })
        assertTrue(result.missingComponents.contains("talent_costs"))
    }

    @Test fun unknownCharacterReturnsNull() {
        assertNull(
            BundledCharacterProgressionCandidateRepository.parseCandidates(
                listOf(chunk(record())),
                "99999999"
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDatasetFromAnotherGameVersion() {
        val invalid = chunk(record()).put("gameVersion", "7.1")
        BundledCharacterProgressionCandidateRepository.parseCandidates(listOf(invalid), "10000021")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDatasetFromAnotherSourceCommit() {
        val invalid = chunk(record()).put("sourceCommit", "mutable-main")
        BundledCharacterProgressionCandidateRepository.parseCandidates(listOf(invalid), "10000021")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateCharacterIdsAcrossChunks() {
        BundledCharacterProgressionCandidateRepository.parseCandidates(
            listOf(chunk(record()), chunk(record(slug = "amber-copy"))),
            "10000021"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsMalformedCharacterRecords() {
        BundledCharacterProgressionCandidateRepository.parseCandidates(
            listOf(chunk(JSONObject().put("slug", "broken"))),
            "10000021"
        )
    }
}
