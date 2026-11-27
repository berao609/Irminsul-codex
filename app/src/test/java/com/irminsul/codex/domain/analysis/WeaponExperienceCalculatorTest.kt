package com.irminsul.codex.domain.analysis

import com.irminsul.codex.data.catalogue.GenshinWeaponExperienceCurveCandidate
import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import org.junit.Assert.*
import org.junit.Test

class WeaponExperienceCalculatorTest {
    private val fourStar = GenshinWeaponExperienceCurveCandidate.fourStar.cumulativeExperienceByLevel
    private val fiveStar = GenshinWeaponExperienceCurveCandidate.fiveStar.cumulativeExperienceByLevel

    private fun request(
        rarity: WeaponExperienceRarity = WeaponExperienceRarity.FOUR_STAR,
        current: Int = 1,
        currentExp: Long = 0L,
        target: Int = 20,
        cap: Int = 90
    ) = WeaponExperienceCalculationRequest(rarity, current, currentExp, target, cap)

    private fun result(
        request: WeaponExperienceCalculationRequest,
        curve: List<Long> = if (request.rarity == WeaponExperienceRarity.FOUR_STAR) fourStar else fiveStar
    ) = WeaponExperienceCalculator.calculate(request, curve)

    private fun quantities(result: WeaponExperienceCalculationResult) =
        result.materials.associate { it.itemId to it.quantity }

    @Test fun bothCurvesContainNinetyStrictlyIncreasingLevelsAndKnownAscensionCheckpoints() {
        assertEquals(90, fourStar.size)
        assertEquals(90, fiveStar.size)
        assertEquals(81_000L, fourStar[19])
        assertEquals(496_125L, fourStar[39])
        assertEquals(6_042_650L, fourStar[89])
        assertEquals(121_550L, fiveStar[19])
        assertEquals(744_350L, fiveStar[39])
        assertEquals(9_064_450L, fiveStar[89])
        assertTrue(fourStar.zipWithNext().all { (a, b) -> b > a })
        assertTrue(fiveStar.zipWithNext().all { (a, b) -> b > a })
    }

    @Test fun fourStarLevelOneToNinetyCalculatesOreOnlyTotals() {
        val calculated = result(request(target = 90))
        assertEquals(WeaponExperienceCalculationStatus.CANDIDATE_DATA, calculated.status)
        assertEquals(6_042_650L, calculated.requiredExperience)
        assertEquals(6_044_000L, calculated.grantedExperience)
        assertEquals(1_350L, calculated.wastedExperience)
        assertEquals(604_400L, calculated.moraRequired)
        assertEquals(
            mapOf(
                WeaponExperienceCalculator.MYSTIC_ENHANCEMENT_ORE_ITEM_ID to 600L,
                WeaponExperienceCalculator.FINE_ENHANCEMENT_ORE_ITEM_ID to 19L,
                WeaponExperienceCalculator.ENHANCEMENT_ORE_ITEM_ID to 15L,
                WeaponExperienceCalculator.MORA_ITEM_ID to 604_400L
            ),
            quantities(calculated)
        )
    }

    @Test fun fiveStarLevelOneToNinetyCalculatesOreOnlyTotals() {
        val calculated = result(request(rarity = WeaponExperienceRarity.FIVE_STAR, target = 90))
        assertEquals(9_064_450L, calculated.requiredExperience)
        assertEquals(9_065_200L, calculated.grantedExperience)
        assertEquals(750L, calculated.wastedExperience)
        assertEquals(906_520L, calculated.moraRequired)
        assertEquals(
            mapOf(
                WeaponExperienceCalculator.MYSTIC_ENHANCEMENT_ORE_ITEM_ID to 903L,
                WeaponExperienceCalculator.FINE_ENHANCEMENT_ORE_ITEM_ID to 15L,
                WeaponExperienceCalculator.ENHANCEMENT_ORE_ITEM_ID to 13L,
                WeaponExperienceCalculator.MORA_ITEM_ID to 906_520L
            ),
            quantities(calculated)
        )
    }

    @Test fun eachAscensionPhaseRoundsOreExperienceIndependently() {
        val calculated = result(request(target = 40, cap = 40))
        assertEquals(496_125L, calculated.requiredExperience)
        assertEquals(496_400L, calculated.grantedExperience)
        assertEquals(275L, calculated.wastedExperience)
        assertEquals(49_640L, calculated.moraRequired)
    }

