package com.irminsul.codex.presentation

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

internal data class WeaponPassive(
    val name: String?,
    val description: String
)

/**
 * Looks up refinement-specific weapon effects from Genshin-DB.
 * HoYoLAB's equipped-weapon payload contains stats and flavour text, but not
 * the refinement passive. Genshin-DB data is sourced from GenshinData.
 */
internal object WeaponPassiveLookup {
    private val cache = ConcurrentHashMap<String, WeaponPassive>()

    fun fetch(weaponName: String?, refinementLevel: Int): WeaponPassive? {
        val name = weaponName?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        val rank = refinementLevel.coerceIn(1, 5)
        val key = "${name.lowercase()}:$rank"
        cache[key]?.let { return it }

        val encodedName = URLEncoder.encode(name, "UTF-8")
        val connection = (URL(
            "https://genshin-db-api.vercel.app/api/v5/weapons?query=$encodedName&dumpResult=true&queryLanguages=english&resultLanguage=English"
        ).openConnection() as HttpURLConnection)

        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            parse(response, rank)?.also { cache[key] = it }
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    internal fun parse(response: String, refinementLevel: Int): WeaponPassive? {
        return try {
            val root = JSONObject(response)
            val weapon = root.optJSONObject("result") ?: root
            val rank = refinementLevel.coerceIn(1, 5)
            val refinement = weapon.optJSONObject("r$rank")
                ?: weapon.optJSONObject("refinement")?.optJSONObject(rank.toString())
            val description = refinement?.optString("description")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: return null
            val name = weapon.optString("effectName")
                .trim()
                .takeIf { it.isNotEmpty() && it != "null" }
            WeaponPassive(name, description)
        } catch (_: Exception) {
            null
        }
    }
}
