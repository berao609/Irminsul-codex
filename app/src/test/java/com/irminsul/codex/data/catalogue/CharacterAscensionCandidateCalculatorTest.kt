package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CharacterAscensionCandidateCalculatorTest {
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

    private fun request(current: Int = 1, rank: Int = 0, target: Int = 90) =
        CharacterAscensionCandidateCalculationRequest("10000002", current, rank, target, "7.2")

    private fun stages() = listOf(
        candidate(20, 40, itemId = "104121", itemName = "Shivada Jade Sliver"),
        candidate(40, 50, itemId = "104122", itemName = "Shivada Jade Fragment"),
        candidate(50, 60, itemId = "104123", itemName = "Shivada Jade Chunk"),
        candidate(60, 70, itemId = "104124", itemName = "Shivada Jade Gemstone"),
        candidate(70, 80, itemId = "112001", itemName = "Whopperflower Nectar"),
        candidate(80, 90, itemId = "112002", itemName = "Shimmering Nectar")
    )

    @Test fun calculatesAllSixStagesAndAggregatesDuplicateMaterialIds() {
        val duplicate = stages().mapIndexed { index, item ->
            if (index == 1) item.copy(costs = listOf(
                GenshinDbParsedCostItem("104121", "Shivada Jade Sliver", 2L),
                GenshinDbParsedCostItem("104122", "Shivada Jade Fragment", 3L)
            )) else item
        }
        val result = CharacterAscensionCandidateCalculator.calculate(request(), duplicate)
        assertEquals(CharacterAscensionCandidateCalculationStatus.CANDIDATE_DATA, result.status)
        assertEquals(6, result.usedCandidateIds.size)
        assertEquals(102L, result.materials.single { it.itemId == "104121" }.quantity)
    }

    @Test fun alreadyPaidAscensionsAreNotChargedAgain() {
        val result = CharacterAscensionCandidateCalculator.calculate(
            request(current = 40, rank = 2, target = 60),
            listOf(candidate(50, 60, itemId = "112001", itemName = "Whopperflower Nectar", quantity = 4))
        )
        assertEquals(CharacterAscensionCandidateCalculationStatus.CANDIDATE_DATA, result.status)
        assertEquals(4L, result.materials.single().quantity)
    }

    @Test fun noAscensionIsNeededForTargetWithinCurrentCap() {
        val result = CharacterAscensionCandidateCalculator.calculate(
            request(current = 1, rank = 0, target = 20),
            emptyList()
        )
        assertEquals(CharacterAscensionCandidateCalculationStatus.NO_ASCENSION_REQUIRED, result.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun missingRequiredStageWithholdsAllPartialMaterials() {
        val result = CharacterAscensionCandidateCalculator.calculate(
            request(current = 1, rank = 0, target = 60),
            listOf(candidate(20, 40), candidate(50, 60))
        )
        assertEquals(CharacterAscensionCandidateCalculationStatus.COST_DATA_INCOMPLETE, result.status)
        assertEquals(40, result.missingFromLevel)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun rejectsDuplicateStageRecords() {
        val result = CharacterAscensionCandidateCalculator.calculate(
            request(target = 40),
            listOf(candidate(20, 40), candidate(20, 40, candidateId = "duplicate"))
        )
        assertEquals(CharacterAscensionCandidateCalculationStatus.OVERLAPPING_SEGMENTS, result.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun rejectsUnsupportedRanksAndInvalidCandidateValues() {
        assertEquals(
            CharacterAscensionCandidateCalculationStatus.INVALID_REQUEST,
            CharacterAscensionCandidateCalculator.calculate(request(rank = 7), stages()).status
        )
        val invalid = candidate(20, 40).copy(costs = listOf(GenshinDbParsedCostItem("x", "Bad", 0)))
        assertEquals(
            CharacterAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
            CharacterAscensionCandidateCalculator.calculate(request(target = 40), listOf(invalid)).status
        )
    }
}
