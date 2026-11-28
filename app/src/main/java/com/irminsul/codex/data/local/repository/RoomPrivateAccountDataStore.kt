package com.irminsul.codex.data.local.repository

import androidx.room.withTransaction
import com.irminsul.codex.api.AccountProfile
import com.irminsul.codex.api.DailyNote
import com.irminsul.codex.data.local.IrminsulDatabase
import com.irminsul.codex.data.local.entity.PrivateAccountDataEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant

/**
 * Persists non-credential profile and Daily Note presentation data in app-private Room.
 * Each channel has independent payload and freshness columns, so one update cannot
 * overwrite or invalidate its sibling.
 */
class RoomPrivateAccountDataStore(
    private val database: IrminsulDatabase,
    private val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default
) : PrivateAccountDataStore {

    override suspend fun get(uid: String): PrivateAccountDataCache? {
        val entity = database.privateAccountDataDao().get(uid) ?: return null
        return withContext(cpuDispatcher) {
            val daily = decodeChannel(entity.dailyNoteJson, entity.dailyNoteRetrievedAt, "Daily Note", ::decodeDailyNote)
            val profile = decodeChannel(entity.accountProfileJson, entity.accountProfileRetrievedAt, "HoYoLAB profile", ::decodeAccountProfile)
            PrivateAccountDataCache(
                uid = uid,
                dailyNote = daily.value,
                accountProfile = profile.value,
                dailyNoteError = daily.error,
                accountProfileError = profile.error
            )
        }
    }

    override suspend fun saveDailyNote(uid: String, value: DailyNote, retrievedAt: Instant) {
        val json = withContext(cpuDispatcher) { encodeDailyNote(value).toString() }
        database.withTransaction {
            val dao = database.privateAccountDataDao()
            dao.insertIfMissing(PrivateAccountDataEntity(uid = uid))
            dao.updateDailyNote(uid, json, retrievedAt.toString())
        }
    }

    override suspend fun saveAccountProfile(uid: String, value: AccountProfile, retrievedAt: Instant) {
        val json = withContext(cpuDispatcher) { encodeAccountProfile(value).toString() }
        database.withTransaction {
            val dao = database.privateAccountDataDao()
            dao.insertIfMissing(PrivateAccountDataEntity(uid = uid))
            dao.updateAccountProfile(uid, json, retrievedAt.toString())
        }
    }

    private data class DecodedChannel<T>(val value: CachedPrivateData<T>?, val error: String?)

    private fun <T> decodeChannel(
        json: String?,
        timestamp: String?,
        channelName: String,
        decoder: (JSONObject) -> T
    ): DecodedChannel<T> {
        if (json == null && timestamp == null) return DecodedChannel(null, null)
        if (json.isNullOrBlank() || timestamp.isNullOrBlank()) {
            return DecodedChannel(null, "Saved $channelName cache is incomplete.")
        }
        return try {
            DecodedChannel(CachedPrivateData(decoder(JSONObject(json)), Instant.parse(timestamp)), null)
        } catch (error: Exception) {
            DecodedChannel(null, "Saved $channelName cache is malformed: ${error.message ?: "invalid data"}")
        }
    }

    private fun encodeDailyNote(value: DailyNote): JSONObject = JSONObject().apply {
        putNullable("currentResin", value.currentResin)
        putNullable("maxResin", value.maxResin)
        putNullable("resinRecoveryTime", value.resinRecoveryTime)
        putNullable("finishedTaskNum", value.finishedTaskNum)
        putNullable("totalTaskNum", value.totalTaskNum)
        putNullable("isExtraTaskRewardReceived", value.isExtraTaskRewardReceived)
        putNullable("remainResinDiscountNum", value.remainResinDiscountNum)
        putNullable("resinDiscountNumLimit", value.resinDiscountNumLimit)
        putNullable("currentHomeCoin", value.currentHomeCoin)
        putNullable("maxHomeCoin", value.maxHomeCoin)
        putNullable("currentExpeditionNum", value.currentExpeditionNum)
        putNullable("maxExpeditionNum", value.maxExpeditionNum)
        putNullable("transformerObtained", value.transformerObtained)
        putNullable("transformerRecoveryTimeSeconds", value.transformerRecoveryTimeSeconds)
        putNullable("transformerReached", value.transformerReached)
    }

    private fun decodeDailyNote(json: JSONObject) = DailyNote(
        currentResin = json.intOrNull("currentResin"),
        maxResin = json.intOrNull("maxResin"),
        resinRecoveryTime = json.longOrNull("resinRecoveryTime"),
        finishedTaskNum = json.intOrNull("finishedTaskNum"),
        totalTaskNum = json.intOrNull("totalTaskNum"),
        isExtraTaskRewardReceived = json.booleanOrNull("isExtraTaskRewardReceived"),
        remainResinDiscountNum = json.intOrNull("remainResinDiscountNum"),
        resinDiscountNumLimit = json.intOrNull("resinDiscountNumLimit"),
        currentHomeCoin = json.intOrNull("currentHomeCoin"),
        maxHomeCoin = json.intOrNull("maxHomeCoin"),
        currentExpeditionNum = json.intOrNull("currentExpeditionNum"),
        maxExpeditionNum = json.intOrNull("maxExpeditionNum"),
        transformerObtained = json.booleanOrNull("transformerObtained"),
        transformerRecoveryTimeSeconds = json.longOrNull("transformerRecoveryTimeSeconds"),
        transformerReached = json.booleanOrNull("transformerReached")
    )

    private fun encodeAccountProfile(value: AccountProfile): JSONObject = JSONObject().apply {
        put("nickname", value.nickname ?: JSONObject.NULL)
        put("uid", value.uid)
        put("serverName", value.serverName ?: JSONObject.NULL)
        put("avatarUrl", value.avatarUrl ?: JSONObject.NULL)
    }

    private fun decodeAccountProfile(json: JSONObject) = AccountProfile(
        nickname = json.stringOrNull("nickname"),
        uid = json.optString("uid").takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Profile UID is missing."),
        serverName = json.stringOrNull("serverName"),
        avatarUrl = json.stringOrNull("avatarUrl")
    )

    private fun JSONObject.putNullable(key: String, value: Any?) {
        put(key, value ?: JSONObject.NULL)
    }

    private fun JSONObject.intOrNull(key: String): Int? =
        if (!has(key) || isNull(key)) null else (opt(key) as? Number)?.toInt()

    private fun JSONObject.longOrNull(key: String): Long? =
        if (!has(key) || isNull(key)) null else (opt(key) as? Number)?.toLong()

    private fun JSONObject.booleanOrNull(key: String): Boolean? =
        if (!has(key) || isNull(key)) null else opt(key) as? Boolean

    private fun JSONObject.stringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else optString(key)
}
