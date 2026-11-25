package com.irminsul.codex.domain.analysis

import org.junit.Assert.assertEquals
import org.junit.Test

class LineupSelectionRulesTest {
    @Test
    fun addsOwnedCharacterWithoutChangingExistingOrder() {
        assertEquals(
            listOf(100, 200, 300),
            LineupSelectionRules.toggle(listOf(100, 200), 300)
        )
    }

    @Test
    fun doesNotAddDuplicateCharacter() {
        assertEquals(
            listOf(100, 200),
            LineupSelectionRules.toggle(listOf(100, 200), 100)
        )
    }

    @Test
    fun removesSelectedCharacter() {
        assertEquals(
            listOf(100, 300),
            LineupSelectionRules.toggle(listOf(100, 200, 300), 200)
        )
    }

    @Test
    fun doesNotExceedFourCharacters() {
        assertEquals(
            listOf(100, 200, 300, 400),
            LineupSelectionRules.toggle(listOf(100, 200, 300, 400), 500)
        )
    }

    @Test
    fun normalizesDuplicateOrOversizedExistingSelection() {
        assertEquals(
            listOf(100, 200, 300, 400),
            LineupSelectionRules.toggle(listOf(100, 100, 200, 300, 400, 500), 600)
        )
    }
}