    @Test fun currentWithinLevelExperienceIsSubtractedBeforeOreRounding() {
        val calculated = result(request(current = 20, currentExp = 5_000L, target = 40, cap = 40))
        assertEquals(410_125L, calculated.requiredExperience)
        assertEquals(410_400L, calculated.grantedExperience)
        assertEquals(275L, calculated.wastedExperience)
    }


    @Test fun calculatesFutureLevelsAcrossAscensionGatesWhenTargetCapIsPlanned() {
        val calculated = result(
            request(current = 20, currentExp = 0L, target = 90, cap = 20).copy(
                targetMaximumUnlockedLevel = 90
            )
        )
        assertEquals(WeaponExperienceCalculationStatus.CANDIDATE_DATA, calculated.status)
        assertEquals(5_961_650L, calculated.requiredExperience)
        assertTrue(calculated.materials.isNotEmpty())
    }

    @Test fun rejectsTargetBeyondThePlannedAscensionCap() {
        val calculated = result(
            request(current = 20, target = 50, cap = 20).copy(
                targetMaximumUnlockedLevel = 40
            )
        )
        assertEquals(WeaponExperienceCalculationIssue.TARGET_EXCEEDS_ASCENSION_CAP, calculated.issue)
        assertTrue(calculated.materials.isEmpty())
    }

    @Test fun rejectsPlannedCapBelowCurrentUnlockedCap() {
        val calculated = result(
            request(current = 20, target = 20, cap = 40).copy(
                targetMaximumUnlockedLevel = 20
            )
        )
        assertEquals(WeaponExperienceCalculationIssue.INVALID_LEVEL_RANGE, calculated.issue)
    }

    @Test fun rejectsTargetAboveUnlockedAscensionCap() {
        val calculated = result(request(current = 20, target = 40, cap = 20))
        assertEquals(WeaponExperienceCalculationIssue.TARGET_EXCEEDS_ASCENSION_CAP, calculated.issue)
        assertTrue(calculated.materials.isEmpty())
    }

    @Test fun rejectsExperienceAtAnAscensionLevelCap() {
        val calculated = result(request(current = 20, currentExp = 1L, target = 20, cap = 20))
        assertEquals(WeaponExperienceCalculationIssue.INVALID_CURRENT_EXPERIENCE, calculated.issue)
    }

    @Test fun maxLevelIsAValidNoOpAndDoesNotChargeMaterials() {
        val calculated = result(request(current = 90, target = 90, cap = 90))
        assertEquals(WeaponExperienceCalculationStatus.CANDIDATE_DATA, calculated.status)
        assertEquals(0L, calculated.requiredExperience)
        assertTrue(calculated.materials.isEmpty())
    }

    @Test fun sameLevelDoesNotChargeForAlreadyCompletedProgression() {
        val calculated = result(request(current = 40, target = 40, cap = 40))
        assertEquals(0L, calculated.requiredExperience)
        assertEquals(0L, calculated.moraRequired)
        assertTrue(calculated.materials.isEmpty())
    }

    @Test fun rejectsMalformedCurvesAndInvalidRequests() {
        assertEquals(
            WeaponExperienceCalculationIssue.INVALID_LEVEL_RANGE,
            result(request(current = 0)).issue
        )
        assertEquals(
            WeaponExperienceCalculationIssue.INVALID_PROGRESSION_DATA,
            result(request(), fourStar.dropLast(1)).issue
        )
        assertEquals(
            WeaponExperienceCalculationIssue.INVALID_PROGRESSION_DATA,
            result(request(), fourStar.toMutableList().also { it[5] = it[4] }).issue
        )
    }

    @Test fun candidateCurvesRetainExplicitUnreviewedSourceAndPermissionState() {
        listOf(GenshinWeaponExperienceCurveCandidate.fourStar, GenshinWeaponExperienceCurveCandidate.fiveStar)
            .forEach { candidate ->
                assertEquals(KnowledgeReviewState.CANDIDATE, candidate.reviewState)
                assertEquals(KnowledgePermissionState.PENDING_REVIEW, candidate.permissionState)
                assertEquals("CC BY-SA 3.0", candidate.sourceLicense)
                assertTrue(candidate.sourceReference.canonicalUrl.contains("Weapon_EXP"))
            }
    }
}
