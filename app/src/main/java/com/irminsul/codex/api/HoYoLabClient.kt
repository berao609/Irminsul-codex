package com.irminsul.codex.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

data class DailyNote(
    val currentResin: Int?,
    val maxResin: Int?,
    val resinRecoveryTime: Long?,
    val finishedTaskNum: Int?,
    val totalTaskNum: Int?,
    val isExtraTaskRewardReceived: Boolean?,
    val remainResinDiscountNum: Int?,
    val resinDiscountNumLimit: Int?,
    val currentHomeCoin: Int?,
    val maxHomeCoin: Int?,
    val currentExpeditionNum: Int?,
    val maxExpeditionNum: Int?
)

object HoYoLabClient {
    private const val API_BASE = "https://sg-public-api.hoyolab.com/event/game_record/genshin/api"
    private const val RECORD_CARD_URL = "https://bbs-api-os.hoyolab.com/game_record/card/wapi/getGameRecordCard"
    private const val DS_SALT = "6s25p5ox5y14umn1p61aqyyvbvvl3lrt"
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private val BLOAT_KEYS = setOf(
        "icon", "image", "img", "url", "banner", "bg", "cover",
        "desc", "description", "effect", "strategy", "wiki",
        "relic_wiki", "weapon_wiki", "avatar_wiki", "property_map",
        "recommend_relic_property", "skill_affix_list"
    )

    private val BLOAT_SUFFIXES = listOf(
        "_icon", "_image", "_url", "_img", "_banner", "_cover", "_bg", "_wiki"
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun detectServer(uid: String): String {
        val cleanUid = uid.trim()
        return when {
            cleanUid.startsWith("18") -> "os_asia"
            cleanUid.startsWith("6") -> "os_usa"
            cleanUid.startsWith("7") -> "os_euro"
            cleanUid.startsWith("8") -> "os_asia"
            cleanUid.startsWith("9") -> "os_cht"
            else -> "os_asia"
        }
    }

    private fun extractCookieValue(cookie: String, key: String): String? {
        return cookie.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("$key=") }
            ?.substringAfter("=")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun generateDs(): String {
        val t = System.currentTimeMillis() / 1000
        val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val r = (1..6).map { chars.random() }.joinToString("")
        val hash = md5("salt=$DS_SALT&t=$t&r=$r")
        return "$t,$r,$hash"
    }

    private fun md5(input: String): String {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun baseRequestBuilder(url: String, cookie: String): Request.Builder {
        return Request.Builder()
            .url(url)
            .addHeader("DS", generateDs())
            .addHeader("x-rpc-app_version", "1.5.0")
            .addHeader("x-rpc-client_type", "5")
            .addHeader("x-rpc-language", "en-us")
            .addHeader("Referer", "https://act.hoyolab.com/")
            .addHeader("Origin", "https://act.hoyolab.com")
            .addHeader("Cookie", cookie.trim())
    }

    private fun fetchRawGet(url: String, cookie: String): JSONObject {
        val request = baseRequestBuilder(url, cookie).get().build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string()
                ?: return JSONObject().put("retcode", -1).put("message", "Empty response (HTTP ${response.code})")
            return JSONObject(body)
        }
    }

    private fun fetchRawPost(url: String, payload: JSONObject, cookie: String): JSONObject {
        val body = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = baseRequestBuilder(url, cookie).post(body).build()
        client.newCall(request).execute().use { response ->
            val respBody = response.body?.string()
                ?: return JSONObject().put("retcode", -1).put("message", "Empty response (HTTP ${response.code})")
            return JSONObject(respBody)
        }
    }

    private fun isBloatKey(key: String): Boolean {
        val lower = key.lowercase(Locale.US)
        if (lower in BLOAT_KEYS) return true
        return BLOAT_SUFFIXES.any { lower.endsWith(it) }
    }

    private fun stripBloat(node: Any?, propertyMap: Map<Int, String>): Any? {
        return when (node) {
            null, JSONObject.NULL -> null
            is JSONObject -> {
                val clean = JSONObject()
                val propType = node.optInt("property_type", -1)
                if (propType != -1 && !node.has("stat_name")) {
                    propertyMap[propType]?.let { clean.put("stat_name", it) }
                }

                val keys = node.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    if (isBloatKey(key)) continue
                    val value = node.opt(key)
                    if (value == null || value === JSONObject.NULL) continue
                    if (value is String && (value.startsWith("http://") || value.startsWith("https://"))) {
                        continue
                    }
                    val cleanedValue = stripBloat(value, propertyMap)
                    if (cleanedValue != null) {
                        clean.put(key, cleanedValue)
                    }
                }
                clean
            }
            is JSONArray -> {
                val clean = JSONArray()
                for (i in 0 until node.length()) {
                    val item = node.opt(i)
                    if (item == null || item === JSONObject.NULL) continue
                    if (item is String && (item.startsWith("http://") || item.startsWith("https://"))) {
                        continue
                    }
                    val cleanedItem = stripBloat(item, propertyMap)
                    if (cleanedItem != null) {
                        clean.put(cleanedItem)
                    }
                }
                clean
            }
            else -> node
        }
    }

