package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class WeaponAscensionCandidateCalculatorTest {
    private val retrievedAt = Instant.parse("2026-11-27T00:00:00Z")

    private fun candidate(
        from: Int,
        to: Int,
        id: String = "202",
        name: String = "Mora",
        quantity: Long = 100L,
        weaponId: String = "15502",
        candidateId: String = "candidate-$from"
    ) = GenshinDbUpgradeCostCandidate(
        candidateId = candidateId,
        subjectType = UpgradeCostSubjectType.WEAPON,
        subjectId = weaponId,
        track = UpgradeCostTrack.WEAPON_ASCENSION,
        fromLevel = from,
        toLevel = to,
        talentSlot = null,
        gameVersion = "7.2",
        costs = listOf(GenshinDbParsedCostItem(id, name, quantity)),
        sourceReference = KnowledgeSourceReference(
            "genshin-db-7.2",
            "https://github.com/theBowja/genshin-db/blob/main/src/data/English/weapons/amosbow.json",
            "Genshin-DB"
        ),
        retrievedAt = retrievedAt
    )

    private fun request(
        rarity: Int = 5,
        currentLevel: Int = 20,
        currentRank: Int = 0,
        targetLevel: Int = 50
    ) = WeaponAscensionCandidateCalculationRequest(
        weaponId = "15502",
        rarity = rarity,
        currentLevel = currentLevel,
        currentAscensionRank = currentRank,
        targetLevel = targetLevel,
        gameVersion = "7.2"
    )

    @Test fun aggregatesOnlyStagesNeededForTargetLevel() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(targetLevel = 50),
            listOf(
                candidate(20, 40, quantity = 10),
                candidate(40, 50, id = "114001", name = "Tile", quantity = 3),
                candidate(50, 60, quantity = 999)
            )
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.CANDIDATE_DATA, result.status)
        assertEquals(listOf("candidate-20", "candidate-40"), result.usedCandidateIds)
        assertEquals(2, result.materials.size)
        assertEquals(10L, result.materials.first { it.itemId == "202" }.quantity)
        assertEquals(3L, result.materials.first { it.itemId == "114001" }.quantity)
    }

    @Test fun combinesRepeatedMaterialsAcrossStages() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(targetLevel = 50),
            listOf(candidate(20, 40, quantity = 10), candidate(40, 50, quantity = 20))
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.CANDIDATE_DATA, result.status)
        assertEquals(30L, result.materials.single().quantity)
    }

    @Test fun beginsAtCurrentAscensionRankAndDoesNotChargeEarlierStages() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(currentLevel = 40, currentRank = 2, targetLevel = 70),
            listOf(
                candidate(20, 40, quantity = 100),
                candidate(40, 50, quantity = 200),
                candidate(50, 60, quantity = 3),
                candidate(60, 70, quantity = 4)
            )
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.CANDIDATE_DATA, result.status)
        assertEquals(listOf("candidate-50", "candidate-60"), result.usedCandidateIds)
        assertEquals(7L, result.materials.single().quantity)
    }

    @Test fun noOpAtThePreviousLevelBoundaryRemainsValidAfterAscending() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(currentLevel = 20, currentRank = 1, targetLevel = 20),
            emptyList()
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.NO_ASCENSION_REQUIRED, result.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun noAscensionCostIsRequiredWithinCurrentPhase() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(currentLevel = 21, currentRank = 1, targetLevel = 40),
            emptyList()
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.NO_ASCENSION_REQUIRED, result.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun missingStageWithholdsAllPartialTotals() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(targetLevel = 60),
            listOf(candidate(20, 40), candidate(50, 60, quantity = 9))
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.COST_DATA_INCOMPLETE, result.status)
        assertEquals(40, result.missingFromLevel)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun duplicateStageCandidatesAreRejected() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(targetLevel = 40),
            listOf(candidate(20, 40), candidate(20, 40, candidateId = "duplicate"))
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.OVERLAPPING_SEGMENTS, result.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun ignoresCandidatesForOtherWeaponsVersionsAndTracks() {
        val otherWeapon = candidate(20, 40, weaponId = "11401")
        val otherVersion = candidate(20, 40).copy(candidateId = "other-version", gameVersion = "7.1")
        val otherTrack = candidate(20, 40).copy(candidateId = "other-track", track = UpgradeCostTrack.WEAPON_LEVEL)
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(targetLevel = 40),
            listOf(otherWeapon, otherVersion, otherTrack)
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.COST_DATA_INCOMPLETE, result.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun rejectsUnsupportedRarityAndInvalidProgressionInputs() {
        listOf(
            request(rarity = 3),
            request(currentLevel = 21, currentRank = 0, targetLevel = 40),
            request(currentLevel = 50, currentRank = 1, targetLevel = 60),
            request(currentLevel = 40, currentRank = 2, targetLevel = 30)
        ).forEach { invalid ->
            assertEquals(
                WeaponAscensionCandidateCalculationStatus.INVALID_REQUEST,
                WeaponAscensionCandidateCalculator.calculate(invalid, emptyList()).status
            )
        }
    }

    @Test fun rejectsMalformedCandidateMaterialAndConflictingNames() {
        val malformed = candidate(20, 40).copy(costs = listOf(GenshinDbParsedCostItem("", "Mora", 10)))
        assertEquals(
            WeaponAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
            WeaponAscensionCandidateCalculator.calculate(request(targetLevel = 40), listOf(malformed)).status
        )
        val conflicting = listOf(
            candidate(20, 40, id = "123", name = "First", quantity = 1),
            candidate(40, 50, id = "123", name = "Different", quantity = 1)
        )
        assertEquals(
            WeaponAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
            WeaponAscensionCandidateCalculator.calculate(request(targetLevel = 50), conflicting).status
        )
    }

    @Test fun reportsQuantityOverflowInsteadOfWrapping() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(targetLevel = 50),
            listOf(
                candidate(20, 40, quantity = Long.MAX_VALUE),
                candidate(40, 50, quantity = 1)
            )
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.QUANTITY_OVERFLOW, result.status)
        assertTrue(result.materials.isEmpty())
    }

    @Test fun preservesFourStarSupport() {
        val result = WeaponAscensionCandidateCalculator.calculate(
            request(rarity = 4, targetLevel = 40),
            listOf(candidate(20, 40))
        )
        assertEquals(WeaponAscensionCandidateCalculationStatus.CANDIDATE_DATA, result.status)
    }
}
