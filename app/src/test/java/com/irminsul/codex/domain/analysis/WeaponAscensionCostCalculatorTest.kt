package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import org.junit.Assert.*
import org.junit.Test

class WeaponAscensionCostCalculatorTest {
    private val favonius = WeaponAscensionCostCandidate(
        weaponId = 11401,
        rarity = 4,
        phaseCosts = listOf(
            phase(1, 5_000, "114001" to 3, "112014" to 3, "112011" to 2),
            phase(2, 15_000, "114002" to 3, "112014" to 12, "112011" to 8),
            phase(3, 20_000, "114002" to 6, "112015" to 6, "112012" to 6),
            phase(4, 30_000, "114003" to 3, "112015" to 12, "112012" to 9),
            phase(5, 35_000, "114003" to 6, "112016" to 9, "112013" to 6),
            phase(6, 45_000, "114004" to 4, "112016" to 18, "112013" to 12)
        ),
        sourceLabel = "Genshin-DB weapon data",
        sourceUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/weapons/favoniussword.json",
        sourceLicense = "MIT; upstream game data attribution retained",
        retrievedOn = "2026-11-27"
    )

    private fun phase(rank: Int, mora: Long, vararg materials: Pair<String, Long>) =
        WeaponAscensionPhaseCost(
            rank,
            listOf(WeaponExperienceMaterialRequirement("202", mora)) +
                materials.map { WeaponExperienceMaterialRequirement(it.first, it.second) }
        )

    private fun request(
        id: Int = 11401,
        rarity: Int = 4,
        current: Int = 1,
        target: Int = 90,
        rank: Int = 0
    ) = WeaponAscensionCostRequest(id, rarity, current, target, rank)

    private fun quantities(result: WeaponAscensionCostResult) =
        result.requirements.associate { it.itemId to it.quantity }

    @Test fun fourStarFavoniusSwordFullAscensionTotalsMatchSourceRows() {
        val result = WeaponAscensionCostCalculator.calculate(request(), favonius)
        assertEquals(WeaponAscensionCostStatus.CANDIDATE_DATA, result.status)
        assertEquals((1..6).toList(), result.requiredAscensionLevels)
        assertEquals(
            mapOf(
                "202" to 150_000L,
                "114001" to 3L, "114002" to 9L, "114003" to 9L, "114004" to 4L,
                "112014" to 15L, "112015" to 18L, "112016" to 27L,
                "112011" to 10L, "112012" to 15L, "112013" to 18L
            ),
            quantities(result)
        )
    }

    @Test fun onlyAscensionsBeyondCurrentRankAreCharged() {
        val result = WeaponAscensionCostCalculator.calculate(
            request(current = 40, target = 70, rank = 1),
            favonius
        )
        assertEquals(listOf(2, 3, 4), result.requiredAscensionLevels)
        assertEquals(65_000L, quantities(result)["202"])
    }

    @Test fun levelFortyRequiresFirstAscensionWhenCurrentRankIsZero() {
        val result = WeaponAscensionCostCalculator.calculate(
            request(current = 20, target = 40, rank = 0),
            favonius
        )
        assertEquals(listOf(1), result.requiredAscensionLevels)
        assertEquals(5_000L, quantities(result)["202"])
    }

    @Test fun alreadyUnlockedTargetRangeIsAValidNoOp() {
        val result = WeaponAscensionCostCalculator.calculate(
            request(current = 20, target = 40, rank = 1),
            favonius
        )
        assertEquals(WeaponAscensionCostStatus.CANDIDATE_DATA, result.status)
        assertTrue(result.requirements.isEmpty())
        assertTrue(result.requiredAscensionLevels.isEmpty())
    }

    @Test fun missingPhaseWithholdsAllPartialRequirements() {
        val incomplete = favonius.copy(phaseCosts = favonius.phaseCosts.filterNot { it.ascensionLevel == 4 })
        val result = WeaponAscensionCostCalculator.calculate(request(), incomplete)
        assertEquals(WeaponAscensionCostStatus.COST_DATA_INCOMPLETE, result.status)
        assertEquals(4, result.missingAscensionLevel)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun absentWeaponCandidateIsIncompleteRatherThanZero() {
        val result = WeaponAscensionCostCalculator.calculate(request(), null)
        assertEquals(WeaponAscensionCostStatus.COST_DATA_INCOMPLETE, result.status)
        assertEquals(1, result.missingAscensionLevel)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun rejectsWrongWeaponAndRarityCandidates() {
        assertEquals(
            WeaponAscensionCostIssue.WEAPON_ID_MISMATCH,
            WeaponAscensionCostCalculator.calculate(request(id = 11402), favonius).issue
        )
        assertEquals(
            WeaponAscensionCostIssue.RARITY_MISMATCH,
            WeaponAscensionCostCalculator.calculate(request(rarity = 5), favonius).issue
        )
    }

    @Test fun rejectsUnsupportedRarityAndInvalidAscensionState() {
        assertEquals(
            WeaponAscensionCostIssue.UNSUPPORTED_RARITY,
            WeaponAscensionCostCalculator.calculate(request(rarity = 3), favonius).issue
        )
        assertEquals(
            WeaponAscensionCostIssue.INVALID_ASCENSION_LEVEL,
            WeaponAscensionCostCalculator.calculate(request(rank = 7), favonius).issue
        )
        assertEquals(
            WeaponAscensionCostIssue.INVALID_LEVEL_RANGE,
            WeaponAscensionCostCalculator.calculate(request(current = 41, rank = 1), favonius).issue
        )
        assertEquals(
            WeaponAscensionCostIssue.TARGET_BELOW_CURRENT_LEVEL,
            WeaponAscensionCostCalculator.calculate(request(current = 50, target = 40, rank = 2), favonius).issue
        )
    }

    @Test fun rejectsMalformedAndDuplicatePhaseData() {
        val duplicate = favonius.copy(phaseCosts = favonius.phaseCosts + favonius.phaseCosts.first())
        assertEquals(
            WeaponAscensionCostIssue.DUPLICATE_PHASE,
            WeaponAscensionCostCalculator.calculate(request(), duplicate).issue
        )
        val invalid = favonius.copy(
            phaseCosts = favonius.phaseCosts.map { if (it.ascensionLevel == 1) it.copy(costs = emptyList()) else it }
        )
        assertEquals(
            WeaponAscensionCostIssue.INVALID_PHASE,
            WeaponAscensionCostCalculator.calculate(request(), invalid).issue
        )
    }

    @Test fun candidateProvenanceRemainsUnreviewedAndUnapproved() {
        assertEquals(KnowledgeReviewState.CANDIDATE, favonius.reviewState)
        assertEquals(KnowledgePermissionState.PENDING_REVIEW, favonius.permissionState)
        assertTrue(favonius.sourceUrl.contains("favoniussword.json"))
    }
}
