package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CharacterBuildAnalyzerTest {
    private fun issue(
        code: String,
        severity: BuildIssueSeverity,
        path: String
    ) = BuildValidationIssue(code, severity, path, "Issue: $code")

    private fun validation() = CharacterBuildValidationReport(
        listOf(
            CharacterBuildValidation(
                characterId = 20,
                issues = listOf(
                    issue("weapon.missing", BuildIssueSeverity.WARNING, "character[20].weapon")
                )
            ),
            CharacterBuildValidation(
                characterId = 10,
                issues = listOf(
                    issue("artifact.slot.invalid", BuildIssueSeverity.ERROR, "character[10].artifacts[0].slot"),
                    issue("character.level.missing", BuildIssueSeverity.WARNING, "character[10].level")
                )
            )
        )
    )

    @Test
    fun convertsValidationIssuesIntoStablePrioritizedFindings() {
        val report = CharacterBuildAnalyzer.analyze(
            DataValue.Available(validation(), Instant.parse("2026-11-25T00:00:00Z")),
            DataValue.Available(
                listOf(
                    CharacterSnapshot(10, "Amber", 1, 4, "Pyro", null, null),
                    CharacterSnapshot(20, "Kaeya", 1, 4, "Cryo", null, null)
                ),
                null
            )
        )

        assertEquals(CharacterBuildAnalysisAvailability.AVAILABLE, report.availability)
        assertEquals(2, report.analyzedCharacterCount)
        assertEquals(1, report.errorCount)
        assertEquals(2, report.warningCount)
        assertEquals(
            listOf("artifact.slot.invalid", "character.level.missing", "weapon.missing"),
            report.insights.map { it.code }
        )
        assertEquals("Amber", report.insights.first().characterName)
        assertTrue(report.insights.all { it.recommendedAction.isNotBlank() })
        assertFalse(report.insights.any { it.basedOnStaleData })
    }

    @Test
    fun marksFindingsDerivedFromStaleValidationAsStale() {
        val report = CharacterBuildAnalyzer.analyze(
            DataValue.Stale(validation(), Instant.parse("2026-11-20T00:00:00Z"))
        )

        assertEquals(CharacterBuildAnalysisAvailability.STALE, report.availability)
        assertTrue(report.insights.isNotEmpty())
        assertTrue(report.insights.all { it.basedOnStaleData })
    }

    @Test
    fun preservesMissingUnavailableAndFailedInputStates() {
        assertEquals(
            CharacterBuildAnalysisAvailability.MISSING,
            CharacterBuildAnalyzer.analyze(DataValue.Missing).availability
        )
        assertEquals(
            CharacterBuildAnalysisAvailability.UNAVAILABLE,
            CharacterBuildAnalyzer.analyze(DataValue.Unavailable).availability
        )
        val failed = CharacterBuildAnalyzer.analyze(DataValue.Failed("validation failed"))
        assertEquals(CharacterBuildAnalysisAvailability.FAILED, failed.availability)
        assertEquals("validation failed", failed.failureReason)
        assertNull(failed.analyzedCharacterCount)
        assertTrue(failed.insights.isEmpty())
    }

    @Test
    fun availableEmptyValidationIsNotConfusedWithMissingData() {
        val report = CharacterBuildAnalyzer.analyze(
            DataValue.Available(CharacterBuildValidationReport(emptyList()), null)
        )

        assertEquals(CharacterBuildAnalysisAvailability.AVAILABLE, report.availability)
        assertEquals(0, report.analyzedCharacterCount)
        assertFalse(report.hasFindings)
    }
}
