package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GenshinDbWeaponAscensionCandidateMapperTest {
    private val retrievedAt = Instant.parse("2026-11-27T00:00:00Z")

    private fun segment(from: Int, to: Int, id: String = "202", name: String = "Mora") =
        GenshinDbUpgradeCostCandidate(
            candidateId = "weapon:$from-$to",
            subjectType = UpgradeCostSubjectType.WEAPON,
            subjectId = "11401",
            track = UpgradeCostTrack.WEAPON_ASCENSION,
            fromLevel = from,
            toLevel = to,
            talentSlot = null,
            gameVersion = "7.2",
            costs = listOf(GenshinDbParsedCostItem(id, name, 5L)),
            sourceReference = KnowledgeSourceReference(
                "genshin-db-7.2",
                "https://github.com/theBowja/genshin-db/blob/main/src/data/English/weapons/favoniussword.json",
                "Genshin-DB"
            ),
            retrievedAt = retrievedAt
        )

    @Test fun mapsAllSixAscensionStagesAndRetainsMaterialNamesAndProvenance() {
        val result = GenshinDbWeaponAscensionCandidateMapper.map(
            weaponId = 11401,
            rarity = 4,
            gameVersion = "7.2",
            candidates = listOf(
                segment(20, 40),
                segment(40, 50, "114001", "Tile of Decarabian's Tower"),
                segment(50, 60),
                segment(60, 70),
                segment(70, 80),
                segment(80, 90)
            )
        )
        val candidate = result.candidate
        assertNotNull(candidate)
        assertTrue(result.issues.isEmpty())
        assertEquals((1..6).toList(), candidate!!.phaseCosts.map { it.ascensionLevel })
        assertEquals("Tile of Decarabian's Tower", candidate.phaseCosts[1].costs.single().itemName)
        assertEquals(KnowledgeReviewState.CANDIDATE, candidate.reviewState)
        assertEquals(KnowledgePermissionState.PENDING_REVIEW, candidate.permissionState)
        assertTrue(candidate.sourceLicense.contains("pending review"))
        assertEquals("Genshin-DB", candidate.sourceLabel)
    }

    @Test fun rejectsMixedWeaponIdentityTrackOrGameVersion() {
        val invalid = segment(20, 40).copy(subjectId = "11402")
        val result = GenshinDbWeaponAscensionCandidateMapper.map(
            11401, 4, "7.2", listOf(invalid)
        )
        assertNull(result.candidate)
        assertFalse(result.issues.isEmpty())
    }

    @Test fun rejectsConflictingSourceProvenance() {
        val changed = segment(40, 50).copy(
            sourceReference = KnowledgeSourceReference(
                "other-source",
                "https://example.com/weapon.json",
                "Other"
            )
        )
        val result = GenshinDbWeaponAscensionCandidateMapper.map(
            11401, 4, "7.2", listOf(segment(20, 40), changed)
        )
        assertNull(result.candidate)
        assertTrue(result.issues.single().contains("conflicting source"))
    }

    @Test fun rejectsUnsupportedAscensionRanges() {
        val result = GenshinDbWeaponAscensionCandidateMapper.map(
            11401, 4, "7.2", listOf(segment(20, 50))
        )
        assertNull(result.candidate)
        assertTrue(result.issues.single().contains("unsupported"))
    }

    @Test fun rejectsUnsupportedRarityAndMissingCandidates() {
        assertNull(
            GenshinDbWeaponAscensionCandidateMapper.map(11401, 3, "7.2", listOf(segment(20, 40))).candidate
        )
        assertNull(
            GenshinDbWeaponAscensionCandidateMapper.map(11401, 4, "7.2", emptyList()).candidate
        )
    }
}
