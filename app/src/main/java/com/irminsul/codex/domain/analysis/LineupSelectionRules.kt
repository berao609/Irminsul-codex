package com.irminsul.codex.domain.analysis

/**
 * Pure selection rules for the four-character lineup sandbox.
 * This does not evaluate combat strength, role coverage, reactions, or synergy.
 */
object LineupSelectionRules {
    const val MAX_TEAM_SIZE = 4

    fun toggle(selectedCharacterIds: List<Int>, characterId: Int): List<Int> {
        val normalized = selectedCharacterIds.distinct().take(MAX_TEAM_SIZE)
        return if (characterId in normalized) {
            normalized.filterNot { it == characterId }
        } else if (normalized.size < MAX_TEAM_SIZE) {
            normalized + characterId
        } else {
            normalized
        }
    }
}
