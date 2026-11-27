package com.irminsul.codex.data.catalogue

import android.content.Context
import com.irminsul.codex.domain.model.GenshinDbUpgradeCostCandidate
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.time.Instant

/**
 * Reads the version-pinned, offline Genshin-DB weapon dataset bundled with the
 * app and returns only 4-star/5-star weapon ascension candidates.
 *
 * This data is source-attributed but remains untrusted candidate data.
 */
object BundledWeaponAscensionCandidateRepository {
    const val GAME_VERSION = "7.2"
    private const val SOURCE_ID = "genshin-db-7.2"
    private const val ASSET_PATH = "genshin_db/english_weapons_7_2.gzip.b64"

    fun loadCandidates(context: Context, weaponId: String): List<GenshinDbUpgradeCostCandidate> {
        require(weaponId.isNotBlank()) { "A weapon ID is required." }
        val encoded = context.assets.open(ASSET_PATH).bufferedReader().use { it.readText() }
        val compressed = Base64.getDecoder().decode(encoded.trim())
        val json = GZIPInputStream(ByteArrayInputStream(compressed)).bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val weapons = root.getJSONObject("data")
            .getJSONObject("English")
            .getJSONObject("weapons")

        val matches = weapons.keys().asSequence().sorted().mapNotNull { slug ->
            val weapon = weapons.optJSONObject(slug) ?: return@mapNotNull null
            if (weapon.optInt("id", -1).toString() != weaponId) return@mapNotNull null
            val rarity = weapon.optInt("rarity", -1)
            if (rarity != 4 && rarity != 5) return@mapNotNull null
            Triple(slug, weapon, rarity)
        }.toList()

        if (matches.isEmpty()) return emptyList()
        val canonical = matches.first()
        require(matches.all { it.second.optJSONObject("costs")?.toString() == canonical.second.optJSONObject("costs")?.toString() }) {
            "The bundled source contains conflicting weapon records for ID $weaponId."
        }
        val sourceUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/weapons/${canonical.first}.json"
        val imported = GenshinDbProgressionCandidateImporter.importWeapon(
            weapon = canonical.second,
            gameVersion = GAME_VERSION,
            sourceId = SOURCE_ID,
            weaponSourceUrl = sourceUrl,
            retrievedAt = Instant.parse("2026-11-27T00:00:00Z")
        )
        return imported.candidates
    }
}
