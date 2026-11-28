package com.irminsul.codex.presentation

import com.irminsul.codex.data.catalogue.EnhancementCatalogueEntry
import com.irminsul.codex.data.catalogue.EnhancementSubjectType
import org.junit.Assert.assertEquals
import org.junit.Test

class EnhancementCatalogueReleaseOrderTest {
    @Test
    fun sortsWeaponsByFirstReleaseDateOldestFirstAndUnknownDatesLast() {
        val entries = listOf(
            weapon("new", "A Teaspoon of Transcendence"),
            weapon("middle", "Aqua Simulacra"),
            weapon("old", "Amos' Bow"),
            weapon("unknown", "Unknown Weapon")
        )

        assertEquals(
            listOf("Amos' Bow", "Aqua Simulacra", "A Teaspoon of Transcendence", "Unknown Weapon"),
            sortEnhancementCatalogueByRelease(entries).map { it.name }
        )
    }

    private fun weapon(id: String, name: String) = EnhancementCatalogueEntry(
        id = id,
        name = name,
        rarity = 5,
        subjectType = EnhancementSubjectType.WEAPON
    )
}
