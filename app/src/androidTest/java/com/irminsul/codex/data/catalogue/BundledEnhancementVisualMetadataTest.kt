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
        assertTrue(characters.all { it.iconUrl?.startsWith("https://enka.network/ui/") == true })
        assertTrue(characters.all { it.weaponType != null && it.weaponType in setOf("SWORD", "CLAYMORE", "BOW", "POLEARM", "CATALYST") })
        assertEquals(4, characters.count { it.element == null })
        assertTrue(characters.filter { it.element == null }.all { it.weaponType == "SWORD" })
    }

    @Test
    fun everyEligibleWeaponHasIconAndWeaponTypeMetadata() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val weapons = BundledEnhancementCatalogueRepository.loadWeapons(context)

        assertTrue("The 4-star/5-star weapon catalogue must not be empty.", weapons.isNotEmpty())
        assertTrue(weapons.all { it.iconUrl?.startsWith("https://enka.network/ui/") == true })
        assertTrue(weapons.all { it.weaponType in setOf("SWORD", "CLAYMORE", "BOW", "POLEARM", "CATALYST") })
    }
}
