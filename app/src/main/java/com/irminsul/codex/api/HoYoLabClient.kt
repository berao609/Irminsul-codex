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
            return HoYoLabResponseParser.parse(
                body = response.body?.string(),
                httpStatus = response.code
            )
        }
    }

    private fun fetchRawPost(url: String, payload: JSONObject, cookie: String): JSONObject {
        val body = payload.toString().toRequestBody(JSON_MEDIA_TYPE)
        val request = baseRequestBuilder(url, cookie).post(body).build()
        client.newCall(request).execute().use { response ->
            return HoYoLabResponseParser.parse(
                body = response.body?.string(),
                httpStatus = response.code
            )
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

            suspend fun requestSafely(request: suspend () -> JSONObject): JSONObject {
                return try {
                    request()
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    JSONObject()
                        .put("retcode", -1)
                        .put("message", "Request failed (${error.javaClass.simpleName})")
                }
            }

            fun sectionFromResponse(response: JSONObject, map: Map<Int, String>): ExportSectionResult {
                val retcode = response.optInt("retcode", -1)
                return if (retcode == 0) {
                    ExportSectionResult.success(
                        AccountExportDataPreserver.preserve(response.opt("data"), map) ?: JSONObject()
                    )
                } else {
                    val message = response.optString("message", "Request failed")
                    ExportSectionResult.failed(
                        JSONObject()
                            .put("retcode", retcode)
                            .put("message", message),
                        message
                    )
                }
            }

            onProgress("Fetching chronicle, exploration & endgame...")
            val results = coroutineScope {
                val cardDeferred = async {
                    if (ltuid != null) {
                        requestSafely { fetchRawGet("$RECORD_CARD_URL?uid=$ltuid", cookie) }
                    } else null
                }
                val indexDeferred = async {
                    requestSafely { fetchRawGet("$API_BASE/index?server=$server&role_id=$cleanUid", cookie) }
                }
                val dailyDeferred = async {
                    requestSafely { fetchRawGet("$API_BASE/dailyNote?server=$server&role_id=$cleanUid", cookie) }
                }
                val listDeferred = async {
                    requestSafely {
                        val payload = JSONObject().put("role_id", cleanUid).put("server", server)
                        fetchRawPost("$API_BASE/character/list", payload, cookie)
                    }
                }
                val abyssCurDeferred = async {
                    requestSafely { fetchRawGet("$API_BASE/spiralAbyss?server=$server&role_id=$cleanUid&schedule_type=1", cookie) }
                }
                val abyssPrevDeferred = async {
                    requestSafely { fetchRawGet("$API_BASE/spiralAbyss?server=$server&role_id=$cleanUid&schedule_type=2", cookie) }
                }
                val theaterDeferred = async {
                    requestSafely { fetchRawGet("$API_BASE/role_combat?server=$server&role_id=$cleanUid&need_detail=true", cookie) }
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
            val indexRaw = results[1]!!
            val dailyRaw = results[2]!!
            val charListRaw = results[3]!!
            val abyssCurrentRaw = results[4]!!
            val abyssPrevRaw = results[5]!!
            val theaterRaw = results[6]!!

            val sectionResults = linkedMapOf<String, ExportSectionResult>()

            if (recordCardRaw == null) {
                sectionResults["account_card"] = ExportSectionResult.skipped(
                    "HoYoLAB account ID cookie was unavailable."
                )
            } else if (recordCardRaw.optInt("retcode", -1) != 0) {
                sectionResults["account_card"] = sectionFromResponse(recordCardRaw, propertyMap)
            } else {
                val cards = recordCardRaw.optJSONObject("data")?.optJSONArray("list")
                var accountCard: Any? = null
                if (cards != null) {
                    for (i in 0 until cards.length()) {
                        val card = cards.optJSONObject(i)
                        if (card?.optInt("game_id") == 2) {
                            accountCard = AccountExportDataPreserver.preserve(card, propertyMap)
                            break
                        }
                    }
                }
                sectionResults["account_card"] = if (accountCard != null) {
                    ExportSectionResult.success(accountCard)
                } else {
                    ExportSectionResult.failed(
                        JSONObject().put("retcode", -1).put("message", "No Genshin Impact account card was returned."),
                        "No Genshin Impact account card was returned."
                    )
                }
            }

            val charIds = mutableListOf<Int>()
            if (charListRaw.optInt("retcode", -1) == 0) {
                val listArr = charListRaw.optJSONObject("data")?.optJSONArray("list")
                if (listArr != null) {
                    for (i in 0 until listArr.length()) {
                        val id = listArr.optJSONObject(i)?.optInt("id", 0) ?: 0
                        if (id != 0) charIds.add(id)
                    }
                }
            }

            val detailedChars = JSONArray()
            val detailFailures = mutableListOf<String>()
            if (charListRaw.optInt("retcode", -1) == 0 && charIds.isNotEmpty()) {
                val chunks = charIds.chunked(8)
                chunks.forEachIndexed { idx, chunk ->
                    onProgress("Fetching character builds (${idx + 1}/${chunks.size})...")
                    val idsArray = JSONArray()
                    chunk.forEach { idsArray.put(it) }
                    val detailPayload = JSONObject()
                        .put("role_id", cleanUid)
                        .put("server", server)
                        .put("character_ids", idsArray)
                    val detailJson = requestSafely {
                        fetchRawPost("$API_BASE/character/detail", detailPayload, cookie)
                    }
                    if (detailJson.optInt("retcode", -1) == 0) {
                        val dataObj = detailJson.optJSONObject("data")
                        extractPropertyMap(dataObj, propertyMap)
                        val items = dataObj?.optJSONArray("list")
                        if (items != null) {
                            for (i in 0 until items.length()) {
                                val cleanedChar = AccountExportDataPreserver.preserve(items.opt(i), propertyMap)
                                if (cleanedChar != null) detailedChars.put(cleanedChar)
                            }
                        } else {
                            detailFailures.add("Character detail batch ${idx + 1} returned no character list.")
                        }
                    } else {
                        detailFailures.add(
                            "Character detail batch ${idx + 1}: ${detailJson.optString("message", "Request failed")}"
                        )
                    }
                    if (idx < chunks.lastIndex) delay(200)
                }
            }

            val profileResult = sectionFromResponse(indexRaw, propertyMap)
            if (profileResult.status == "success" && profileResult.data is JSONObject && detailedChars.length() > 0) {
                profileResult.data.remove("avatars")
            }
            sectionResults["profile_and_exploration"] = profileResult
            sectionResults["daily_note"] = sectionFromResponse(dailyRaw, propertyMap)

            sectionResults["characters"] = when {
                charListRaw.optInt("retcode", -1) != 0 ->
                    sectionFromResponse(charListRaw, propertyMap)
                detailFailures.isNotEmpty() -> {
                    val fallback = if (detailedChars.length() > 0) {
                        detailedChars
                    } else {
                        sectionFromResponse(charListRaw, propertyMap).data ?: JSONObject()
                    }
                    ExportSectionResult.partial(fallback, detailFailures.joinToString(" "))
                }
                detailedChars.length() > 0 -> ExportSectionResult.success(detailedChars)
                else -> sectionFromResponse(charListRaw, propertyMap)
            }

            sectionResults["spiral_abyss_current"] = sectionFromResponse(abyssCurrentRaw, propertyMap)
            sectionResults["spiral_abyss_previous"] = sectionFromResponse(abyssPrevRaw, propertyMap)
            sectionResults["imaginarium_theater"] = sectionFromResponse(theaterRaw, propertyMap)

            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val root = AccountExportAssembler.assemble(
                uid = cleanUid,
                server = server,
                exportedAt = isoFormat.format(Date()),
                sections = sectionResults
            )

            Result.success(root.toString(2))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}