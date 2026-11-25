package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CharacterBuildAccountSummaryAnalyzerTest {
    private fun finding(
        characterId: Int,
        name: String?,
        code: String,
        severity: CharacterBuildInsightSeverity,
        path: String = "character[$characterId].$code",
        stale: Boolean = false
    ) = CharacterBuildInsight(
        characterId = characterId,
        characterName = name,
        code = code,
        severity = severity,
        fieldPath = path,
        message = "Finding: $code",
        recommendedAction = "Review $path",
        basedOnStaleData = stale
    )

    @Test
    fun groupsFindingsAndOrdersCharactersByErrorsWarningsThenId() {
        val report = CharacterBuildAnalysisReport(
            availability = CharacterBuildAnalysisAvailability.AVAILABLE,
            analyzedCharacterCount = 5,
            insights = listOf(
                finding(30, "Lisa", "weapon.missing", CharacterBuildInsightSeverity.WARNING),
                finding(20, "Kaeya", "artifact.invalid", CharacterBuildInsightSeverity.ERROR),
                finding(10, "Amber", "weapon.missing", CharacterBuildInsightSeverity.WARNING),
                finding(20, "Kaeya", "artifact.duplicate", CharacterBuildInsightSeverity.ERROR),
                finding(10, "Amber", "character.level.missing", CharacterBuildInsightSeverity.WARNING),
                finding(40, "Barbara", "stat.invalid", CharacterBuildInsightSeverity.ERROR)
            )
        )

        val summary = CharacterBuildAccountSummaryAnalyzer.summarize(report)

        assertEquals(CharacterBuildAnalysisAvailability.AVAILABLE, summary.availability)
        assertEquals(5, summary.analyzedCharacterCount)
        assertEquals(4, summary.affectedCharacterCount)
        assertEquals(3, summary.errorCount)
        assertEquals(3, summary.warningCount)
        assertEquals(
            listOf(20, 40, 10, 30),
            summary.prioritizedCharacters.map { it.characterId }
        )
        assertEquals(CharacterBuildAttentionLevel.NEEDS_ATTENTION, summary.prioritizedCharacters[0].attentionLevel)
        assertEquals(2, summary.prioritizedCharacters[0].errorCount)
        assertEquals(0, summary.prioritizedCharacters[0].warningCount)
        assertEquals(CharacterBuildAttentionLevel.REVIEW, summary.prioritizedCharacters[2].attentionLevel)
        assertEquals(2, summary.prioritizedCharacters[2].warningCount)
        assertEquals(
            listOf("artifact.duplicate", "artifact.invalid"),
            summary.prioritizedCharacters[0].findings.map { it.code }
        )
        assertTrue(summary.hasFindings)
    }

    @Test
    fun availableEmptyReportProducesValidEmptySummary() {
        val summary = CharacterBuildAccountSummaryAnalyzer.summarize(
            CharacterBuildAnalysisReport(
                availability = CharacterBuildAnalysisAvailability.AVAILABLE,
                analyzedCharacterCount = 7
            )
        )

        assertEquals(CharacterBuildAnalysisAvailability.AVAILABLE, summary.availability)
        assertEquals(7, summary.analyzedCharacterCount)
        assertEquals(0, summary.affectedCharacterCount)
        assertEquals(0, summary.errorCount)
        assertEquals(0, summary.warningCount)
        assertTrue(summary.prioritizedCharacters.isEmpty())
        assertFalse(summary.hasFindings)
    }

    @Test
    fun preservesStaleStateAndUnderlyingFindingMarkers() {
        val summary = CharacterBuildAccountSummaryAnalyzer.summarize(
            CharacterBuildAnalysisReport(
                availability = CharacterBuildAnalysisAvailability.STALE,
                analyzedCharacterCount = 1,
                insights = listOf(
                    finding(
                        8,
                        "Noelle",
                        "weapon.stale",
                        CharacterBuildInsightSeverity.WARNING,
                        stale = true
                    )
                )
            )
        )

        assertEquals(CharacterBuildAnalysisAvailability.STALE, summary.availability)
        assertTrue(summary.prioritizedCharacters.single().findings.single().basedOnStaleData)
    }

    @Test
    fun preservesMissingUnavailableAndFailedStates() {
        val missing = CharacterBuildAccountSummaryAnalyzer.summarize(
            CharacterBuildAnalysisReport(CharacterBuildAnalysisAvailability.MISSING)
        )
        val unavailable = CharacterBuildAccountSummaryAnalyzer.summarize(
            CharacterBuildAnalysisReport(CharacterBuildAnalysisAvailability.UNAVAILABLE)
        )
        val failed = CharacterBuildAccountSummaryAnalyzer.summarize(
            CharacterBuildAnalysisReport(
                CharacterBuildAnalysisAvailability.FAILED,
                failureReason = "validation unavailable"
            )
        )

        assertEquals(CharacterBuildAnalysisAvailability.MISSING, missing.availability)
        assertEquals(CharacterBuildAnalysisAvailability.UNAVAILABLE, unavailable.availability)
        assertEquals(CharacterBuildAnalysisAvailability.FAILED, failed.availability)
        assertEquals("validation unavailable", failed.failureReason)
        assertNull(failed.analyzedCharacterCount)
        assertTrue(failed.prioritizedCharacters.isEmpty())
    }
}
