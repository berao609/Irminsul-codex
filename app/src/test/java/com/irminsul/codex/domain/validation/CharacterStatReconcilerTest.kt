package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CharacterStatReconcilerTest {
    private fun character(vararg stats: StatValueSnapshot): CharacterSnapshot =
        CharacterSnapshot(
            id = 10000002,
            name = "Test",
            level = 90,
            rarity = 5,
            element = "Wind",
            friendshipLevel = 10,
            activeConstellationCount = 0,
            selectedProperties = stats.toList()
        )

    private fun stat(base: String?, add: String?, final: String?) =
        StatValueSnapshot(2000, "ATK", base, add, final, null, null)

    @Test fun exactArithmeticIsReportedAsMatch() {
        val report = CharacterStatReconciler.reconcile(listOf(character(stat("100", "20", "120"))))
        val result = report.characters.single().results.single()
        assertEquals(StatReconciliationStatus.MATCH, result.status)
        assertEquals("120", result.expectedFinal)
        assertEquals("0", result.difference)
        assertEquals(1, report.matchCount)
    }

    @Test fun finalValueWithinItsDisplayedRoundingPrecisionIsTolerated() {
        val result = CharacterStatReconciler.reconcile(
            listOf(character(stat("100", "20.04", "120.0")))
        ).characters.single().results.single()
        assertEquals(StatReconciliationStatus.WITHIN_ROUNDING_TOLERANCE, result.status)
        assertEquals("-0.04", result.difference)
        assertEquals("0.555", result.roundingTolerance)
    }

    @Test fun independentlyRoundedIntegerInputsAreWithinCombinedTolerance() {
        val result = CharacterStatReconciler.reconcile(
            listOf(character(stat("747", "1", "747")))
        ).characters.single().results.single()

        assertEquals(StatReconciliationStatus.WITHIN_ROUNDING_TOLERANCE, result.status)
        assertEquals("-1", result.difference)
        assertEquals("1.5", result.roundingTolerance)
    }

    @Test fun discrepancyBeyondCombinedInputPrecisionIsMismatch() {
        val report = CharacterStatReconciler.reconcile(listOf(character(stat("100", "20", "122.2"))))
        val result = report.characters.single().results.single()
        assertEquals(StatReconciliationStatus.MISMATCH, result.status)
        assertEquals("2.2", result.difference)
        assertEquals("1.05", result.roundingTolerance)
        assertEquals(1, report.mismatchCount)
    }

    @Test fun negativeAdditiveValuesAreReconciledCorrectly() {
        val result = CharacterStatReconciler.reconcile(
            listOf(character(stat("100", "-20", "80")))
        ).characters.single().results.single()
        assertEquals(StatReconciliationStatus.MATCH, result.status)
    }

    @Test fun percentageValuesAreComparedWithoutDroppingTheirUnit() {
        val result = CharacterStatReconciler.reconcile(
            listOf(character(stat("46.6%", "20.0%", "66.6%")))
        ).characters.single().results.single()
        assertEquals(StatReconciliationStatus.MATCH, result.status)
    }

    @Test fun inconsistentUnitsAreNotChecked() {
        val result = CharacterStatReconciler.reconcile(
            listOf(character(stat("100", "20%", "120")))
        ).characters.single().results.single()
        assertEquals(StatReconciliationStatus.NOT_CHECKED, result.status)
        assertTrue(result.reason!!.contains("inconsistent units"))
    }

    @Test fun missingOrMalformedValuesAreNotCheckedRatherThanTreatedAsZero() {
        val report = CharacterStatReconciler.reconcile(listOf(character(
            stat(null, "20", "120"),
            stat("100", "invalid", "120"),
            stat("100", "20", null)
        )))
        assertEquals(3, report.notCheckedCount)
        assertEquals(0, report.checkedCount)
    }

    @Test fun zeroValuesRemainValidAndSourceStringsArePreserved() {
        val source = stat("0.00", "0.00", "0.00")
        val result = CharacterStatReconciler.reconcile(listOf(character(source)))
            .characters.single().results.single()
        assertEquals(StatReconciliationStatus.MATCH, result.status)
        assertEquals("0.00", result.base)
        assertEquals("0.00", result.add)
        assertEquals("0.00", result.final)
    }

    @Test fun weaponPropertiesAreAlsoReconciled() {
        val weapon = WeaponSnapshot(
            id = 1, name = "Weapon", icon = null, type = 1, rarity = 5,
            level = 90, ascensionLevel = 6, typeName = null, description = null,
            refinementLevel = 1,
            mainProperty = stat("100", "20", "120"),
            subProperty = stat("10", "5", "17")
        )
        val result = CharacterStatReconciler.reconcile(listOf(
            character().copy(weapon = DataValue.Available(weapon, null))
        )).characters.single()
        assertEquals(2, result.results.size)
        assertEquals(StatReconciliationStatus.MATCH, result.results[0].status)
        assertEquals(StatReconciliationStatus.MISMATCH, result.results[1].status)
        assertEquals("weapon.subProperty", result.results[1].fieldPath)
    }

    @Test fun staleWeaponDataIsStillReconciledFromItsLastKnownValues() {
        val weapon = WeaponSnapshot(
            1, "Weapon", null, 1, 5, 90, 6, null, null, 1,
            stat("100", "20", "120"), null
        )
        val result = CharacterStatReconciler.reconcile(listOf(character().copy(
            weapon = DataValue.Stale(weapon, java.time.Instant.parse("2026-11-25T00:00:00Z"))
        ))).characters.single()
        assertEquals(StatReconciliationStatus.MATCH, result.results.single().status)
    }
}
