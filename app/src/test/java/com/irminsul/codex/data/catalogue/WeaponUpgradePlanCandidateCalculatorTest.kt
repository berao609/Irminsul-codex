package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.WeaponExperienceRarity
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculator
import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class WeaponUpgradePlanCandidateCalculatorTest {
    private val curve = GenshinWeaponExperienceCurveCandidate.fourStar.cumulativeExperienceByLevel

    private fun candidate(from: Int, to: Int, count: Long = 100L) =
        GenshinDbUpgradeCostCandidate(
            candidateId = "weapon:123:$from-$to",
            subjectType = UpgradeCostSubjectType.WEAPON,
            subjectId = "123",
            track = UpgradeCostTrack.WEAPON_ASCENSION,
            fromLevel = from,
            toLevel = to,
            talentSlot = null,
            gameVersion = "7.2",
            costs = listOf(GenshinDbParsedCostItem("202", "Mora", count)),
            sourceReference = KnowledgeSourceReference(
                "genshin-db-7.2",
                "https://github.com/theBowja/genshin-db/blob/main/src/data/English/weapons/example.json",
                "Genshin-DB"
            ),
            retrievedAt = Instant.parse("2026-11-27T00:00:00Z")
        )

    private fun request(current: Int = 1, target: Int = 90, ascension: Int = 0) =
        WeaponUpgradePlanCandidateRequest(
            weaponId = "123",
            gameVersion = "7.2",
            rarity = WeaponExperienceRarity.FOUR_STAR,
            currentLevel = current,
            currentExperienceWithinLevel = 0,
            currentAscensionLevel = ascension,
            targetLevel = target
        )

    @Test fun fullUpgradeCombinesAllSixAscensionStagesWithOreCosts() {
        val stages = listOf(
            candidate(20, 40), candidate(40, 50), candidate(50, 60),
            candidate(60, 70), candidate(70, 80), candidate(80, 90)
        )
        val result = WeaponUpgradePlanCandidateCalculator.calculate(request(), curve, stages)
        assertEquals(WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertTrue(result.missingAscensionFromLevels.isEmpty())
        assertEquals(605_000L, result.requirements.first { it.itemId == "202" }.quantity)
    }

    @Test fun missingAscensionStageKeepsPlanIncompleteAndReportsMissingBoundary() {
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(target = 50),
            curve,
            listOf(candidate(20, 40))
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.INCOMPLETE, result.status)
        assertEquals(listOf(40), result.missingAscensionFromLevels)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun alreadyCompletedAscensionStagesAreNotChargedAgain() {
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(current = 45, target = 60, ascension = 2),
            curve,
            listOf(candidate(50, 60))
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertEquals(result.experience!!.moraRequired + 100L, result.requirements.first { it.itemId == "202" }.quantity)
    }

    @Test fun noAscensionCostIsRequiredWhenTargetStopsAtCurrentCap() {
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(target = 20),
            curve,
            emptyList()
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertTrue(result.requirements.any { it.itemId == "202" })
        assertTrue(result.requirements.any { it.itemId == WeaponExperienceCalculator.MYSTIC_ENHANCEMENT_ORE_ITEM_ID })
    }

    @Test fun rejectsDuplicateAscensionSegments() {
        val duplicate = candidate(20, 40)
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(target = 40),
            curve,
            listOf(duplicate, duplicate.copy(candidateId = "duplicate"))
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.INVALID, result.status)
        assertTrue(result.invalidCandidateIds.isNotEmpty())
    }

    @Test fun filtersCandidatesToExactWeaponAndVersion() {
        val wrongWeapon = candidate(20, 40).copy(subjectId = "999")
        val wrongVersion = candidate(20, 40).copy(gameVersion = "7.1")
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(target = 40),
            curve,
            listOf(wrongWeapon, wrongVersion)
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.INCOMPLETE, result.status)
        assertEquals(listOf(20), result.missingAscensionFromLevels)
    }
}
