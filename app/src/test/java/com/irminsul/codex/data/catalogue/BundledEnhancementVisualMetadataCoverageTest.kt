package com.irminsul.codex.data.catalogue

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Base64
import java.util.zip.GZIPInputStream

class BundledEnhancementVisualMetadataCoverageTest {

    private val assets = File("src/main/assets/genshin_db")

    @Test
    fun everyEligibleCharacterHasFullPortraitAndWeaponTypeMetadata() {
        val chunks = (1..15).map { index ->
            val file = File(assets, "characters_7_2_${index.toString().padStart(2, '0')}.json")
            JSONObject(file.readText())
        }
        val entries = BundledEnhancementCatalogueRepository.parseCharacters(chunks)
        val metadata = JSONObject(File(assets, "enhancement_visual_metadata.json").readText())
        val mapped = BundledEnhancementCatalogueRepository.applyVisualMetadata(entries, metadata, "characters")

        assertEquals(126, mapped.size)
        val missingIcons = mapped.filter { it.iconUrl?.startsWith("https://enka.network/ui/UI_AvatarIcon_") != true }
        assertTrue("Missing character portraits: " + missingIcons.joinToString { "${it.id}:${it.name}" }, missingIcons.isEmpty())
        val validWeaponTypes = setOf("SWORD", "CLAYMORE", "BOW", "POLEARM", "CATALYST")
        val missingTypes = mapped.filter { it.weaponType !in validWeaponTypes }
        assertTrue("Missing character weapon types: " + missingTypes.joinToString { "${it.id}:${it.name}" }, missingTypes.isEmpty())
        assertEquals(4, mapped.count { it.element == null })
        assertTrue(mapped.filter { it.element == null }.all { it.weaponType == "SWORD" })
    }

    @Test
    fun everyEligibleWeaponHasIconAndWeaponTypeMetadata() {
        val encoded = File(assets, "english_weapons_7_2.gzip.b64").readText()
        val compressed = Base64.getMimeDecoder().decode(encoded)
        val rootText = GZIPInputStream(ByteArrayInputStream(compressed)).bufferedReader().use { it.readText() }
        val entries = BundledEnhancementCatalogueRepository.parseWeapons(JSONObject(rootText))
        val metadata = JSONObject(File(assets, "enhancement_visual_metadata.json").readText())
        val mapped = BundledEnhancementCatalogueRepository.applyVisualMetadata(entries, metadata, "weapons")

        assertTrue("The eligible weapon catalogue must not be empty.", mapped.isNotEmpty())
        val missingIcons = mapped.filter { it.iconUrl?.startsWith("https://enka.network/ui/") != true }
        assertTrue("Missing weapon icons: " + missingIcons.joinToString { "${it.id}:${it.name}" }, missingIcons.isEmpty())
        val validWeaponTypes = setOf("SWORD", "CLAYMORE", "BOW", "POLEARM", "CATALYST")
        val missingTypes = mapped.filter { it.weaponType !in validWeaponTypes }
        assertTrue("Missing weapon types: " + missingTypes.joinToString { "${it.id}:${it.name}" }, missingTypes.isEmpty())
    }
}
