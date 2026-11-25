package com.irminsul.codex.data.mapper

import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.SectionStatus
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AccountExportMapperTest {
    private fun export(daily: String = """{"current_resin":0,"max_resin":200,"is_extra_task_reward_received":false}""") =
        JSONObject()
            .put("export_format_version", 1)
            .put("exported_at", "2026-11-25T08:00:00Z")
            .put("uid", "800000001")
            .put("server", "os_asia")
            .put("daily_note", JSONObject(daily))
            .put("characters", org.json.JSONArray().put(JSONObject().put("id", 10000002).put("level", 90)))
            .put("export_status", JSONObject()
                .put("overall_status", "complete")
                .put("sections", JSONObject()
                    .put("daily_note", JSONObject().put("status", "success").put("source", "HoYoLAB dailyNote").put("retrieved_at", "2026-11-25T07:59:00Z"))
                    .put("characters", JSONObject().put("status", "success"))))

    @Test fun mapsIdentitySnapshotAndAvailableZeroAndFalse() {
        val model = AccountExportMapper.map(export())
        assertEquals("800000001", model.identity.uid)
        assertEquals(SectionStatus.SUCCESS, model.overallStatus)
        val daily = (model.dailyNote as DataValue.Available).value
        assertEquals(DataValue.Available(0, java.time.Instant.parse("2026-11-25T07:59:00Z")), daily.currentResin)
        assertEquals(DataValue.Available(false, java.time.Instant.parse("2026-11-25T07:59:00Z")), daily.extraTaskRewardReceived)
    }

    @Test fun missingFieldsRemainMissingRatherThanZero() {
        val model = AccountExportMapper.map(export("""{"max_resin":200}"""))
        val daily = (model.dailyNote as DataValue.Available).value
        assertEquals(DataValue.Missing, daily.currentResin)
        assertEquals(DataValue.Missing, daily.extraTaskRewardReceived)
    }

    @Test fun mapsCharacterRosterWithoutChangingSource() {
        val source = export()
        val model = AccountExportMapper.map(source)
        val chars = (model.characters as DataValue.Available).value
        assertEquals(1, chars.size)
        assertEquals(10000002, chars.single().id)
        assertEquals(90, chars.single().level)
        assertTrue(source.has("characters"))
    }

    @Test fun rejectsMissingIdentityOrInvalidExportTimestamp() {
        val invalid = export().put("uid", "")
        try {
            AccountExportMapper.map(invalid)
            fail("Expected invalid identity to be rejected")
        } catch (_: IllegalArgumentException) { }
    }
}
