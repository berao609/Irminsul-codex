package com.irminsul.codex.data.mapper

import com.irminsul.codex.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/**
 * Converts the versioned selected-section export into the application domain model.
 * Raw export JSON remains the archival/source representation and is not modified.
 */
object AccountExportMapper {
    fun map(root: JSONObject): AccountSnapshot {
        val exportedAt = parseInstant(root.optString("exported_at"))
            ?: throw IllegalArgumentException("Export is missing a valid exported_at timestamp.")
        val identity = AccountIdentity(
            uid = root.optString("uid").takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Export is missing uid."),
            server = root.optString("server").takeIf { it.isNotBlank() }
                ?: throw IllegalArgumentException("Export is missing server.")
        )
        val sectionsJson = root.optJSONObject("export_status")?.optJSONObject("sections")
            ?: JSONObject()
        val sections = linkedMapOf<String, AccountSectionMetadata>()
        val keys = sectionsJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val item = sectionsJson.optJSONObject(key) ?: continue
            sections[key] = AccountSectionMetadata(
                status = parseStatus(item.optString("status")),
                source = item.optString("source").takeIf { it.isNotBlank() },
                retrievedAt = parseInstant(item.optString("retrieved_at")),
                lastSuccessfulRetrievalAt = parseInstant(item.optString("last_successful_retrieval_at")),
                message = item.optString("message").takeIf { it.isNotBlank() }
            )
        }

        val daily = sections["daily_note"]
        val dailyData = root.optJSONObject("daily_note")
        val dailyValue = when {
            daily?.status == SectionStatus.FAILED -> DataValue.Failed(daily.message ?: "Daily Note retrieval failed.")
            dailyData == null -> if (daily?.status == SectionStatus.SKIPPED) DataValue.Unavailable else DataValue.Missing
            else -> DataValue.Available(mapDailyNote(dailyData, daily?.retrievedAt), daily?.retrievedAt)
        }

        val characterMeta = sections["characters"]
        val characterArray = root.optJSONArray("characters")
        val characterValue: DataValue<List<CharacterSnapshot>> = when {
            characterMeta?.status == SectionStatus.FAILED -> DataValue.Failed(characterMeta.message ?: "Character retrieval failed.")
            characterArray == null -> if (characterMeta?.status == SectionStatus.SKIPPED) DataValue.Unavailable else DataValue.Missing
            else -> DataValue.Available(mapCharacters(characterArray), characterMeta?.retrievedAt)
        }

        return AccountSnapshot(
            identity = identity,
            metadata = SnapshotMetadata(exportedAt, root.optInt("export_format_version", 0)),
            overallStatus = parseStatus(root.optJSONObject("export_status")?.optString("overall_status")),
            dailyNote = dailyValue,
            characters = characterValue,
            sections = sections
        )
    }

    private fun mapDailyNote(data: JSONObject, at: Instant?) = DailyNoteSnapshot(
        currentResin = intValue(data, "current_resin", at),
        maxResin = intValue(data, "max_resin", at),
        resinRecoveryTimeSeconds = longValue(data, "resin_recovery_time", at),
        finishedTaskCount = intValue(data, "finished_task_num", at),
        totalTaskCount = intValue(data, "total_task_num", at),
        extraTaskRewardReceived = booleanValue(data, "is_extra_task_reward_received", at),
        currentHomeCurrency = intValue(data, "current_home_coin", at),
        maxHomeCurrency = intValue(data, "max_home_coin", at)
    )

    private fun intValue(data: JSONObject, key: String, at: Instant?): DataValue<Int> {
        if (!data.has(key) || data.isNull(key)) return DataValue.Missing
        val value = data.opt(key)
        val number = (value as? Number)?.toLong()?.takeIf {
            it in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()
        }?.toInt() ?: return DataValue.Missing
        return DataValue.Available(number, at)
    }

    private fun longValue(data: JSONObject, key: String, at: Instant?): DataValue<Long> {
        if (!data.has(key) || data.isNull(key)) return DataValue.Missing
        val value = data.opt(key)
        val number = when (value) {
            is Number -> value.toLong().takeIf { value.toDouble() == it.toDouble() }
            is String -> value.toLongOrNull()
            else -> null
        } ?: return DataValue.Missing
        return DataValue.Available(number, at)
    }

    private fun booleanValue(data: JSONObject, key: String, at: Instant?): DataValue<Boolean> {
        if (!data.has(key) || data.isNull(key)) return DataValue.Missing
        val value = data.opt(key) as? Boolean ?: return DataValue.Missing
        return DataValue.Available(value, at)
    }

    private fun mapCharacters(array: JSONArray): List<CharacterSnapshot> =
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val id = item.optInt("id", 0)
            if (id <= 0) return@mapNotNull null
            CharacterSnapshot(
                id = id,
                name = item.optString("name").takeIf { it.isNotBlank() },
                level = item.optInt("level").takeIf { item.has("level") && !item.isNull("level") },
                rarity = item.optInt("rarity").takeIf { item.has("rarity") && !item.isNull("rarity") },
                element = item.optString("element").takeIf { it.isNotBlank() },
                friendshipLevel = item.optInt("fetter").takeIf { item.has("fetter") && !item.isNull("fetter") },
                activeConstellationCount = item.optInt("actived_constellation_num")
                    .takeIf { item.has("actived_constellation_num") && !item.isNull("actived_constellation_num") }
            )
        }

    private fun parseStatus(value: String?): SectionStatus = when (value?.lowercase()) {
        "success", "complete" -> SectionStatus.SUCCESS
        "partial" -> SectionStatus.PARTIAL
        "failed" -> SectionStatus.FAILED
        "skipped" -> SectionStatus.SKIPPED
        else -> SectionStatus.UNKNOWN
    }

    private fun parseInstant(value: String?): Instant? =
        try { value?.takeIf { it.isNotBlank() }?.let(Instant::parse) } catch (_: Exception) { null }
}
