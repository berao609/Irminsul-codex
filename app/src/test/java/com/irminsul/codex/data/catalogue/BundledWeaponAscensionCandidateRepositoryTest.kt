package com.irminsul.codex.data.catalogue

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BundledWeaponAscensionCandidateRepositoryTest {
    private fun weapon(id: Int, rarity: Int, mora: Int = 100): JSONObject {
        val costs = JSONObject()
        for (stage in 1..6) {
            costs.put(
                "ascend$stage",
                JSONArray().put(
                    JSONObject()
                        .put("id", 202)
                        .put("name", "Mora")
                        .put("count", mora + stage)
                )
            )
        }
        return JSONObject()
            .put("id", id)
            .put("name", "Test Weapon $id")
            .put("rarity", rarity)
            .put("costs", costs)
    }

    private fun root(vararg weapons: Pair<String, JSONObject>) = JSONObject()
        .put(
            "data",
            JSONObject().put(
                "English",
                JSONObject().put(
                    "weapons",
                    JSONObject().apply { weapons.forEach { (slug, weapon) -> put(slug, weapon) } }
                )
            )
        )

    @Test fun importsOnlyMatchingFourAndFiveStarWeaponRecords() {
        val payload = root(
            "fourstar" to weapon(1001, 4),
            "threestar" to weapon(1002, 3),
            "fivestar" to weapon(1003, 5)
        )

        val fourStar = BundledWeaponAscensionCandidateRepository.parseCandidates(payload, "1001")
        val fiveStar = BundledWeaponAscensionCandidateRepository.parseCandidates(payload, "1003")
        val threeStar = BundledWeaponAscensionCandidateRepository.parseCandidates(payload, "1002")

        assertEquals(6, fourStar.size)
        assertEquals(6, fiveStar.size)
        assertTrue(threeStar.isEmpty())
        assertTrue(fourStar.all { it.subjectId == "1001" && it.gameVersion == "7.2" })
        assertTrue(fiveStar.all { it.subjectId == "1003" && it.gameVersion == "7.2" })
    }

    @Test fun sourceReferencesUseTheImmutableUpstreamCommit() {
        val candidates = BundledWeaponAscensionCandidateRepository.parseCandidates(
            root("amosbow" to weapon(15502, 5)),
            "15502"
        )

        assertEquals(6, candidates.size)
        assertTrue(candidates.all {
            it.sourceReference.canonicalUrl.contains(
                "/blob/8e5badd60b5d894284ccc927a56c9683529d2592/"
            )
        })
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsConflictingDuplicateWeaponRecords() {
        BundledWeaponAscensionCandidateRepository.parseCandidates(
            root(
                "weapon-a" to weapon(1001, 4, mora = 100),
                "weapon-b" to weapon(1001, 4, mora = 200)
            ),
            "1001"
        )
    }

    @Test fun unknownWeaponReturnsNoCandidateRecords() {
        val candidates = BundledWeaponAscensionCandidateRepository.parseCandidates(
            root("fourstar" to weapon(1001, 4)),
            "9999"
        )
        assertTrue(candidates.isEmpty())
    }
}
