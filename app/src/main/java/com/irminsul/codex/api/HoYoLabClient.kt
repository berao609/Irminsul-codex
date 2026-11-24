package com.irminsul.codex.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class DailyNote(
    val currentResin: Int,
    val maxResin: Int,
    val resinRecoveryTime: Long,
    val finishedTaskNum: Int,
    val totalTaskNum: Int,
    val isExtraTaskRewardReceived: Boolean,
    val remainResinDiscountNum: Int,
    val resinDiscountNumLimit: Int,
    val currentHomeCoin: Int,
    val maxHomeCoin: Int,
    val currentExpeditionNum: Int,
    val maxExpeditionNum: Int
)

object HoYoLabClient {
    private const val API_BASE = "https://sg-public-api.hoyolab.com/event/game_record/genshin/api"
    private const val RECORD_CARD_URL = "https://bbs-api-os.hoyolab.com/game_record/card/wapi/getGameRecordCard"
    private const val DS_SALT = "6s25p5ox5y14umn1p61aqyyvbvvl3lrt"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
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

    suspend fun fetchGenshinUid(cookie: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val ltuid = extractCookieValue(cookie, "ltuid_v2")
                ?: extractCookieValue(cookie, "ltuid")
                ?: return@withContext Result.failure(Exception("Missing ltuid_v2 in cookie."))

            val request = Request.Builder()
                .url("$RECORD_CARD_URL?uid=$ltuid")
                .get()
                .addHeader("DS", generateDs())
                .addHeader("x-rpc-app_version", "1.5.0")
                .addHeader("x-rpc-client_type", "5")
                .addHeader("x-rpc-language", "en-us")
                .addHeader("Referer", "https://act.hoyolab.com/")
                .addHeader("Origin", "https://act.hoyolab.com")
                .addHeader("Cookie", cookie.trim())
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response"))

                val json = JSONObject(body)
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
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchDailyNote(uid: String, cookie: String): Result<DailyNote> = withContext(Dispatchers.IO) {
        try {
            val cleanUid = uid.trim()
            val server = detectServer(cleanUid)
            val url = "$API_BASE/dailyNote?server=$server&role_id=$cleanUid"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("DS", generateDs())
                .addHeader("x-rpc-app_version", "1.5.0")
                .addHeader("x-rpc-client_type", "5")
                .addHeader("x-rpc-language", "en-us")
                .addHeader("Referer", "https://act.hoyolab.com/")
                .addHeader("Origin", "https://act.hoyolab.com")
                .addHeader("Cookie", cookie.trim())
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty response (HTTP ${response.code})"))

                val json = JSONObject(body)
                val retcode = json.optInt("retcode", -1)
                if (retcode != 0) {
                    val message = json.optString("message", "Unknown API error")
                    return@withContext Result.failure(Exception("Error $retcode: $message"))
                }

                val data = json.getJSONObject("data")
                Result.success(
                    DailyNote(
                        currentResin = data.optInt("current_resin", 0),
                        maxResin = data.optInt("max_resin", 200),
                        resinRecoveryTime = data.optString("resin_recovery_time", "0").toLongOrNull() ?: 0L,
                        finishedTaskNum = data.optInt("finished_task_num", 0),
                        totalTaskNum = data.optInt("total_task_num", 4),
                        isExtraTaskRewardReceived = data.optBoolean("is_extra_task_reward_received", false),
                        remainResinDiscountNum = data.optInt("remain_resin_discount_num", 0),
                        resinDiscountNumLimit = data.optInt("resin_discount_num_limit", 3),
                        currentHomeCoin = data.optInt("current_home_coin", 0),
                        maxHomeCoin = data.optInt("max_home_coin", 2400),
                        currentExpeditionNum = data.optInt("current_expedition_num", 0),
                        maxExpeditionNum = data.optInt("max_expedition_num", 5)
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
