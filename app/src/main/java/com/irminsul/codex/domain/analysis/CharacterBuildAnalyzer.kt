package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*

/**
 * Converts deterministic character-build validation results into stable,
 * explainable findings suitable for later presentation in the app.
 *
 * This analyzer does not assign a build score, infer team roles, compare against
 * community builds, or modify source data.
 */
object CharacterBuildAnalyzer {
    fun analyze(snapshot: AccountSnapshot): CharacterBuildAnalysisReport =
        analyze(snapshot.characterBuildValidation, snapshot.characters)

    fun analyze(
        validation: DataValue<CharacterBuildValidationReport>,
        characters: DataValue<List<CharacterSnapshot>> = DataValue.Missing
    ): CharacterBuildAnalysisReport {
        val names = when (characters) {
            is DataValue.Available -> characters.value.associate { it.id to it.name }
            is DataValue.Stale -> characters.value.associate { it.id to it.name }
            else -> emptyMap()
        }

        val source = when (validation) {
            is DataValue.Available -> validation.value to false
            is DataValue.Stale -> validation.value to true
            DataValue.Missing -> return CharacterBuildAnalysisReport(
                availability = CharacterBuildAnalysisAvailability.MISSING
            )
            DataValue.Unavailable -> return CharacterBuildAnalysisReport(
                availability = CharacterBuildAnalysisAvailability.UNAVAILABLE
            )
            is DataValue.Failed -> return CharacterBuildAnalysisReport(
                availability = CharacterBuildAnalysisAvailability.FAILED,
                failureReason = validation.reason
            )
        }

        val (report, isStale) = source
        val insights = report.characters
            .flatMap { character ->
                character.issues.map { issue ->
                    CharacterBuildInsight(
                        characterId = character.characterId,
                        characterName = names[character.characterId],
                        code = issue.code,
                        severity = when (issue.severity) {
                            BuildIssueSeverity.ERROR -> CharacterBuildInsightSeverity.ERROR
                            BuildIssueSeverity.WARNING -> CharacterBuildInsightSeverity.WARNING
                        },
                        fieldPath = issue.fieldPath,
                        message = issue.message,
                        recommendedAction = recommendedAction(issue.code),
                        basedOnStaleData = isStale
                    )
                }
            }
            .sortedWith(
                compareBy<CharacterBuildInsight> { severityOrder(it.severity) }
                    .thenBy { it.characterId }
                    .thenBy { it.fieldPath }
                    .thenBy { it.code }
            )

        return CharacterBuildAnalysisReport(
            availability = if (isStale) {
                CharacterBuildAnalysisAvailability.STALE
            } else {
                CharacterBuildAnalysisAvailability.AVAILABLE
            },
            analyzedCharacterCount = report.characters.size,
            insights = insights
        )
    }

    private fun severityOrder(severity: CharacterBuildInsightSeverity): Int =
        when (severity) {
            CharacterBuildInsightSeverity.ERROR -> 0
            CharacterBuildInsightSeverity.WARNING -> 1
        }

    private fun recommendedAction(code: String): String = when {
        code.endsWith(".missing") || code.endsWith(".unavailable") ->
            "Refresh the account data and confirm this item is visible in the in-game profile."
        code.endsWith(".failed") ->
            "Refresh the account data. If the problem persists, review the source response."
        code.contains("duplicate") || code.contains("invalid") || code.contains("out_of_range") || code.contains("conflict") ->
            "Inspect this value or equipment entry in-game and refresh the account data before relying on it."
        else ->
            "Review this field in-game and refresh the account data if the reported value appears incorrect."
    }
}
