package com.irminsul.codex.domain.analysis

import com.irminsul.codex.data.catalogue.GenshinCharacterExperienceCurveCandidate
import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import org.junit.Assert.*
import org.junit.Test

class CharacterExperienceCalculatorTest {
    private val curve = GenshinCharacterExperienceCurveCandidate.current.cumulativeExperienceByLevel

    private fun request(
        current: Int = 1,
        currentExp: Long = 0L,
        target: Int = 20,
        cap: Int = 90
    ) = CharacterExperienceCalculationRequest(current, currentExp, target, cap)

    private fun result(request: CharacterExperienceCalculationRequest) =
        CharacterExperienceCalculator.calculate(request, curve)

    @Test fun candidateCurveHasAllNinetyStrictlyIncreasingLevelsAndKnownCheckpoints() {
        assertEquals(90, curve.size)
        assertEquals(0L, curve.first())
        assertEquals(120_175L, curve[19])
        assertEquals(698_500L, curve[39])
        assertEquals(1_277_600L, curve[49])
        assertEquals(2_131_725L, curve[59])
        assertEquals(3_327_650L, curve[69])
        assertEquals(4_939_525L, curve[79])
        assertEquals(8_362_650L, curve[89])
        assertTrue(curve.zipWithNext().all { (a, b) -> b > a })
    }

    @Test fun levelOneToTwentyMatchesPublishedBookAndMoraTotals() {
        val calculated = result(request(target = 20))
        assertEquals(CharacterExperienceCalculationStatus.CANDIDATE_DATA, calculated.status)
        assertEquals(120_175L, calculated.requiredExperience)
        assertEquals(121_000L, calculated.grantedExperience)
        assertEquals(825L, calculated.wastedExperience)
        assertEquals(24_200L, calculated.moraRequired)
        assertEquals(
            mapOf(
                CharacterExperienceCalculator.HEROS_WIT_ITEM_ID to 6L,
                CharacterExperienceCalculator.WANDERERS_ADVICE_ITEM_ID to 1L
            ),
            calculated.materials.filter { it.itemId != CharacterExperienceCalculator.MORA_ITEM_ID }
                .associate { it.itemId to it.quantity }
        )
    }

    @Test fun levelOneToNinetyMatchesPublishedTotals() {
        val calculated = result(request(target = 90))
        assertEquals(8_362_650L, calculated.requiredExperience)
        assertEquals(8_367_000L, calculated.grantedExperience)
        assertEquals(4_350L, calculated.wastedExperience)
        assertEquals(1_673_400L, calculated.moraRequired)
        assertEquals(
            mapOf(
                CharacterExperienceCalculator.HEROS_WIT_ITEM_ID to 415L,
                CharacterExperienceCalculator.ADVENTURERS_EXPERIENCE_ITEM_ID to 11L,
                CharacterExperienceCalculator.WANDERERS_ADVICE_ITEM_ID to 12L,
                CharacterExperienceCalculator.MORA_ITEM_ID to 1_673_400L
            ),
            calculated.materials.associate { it.itemId to it.quantity }
        )
    }

    @Test fun currentLevelExperienceIsSubtractedBeforeBookRounding() {
        val calculated = result(request(current = 20, currentExp = 5_000L, target = 40, cap = 40))
        assertEquals(573_325L, calculated.requiredExperience)
        assertEquals(574_000L, calculated.grantedExperience)
        assertEquals(675L, calculated.wastedExperience)
        assertEquals(114_800L, calculated.moraRequired)
    }

    @Test fun targetCannotExceedCurrentAscensionUnlockedCap() {
        val calculated = result(request(current = 20, target = 40, cap = 20))
        assertEquals(CharacterExperienceCalculationStatus.INVALID_REQUEST, calculated.status)
        assertEquals(CharacterExperienceCalculationIssue.TARGET_EXCEEDS_ASCENSION_CAP, calculated.issue)
        assertTrue(calculated.materials.isEmpty())
    }

    @Test fun sameLevelDoesNotChargeForAlreadyCompletedProgression() {
        val calculated = result(request(current = 40, currentExp = 0L, target = 40, cap = 40))
        assertEquals(CharacterExperienceCalculationStatus.CANDIDATE_DATA, calculated.status)
        assertEquals(0L, calculated.requiredExperience)
        assertEquals(0L, calculated.moraRequired)
        assertTrue(calculated.materials.isEmpty())
    }

    @Test fun rejectsExperienceAtOrAboveTheNextLevelBoundary() {
        val calculated = result(request(current = 20, currentExp = 14_875L, target = 40, cap = 40))
        assertEquals(CharacterExperienceCalculationIssue.INVALID_CURRENT_EXPERIENCE, calculated.issue)
    }

    @Test fun rejectsInvalidLevelsAndMalformedCurves() {
        assertEquals(
            CharacterExperienceCalculationIssue.INVALID_LEVEL_RANGE,
            result(request(current = 0)).issue
        )
        assertEquals(
            CharacterExperienceCalculationIssue.INVALID_PROGRESSION_DATA,
            CharacterExperienceCalculator.calculate(request(), curve.dropLast(1)).issue
        )
        assertEquals(
            CharacterExperienceCalculationIssue.INVALID_PROGRESSION_DATA,
            CharacterExperienceCalculator.calculate(request(), curve.toMutableList().also { it[5] = it[4] })
                .issue
        )
    }

    @Test fun curveRemainsExplicitlyUnreviewedAndPermissionPending() {
        val candidate = GenshinCharacterExperienceCurveCandidate.current
        assertEquals(KnowledgeReviewState.CANDIDATE, candidate.reviewState)
        assertEquals(KnowledgePermissionState.PENDING_REVIEW, candidate.permissionState)
        assertEquals("CC BY-SA 3.0", candidate.sourceLicense)
        assertTrue(candidate.sourceReference.canonicalUrl.contains("Character_EXP"))
    }
}
