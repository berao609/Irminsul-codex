package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.CharacterSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterReleaseOrderTest {
    @Test
    fun sortsByFirstReleaseNewestFirstAndKeepsUnknownNamesLast() {
        val input = listOf(
            CharacterSnapshot(id = 3, name = "Vesna", level = 1, rarity = 5, element = null, friendshipLevel = null, activeConstellationCount = null),
            CharacterSnapshot(id = 2, name = "Xiao", level = 1, rarity = 5, element = null, friendshipLevel = null, activeConstellationCount = null),
            CharacterSnapshot(id = 1, name = "Amber", level = 1, rarity = 4, element = null, friendshipLevel = null, activeConstellationCount = null),
            CharacterSnapshot(id = 4, name = "Albedo", level = 1, rarity = 5, element = null, friendshipLevel = null, activeConstellationCount = null)
        )
        assertEquals(
            listOf("Vesna", "Xiao", "Albedo", "Amber"),
            sortCharactersByRelease(input).map { it.name }
        )
    }

    @Test
    fun sortsCharactersWithinSameReleaseUpdateAlphabetically() {
        val input = listOf(
            CharacterSnapshot(id = 2, name = "Keqing", level = 1, rarity = 5, element = null, friendshipLevel = null, activeConstellationCount = null),
            CharacterSnapshot(id = 1, name = "Amber", level = 1, rarity = 4, element = null, friendshipLevel = null, activeConstellationCount = null)
        )
        assertEquals(listOf("Amber", "Keqing"), sortCharactersByRelease(input).map { it.name })
    }
}
