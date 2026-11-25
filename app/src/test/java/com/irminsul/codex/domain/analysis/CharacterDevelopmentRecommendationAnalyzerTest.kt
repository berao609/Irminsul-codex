package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CharacterDevelopmentRecommendationAnalyzerTest {
    private fun assessment(
        id: Int,
        status: CharacterAttentionStatus = CharacterAttentionStatus.CLEAR,
        build: List<CharacterBuildInsight> = emptyList(),
        stats: List<CharacterStatInsight> = emptyList(),
        missing: List<String> = emptyList(),
        unverified: List<String> = emptyList(),
        stale: Boolean = false
    ) = UnifiedCharacterAssessment(
        characterId = id,
        characterName = "Character$id",
        attentionStatus = status,
        buildErrors = build.count { it.severity == CharacterBuildInsightSeverity.ERROR },
        buildWarnings = build.count { it.severity == CharacterBuildInsightSeverity.WARNING },
        statMismatches = stats.size,
        completeness = CharacterCompletenessEvidence(
            missingOrUnverifiedComponents = missing,
            level = if (missing.isEmpty()) CharacterCompletenessLevel.COMPLETE else CharacterCompletenessLevel.PARTIAL,
            unverifiedAnalyses = unverified
        ),
        buildFindings = build,
        statFindings = stats,
        basedOnStaleData = stale
    )

    private fun buildFinding(id: Int, severity: CharacterBuildInsightSeverity) = CharacterBuildInsight(
        characterId = id, characterName = "Character$id", code = "test.code",
        severity = severity, fieldPath = "character.field", message = "Detected issue",
        recommendedAction = "Verify the value", basedOnStaleData = false
    )

    private fun statFinding(id: Int) = CharacterStatInsight(
        characterId = id, characterName = "Character$id", fieldPath = "stats[0]",
        propertyType = 1, statName = "ATK", base = "100", additive = "20",
        reportedFinal = "121", expectedFinal = "120", difference = "1",
        roundingTolerance = "0.05", explanation = "Arithmetic mismatch",
        recommendedAction = "Verify stat", basedOnStaleData = false
    )

    private fun report(vararg assessments: UnifiedCharacterAssessment) =
        UnifiedCharacterAssessmentReport(
            availability = UnifiedCharacterAssessmentAvailability.AVAILABLE,
            assessedCharacterCount = assessments.size,
            assessments = assessments.toList()
        )

    @Test fun producesEvidenceBackedRecommendationsAndPriorityOrdering() {
        val a = assessment(
            id = 5,
            status = CharacterAttentionStatus.NEEDS_ATTENTION,
            build = listOf(buildFinding(5, CharacterBuildInsightSeverity.ERROR), buildFinding(5, CharacterBuildInsightSeverity.WARNING)),
            stats = listOf(statFinding(5)),
            missing = listOf("Artifact data missing"),
            unverified = listOf("Stat reconciliation unavailable")
        )
        val result = CharacterDevelopmentRecommendationAnalyzer.recommend(report(a))
        assertEquals(4, result.recommendationCount)
        assertEquals(1, result.affectedCharacterCount)
        assertEquals(
            listOf(
                CharacterDevelopmentRecommendationType.BUILD_ERRORS,
                CharacterDevelopmentRecommendationType.STAT_MISMATCHES,
                CharacterDevelopmentRecommendationType.BUILD_WARNINGS,
                CharacterDevelopmentRecommendationType.DATA_REVIEW
            ),
            result.recommendations.map { it.type }
        )
        assertEquals(listOf(
            CharacterDevelopmentRecommendationPriority.P1,
            CharacterDevelopmentRecommendationPriority.P2,
            CharacterDevelopmentRecommendationPriority.P3,
            CharacterDevelopmentRecommendationPriority.P4
        ), result.recommendations.map { it.priority })
        assertTrue(result.recommendations.first().evidence.any { it.contains("test.code") })
        assertTrue(result.recommendations[1].evidence.any { it.contains("expected 120") })
        assertTrue(result.recommendations.last().evidence.any { it.contains("Analysis not verified") })
        assertTrue(result.recommendations.all { it.recommendationId.startsWith("5-") })
    }

    @Test fun clearCharacterProducesNoUnnecessaryRecommendation() {
        val result = CharacterDevelopmentRecommendationAnalyzer.recommend(
            report(assessment(9))
        )
        assertTrue(result.recommendations.isEmpty())
        assertEquals(0, result.recommendationCount)
        assertEquals(0, result.affectedCharacterCount)
    }

    @Test fun incompleteDataCreatesReviewRecommendationWithoutBuildOrStatConclusions() {
        val result = CharacterDevelopmentRecommendationAnalyzer.recommend(
            report(assessment(2, status = CharacterAttentionStatus.REVIEW, missing = listOf("Weapon data missing")))
        )
        assertEquals(1, result.recommendationCount)
        assertEquals(CharacterDevelopmentRecommendationType.DATA_REVIEW, result.recommendations.single().type)
        assertEquals(CharacterDevelopmentRecommendationPriority.P4, result.recommendations.single().priority)
        assertTrue(result.recommendations.single().evidence.single().contains("Weapon data missing"))
    }

    @Test fun unverifiedAnalysisCreatesReviewActionEvenWhenCharacterDataIsComplete() {
        val result = CharacterDevelopmentRecommendationAnalyzer.recommend(
            report(assessment(3, status = CharacterAttentionStatus.REVIEW, unverified = listOf("Build validation missing")))
        )
        val rec = result.recommendations.single()
        assertEquals(CharacterDevelopmentRecommendationType.DATA_REVIEW, rec.type)
        assertTrue(rec.evidence.single().contains("Build validation missing"))
    }

    @Test fun staleReportMarksRecommendationsAsStaleAndKeepsOrderingDeterministic() {
        val stale = UnifiedCharacterAssessmentReport(
            availability = UnifiedCharacterAssessmentAvailability.STALE,
            assessments = listOf(
                assessment(10, status = CharacterAttentionStatus.REVIEW, missing = listOf("Artifacts"), stale = true),
                assessment(2, status = CharacterAttentionStatus.REVIEW, missing = listOf("Weapon"))
            )
        )
        val result = CharacterDevelopmentRecommendationAnalyzer.recommend(stale)
        assertTrue(result.recommendations.all { it.basedOnStaleData })
        assertEquals(listOf(2, 10), result.recommendations.map { it.characterId })
        assertEquals(CharacterDevelopmentRecommendationAvailability.STALE, result.availability)
    }

    @Test fun unusableAssessmentAvailabilityIsPreservedWithoutRecommendations() {
        val cases = listOf(
            UnifiedCharacterAssessmentAvailability.MISSING to CharacterDevelopmentRecommendationAvailability.MISSING,
            UnifiedCharacterAssessmentAvailability.UNAVAILABLE to CharacterDevelopmentRecommendationAvailability.UNAVAILABLE,
            UnifiedCharacterAssessmentAvailability.FAILED to CharacterDevelopmentRecommendationAvailability.FAILED
        )
        cases.forEach { (source, expected) ->
            val result = CharacterDevelopmentRecommendationAnalyzer.recommend(
                UnifiedCharacterAssessmentReport(availability = source, failureReason = "source issue")
            )
            assertEquals(expected, result.availability)
            assertEquals("source issue", result.failureReason)
            assertTrue(result.recommendations.isEmpty())
        }
    }

    @Test fun recommendationsAcrossCharactersAreSortedByPriorityThenCharacterId() {
        val result = CharacterDevelopmentRecommendationAnalyzer.recommend(
            report(
                assessment(8, status = CharacterAttentionStatus.REVIEW, missing = listOf("Weapon")),
                assessment(4, status = CharacterAttentionStatus.NEEDS_ATTENTION, build = listOf(buildFinding(4, CharacterBuildInsightSeverity.ERROR))),
                assessment(6, status = CharacterAttentionStatus.NEEDS_ATTENTION, stats = listOf(statFinding(6))),
                assessment(3, status = CharacterAttentionStatus.REVIEW, build = listOf(buildFinding(3, CharacterBuildInsightSeverity.WARNING)))
            )
        )
        assertEquals(
            listOf(
                CharacterDevelopmentRecommendationType.BUILD_ERRORS,
                CharacterDevelopmentRecommendationType.STAT_MISMATCHES,
                CharacterDevelopmentRecommendationType.BUILD_WARNINGS,
                CharacterDevelopmentRecommendationType.DATA_REVIEW
            ),
            result.recommendations.map { it.type }
        )
    }
}