    private fun extractPropertyMap(detailData: JSONObject?, target: MutableMap<Int, String>) {
        val mapObj = detailData?.optJSONObject("property_map") ?: return
        val keys = mapObj.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val entry = mapObj.optJSONObject(key) ?: continue
            val propType = entry.optInt("property_type", key.toIntOrNull() ?: -1)
            val name = entry.optString("name", "").trim()
            if (propType != -1 && name.isNotEmpty()) {
                target[propType] = name
            }
        }
    }

    suspend fun fetchGenshinUid(cookie: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val ltuid = extractCookieValue(cookie, "ltuid_v2")
                ?: extractCookieValue(cookie, "ltuid")
                ?: return@withContext Result.failure(Exception("Missing ltuid_v2 in cookie."))

            val json = fetchRawGet("$RECORD_CARD_URL?uid=$ltuid", cookie)
            if (json.optInt("retcode", -1) != 0) {
                return@withContext Result.failure(Exception(json.optString("message", "Could not fetch UID")))
            }

            val list = json.getJSONObject("data").getJSONArray("list")
            for (i in 0 until list.length()) {
                val card = list.getJSONObject(i)
                if (card.optInt("game_id") == 2) {
                    val roleId = card.optString("game_role_id", "")
                    if (roleId.isNotEmpty()) {
                        return@withContext Result.success(roleId)
                    }
                }
            }
            Result.failure(Exception("No Genshin Impact profile linked to this HoYoLAB account."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchDailyNote(uid: String, cookie: String): Result<DailyNote> = withContext(Dispatchers.IO) {
        try {
            val cleanUid = uid.trim()
            val server = detectServer(cleanUid)
            val json = fetchRawGet("$API_BASE/dailyNote?server=$server&role_id=$cleanUid", cookie)

            val retcode = json.optInt("retcode", -1)
            if (retcode != 0) {
                val message = json.optString("message", "Unknown API error")
                return@withContext Result.failure(Exception("Error $retcode: $message"))
            }

            val data = json.optJSONObject("data")
                ?: return@withContext Result.failure(Exception("Daily Note response did not contain account data."))

            Result.success(DailyNoteParser.parse(data))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportFullAccountJson(
        uid: String,
        cookie: String,
        onProgress: (String) -> Unit = {}
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val cleanUid = uid.trim()
            val server = detectServer(cleanUid)
            val ltuid = extractCookieValue(cookie, "ltuid_v2") ?: extractCookieValue(cookie, "ltuid")
            val propertyMap = mutableMapOf<Int, String>()

            fun unwrap(json: JSONObject): Any {
                return if (json.optInt("retcode", -1) == 0) {
                    stripBloat(json.opt("data"), propertyMap) ?: JSONObject()
                } else {
                    JSONObject()
                        .put("retcode", json.optInt("retcode", -1))
                        .put("message", json.optString("message", "Request failed"))
                }
            }

            onProgress("Fetching chronicle, exploration & endgame...")
            val results = coroutineScope {
                val cardDeferred = async {
                    if (ltuid != null) fetchRawGet("$RECORD_CARD_URL?uid=$ltuid", cookie) else null
                }
                val indexDeferred = async { fetchRawGet("$API_BASE/index?server=$server&role_id=$cleanUid", cookie) }
                val dailyDeferred = async { fetchRawGet("$API_BASE/dailyNote?server=$server&role_id=$cleanUid", cookie) }
                val listDeferred = async {
                    val payload = JSONObject().put("role_id", cleanUid).put("server", server)
                    fetchRawPost("$API_BASE/character/list", payload, cookie)
                }
                val abyssCurDeferred = async {
                    fetchRawGet("$API_BASE/spiralAbyss?server=$server&role_id=$cleanUid&schedule_type=1", cookie)
                }
                val abyssPrevDeferred = async {
                    fetchRawGet("$API_BASE/spiralAbyss?server=$server&role_id=$cleanUid&schedule_type=2", cookie)
                }
                val theaterDeferred = async {
                    fetchRawGet("$API_BASE/role_combat?server=$server&role_id=$cleanUid&need_detail=true", cookie)
                }

                listOf(
                    cardDeferred.await(),
                    indexDeferred.await(),
                    dailyDeferred.await(),
                    listDeferred.await(),
                    abyssCurDeferred.await(),
                    abyssPrevDeferred.await(),
                    theaterDeferred.await()
                )
            }

            val recordCardRaw = results[0]
            val indexRaw = results[1]
            val dailyRaw = results[2]
            val charListRaw = results[3]
            val abyssCurrentRaw = results[4]
            val abyssPrevRaw = results[5]
            val theaterRaw = results[6]

            val charIds = mutableListOf<Int>()
            if (charListRaw != null && charListRaw.optInt("retcode", -1) == 0) {
                val listArr = charListRaw.optJSONObject("data")?.optJSONArray("list")
                if (listArr != null) {
                    for (i in 0 until listArr.length()) {
                        val id = listArr.optJSONObject(i)?.optInt("id", 0) ?: 0
                        if (id != 0) charIds.add(id)
                    }
                }
            }

            val detailedChars = JSONArray()
            if (charIds.isNotEmpty()) {
                val chunks = charIds.chunked(8)
                chunks.forEachIndexed { idx, chunk ->
                    onProgress("Fetching character builds (${idx + 1}/${chunks.size})...")
                    val idsArray = JSONArray()
                    chunk.forEach { idsArray.put(it) }
                    val detailPayload = JSONObject()
                        .put("role_id", cleanUid)
                        .put("server", server)
                        .put("character_ids", idsArray)
                    val detailJson = fetchRawPost("$API_BASE/character/detail", detailPayload, cookie)
                    if (detailJson.optInt("retcode", -1) == 0) {
                        val dataObj = detailJson.optJSONObject("data")
                        extractPropertyMap(dataObj, propertyMap)
                        val items = dataObj?.optJSONArray("list")
                        if (items != null) {
                            for (i in 0 until items.length()) {
                                val cleanedChar = stripBloat(items.opt(i), propertyMap)
                                if (cleanedChar != null) detailedChars.put(cleanedChar)
                            }
                        }
                    }
                    if (idx < chunks.lastIndex) delay(200)
                }
            }

            val root = JSONObject()
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            root.put("exported_at", isoFormat.format(Date()))
            root.put("uid", cleanUid)
            root.put("server", server)

            if (recordCardRaw != null && recordCardRaw.optInt("retcode", -1) == 0) {
                val cards = recordCardRaw.optJSONObject("data")?.optJSONArray("list")
                if (cards != null) {
                    for (i in 0 until cards.length()) {
                        val card = cards.optJSONObject(i)
                        if (card?.optInt("game_id") == 2) {
                            root.put("account_card", stripBloat(card, propertyMap))
                            break
                        }
                    }
                }
            }

            val profileClean = unwrap(indexRaw ?: JSONObject())
            if (profileClean is JSONObject && detailedChars.length() > 0) {
                profileClean.remove("avatars")
            }
            root.put("profile_and_exploration", profileClean)
            root.put("daily_note", unwrap(dailyRaw ?: JSONObject()))

            if (detailedChars.length() > 0) {
                root.put("characters", detailedChars)
            } else {
                root.put("characters", unwrap(charListRaw ?: JSONObject()))
            }

            root.put("spiral_abyss_current", unwrap(abyssCurrentRaw ?: JSONObject()))
            root.put("spiral_abyss_previous", unwrap(abyssPrevRaw ?: JSONObject()))
            root.put("imaginarium_theater", unwrap(theaterRaw ?: JSONObject()))

            Result.success(root.toString(2))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
