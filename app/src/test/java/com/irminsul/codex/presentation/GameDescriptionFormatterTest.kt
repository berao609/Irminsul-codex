package com.irminsul.codex.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class GameDescriptionFormatterTest {
    @Test
    fun removesLinkMetadataButPreservesLinkedText() {
        assertEquals(
            "Deals Pyro DMG to the target.",
            formatGameDescription("Deals {LINK#ELEMENT_FIRE}Pyro{/LINK} DMG to the target.")
        )
    }

    @Test
    fun removesColorAndHtmlMarkup() {
        assertEquals(
            "Increases damage by 20%.",
            formatGameDescription("<color=#FF0000>Increases <b>damage</b> by 20%.</color>")
        )
    }

    @Test
    fun normalizesEscapedNewlinesAndCollapsesExcessBlankLines() {
        assertEquals(
            "First line\n\nSecond line",
            formatGameDescription("First line\\n\\n\\n\\nSecond line")
        )
    }

    @Test
    fun handlesPlainTextWithoutChangingIt() {
        assertEquals("A normal description.", formatGameDescription("A normal description."))
    }
}
