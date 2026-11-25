package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*

/**
 * Converts existing stat reconciliation mismatches into explainable findings.
 *
 * The reconciler remains the sole authority for arithmetic and rounding.
 * This analyzer does not reinterpret values, assign scores, or claim that a
 * mismatch proves an upstream game-data defect.
 */
object CharacterStatAnalyzer {
    fun analyze(snapshot: AccountSnapshot): CharacterStatAnalysisReport =
        analyze(snapshot.characterStatReconciliation, snapshot.characters)

    fun analyze(
        reconciliation: DataValue<CharacterStatReconciliationReport>,
        characters: DataValue<List<CharacterSnapshot>> = DataValue.Missing
    ): CharacterStatAnalysisReport {
        val source = when (reconciliation) {
            is DataValue.Available -> reconciliation.value to false
            is DataValue.Stale -> reconciliation.value to true
            DataValue.Missing -> return CharacterStatAnalysisReport(
                availability = CharacterStatAnalysisAvailability.MISSING
            )
            DataValue.Unavailable -> return CharacterStatAnalysisReport(
                availability = CharacterStatAnalysisAvailability.UNAVAILABLE
            )
            is DataValue.Failed -> return CharacterStatAnalysisReport(
                availability = CharacterStatAnalysisAvailability.FAILED,
                failureReason = reconciliation.reason
            )
        }

        val (report, reconciliationIsStale) = source
        val characterData = when (characters) {
            is DataValue.Available -> characters.value to false
            is DataValue.Stale -> characters.value to true
            else -> emptyList<CharacterSnapshot>() to false
        }
        val characterById = characterData.first.associateBy { it.id }
        val characterDataIsStale = characterData.second

        val insights = report.characters
            .flatMap { characterResult ->
                val character = characterById[characterResult.characterId]
                characterResult.results
                    .filter { it.status == StatReconciliationStatus.MISMATCH }
                    .mapNotNull { result ->
                        val expected = result.expectedFinal ?: return@mapNotNull null
                        val difference = result.difference ?: return@mapNotNull null
                        val tolerance = result.roundingTolerance ?: return@mapNotNull null
                        val staleWeapon = result.fieldPath.startsWith("weapon.") &&
                            character?.weapon is DataValue.Stale
                        val stale = reconciliationIsStale || characterDataIsStale || staleWeapon
                        val statLabel = result.statName
                            ?.takeIf { it.isNotBlank() }
                            ?: result.propertyType?.let { "property type $it" }
                            ?: result.fieldPath

                        CharacterStatInsight(
                            characterId = characterResult.characterId,
                            characterName = character?.name,
                            fieldPath = result.fieldPath,
                            propertyType = result.propertyType,
                            statName = result.statName,
                            base = result.base,
                            additive = result.add,
                            reportedFinal = result.final,
                            expectedFinal = expected,
                            difference = difference,
                            roundingTolerance = tolerance,
                            explanation = "Reported final value ${result.final} differs from base plus additive (${result.base} + ${result.add} = $expected) by $difference. The permitted rounding tolerance is $tolerance.",
                            recommendedAction = "Verify $statLabel and its displayed base, additive, and final values in-game, then refresh the account data. This discrepancy alone does not establish which source value is incorrect.",
                            basedOnStaleData = stale
                        )
                    }
            }
            .sortedWith(
                compareBy<CharacterStatInsight> { it.characterId }
                    .thenBy { it.fieldPath }
                    .thenBy { it.statName.orEmpty() }
            )

        return CharacterStatAnalysisReport(
            availability = if (reconciliationIsStale || characterDataIsStale) {
                CharacterStatAnalysisAvailability.STALE
            } else {
                CharacterStatAnalysisAvailability.AVAILABLE
            },
            analyzedCharacterCount = report.characters.size,
            matchCount = report.matchCount,
            withinToleranceCount = report.withinToleranceCount,
            mismatchCount = report.mismatchCount,
            notCheckedCount = report.notCheckedCount,
            insights = insights
        )
    }
}
