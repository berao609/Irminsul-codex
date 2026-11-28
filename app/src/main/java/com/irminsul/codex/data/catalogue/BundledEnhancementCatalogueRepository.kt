package com.irminsul.codex.data.catalogue

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream

enum class EnhancementSubjectType { CHARACTER, WEAPON }

data class EnhancementCatalogueEntry(
    val id: String,
    val name: String,
    val rarity: Int,
    val subjectType: EnhancementSubjectType,
    val detail: String? = null
)

/**
 * Loads the complete 4-star/5-star selection catalogue from the version-pinned
 * Genshin-DB assets. Ownership is deliberately not used as a catalogue filter.
 *
 * The bundled source contains names, rarity, and weapon type, but no complete
 * portrait/icon mapping for every character and weapon.
 */
object BundledEnhancementCatalogueRepository {
    const val GAME_VERSION = BundledCharacterProgressionCandidateRepository.GAME_VERSION
    const val SOURCE_COMMIT = BundledCharacterProgressionCandidateRepository.SOURCE_COMMIT
    private const val CHARACTER_CHUNK_COUNT = 15
    private const val WEAPON_ASSET = "genshin_db/english_weapons_7_2.gzip.b64"

    fun loadCharacters(context: Context): List<EnhancementCatalogueEntry> {
        val chunks = (1..CHARACTER_CHUNK_COUNT).map { index ->
            val fileName = "genshin_db/characters_7_2_${index.toString().padStart(2, '0')}.json"
            context.assets.open(fileName).bufferedReader().use { JSONObject(it.readText()) }
        }
        return parseCharacters(chunks)
    }

    internal fun parseCharacters(chunks: List<JSONObject>): List<EnhancementCatalogueEntry> {
        require(chunks.isNotEmpty()) { "At least one character catalogue chunk is required." }
        val entries = mutableListOf<EnhancementCatalogueEntry>()
        chunks.forEach { root ->
            require(root.optInt("schemaVersion", -1) == 1) {
                "The bundled character catalogue schema is unsupported."
            }
            require(root.optString("gameVersion") == GAME_VERSION &&
                root.optString("sourceCommit") == SOURCE_COMMIT
            ) {
                "The bundled character catalogue version or source commit does not match."
            }
            val records = root.optJSONArray("characters")
                ?: throw IllegalArgumentException("A character catalogue chunk has no records.")
            for (recordIndex in 0 until records.length()) {
                val record = records.optJSONObject(recordIndex)
                    ?: throw IllegalArgumentException("A character catalogue record is malformed.")
                val character = record.optJSONObject("character")
                    ?: throw IllegalArgumentException("A character catalogue record has no character.")
                val id = character.opt("id")?.toString()?.takeIf(String::isNotBlank)
                    ?: throw IllegalArgumentException("A character catalogue record has no ID.")
                val name = character.optString("name").takeIf(String::isNotBlank)
                    ?: throw IllegalArgumentException("Character $id has no name.")
                val rarity = character.optInt("rarity", -1)
                if (rarity == 4 || rarity == 5) {
                    entries += EnhancementCatalogueEntry(id, name, rarity, EnhancementSubjectType.CHARACTER)
                }
            }
        }
        require(entries.map { it.id }.distinct().size == entries.size) {
            "The bundled character catalogue contains duplicate IDs."
        }
        return entries.sortedWith(compareBy<EnhancementCatalogueEntry> { it.name.lowercase() }.thenBy { it.id })
    }

    fun loadWeapons(context: Context): List<EnhancementCatalogueEntry> {
        val encoded = context.assets.open(WEAPON_ASSET).bufferedReader().use { it.readText() }
        val compressed = Base64.decode(encoded.trim(), Base64.DEFAULT)
        val rootText = GZIPInputStream(ByteArrayInputStream(compressed)).bufferedReader().use { it.readText() }
        return parseWeapons(JSONObject(rootText))
    }

    internal fun parseWeapons(root: JSONObject): List<EnhancementCatalogueEntry> {
        val weapons = root.getJSONObject("data").getJSONObject("English").getJSONObject("weapons")
        val entries = weapons.keys().asSequence().sorted().mapNotNull { slug ->
            val weapon = weapons.optJSONObject(slug) ?: return@mapNotNull null
            val rarity = weapon.optInt("rarity", -1)
            if (rarity != 4 && rarity != 5) return@mapNotNull null
            val id = weapon.opt("id")?.toString()?.takeIf(String::isNotBlank)
                ?: throw IllegalArgumentException("Weapon $slug has no ID.")
            val name = weapon.optString("name").takeIf(String::isNotBlank)
                ?: throw IllegalArgumentException("Weapon $id has no name.")
            EnhancementCatalogueEntry(
                id = id,
                name = name,
                rarity = rarity,
                subjectType = EnhancementSubjectType.WEAPON,
                detail = weapon.optString("weaponText").takeIf(String::isNotBlank)
            )
        }.toList()
        require(entries.map { it.id }.distinct().size == entries.size) {
            "The bundled weapon catalogue contains duplicate IDs."
        }
        return entries.sortedWith(compareBy<EnhancementCatalogueEntry> { it.name.lowercase() }.thenBy { it.id })
    }

    fun loadAll(context: Context): List<EnhancementCatalogueEntry> =
        loadCharacters(context) + loadWeapons(context)
}
