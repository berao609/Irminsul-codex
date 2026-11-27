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
    private val fourStarCurve = GenshinWeaponExperienceCurveCandidate.fourStar.cumulativeExperienceByLevel
    private val fiveStarCurve = GenshinWeaponExperienceCurveCandidate.fiveStar.cumulativeExperienceByLevel

    private fun candidate(
        from: Int,
        to: Int,
        itemId: String = "202",
        itemName: String = "Mora",
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
        costs = listOf(GenshinDbParsedCostItem(itemId, itemName, quantity)),
        sourceReference = KnowledgeSourceReference(
            "genshin-db-7.2",
            "https://github.com/theBowja/genshin-db/blob/8e5badd60b5d894284ccc927a56c9683529d2592/src/data/English/weapons/amosbow.json",
            "Genshin-DB"
        ),
        retrievedAt = Instant.parse("2026-11-27T00:00:00Z")
    )

    private fun stages() = listOf(
        candidate(20, 40, quantity = 10),
        candidate(40, 50, itemId = "114001", itemName = "Tile of Decarabian's Tower", quantity = 3),
        candidate(50, 60, itemId = "112023", itemName = "Chaos Device", quantity = 4),
        candidate(60, 70, itemId = "112002", itemName = "Slime Condensate", quantity = 5),
        candidate(70, 80, itemId = "114002", itemName = "Debris of Decarabian's City", quantity = 6),
        candidate(80, 90, itemId = "112024", itemName = "Chaos Circuit", quantity = 7)
    )

    private fun request(
        rarity: WeaponExperienceRarity = WeaponExperienceRarity.FIVE_STAR,
        current: Int = 1,
        currentExp: Long = 0L,
        rank: Int = 0,
        target: Int = 90
    ) = WeaponUpgradePlanCandidateRequest(
        weaponId = "15502",
        gameVersion = "7.2",
        rarity = rarity,
        currentLevel = current,
        currentExperienceWithinLevel = currentExp,
        currentAscensionRank = rank,
        targetLevel = target
    )

    @Test fun fullPlanCombinesOreAscensionEnemyDropsAndMoraWithoutDoubleCounting() {
        val result = WeaponUpgradePlanCandidateCalculator.calculate(request(), fiveStarCurve, stages())
        assertEquals(WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertEquals(6, result.usedAscensionCandidateIds.size)
        assertEquals(9, result.requirements.size)
        val mora = result.requirements.single { it.itemId == WeaponExperienceCalculator.MORA_ITEM_ID }
        val expectedExperience = WeaponExperienceCalculator.calculate(
            com.irminsul.codex.domain.analysis.WeaponExperienceCalculationRequest(
                rarity = WeaponExperienceRarity.FIVE_STAR,
                currentLevel = 1,
                currentExperienceWithinLevel = 0L,
                targetLevel = 90,
                maximumUnlockedLevel = 20,
                targetMaximumUnlockedLevel = 90
            ),
            fiveStarCurve
        )
        assertEquals(expectedExperience.moraRequired + 10L, mora.quantity)
        expectedExperience.materials.filterNot { it.itemId == WeaponExperienceCalculator.MORA_ITEM_ID }.forEach { expected ->
            assertEquals(
                expected.quantity,
                result.requirements.single { it.itemId == expected.itemId }.quantity
            )
        }
        assertEquals(3L, result.requirements.single { it.itemId == "114001" }.quantity)
        assertEquals(7L, result.requirements.single { it.itemId == "112024" }.quantity)
    }

    @Test fun missingAscensionStageWithholdsAllPartialTotals() {
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(target = 60),
            fiveStarCurve,
            listOf(candidate(20, 40), candidate(50, 60))
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.INCOMPLETE, result.status)
        assertEquals(40, result.missingAscensionFromLevel)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun noAscensionRequiredStillReturnsWeaponExpAndMora() {
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(current = 1, target = 20),
            fourStarCurve,
            emptyList()
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertTrue(result.requirements.any { it.itemId == WeaponExperienceCalculator.MORA_ITEM_ID })
        assertTrue(result.requirements.any {
            it.itemId == WeaponExperienceCalculator.MYSTIC_ENHANCEMENT_ORE_ITEM_ID
        })
        assertTrue(result.usedAscensionCandidateIds.isEmpty())
    }

    @Test fun alreadyPaidAscensionsAreNotChargedAgain() {
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(current = 40, rank = 2, target = 60),
            fiveStarCurve,
            listOf(candidate(50, 60, itemId = "112023", itemName = "Chaos Device", quantity = 4))
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertEquals(4L, result.requirements.single { it.itemId == "112023" }.quantity)
        val expectedExperienceMora = WeaponExperienceCalculator.calculate(
            com.irminsul.codex.domain.analysis.WeaponExperienceCalculationRequest(
                rarity = WeaponExperienceRarity.FIVE_STAR,
                currentLevel = 40,
                currentExperienceWithinLevel = 0L,
                targetLevel = 60,
                maximumUnlockedLevel = 50,
                targetMaximumUnlockedLevel = 60
            ),
            fiveStarCurve
        ).moraRequired
        assertEquals(
            expectedExperienceMora,
            result.requirements.single { it.itemId == WeaponExperienceCalculator.MORA_ITEM_ID }.quantity
        )
    }

    @Test fun rejectsCurrentExpInvalidAtCurrentAscensionCap() {
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(current = 20, currentExp = 1L, target = 40),
            fiveStarCurve,
            listOf(candidate(20, 40))
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.INVALID, result.status)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun rejectsUnsupportedRarityAndInvalidLevels() {
        val invalid = request().copy(rarity = WeaponExperienceRarity.FOUR_STAR, currentLevel = 21, currentAscensionRank = 0)
        val result = WeaponUpgradePlanCandidateCalculator.calculate(invalid, fourStarCurve, emptyList())
        assertEquals(WeaponUpgradePlanCandidateStatus.INVALID, result.status)
    }

    @Test fun conflictingMaterialNamesAreNotSilentlyMerged() {
        val conflicting = listOf(
            candidate(20, 40, itemId = "114001", itemName = "First", quantity = 1),
            candidate(40, 50, itemId = "114001", itemName = "Different", quantity = 1)
        )
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(target = 50),
            fiveStarCurve,
            conflicting
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.INVALID, result.status)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun quantityOverflowIsReportedWithoutPartialTotals() {
        val huge = listOf(
            candidate(20, 40, itemId = "999001", itemName = "Test", quantity = Long.MAX_VALUE),
            candidate(40, 50, itemId = "999001", itemName = "Test", quantity = 1L)
        )
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(target = 50),
            fiveStarCurve,
            huge
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.INVALID, result.status)
        assertTrue(result.requirements.isEmpty())
    }

    @Test fun everyAscensionBoundaryWorksForFourStarAndFiveStarWeapons() {
        val targets = listOf(20, 40, 50, 60, 70, 80, 90)
        listOf(
            WeaponExperienceRarity.FOUR_STAR to fourStarCurve,
            WeaponExperienceRarity.FIVE_STAR to fiveStarCurve
        ).forEach { (rarity, curve) ->
            targets.forEachIndexed { index, target ->
                val result = WeaponUpgradePlanCandidateCalculator.calculate(
                    request(rarity = rarity, target = target), curve, stages()
                )
                assertEquals("rarity=${rarity.starCount}, target=$target",
                    WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
                assertEquals("rarity=${rarity.starCount}, target=$target",
                    index, result.usedAscensionCandidateIds.size)
                assertTrue(result.requirements.any {
                    it.itemId == WeaponExperienceCalculator.MORA_ITEM_ID
                })
            }
        }
    }

    @Test fun partialCurrentWeaponExpIsPreservedInTheCombinedPlan() {
        listOf(
            WeaponExperienceRarity.FOUR_STAR to fourStarCurve,
            WeaponExperienceRarity.FIVE_STAR to fiveStarCurve
        ).forEach { (rarity, curve) ->
            val result = WeaponUpgradePlanCandidateCalculator.calculate(
                request(rarity = rarity, current = 5, currentExp = 100L, target = 90),
                curve, stages()
            )
            val expectedExp = WeaponExperienceCalculator.calculate(
                com.irminsul.codex.domain.analysis.WeaponExperienceCalculationRequest(
                    rarity = rarity, currentLevel = 5, currentExperienceWithinLevel = 100L,
                    targetLevel = 90, maximumUnlockedLevel = 20, targetMaximumUnlockedLevel = 90
                ), curve
            )
            assertEquals(WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
            val expectedAscensionMora = stages().sumOf { stage ->
                stage.costs.filter { it.itemId == WeaponExperienceCalculator.MORA_ITEM_ID }
                    .sumOf { it.quantity }
            }
            assertEquals(expectedExp.moraRequired + expectedAscensionMora,
                result.requirements.single { it.itemId == WeaponExperienceCalculator.MORA_ITEM_ID }.quantity)
            expectedExp.materials.filterNot {
                it.itemId == WeaponExperienceCalculator.MORA_ITEM_ID
            }.forEach { expected ->
                assertEquals(expected.quantity, result.requirements.single {
                    it.itemId == expected.itemId
                }.quantity)
            }
        }
    }

    @Test fun repeatedAscensionMaterialIdsAreAggregatedAcrossStages() {
        val repeated = listOf(
            candidate(20, 40, itemId = "114001", itemName = "Tile", quantity = 3L),
            candidate(40, 50, itemId = "114001", itemName = "Tile", quantity = 4L)
        )
        val result = WeaponUpgradePlanCandidateCalculator.calculate(
            request(target = 50), fiveStarCurve, repeated
        )
        assertEquals(WeaponUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA, result.status)
        assertEquals(7L, result.requirements.single { it.itemId == "114001" }.quantity)
    }
}
