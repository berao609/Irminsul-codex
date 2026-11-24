package com.irminsul.codex.api

import org.json.JSONObject

/**
 * Parses the HoYoLAB Daily Note payload without treating absent or malformed
 * values as real zeroes or false values.
 */
internal object DailyNoteParser {

    fun parse(data: JSONObject): DailyNote = DailyNote(
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
        maxExpeditionNum = data.optionalInt("max_expedition_num")
    )

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
