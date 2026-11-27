package com.irminsul.codex.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class VisualsNavigationStackTest {
    @Test
    fun navigateAppendsNewDestination() {
        val start = listOf(VisualsDestination.OVERVIEW)

        assertEquals(
            listOf(VisualsDestination.OVERVIEW, VisualsDestination.WORLD_EXPLORATION),
            VisualsNavigationStack.navigate(start, VisualsDestination.WORLD_EXPLORATION)
        )
    }

    @Test
    fun navigateDoesNotAppendCurrentDestinationAgain() {
        val start = listOf(
            VisualsDestination.OVERVIEW,
            VisualsDestination.WORLD_EXPLORATION
        )

        assertEquals(
            start,
            VisualsNavigationStack.navigate(start, VisualsDestination.WORLD_EXPLORATION)
        )
    }

    @Test
    fun backReturnsToPreviousDestination() {
        val start = listOf(
            VisualsDestination.OVERVIEW,
            VisualsDestination.CHARACTER_ROSTER,
            VisualsDestination.CHARACTER_DETAILS
        )

        assertEquals(
            listOf(VisualsDestination.OVERVIEW, VisualsDestination.CHARACTER_ROSTER),
            VisualsNavigationStack.back(start)
        )
    }

    @Test
    fun backKeepsOverviewAsRoot() {
        val start = listOf(VisualsDestination.OVERVIEW)

        assertEquals(start, VisualsNavigationStack.back(start))
    }
}
