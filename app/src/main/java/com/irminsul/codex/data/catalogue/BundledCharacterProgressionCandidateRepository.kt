package com.irminsul.codex.data.catalogue

import android.content.Context
import org.json.JSONObject
import java.time.Instant

/**
 * Loads version-pinned Genshin-DB character ascension and talent costs from
 * offline assets. Imported records remain untrusted candidate data.
 *
 * Character EXP is supplied separately by GenshinCharacterExperienceCurveCandidate.
 */
object BundledCharacterProgressionCandidateRepository {
    const val GAME_VERSION = "7.2"
    const val SOURCE_ID = "genshin-db-7.2"
    const val SOURCE_COMMIT = "8e5badd60b5d894284ccc927a56c9683529d2592"
    private const val ASSET_DIRECTORY = "genshin_db"
    private const val ASSET_CHUNK_COUNT = 15
    private val RETRIEVED_AT = Instant.parse("2026-11-27T00:00:00Z")

    fun loadCandidates(
        context: Context,
        characterId: String
    ): GenshinDbProgressionImportResult? {
        require(characterId.isNotBlank()) { "A character ID is required." }
        val chunks = (1..ASSET_CHUNK_COUNT).map { index ->
            val fileName = "characters_7_2_${index.toString().padStart(2, '0')}.json"
            val path = "$ASSET_DIRECTORY/$fileName"
            context.assets.open(path).bufferedReader().use { JSONObject(it.readText()) }
        }
        return parseCandidates(chunks, characterId)
    }

    internal fun parseCandidates(
        chunks: List<JSONObject>,
        characterId: String
    ): GenshinDbProgressionImportResult? {
        require(characterId.isNotBlank()) { "A character ID is required." }
        require(chunks.isNotEmpty()) { "At least one bundled character data chunk is required." }

        val matches = mutableListOf<Pair<String, JSONObject>>()
        chunks.forEach { chunk ->
            require(chunk.optInt("schemaVersion", -1) == 1) {
                "The bundled character dataset schema version is unsupported."
            }
            require(chunk.optString("gameVersion") == GAME_VERSION &&
                chunk.optString("sourceCommit") == SOURCE_COMMIT
            ) {
                "The bundled character dataset does not match its pinned game version and source commit."
            }
            val records = chunk.optJSONArray("characters")
                ?: throw IllegalArgumentException("A bundled character data chunk has no character records.")
            for (index in 0 until records.length()) {
                val record = records.optJSONObject(index)
                    ?: throw IllegalArgumentException("A bundled character record is malformed.")
                val character = record.optJSONObject("character")
                    ?: throw IllegalArgumentException("A bundled character record has no character object.")
                if (character.opt("id")?.toString() == characterId) {
                    val slug = record.optString("slug")
                    require(slug.isNotBlank()) { "A bundled character record has no source slug." }
                    matches += slug to record
                }
            }
        }

        if (matches.isEmpty()) return null
        require(matches.size == 1) {
            "The bundled dataset contains duplicate records for character ID $characterId."
        }

        val (slug, record) = matches.single()
        val character = record.getJSONObject("character")
        val talents = record.optJSONObject("talents")
        val characterUrl =
            "https://github.com/theBowja/genshin-db/blob/$SOURCE_COMMIT/src/data/English/characters/$slug.json"
        val talentUrl =
            "https://github.com/theBowja/genshin-db/blob/$SOURCE_COMMIT/src/data/English/talents/$slug.json"

        return GenshinDbProgressionCandidateImporter.importCharacter(
            character = character,
            talents = talents,
            gameVersion = GAME_VERSION,
            sourceId = SOURCE_ID,
            characterSourceUrl = characterUrl,
            talentSourceUrl = talentUrl,
            retrievedAt = RETRIEVED_AT
        )
    }
}
