package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class AccountIntelligenceEngineTest {
    private val now = Instant.parse("2026-11-25T12:00:00Z")

    private fun snapshot(
        characters: DataValue<List<CharacterSnapshot>>,
        build: DataValue<CharacterBuildValidationReport>,
        stats: DataValue<CharacterStatReconciliationReport>
    ) = AccountSnapshot(
        identity = AccountIdentity("700000001", "os_asia"),
        metadata = SnapshotMetadata(now, 1),
        overallStatus = SectionStatus.SUCCESS,
        dailyNote = DataValue.Missing,
        characters = characters,
        accountCard = DataValue.Missing,
        characterRosterSource = DataValue.Missing,
        characterDetailSource = DataValue.Missing,
        profileAndExploration = DataValue.Missing,
        spiralAbyssCurrent = DataValue.Missing,
        spiralAbyssPrevious = DataValue.Missing,
        imaginariumTheater = DataValue.Missing,
        sections = emptyMap(),
        characterBuildValidation = build,
        characterStatReconciliation = stats,
        sectionCoverage = AccountSnapshotCoverageReport(emptyList()),
        sectionFreshness = AccountSnapshotFreshnessReport(now, emptyList())
    )

    @Test
    fun analyze_composesAllDeterministicReportsFromOneSnapshot() {
        val character = CharacterSnapshot(
            id = 100,
            name = "Example Character",
            level = 90,
            rarity = 5,
            element = "Pyro",
            friendshipLevel = 10,
            activeConstellationCount = 0,
            weapon = DataValue.Available(
                WeaponSnapshot(
                    id = 1000,
                    name = "Example Weapon",
                    icon = null,
                    type = null,
                    rarity = 5,
                    level = 90,
                    ascensionLevel = 6,
                    typeName = null,
                    description = null,
                    refinementLevel = 1,
                    mainProperty = null,
                    subProperty = null
                ),
                now
            ),
            artifacts = DataValue.Available(emptyList(), now)
        )
        val source = snapshot(
            characters = DataValue.Available(listOf(character), now),
            build = DataValue.Available(
                CharacterBuildValidationReport(
                    listOf(
                        CharacterBuildValidation(
                            characterId = 100,
                            issues = listOf(
                                BuildValidationIssue(
                                    code = "weapon.level.missing",
                                    severity = BuildIssueSeverity.ERROR,
                                    fieldPath = "weapon.level",
                                    message = "Weapon level is below the expected range."
                                )
                            )
                        )
                    ),
                ),
                now
            ),
            stats = DataValue.Available(CharacterStatReconciliationReport(emptyList()), now)
        )

        val result = AccountIntelligenceEngine.analyze(
            source,
            CharacterDevelopmentGoal.STAT_INTEGRITY
        )

        assertEquals(UnifiedCharacterAssessmentAvailability.AVAILABLE, result.characterAssessment.availability)
        assertEquals(1, result.characterAssessment.assessedCharacterCount)
        assertEquals(1, result.characterAssessment.needsAttentionCount)
        assertEquals(1, result.goalAwareDevelopment.recommendationCount)
        assertEquals(CharacterDevelopmentGoal.STAT_INTEGRITY, result.selectedGoal)
        assertEquals(
            CharacterDevelopmentRecommendationType.BUILD_ERRORS,
            result.goalAwareDevelopment.recommendations.single().recommendation.type
        )
        assertEquals(AccountGapAnalysisAvailability.AVAILABLE, result.accountGaps.availability)
        assertEquals(0, result.accountSectionCount)
        assertFalse(result.hasUnverifiedSources)
    }

    @Test
    fun analyze_preservesMissingCharacterSourcesInsteadOfTreatingThemAsEmptySuccess() {
        val source = snapshot(
            characters = DataValue.Missing,
            build = DataValue.Missing,
            stats = DataValue.Missing
        )

        val result = AccountIntelligenceEngine.analyze(
            source,
            CharacterDevelopmentGoal.BALANCED_DEVELOPMENT
        )

        assertEquals(UnifiedCharacterAssessmentAvailability.MISSING, result.characterAssessment.availability)
        assertEquals(CharacterDevelopmentRecommendationAvailability.MISSING, result.goalAwareDevelopment.availability)
        assertEquals(AccountGapAnalysisAvailability.MISSING, result.accountGaps.availability)
        assertTrue(result.hasUnverifiedSources)
        assertFalse(result.hasUnassessedFreshness)
    }

    @Test
    fun analyze_keepsFailedAnalysisAvailabilityAndFailureReason() {
        val source = snapshot(
            characters = DataValue.Available(listOf(CharacterSnapshot(id = 100, name = "Example Character", level = 90, rarity = 5, element = "Pyro", friendshipLevel = 10, activeConstellationCount = 0)), now),
            build = DataValue.Failed("Build validation source failed."),
            stats = DataValue.Available(CharacterStatReconciliationReport(emptyList()), now)
        )

        val result = AccountIntelligenceEngine.analyze(
            source,
            CharacterDevelopmentGoal.BUILD_RELIABILITY
        )

        assertEquals(UnifiedCharacterAssessmentAvailability.AVAILABLE, result.characterAssessment.availability)
        assertEquals(1, result.characterAssessment.assessments.size)
        assertTrue(result.characterAssessment.assessments.single().completeness.unverifiedAnalyses.contains("Build validation failed"))
        assertEquals(CharacterDevelopmentRecommendationAvailability.AVAILABLE, result.goalAwareDevelopment.availability)
        assertEquals(CharacterDevelopmentGoal.BUILD_RELIABILITY, result.selectedGoal)
        assertFalse(result.hasUnverifiedSources)
    }
}
