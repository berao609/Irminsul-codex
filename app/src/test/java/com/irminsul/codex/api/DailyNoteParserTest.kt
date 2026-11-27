package com.irminsul.codex.api

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyNoteParserTest {

    @Test
    fun parsesValidDailyNoteValues() {
        val note = DailyNoteParser.parse(
            JSONObject(
                """
                {
                  "current_resin": 120,
                  "max_resin": 200,
                  "resin_recovery_time": "14400",
                  "finished_task_num": 3,
                  "total_task_num": 4,
                  "is_extra_task_reward_received": true,
                  "remain_resin_discount_num": 2,
                  "resin_discount_num_limit": 3,
                  "current_home_coin": 1800,
                  "max_home_coin": 2400,
                  "current_expedition_num": 5,
                  "max_expedition_num": 5,
                  "transformer": {
                    "obtained": true,
                    "reached": false,
                    "recovery_time": {"Day": 1, "Hour": 2, "Minute": 3, "Second": 4}
                  }
                }
                """.trimIndent()
            )
        )

        assertEquals(120, note.currentResin)
        assertEquals(200, note.maxResin)
        assertEquals(14400L, note.resinRecoveryTime)
        assertEquals(3, note.finishedTaskNum)
        assertEquals(4, note.totalTaskNum)
        assertTrue(note.isExtraTaskRewardReceived!!)
        assertEquals(2, note.remainResinDiscountNum)
        assertEquals(3, note.resinDiscountNumLimit)
        assertEquals(1800, note.currentHomeCoin)
        assertEquals(2400, note.maxHomeCoin)
        assertEquals(5, note.currentExpeditionNum)
        assertEquals(5, note.maxExpeditionNum)
        assertTrue(note.transformerObtained!!)
        assertFalse(note.transformerReached!!)
        assertEquals(93_784L, note.transformerRecoveryTimeSeconds)
    }

    @Test
    fun preservesRealZeroValues() {
        val note = DailyNoteParser.parse(
            JSONObject("""{"current_resin":0,"finished_task_num":0,"resin_recovery_time":"0","is_extra_task_reward_received":false}""")
        )

        assertEquals(0, note.currentResin)
        assertEquals(0, note.finishedTaskNum)
        assertEquals(0L, note.resinRecoveryTime)
        assertFalse(note.isExtraTaskRewardReceived!!)
    }

    @Test
    fun missingAndNullValuesRemainUnavailable() {
        val note = DailyNoteParser.parse(
            JSONObject("""{"current_resin":null,"max_resin":200}""")
        )

        assertNull(note.currentResin)
        assertEquals(200, note.maxResin)
        assertNull(note.resinRecoveryTime)
        assertNull(note.finishedTaskNum)
        assertNull(note.totalTaskNum)
        assertNull(note.isExtraTaskRewardReceived)
        assertNull(note.remainResinDiscountNum)
        assertNull(note.resinDiscountNumLimit)
        assertNull(note.currentHomeCoin)
        assertNull(note.maxHomeCoin)
        assertNull(note.currentExpeditionNum)
        assertNull(note.maxExpeditionNum)
        assertNull(note.transformerObtained)
        assertNull(note.transformerRecoveryTimeSeconds)
        assertNull(note.transformerReached)
    }

    @Test
    fun malformedTypesRemainUnavailable() {
        val note = DailyNoteParser.parse(
            JSONObject(
                """{"current_resin":"not-a-number","max_resin":200.5,"resin_recovery_time":"unknown","is_extra_task_reward_received":"yes"}"""
            )
        )

        assertNull(note.currentResin)
        assertNull(note.maxResin)
        assertNull(note.resinRecoveryTime)
        assertNull(note.isExtraTaskRewardReceived)
    }
    @Test
    fun preservesReadyParametricTransformerState() {
        val note = DailyNoteParser.parse(
            JSONObject("""{"transformer":{"obtained":true,"reached":true,"recovery_time":{"Day":0,"Hour":0,"Minute":0,"Second":0}}}""")
        )

        assertTrue(note.transformerObtained!!)
        assertTrue(note.transformerReached!!)
        assertEquals(0L, note.transformerRecoveryTimeSeconds)
    }

    @Test
    fun malformedTransformerCooldownRemainsUnavailable() {
        val note = DailyNoteParser.parse(
            JSONObject("""{"transformer":{"obtained":true,"reached":false,"recovery_time":{"Day":0,"Hour":24,"Minute":0,"Second":0}}}""")
        )

        assertTrue(note.transformerObtained!!)
        assertFalse(note.transformerReached!!)
        assertNull(note.transformerRecoveryTimeSeconds)
    }

}
