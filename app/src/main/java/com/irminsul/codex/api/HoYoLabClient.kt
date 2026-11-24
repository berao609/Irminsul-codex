package com.irminsul.codex.api

import kotlinx.coroutines.CancellationException
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

private data class FetchedExportResponse(
    val response: JSONObject,
    val source: String,
    val retrievedAt: String
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

    private fun nowUtc(): String = SimpleDateFormat(
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        Locale.US
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date())

    private suspend fun requestSafely(
        source: String,
        request: suspend () -> JSONObject
    ): FetchedExportResponse {
        val response = try {
            request()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            JSONObject()
                .put("retcode", -1)
                .put("message", "Request failed (${error.javaClass.simpleName})")
        }
        return FetchedExportResponse(response, source, nowUtc())
    }

    private fun sectionFromResponse(
        fetched: FetchedExportResponse,
        sectionName: String,
        requiredArrayField: String? = null,
        propertyMap: Map<Int, String> = emptyMap()
    ): ExportSectionResult {
        val validationError = HoYoLabExportResponseValidator.validationError(
            fetched.response,
            sectionName,
            requiredArrayField
        )
        if (validationError != null) {
            val retcode = fetched.response.optInt("retcode", -1)
            val errorData = JSONObject()
                .put("retcode", retcode)
                .put("message", validationError)
            return ExportSectionResult.failed(
                errorData,
                validationError,
                fetched.source,
                fetched.retrievedAt
            )
        }

        val data = fetched.response.getJSONObject("data")
        return ExportSectionResult.success(
            AccountExportDataPreserver.preserve(data, propertyMap) ?: JSONObject(),
            fetched.source,
            fetched.retrievedAt
        )
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
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchDailyNote(uid: String, cookie: String): Result<DailyNote> = withContext(Dispatchers.IO) {
        try {
            val cleanUid = uid.trim()
            val server = detectServer(cleanUid)
            val json = fetchRawGet("$API_BASE/dailyNote?server=$server&role_id=$cleanUid", cookie)

            val validationError = HoYoLabExportResponseValidator.validationError(
                json,
                "Daily Note"
            )
            if (validationError != null) {
                return@withContext Result.failure(Exception(validationError))
            }

            Result.success(DailyNoteParser.parse(json.getJSONObject("data")))
        } catch (cancelled: CancellationException) {
            throw cancelled
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

            onProgress("Fetching chronicle, exploration & endgame...")
            val fetched = coroutineScope {
                val cardDeferred = async {
                    if (ltuid != null) {
                        requestSafely("HoYoLAB game record card") {
                            fetchRawGet("$RECORD_CARD_URL?uid=$ltuid", cookie)
                        }
                    } else null
                }
                val indexDeferred = async {
                    requestSafely("HoYoLAB index") {
                        fetchRawGet("$API_BASE/index?server=$server&role_id=$cleanUid", cookie)
                    }
                }
                val dailyDeferred = async {
                    requestSafely("HoYoLAB dailyNote") {
                        fetchRawGet("$API_BASE/dailyNote?server=$server&role_id=$cleanUid", cookie)
                    }
                }
                val listDeferred = async {
                    requestSafely("HoYoLAB character/list") {
                        val payload = JSONObject().put("role_id", cleanUid).put("server", server)
                        fetchRawPost("$API_BASE/character/list", payload, cookie)
                    }
                }
                val abyssCurDeferred = async {
                    requestSafely("HoYoLAB spiralAbyss schedule 1") {
                        fetchRawGet("$API_BASE/spiralAbyss?server=$server&role_id=$cleanUid&schedule_type=1", cookie)
                    }
                }
                val abyssPrevDeferred = async {
                    requestSafely("HoYoLAB spiralAbyss schedule 2") {
                        fetchRawGet("$API_BASE/spiralAbyss?server=$server&role_id=$cleanUid&schedule_type=2", cookie)
                    }
                }
                val theaterDeferred = async {
                    requestSafely("HoYoLAB role_combat") {
                        fetchRawGet("$API_BASE/role_combat?server=$server&role_id=$cleanUid&need_detail=true", cookie)
                    }
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

            val cardFetched = fetched[0]
            val indexFetched = fetched[1]!!
            val dailyFetched = fetched[2]!!
            val rosterFetched = fetched[3]!!
            val abyssCurrentFetched = fetched[4]!!
            val abyssPreviousFetched = fetched[5]!!
            val theaterFetched = fetched[6]!!

            val sectionResults = linkedMapOf<String, ExportSectionResult>()

            if (cardFetched == null) {
                sectionResults["account_card"] = ExportSectionResult.skipped(
                    "HoYoLAB account ID cookie was unavailable.",
                    "HoYoLAB game record card"
                )
            } else {
                val cardError = HoYoLabExportResponseValidator.validationError(
                    cardFetched.response,
                    "Account card",
                    "list"
                )
                if (cardError != null) {
                    sectionResults["account_card"] = ExportSectionResult.failed(
                        JSONObject()
                            .put("retcode", cardFetched.response.optInt("retcode", -1))
                            .put("message", cardError),
                        cardError,
                        cardFetched.source,
                        cardFetched.retrievedAt
                    )
                } else {
                    val cards = cardFetched.response.getJSONObject("data").getJSONArray("list")
                    var accountCard: Any? = null
                    for (i in 0 until cards.length()) {
                        val card = cards.optJSONObject(i)
                        if (card?.optInt("game_id") == 2) {
                            accountCard = AccountExportDataPreserver.preserve(card, propertyMap)
                            break
                        }
                    }
                    sectionResults["account_card"] = if (accountCard != null) {
                        ExportSectionResult.success(
                            accountCard,
                            cardFetched.source,
                            cardFetched.retrievedAt
                        )
                    } else {
                        ExportSectionResult.failed(
                            JSONObject().put("retcode", -1)
                                .put("message", "No Genshin Impact account card was returned."),
                            "No Genshin Impact account card was returned.",
                            cardFetched.source,
                            cardFetched.retrievedAt
                        )
                    }
                }
            }

            val rosterResult = sectionFromResponse(
                rosterFetched,
                "Character list",
                requiredArrayField = "list"
            )
            sectionResults["character_roster_source"] = rosterResult

            val charList = if (rosterResult.status == "success") {
                rosterFetched.response.getJSONObject("data").getJSONArray("list")
            } else JSONArray()
            val charIds = mutableListOf<Int>()
            for (i in 0 until charList.length()) {
                val id = charList.optJSONObject(i)?.optInt("id", 0) ?: 0
                if (id != 0) charIds.add(id)
            }

            val detailedChars = JSONArray()
            val rawDetailBatches = JSONArray()
            val detailFailures = mutableListOf<String>()
            var lastDetailFetched: FetchedExportResponse? = null

            if (rosterResult.status == "success" && charIds.isNotEmpty()) {
                val chunks = charIds.chunked(8)
                chunks.forEachIndexed { idx, chunk ->
                    onProgress("Fetching character builds (${idx + 1}/${chunks.size})...")
                    val idsArray = JSONArray()
                    chunk.forEach { idsArray.put(it) }
                    val detailPayload = JSONObject()
                        .put("role_id", cleanUid)
                        .put("server", server)
                        .put("character_ids", idsArray)
                    val detailFetched = requestSafely("HoYoLAB character/detail batch ${idx + 1}") {
                        fetchRawPost("$API_BASE/character/detail", detailPayload, cookie)
                    }
                    lastDetailFetched = detailFetched
                    val detailError = HoYoLabExportResponseValidator.validationError(
                        detailFetched.response,
                        "Character detail batch ${idx + 1}",
                        "list"
                    )
                    if (detailError == null) {
                        val dataObj = detailFetched.response.getJSONObject("data")
                        rawDetailBatches.put(AccountExportDataPreserver.preserve(dataObj) ?: JSONObject())
                        extractPropertyMap(dataObj, propertyMap)
                        val items = dataObj.getJSONArray("list")
                        for (i in 0 until items.length()) {
                            val cleanedChar = AccountExportDataPreserver.preserve(items.opt(i), propertyMap)
                            if (cleanedChar != null) detailedChars.put(cleanedChar)
                        }
                    } else {
                        detailFailures.add(detailError)
                    }
                    if (idx < chunks.lastIndex) delay(200)
                }
            }

            sectionResults["character_detail_source"] = when {
                rawDetailBatches.length() > 0 -> ExportSectionResult.success(
                    JSONObject().put("batches", rawDetailBatches),
                    "HoYoLAB character/detail raw data",
                    lastDetailFetched?.retrievedAt
                )
                lastDetailFetched != null -> ExportSectionResult.failed(
                    JSONObject().put("message", detailFailures.joinToString(" ")),
                    detailFailures.joinToString(" "),
                    "HoYoLAB character/detail raw data",
                    lastDetailFetched.retrievedAt
                )
                rosterResult.status != "success" -> ExportSectionResult.skipped(
                    "Character detail requests were skipped because the character roster was unavailable.",
                    "HoYoLAB character/detail"
                )
                else -> ExportSectionResult.skipped(
                    "The account returned no character IDs, so no detail batches were requested.",
                    "HoYoLAB character/detail"
                )
            }

            val profileResult = sectionFromResponse(
                indexFetched,
                "Profile and exploration",
                propertyMap = propertyMap
            )
            sectionResults["profile_and_exploration"] = profileResult
            sectionResults["daily_note"] = sectionFromResponse(
                dailyFetched,
                "Daily Note",
                propertyMap = propertyMap
            )

            sectionResults["characters"] = when {
                rosterResult.status != "success" -> ExportSectionResult.failed(
                    JSONObject().put("message", "Character roster retrieval failed."),
                    "Character roster retrieval failed.",
                    rosterFetched.source,
                    rosterFetched.retrievedAt
                )
                detailFailures.isNotEmpty() -> ExportSectionResult.partial(
                    detailedChars,
                    detailFailures.joinToString(" "),
                    "HoYoLAB character/detail",
                    lastDetailFetched?.retrievedAt ?: rosterFetched.retrievedAt
                )
                else -> ExportSectionResult.success(
                    detailedChars,
                    "HoYoLAB character/detail",
                    lastDetailFetched?.retrievedAt ?: rosterFetched.retrievedAt
                )
            }

            sectionResults["spiral_abyss_current"] = sectionFromResponse(
                abyssCurrentFetched,
                "Current Spiral Abyss",
                propertyMap = propertyMap
            )
            sectionResults["spiral_abyss_previous"] = sectionFromResponse(
                abyssPreviousFetched,
                "Previous Spiral Abyss",
                propertyMap = propertyMap
            )
            sectionResults["imaginarium_theater"] = sectionFromResponse(
                theaterFetched,
                "Imaginarium Theater",
                propertyMap = propertyMap
            )

            val exportTimestamp = nowUtc()
            val root = AccountExportAssembler.assemble(
                uid = cleanUid,
                server = server,
                exportedAt = exportTimestamp,
                sections = sectionResults
            )

            Result.success(root.toString(2))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
