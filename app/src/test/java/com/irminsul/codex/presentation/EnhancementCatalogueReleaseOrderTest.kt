package com.irminsul.codex.presentation

import com.irminsul.codex.data.catalogue.EnhancementCatalogueEntry
import com.irminsul.codex.data.catalogue.EnhancementSubjectType
import org.junit.Assert.assertEquals
import org.junit.Test

class EnhancementCatalogueReleaseOrderTest {
    @Test
    fun sortsWeaponsByFirstReleaseDateNewestFirstAndUnknownDatesLast() {
        val entries = listOf(
            weapon("new", "A Teaspoon of Transcendence"),
            weapon("middle", "Aqua Simulacra"),
            weapon("old", "Amos' Bow"),
            weapon("unknown", "Unknown Weapon")
        )

        assertEquals(
            listOf("A Teaspoon of Transcendence", "Aqua Simulacra", "Amos' Bow", "Unknown Weapon"),
            sortEnhancementCatalogueByRelease(entries).map { it.name }
        )
    }

    @Test
    fun sortsCharactersByReleaseNewestFirst() {
        val entries = listOf(
            character("amber", "Amber"),
            character("vesna", "Vesna"),
            character("xiao", "Xiao")
        )

        assertEquals(
            listOf("Vesna", "Xiao", "Amber"),
            sortEnhancementCatalogueByRelease(entries).map { it.name }
        )
    }

    private fun character(id: String, name: String) = EnhancementCatalogueEntry(
        id = id,
        name = name,
        rarity = 5,
        subjectType = EnhancementSubjectType.CHARACTER
    )

    private fun weapon(id: String, name: String) = EnhancementCatalogueEntry(
        id = id,
        name = name,
        rarity = 5,
        subjectType = EnhancementSubjectType.WEAPON
    )
}
