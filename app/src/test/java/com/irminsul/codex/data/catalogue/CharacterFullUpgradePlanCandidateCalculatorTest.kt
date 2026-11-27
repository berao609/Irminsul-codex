package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CharacterFullUpgradePlanCandidateCalculatorTest {
    private fun talent(slot: String, from: Int, to: Int, bookId: String) =
        GenshinDbUpgradeCostCandidate(
            candidateId = "talent-$slot-$to",
            subjectType = UpgradeCostSubjectType.CHARACTER,
            subjectId = "10000002",
            track = UpgradeCostTrack.TALENT_LEVEL,
            fromLevel = from,
            toLevel = to,
            talentSlot = slot,
            gameVersion = "7.2",
            costs = listOf(
                GenshinDbParsedCostItem("202", "Mora", 100L),
                GenshinDbParsedCostItem(bookId, "Talent Book", 3L)
            ),
            sourceReference = KnowledgeSourceReference(
                "genshin-db-7.2",
                "https://github.com/theBowja/genshin-db/blob/8e5badd60b5d894284ccc927a56c9683529d2592/src/data/English/talents/ayaka.json",
                "Genshin-DB"
            ),
            retrievedAt = Instant.parse("2026-11-27T00:00:00Z")
        )

    private fun levelPlan() = CharacterUpgradePlanCandidateCalculator.calculate(
        CharacterUpgradePlanCandidateRequest("10000002", "7.2", 1, 0L, 0, 20),
        GenshinCharacterExperienceCurveCandidate.current.cumulativeExperienceByLevel,
        emptyList()
    )

    @Test fun combinesAllThreeIndependentTalentTracksAndAggregatesMora() {
        val result = CharacterFullUpgradePlanCandidateCalculator.calculate(
            levelPlan(), "10000002", "7.2",
            listOf(
                CharacterTalentLevelRange("normal", 1, 2),
                CharacterTalentLevelRange("skill", 1, 2),
                CharacterTalentLevelRange("burst", 1, 2)
            ),
            listOf(
                talent("normal", 1, 2, "book-normal"),
                talent("skill", 1, 2, "book-skill"),
                talent("burst", 1, 2, "book-burst")
            )
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertEquals(300L + levelPlan().requirements.single { it.itemId == "202" }.quantity,
            result.requirements.single { it.itemId == "202" }.quantity)
        assertEquals(3L, result.requirements.single { it.itemId == "book-normal" }.quantity)
        assertEquals(3L, result.requirements.single { it.itemId == "book-skill" }.quantity)
        assertEquals(3L, result.requirements.single { it.itemId == "book-burst" }.quantity)
    }

    @Test fun unchangedTalentDoesNotChargeAndMissingTrackWithholdsAllTotals() {
        val ranges = listOf(
            CharacterTalentLevelRange("normal", 1, 1),
            CharacterTalentLevelRange("skill", 1, 2),
            CharacterTalentLevelRange("burst", 1, 1)
        )
        val missing = CharacterFullUpgradePlanCandidateCalculator.calculate(
            levelPlan(), "10000002", "7.2", ranges,
            listOf(talent("skill", 1, 2, "book-skill"))
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.INCOMPLETE, missing.status)
        assertTrue(missing.requirements.isEmpty())

        val complete = CharacterFullUpgradePlanCandidateCalculator.calculate(
            levelPlan(), "10000002", "7.2", ranges,
            listOf(talent("skill", 1, 2, "book-skill"))
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.INCOMPLETE, complete.status)
    }

    @Test fun rejectsInvalidTalentLevels() {
        val result = CharacterFullUpgradePlanCandidateCalculator.calculate(
            levelPlan(), "10000002", "7.2",
            listOf(
                CharacterTalentLevelRange("normal", 0, 2),
                CharacterTalentLevelRange("skill", 1, 1),
                CharacterTalentLevelRange("burst", 1, 1)
            ), emptyList()
        )
        assertEquals(CharacterUpgradePlanCandidateStatus.INVALID, result.status)
        assertTrue(result.requirements.isEmpty())
    }
}
