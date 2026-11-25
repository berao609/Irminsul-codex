package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import com.irminsul.codex.domain.validation.CharacterStatReconciler
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CharacterStatAnalyzerTest {
    private fun character(
        id: Int = 10000002,
        name: String? = "Test",
        vararg stats: StatValueSnapshot
    ) = CharacterSnapshot(
        id = id,
        name = name,
        level = 90,
        rarity = 5,
        element = "Wind",
        friendshipLevel = 10,
        activeConstellationCount = 0,
        selectedProperties = stats.toList()
    )

    private fun stat(base: String?, add: String?, final: String?) =
        StatValueSnapshot(2000, "ATK", base, add, final, null, null)

    private fun report(vararg characters: CharacterSnapshot) =
        CharacterStatReconciler.reconcile(characters.toList())

    @Test fun mismatchBecomesExplainableFindingWithOriginalEvidence() {
        val character = character(stats = *arrayOf(stat("100", "20", "120.2")))
        val reconciliation = report(character)
        val result = CharacterStatAnalyzer.analyze(
            DataValue.Available(reconciliation, null),
            DataValue.Available(listOf(character), null)
        )
        val finding = result.insights.single()

        assertEquals(CharacterStatAnalysisAvailability.AVAILABLE, result.availability)
        assertEquals(1, result.mismatchCount)
        assertEquals(1, result.insights.size)
        assertEquals(character.id, finding.characterId)
        assertEquals("Test", finding.characterName)
        assertEquals("selectedProperties[0]", finding.fieldPath)
        assertEquals("100", finding.base)
        assertEquals("20", finding.additive)
        assertEquals("120.2", finding.reportedFinal)
        assertEquals("120", finding.expectedFinal)
        assertEquals("0.2", finding.difference)
        assertEquals("0.05", finding.roundingTolerance)
        assertTrue(finding.explanation.contains("100 + 20 = 120"))
        assertTrue(finding.recommendedAction.contains("in-game"))
        assertFalse(finding.basedOnStaleData)
    }

    @Test fun matchesAndWithinToleranceDoNotBecomeFindings() {
        val character = character(
            stats = *arrayOf(
                stat("100", "20", "120"),
                stat("100", "20.04", "120.0")
            )
        )
        val result = CharacterStatAnalyzer.analyze(
            DataValue.Available(report(character), null),
            DataValue.Available(listOf(character), null)
        )

        assertEquals(1, result.matchCount)
        assertEquals(1, result.withinToleranceCount)
        assertEquals(0, result.mismatchCount)
        assertTrue(result.insights.isEmpty())
    }

    @Test fun notCheckedValuesRemainCountedButAreNotPresentedAsMismatches() {
        val character = character(stats = *arrayOf(stat(null, "20", "120")))
        val result = CharacterStatAnalyzer.analyze(
            DataValue.Available(report(character), null),
            DataValue.Available(listOf(character), null)
        )

        assertEquals(1, result.notCheckedCount)
        assertEquals(0, result.mismatchCount)
        assertTrue(result.insights.isEmpty())
    }

    @Test fun staleReconciliationAndStaleCharacterDataAreMarked() {
        val character = character(stats = *arrayOf(stat("100", "20", "120.2")))
        val reconciliation = report(character)
        val timestamp = Instant.parse("2026-11-25T00:00:00Z")

        val staleReconciliation = CharacterStatAnalyzer.analyze(
            DataValue.Stale(reconciliation, timestamp),
            DataValue.Available(listOf(character), null)
        )
        assertEquals(CharacterStatAnalysisAvailability.STALE, staleReconciliation.availability)
        assertTrue(staleReconciliation.insights.single().basedOnStaleData)

        val staleCharacters = CharacterStatAnalyzer.analyze(
            DataValue.Available(reconciliation, null),
            DataValue.Stale(listOf(character), timestamp)
        )
        assertEquals(CharacterStatAnalysisAvailability.STALE, staleCharacters.availability)
        assertTrue(staleCharacters.insights.single().basedOnStaleData)
    }

    @Test fun missingUnavailableAndFailedInputsRemainDistinct() {
        assertEquals(
            CharacterStatAnalysisAvailability.MISSING,
            CharacterStatAnalyzer.analyze(DataValue.Missing).availability
        )
        assertEquals(
            CharacterStatAnalysisAvailability.UNAVAILABLE,
            CharacterStatAnalyzer.analyze(DataValue.Unavailable).availability
        )
        val failed = CharacterStatAnalyzer.analyze(DataValue.Failed("source failed"))
        assertEquals(CharacterStatAnalysisAvailability.FAILED, failed.availability)
        assertEquals("source failed", failed.failureReason)
    }

    @Test fun availableEmptyReportIsNotMissing() {
        val result = CharacterStatAnalyzer.analyze(
            DataValue.Available(CharacterStatReconciliationReport(emptyList()), null),
            DataValue.Available(emptyList(), null)
        )
        assertEquals(CharacterStatAnalysisAvailability.AVAILABLE, result.availability)
        assertEquals(0, result.analyzedCharacterCount)
        assertFalse(result.hasFindings)
    }

    @Test fun findingsHaveStableCharacterAndFieldOrdering() {
        val first = character(id = 200, name = "Second", stats = *arrayOf(
            stat("100", "20", "120.2"),
            stat("100", "20", "120.3")
        ))
        val second = character(id = 100, name = "First", stats = *arrayOf(
            stat("100", "20", "120.2")
        ))
        val result = CharacterStatAnalyzer.analyze(
            DataValue.Available(report(first, second), null),
            DataValue.Available(listOf(first, second), null)
        )

        assertEquals(listOf(100, 200, 200), result.insights.map { it.characterId })
        assertEquals(
            listOf("selectedProperties[0]", "selectedProperties[0]", "selectedProperties[1]"),
            result.insights.map { it.fieldPath }
        )
    }
}
