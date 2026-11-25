package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.AccountIntelligenceOverview
import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.model.CharacterDevelopmentGoal

/**
 * Runs the existing deterministic account-analysis stages from one normalized snapshot.
 *
 * This is an orchestration boundary only: each established analyzer remains authoritative
 * for its own findings, availability semantics, and ordering rules.
 */
object AccountIntelligenceEngine {
    fun analyze(
        snapshot: AccountSnapshot,
        goal: CharacterDevelopmentGoal
    ): AccountIntelligenceOverview {
        val buildAnalysis = CharacterBuildAnalyzer.analyze(snapshot)
        val statAnalysis = CharacterStatAnalyzer.analyze(snapshot)

        val characterAssessment = UnifiedCharacterAssessmentAnalyzer.assess(
            build = buildAnalysis,
            stats = statAnalysis,
            characters = snapshot.characters
        )
        val recommendations = CharacterDevelopmentRecommendationAnalyzer.recommend(
            characterAssessment
        )
        val goalAwareDevelopment = GoalAwareCharacterDevelopmentAnalyzer.prioritize(
            report = recommendations,
            goal = goal
        )
        val accountGaps = AccountGapAnalyzer.analyze(
            characters = snapshot.characters,
            integrity = snapshot.characterCollectionIntegrity
        )
        val dataConfidence = AccountDataConfidenceAnalyzer.summarize(
            coverage = snapshot.sectionCoverage,
            freshness = snapshot.sectionFreshness
        )

        return AccountIntelligenceOverviewAnalyzer.compose(
            characterAssessment = characterAssessment,
            goalAwareDevelopment = goalAwareDevelopment,
            accountGaps = accountGaps,
            dataConfidence = dataConfidence
        )
    }
}
