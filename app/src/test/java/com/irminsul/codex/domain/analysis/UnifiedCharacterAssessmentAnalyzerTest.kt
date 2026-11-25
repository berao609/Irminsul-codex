package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class UnifiedCharacterAssessmentAnalyzerTest {
    private fun char(id: Int, complete: Boolean = true) = CharacterSnapshot(
        id=id, name="Character$id", level=if(complete) 90 else null, rarity=if(complete) 5 else null,
        element="Pyro", friendshipLevel=10, activeConstellationCount=0,
        weapon=DataValue.Available(WeaponSnapshot(1,"W",null,1,5,90,6,null,null,1,null,null),null),
        artifacts=DataValue.Available(if(complete) List(5){i->ArtifactSnapshot(i,"A",null,i+1,null,5,20,null,null,emptyList())} else emptyList(),null),
        selectedProperties=listOf(StatValueSnapshot(1,"ATK","100","20","120",null,null))
    )
    private fun build(vararg insights: CharacterBuildInsight) = CharacterBuildAnalysisReport(
        CharacterBuildAnalysisAvailability.AVAILABLE, 1, insights.toList()
    )
    private fun stats(vararg insights: CharacterStatInsight) = CharacterStatAnalysisReport(
        CharacterStatAnalysisAvailability.AVAILABLE, 1, insights = insights.toList()
    )
    private fun buildFinding(id:Int,severity:CharacterBuildInsightSeverity) = CharacterBuildInsight(
        id,"Character$id","test",severity,"field","issue","action",false
    )
    private fun statFinding(id:Int) = CharacterStatInsight(
        id,"Character$id","stats[0]",1,"ATK","100","20","120.2","120","0.2","0.05",
        "Explanation","Action",false
    )

    @Test fun combinesBuildErrorsAndStatMismatchesAndOrdersAttentionFirst() {
        val result=UnifiedCharacterAssessmentAnalyzer.assess(
            build(buildFinding(1,CharacterBuildInsightSeverity.ERROR),buildFinding(2,CharacterBuildInsightSeverity.WARNING)),
            stats(statFinding(2)),DataValue.Available(listOf(char(1),char(2)),null)
        )
        assertEquals(2,result.assessedCharacterCount)
        assertEquals(2,result.needsAttentionCount)
        assertEquals(0,result.reviewCount)
        assertEquals(listOf(1,2),result.assessments.map{it.characterId})
        assertEquals(CharacterAttentionStatus.NEEDS_ATTENTION,result.assessments[0].attentionStatus)
        assertEquals(CharacterAttentionStatus.NEEDS_ATTENTION,result.assessments[1].attentionStatus)
    }

    @Test fun warningOnlyCharacterIsReview() {
        val result=UnifiedCharacterAssessmentAnalyzer.assess(
            build(buildFinding(3,CharacterBuildInsightSeverity.WARNING)),stats(),DataValue.Available(listOf(char(3)),null)
        )
        assertEquals(CharacterAttentionStatus.REVIEW,result.assessments.single().attentionStatus)
        assertEquals(1,result.reviewCount)
    }

    @Test fun incompleteCharacterIsReviewAndCompletenessEvidenceIsExplicit() {
        val result=UnifiedCharacterAssessmentAnalyzer.assess(build(),stats(),DataValue.Available(listOf(char(9,false)),null))
        val assessment=result.assessments.single()
        assertEquals(CharacterAttentionStatus.REVIEW,assessment.attentionStatus)
        assertEquals(CharacterCompletenessLevel.INSUFFICIENT,assessment.completeness.level)
        assertTrue(assessment.completeness.missingOrUnverifiedComponents.contains("Character level"))
        assertTrue(assessment.completeness.missingOrUnverifiedComponents.contains("Artifact collection has fewer than five entries"))
    }

    @Test fun fullyPopulatedCharacterCanBeClear() {
        val result=UnifiedCharacterAssessmentAnalyzer.assess(build(),stats(),DataValue.Available(listOf(char(7)),null))
        assertEquals(CharacterAttentionStatus.CLEAR,result.assessments.single().attentionStatus)
        assertEquals(CharacterCompletenessLevel.COMPLETE,result.assessments.single().completeness.level)
        assertEquals(1,result.clearCount)
    }

    @Test fun staleDataIsPreserved() {
        val result=UnifiedCharacterAssessmentAnalyzer.assess(
            build(),stats(),DataValue.Stale(listOf(char(8)),java.time.Instant.parse("2026-11-25T00:00:00Z"))
        )
        assertEquals(UnifiedCharacterAssessmentAvailability.STALE,result.availability)
        assertTrue(result.assessments.single().basedOnStaleData)
    }

    @Test fun missingUnavailableAndFailedInputsRemainDistinctWhenNoUsableData() {
        val missing=UnifiedCharacterAssessmentAnalyzer.assess(
            CharacterBuildAnalysisReport(CharacterBuildAnalysisAvailability.MISSING),
            CharacterStatAnalysisReport(CharacterStatAnalysisAvailability.MISSING),DataValue.Missing)
        val unavailable=UnifiedCharacterAssessmentAnalyzer.assess(
            CharacterBuildAnalysisReport(CharacterBuildAnalysisAvailability.UNAVAILABLE),
            CharacterStatAnalysisReport(CharacterStatAnalysisAvailability.UNAVAILABLE),DataValue.Unavailable)
        val failed=UnifiedCharacterAssessmentAnalyzer.assess(
            CharacterBuildAnalysisReport(CharacterBuildAnalysisAvailability.FAILED,failureReason="bad"),
            CharacterStatAnalysisReport(CharacterStatAnalysisAvailability.FAILED,failureReason="bad"),DataValue.Failed("bad"))
        assertEquals(UnifiedCharacterAssessmentAvailability.MISSING,missing.availability)
        assertEquals(UnifiedCharacterAssessmentAvailability.UNAVAILABLE,unavailable.availability)
        assertEquals(UnifiedCharacterAssessmentAvailability.FAILED,failed.availability)
    }
}
