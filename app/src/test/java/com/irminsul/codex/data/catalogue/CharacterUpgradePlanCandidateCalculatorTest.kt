package com.irminsul.codex.data.catalogue

import com.irminsul.codex.data.catalogue.GenshinCharacterExperienceCurveCandidate
import com.irminsul.codex.domain.analysis.CharacterExperienceCalculator
import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CharacterUpgradePlanCandidateCalculatorTest {
    private val curve = GenshinCharacterExperienceCurveCandidate.current.cumulativeExperienceByLevel

    private fun candidate(from: Int, to: Int, itemId: String = "202", itemName: String = "Mora",
                          quantity: Long = 100L, candidateId: String = "candidate-$from") =
        GenshinDbUpgradeCostCandidate(
            candidateId = candidateId,
            subjectType = UpgradeCostSubjectType.CHARACTER,
            subjectId = "10000002",
            track = UpgradeCostTrack.CHARACTER_ASCENSION,
            fromLevel = from,
            toLevel = to,
            talentSlot = null,
            gameVersion = "7.2",
            costs = listOf(GenshinDbParsedCostItem(itemId, itemName, quantity)),
            sourceReference = KnowledgeSourceReference(
                "genshin-db-7.2",
                "https://github.com/theBowja/genshin-db/blob/8e5badd60b5d894284ccc927a56c9683529d2592/src/data/English/characters/ayaka.json",
                "Genshin-DB"
            ),
            retrievedAt = Instant.parse("2026-11-27T00:00:00Z")
        )

    private fun request(current: Int = 1, currentExp: Long = 0L, rank: Int = 0, target: Int = 90) =
        CharacterUpgradePlanCandidateRequest("10000002", "7.2", current, currentExp, rank, target)

    private fun stages() = listOf(
        candidate(20, 40, itemId = "104121", itemName = "Shivada Jade Sliver", quantity = 3),
        candidate(40, 50, itemId = "104122", itemName = "Shivada Jade Fragment", quantity = 9),
        candidate(50, 60, itemId = "104123", itemName = "Shivada Jade Chunk", quantity = 9),
        candidate(60, 70, itemId = "104124", itemName = "Shivada Jade Gemstone", quantity = 6),
        candidate(70, 80, itemId = "112001", itemName = "Whopperflower Nectar", quantity = 18),
        candidate(80, 90, itemId = "112002", itemName = "Shimmering Nectar", quantity = 12)
    )

    @Test fun fullPlanCombinesCharacterExpAscensionDropsAndMoraOnce() {
        val result = CharacterUpgradePlanCandidateCalculator.calculate(request(), curve, stages())
        assertEquals(CharacterUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertEquals(6, result.usedAscensionCandidateIds.size)
        val exp = CharacterExperienceCalculator.calculate(
            com.irminsul.codex.domain.analysis.CharacterExperienceCalculationRequest(
                currentLevel = 1,
                currentExperienceWithinLevel = 0L,
                targetLevel = 90,
                maximumUnlockedLevel = 20,
                targetMaximumUnlockedLevel = 90
            ),
            curve
        )
        assertEquals(
            exp.moraRequired + 600L,
            result.requirements.single { it.itemId == CharacterExperienceCalculator.MORA_ITEM_ID }.quantity
        )
        assertEquals(415L, result.requirements.single { it.itemId == CharacterExperienceCalculator.HEROS_WIT_ITEM_ID }.quantity)
        assertEquals(3L, result.requirements.single { it.itemId == "104121" }.quantity)
    }

    @Test fun missingAscensionStageWithholdsAllCombinedTotals() {
        val result = CharacterUpgradePlanCandidateCalculator.calculate(
            request(target = 60),
            curve,
            listOf(candidate(20, 40), candidate(50, 60))
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.INCOMPLETE, result.status)
        assertEquals(40, result.missingAscensionFromLevel)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun alreadyPaidAscensionsAreNotChargedAgain() {
        val result = CharacterUpgradePlanCandidateCalculator.calculate(
            request(current = 40, rank = 2, target = 60),
            curve,
            listOf(candidate(50, 60, itemId = "112001", itemName = "Whopperflower Nectar", quantity = 4))
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertEquals(4L, result.requirements.single { it.itemId == "112001" }.quantity)
    }

    @Test fun noAscensionRequiredStillReturnsCharacterExpAndMora() {
        val result = CharacterUpgradePlanCandidateCalculator.calculate(
            request(target = 20),
            curve,
            emptyList()
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertTrue(result.requirements.any { it.itemId == CharacterExperienceCalculator.HEROS_WIT_ITEM_ID })
        assertTrue(result.requirements.any { it.itemId == CharacterExperienceCalculator.MORA_ITEM_ID })
    }

    @Test fun rejectsInvalidCurrentExperienceAndQuantityOverflow() {
        val invalidExp = CharacterUpgradePlanCandidateCalculator.calculate(
            request(current = 20, currentExp = 1L, target = 40),
            curve,
            listOf(candidate(20, 40))
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.INVALID, invalidExp.status)
        assertTrue(invalidExp.requirements.isEmpty())

        val overflow = CharacterUpgradePlanCandidateCalculator.calculate(
            request(target = 50),
            curve,
            listOf(
                candidate(20, 40, itemId = "999", itemName = "Overflow", quantity = Long.MAX_VALUE),
                candidate(40, 50, itemId = "999", itemName = "Overflow", quantity = 1L)
            )
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.INVALID, overflow.status)
        assertTrue(overflow.requirements.isEmpty())
    }
}
