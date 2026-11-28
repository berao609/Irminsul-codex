package com.irminsul.codex.data.catalogue

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BundledEnhancementVisualMetadataTest {

    @Test
    fun everyEligibleCharacterHasPortraitAndWeaponTypeMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val characters = BundledEnhancementCatalogueRepository.loadCharacters(context)

        assertEquals(126, characters.size)
        val missingCharacterIcons = characters.filter { it.iconUrl?.startsWith("https://enka.network/ui/") != true }
        assertTrue("Missing character portrait mappings: " + missingCharacterIcons.joinToString { "${it.id}:${it.name}" }, missingCharacterIcons.isEmpty())
        val missingCharacterWeaponTypes = characters.filter { it.weaponType?.let { type -> type in setOf("SWORD", "CLAYMORE", "BOW", "POLEARM", "CATALYST") } != true }
        assertTrue("Missing character weapon types: " + missingCharacterWeaponTypes.joinToString { "${it.id}:${it.name}" }, missingCharacterWeaponTypes.isEmpty())
        assertEquals(4, characters.count { it.element == null })
        assertTrue(characters.filter { it.element == null }.all { it.weaponType == "SWORD" })
    }

    @Test
    fun everyEligibleWeaponHasIconAndWeaponTypeMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val weapons = BundledEnhancementCatalogueRepository.loadWeapons(context)

        assertTrue("The 4-star/5-star weapon catalogue must not be empty.", weapons.isNotEmpty())
        val missingWeaponIcons = weapons.filter { it.iconUrl?.startsWith("https://enka.network/ui/") != true }
        assertTrue("Missing weapon icon mappings: " + missingWeaponIcons.joinToString { "${it.id}:${it.name}" }, missingWeaponIcons.isEmpty())
        val missingWeaponTypes = weapons.filter { it.weaponType?.let { type -> type in setOf("SWORD", "CLAYMORE", "BOW", "POLEARM", "CATALYST") } != true }
        assertTrue("Missing weapon type mappings: " + missingWeaponTypes.joinToString { "${it.id}:${it.name}" }, missingWeaponTypes.isEmpty())
    }
}
