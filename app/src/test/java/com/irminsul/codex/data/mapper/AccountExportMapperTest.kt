package com.irminsul.codex.data.mapper

import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.SectionStatus
import com.irminsul.codex.domain.model.StructuredData
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

    @Test fun mapsProfileAbyssAndTheaterAsJsonIndependentStructuredData() {
        val source = export()
            .put("profile_and_exploration", JSONObject()
                .put("exploration", org.json.JSONArray().put(JSONObject().put("exploration_percentage", 100)))
                .put("achievement_number", 500)
                .put("optional_null", JSONObject.NULL))
            .put("spiral_abyss_current", JSONObject()
                .put("floors", org.json.JSONArray().put(JSONObject().put("star", 9))))
            .put("spiral_abyss_previous", JSONObject().put("total_battle_times", 12))
            .put("imaginarium_theater", JSONObject().put("difficulty", "hard"))
        val model = AccountExportMapper.map(source)

        val profile = (model.profileAndExploration as DataValue.Available).value
        assertTrue(profile is StructuredData.ObjectValue)
        val profileFields = (profile as StructuredData.ObjectValue).fields
        assertEquals(StructuredData.NumberValue("500"), profileFields["achievement_number"])
        assertEquals(StructuredData.NullValue, profileFields["optional_null"])
        val exploration = profileFields["exploration"] as StructuredData.ArrayValue
        assertEquals(1, exploration.items.size)

        val currentAbyss = (model.spiralAbyssCurrent as DataValue.Available).value
        assertTrue(currentAbyss is StructuredData.ObjectValue)
        val previousAbyss = (model.spiralAbyssPrevious as DataValue.Available).value
        assertTrue(previousAbyss is StructuredData.ObjectValue)
        val theater = (model.imaginariumTheater as DataValue.Available).value
        assertTrue(theater is StructuredData.ObjectValue)
        assertTrue(source.has("profile_and_exploration"))
    }

    @Test fun failedAndSkippedStructuredSectionsKeepTheirAvailabilityState() {
        val source = export()
            .put("export_status", JSONObject()
                .put("overall_status", "partial")
                .put("sections", JSONObject()
                    .put("profile_and_exploration", JSONObject().put("status", "failed").put("message", "request failed"))
                    .put("spiral_abyss_current", JSONObject().put("status", "skipped"))))
        val model = AccountExportMapper.map(source)
        assertTrue(model.profileAndExploration is DataValue.Failed)
        assertEquals(DataValue.Unavailable, model.spiralAbyssCurrent)
        assertEquals(DataValue.Missing, model.spiralAbyssPrevious)
    }

    @Test fun mapsAccountCardRosterAndRawCharacterDetailBatches() {
        val source = export()
            .put("account_card", JSONObject().put("nickname", "Traveler").put("level", 60))
            .put("character_roster_source", JSONObject()
                .put("list", org.json.JSONArray().put(JSONObject().put("id", 10000002).put("weapon", JSONObject().put("id", 11501)))))
            .put("character_detail_source", JSONObject()
                .put("batches", org.json.JSONArray().put(JSONObject()
                    .put("list", org.json.JSONArray().put(JSONObject()
                        .put("id", 10000002)
                        .put("weapon", JSONObject().put("id", 11501).put("level", 90))
                        .put("relics", org.json.JSONArray().put(JSONObject().put("id", 34501)))))
                    .put("property_map", JSONObject().put("1", "FIGHT_PROP_HP")))))
            .put("export_status", JSONObject()
                .put("overall_status", "complete")
                .put("sections", JSONObject()
                    .put("account_card", JSONObject().put("status", "success"))
                    .put("character_roster_source", JSONObject().put("status", "success"))
                    .put("character_detail_source", JSONObject().put("status", "success"))))
        val model = AccountExportMapper.map(source)

        val card = (model.accountCard as DataValue.Available).value as StructuredData.ObjectValue
        assertEquals(StructuredData.StringValue("Traveler"), card.fields["nickname"])

        val roster = (model.characterRosterSource as DataValue.Available).value as StructuredData.ObjectValue
        val rosterList = roster.fields["list"] as StructuredData.ArrayValue
        assertEquals(1, rosterList.items.size)

        val details = (model.characterDetailSource as DataValue.Available).value as StructuredData.ObjectValue
        val batches = details.fields["batches"] as StructuredData.ArrayValue
        val firstBatch = batches.items.single() as StructuredData.ObjectValue
        assertTrue(firstBatch.fields.containsKey("property_map"))
        val detailList = firstBatch.fields["list"] as StructuredData.ArrayValue
        val character = detailList.items.single() as StructuredData.ObjectValue
        assertTrue(character.fields.containsKey("weapon"))
        assertTrue(character.fields.containsKey("relics"))
        assertTrue(source.has("character_detail_source"))
    }

    @Test fun rawCharacterSourceSectionsPreserveFailedAndSkippedStates() {
        val source = export().put("export_status", JSONObject()
            .put("overall_status", "partial")
            .put("sections", JSONObject()
                .put("account_card", JSONObject().put("status", "skipped"))
                .put("character_roster_source", JSONObject().put("status", "failed").put("message", "roster failed"))))
        val model = AccountExportMapper.map(source)
        assertEquals(DataValue.Unavailable, model.accountCard)
        assertTrue(model.characterRosterSource is DataValue.Failed)
        assertEquals(DataValue.Missing, model.characterDetailSource)
    }

    @Test fun rejectsMissingIdentityOrInvalidExportTimestamp() {
        val invalid = export().put("uid", "")
        try {
            AccountExportMapper.map(invalid)
            fail("Expected invalid identity to be rejected")
        } catch (_: IllegalArgumentException) { }
    }
}
