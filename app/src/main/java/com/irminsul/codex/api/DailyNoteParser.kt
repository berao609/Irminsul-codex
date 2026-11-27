package com.irminsul.codex.api

import org.json.JSONObject

/**
 * Parses the HoYoLAB Daily Note payload without treating absent or malformed
 * values as real zeroes or false values.
 */
internal object DailyNoteParser {

    fun parse(data: JSONObject): DailyNote {
        val transformer = data.optJSONObject("transformer")
        val recovery = transformer?.optJSONObject("recovery_time")
        val recoverySeconds = recovery?.let {
            val day = it.optionalLong("Day") ?: return@let null
            val hour = it.optionalLong("Hour") ?: return@let null
            val minute = it.optionalLong("Minute") ?: return@let null
            val second = it.optionalLong("Second") ?: return@let null
            if (day < 0L || hour !in 0L..23L || minute !in 0L..59L || second !in 0L..59L) {
                null
            } else {
                day * 86_400L + hour * 3_600L + minute * 60L + second
            }
        }
        return DailyNote(
            currentResin = data.optionalInt("current_resin"),
            maxResin = data.optionalInt("max_resin"),
            resinRecoveryTime = data.optionalLong("resin_recovery_time"),
            finishedTaskNum = data.optionalInt("finished_task_num"),
            totalTaskNum = data.optionalInt("total_task_num"),
            isExtraTaskRewardReceived = data.optionalBoolean("is_extra_task_reward_received"),
            remainResinDiscountNum = data.optionalInt("remain_resin_discount_num"),
            resinDiscountNumLimit = data.optionalInt("resin_discount_num_limit"),
            currentHomeCoin = data.optionalInt("current_home_coin"),
            maxHomeCoin = data.optionalInt("max_home_coin"),
            currentExpeditionNum = data.optionalInt("current_expedition_num"),
            maxExpeditionNum = data.optionalInt("max_expedition_num"),
            transformerObtained = transformer?.optionalBoolean("obtained"),
            transformerRecoveryTimeSeconds = recoverySeconds,
            transformerReached = transformer?.optionalBoolean("reached")
        )
    }

    private fun JSONObject.optionalInt(key: String): Int? {
        if (!has(key) || isNull(key)) return null
        return when (val value = opt(key)) {
            is Int -> value
            is Long -> value.takeIf { it >= Int.MIN_VALUE.toLong() && it <= Int.MAX_VALUE.toLong() }?.toInt()
            is Number -> value.toInt().takeIf { value.toDouble() == it.toDouble() }
            else -> null
        }
    }

    private fun JSONObject.optionalLong(key: String): Long? {
        if (!has(key) || isNull(key)) return null
        return when (val value = opt(key)) {
            is Number -> value.toLong().takeIf { value.toDouble() == it.toDouble() }
            is String -> value.toLongOrNull()
            else -> null
        }
    }

    private fun JSONObject.optionalBoolean(key: String): Boolean? {
        if (!has(key) || isNull(key)) return null
        return opt(key) as? Boolean
    }
}
