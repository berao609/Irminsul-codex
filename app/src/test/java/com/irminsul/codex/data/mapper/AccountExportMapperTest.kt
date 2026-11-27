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

    @Test fun mapsCharacterAscensionRankFromBaseOrDetailPayload() {
        val source = export().put("characters", org.json.JSONArray()
            .put(JSONObject()
                .put("base", JSONObject().put("id", 10000002).put("level", 80).put("promote_level", 6)))
            .put(JSONObject()
                .put("base", JSONObject().put("id", 10000003).put("level", 70))
                .put("promote_level", 5)))
        val characters = (AccountExportMapper.map(source).characters as DataValue.Available).value
        assertEquals(6, characters[0].ascensionLevel)
        assertEquals(5, characters[1].ascensionLevel)
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

    @Test fun mapsStygianOnslaughtFromNestedBattleChronicleIndexStats() {
        val source = export().put("profile_and_exploration", JSONObject()
            .put("stats", JSONObject()
                .put("hard_challenge", JSONObject().put("name", "Challenge Alpha"))
                .put("difficulty", 6)))
        val snapshot = AccountExportMapper.map(source)
        val stygian = snapshot.stygianOnslaught as DataValue.Available
        assertEquals("Challenge Alpha", stygian.value.bestRecordName)
        assertEquals("6", stygian.value.difficulty)
        assertTrue(source.has("profile_and_exploration"))
    }

    @Test fun missingStygianFieldsRemainMissingAndFailedIndexRemainsFailed() {
        val missing = export().put("profile_and_exploration", JSONObject()
            .put("stats", JSONObject().put("hard_challenge", JSONObject())))
        assertEquals(DataValue.Missing, AccountExportMapper.map(missing).stygianOnslaught)

        val failed = export().put("export_status", JSONObject()
            .put("overall_status", "partial")
            .put("sections", JSONObject()
                .put("profile_and_exploration", JSONObject()
                    .put("status", "failed")
                    .put("message", "index request failed"))))
        val stygian = AccountExportMapper.map(failed).stygianOnslaught
        assertTrue(stygian is DataValue.Failed)
        assertEquals("index request failed", (stygian as DataValue.Failed).reason)
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

    @Test fun mapsCharacterAscensionLevelAndPreservesZeroAndMissing() {
        val ascensionZero = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 10000003).put("level", 20).put("promote_level", 0))))
        val zeroCharacter = (AccountExportMapper.map(ascensionZero).characters as DataValue.Available).value.single()
        assertEquals(0, zeroCharacter.ascensionLevel)

        val unavailable = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 10000004).put("level", 20))))
        val unavailableCharacter = (AccountExportMapper.map(unavailable).characters as DataValue.Available).value.single()
        assertNull(unavailableCharacter.ascensionLevel)
    }

    @Test fun mapsOptionalCharacterPortraitIconWithoutRequiringIt() {
        val withIcon = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 10000003).put("name", "Jean")
                .put("icon", "https://example.invalid/jean.png"))))
        val mapped = (AccountExportMapper.map(withIcon).characters as DataValue.Available).value.single()
        assertEquals("https://example.invalid/jean.png", mapped.avatarIcon)

        val withoutIcon = (AccountExportMapper.map(export()).characters as DataValue.Available).value.single()
        assertNull(withoutIcon.avatarIcon)
    }

    @Test fun mapsTypedCharacterWeaponArtifactsSkillsAndStats() {
        val source = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 10000002).put("name", "Character").put("level", 90)
                .put("rarity", 5).put("element", "Wind").put("fetter", 10).put("actived_constellation_num", 2))
            .put("weapon", JSONObject().put("id", 11501).put("name", "Weapon").put("level", 90)
                .put("promote_level", 6).put("affix_level", 2)
                .put("main_property", JSONObject().put("property_type", 2000).put("base", "100").put("add", "20").put("final", "120")))
            .put("relics", org.json.JSONArray().put(JSONObject().put("id", 34501).put("pos", 1).put("level", 20)
                .put("main_property", JSONObject().put("property_type", 1).put("value", "46.6").put("times", 0))
                .put("sub_property_list", org.json.JSONArray().put(JSONObject().put("property_type", 2).put("value", "5.8").put("times", 2)))))
            .put("constellations", org.json.JSONArray().put(JSONObject().put("id", 1).put("is_actived", true)))
            .put("skills", org.json.JSONArray().put(JSONObject().put("skill_id", 10).put("level", 8).put("is_unlock", true)))
            .put("selected_properties", org.json.JSONArray().put(JSONObject().put("property_type", 2000).put("final", "120")))))
        val mapped = (AccountExportMapper.map(source).characters as DataValue.Available).value.single()
        assertEquals(10000002, mapped.id)
        val weapon = (mapped.weapon as DataValue.Available).value
        assertEquals(11501, weapon.id)
        assertEquals("120", weapon.mainProperty?.final)
        val artifacts = (mapped.artifacts as DataValue.Available).value
        assertEquals(1, artifacts.size)
        assertEquals(0, artifacts.single().mainProperty?.rolls)
        assertEquals(2, artifacts.single().subProperties.single().rolls)
        assertEquals(listOf(2, 3, 4, 5), mapped.equipmentIntegrity.unoccupiedArtifactSlots)
        assertEquals(0, mapped.equipmentIntegrity.invalidArtifactEntryIndexes.size)
        assertTrue(mapped.constellations.single().isActive == true)
        assertEquals(8, mapped.skills.single().level)
        assertEquals("120", mapped.selectedProperties.single().final)
        assertTrue(source.has("characters"))
    }

    @Test fun incompleteEquipmentDoesNotInventIdentifiersOrValues() {
        val source = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 10000002))
            .put("weapon", JSONObject().put("name", "Unknown weapon"))
            .put("relics", org.json.JSONArray().put(JSONObject().put("name", "Unknown artifact")))))
        val mapped = (AccountExportMapper.map(source).characters as DataValue.Available).value.single()
        assertTrue(mapped.weapon is DataValue.Failed)
        assertTrue(mapped.artifacts is DataValue.Available)
        assertTrue((mapped.artifacts as DataValue.Available).value.isEmpty())
    }


    @Test fun preservesWeaponToCharacterAssociationsAcrossMultipleCharacters() {
        val source = export().put("characters", org.json.JSONArray()
            .put(JSONObject().put("base", JSONObject().put("id", 10000002))
                .put("weapon", JSONObject().put("id", 11501)))
            .put(JSONObject().put("base", JSONObject().put("id", 10000003))
                .put("weapon", JSONObject().put("id", 12501))))
        val chars = (AccountExportMapper.map(source).characters as DataValue.Available).value
        assertEquals(2, chars.size)
        assertEquals(11501, ((chars[0].weapon as DataValue.Available).value).id)
        assertEquals(12501, ((chars[1].weapon as DataValue.Available).value).id)
    }

    @Test fun distinguishesMissingEmptyAndMalformedEquipmentCollections() {
        val source = export().put("characters", org.json.JSONArray()
            .put(JSONObject().put("base", JSONObject().put("id", 1)))
            .put(JSONObject().put("base", JSONObject().put("id", 2)).put("relics", org.json.JSONArray()))
            .put(JSONObject().put("base", JSONObject().put("id", 3)).put("relics", "not-an-array"))
            .put(JSONObject().put("base", JSONObject().put("id", 4)).put("weapon", "not-an-object")))
        val chars = (AccountExportMapper.map(source).characters as DataValue.Available).value
        assertEquals(DataValue.Missing, chars[0].artifacts)
        assertEquals(DataValue.Missing, chars[0].weapon)
        assertEquals(DataValue.Available(emptyList<Any>(), null)::class, (chars[1].artifacts as DataValue.Available).let { it::class })
        assertTrue(chars[2].artifacts is DataValue.Failed)
        assertTrue(chars[3].weapon is DataValue.Failed)
    }

    @Test fun reportsDuplicateArtifactSlotsIdsAndUnoccupiedSlotsWithoutDroppingRecords() {
        val relics = org.json.JSONArray()
            .put(JSONObject().put("id", 10).put("pos", 1)
                .put("sub_property_list", org.json.JSONArray().put(JSONObject().put("value", "3.5"))))
            .put(JSONObject().put("id", 10).put("pos", 1))
            .put(JSONObject().put("name", "missing id").put("pos", 8))
        val source = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 10000002)).put("relics", relics)))
        val character = (AccountExportMapper.map(source).characters as DataValue.Available).value.single()
        val mapped = (character.artifacts as DataValue.Available).value
        assertEquals(2, mapped.size)
        assertEquals(listOf(10), character.equipmentIntegrity.duplicateArtifactIds)
        assertEquals(listOf(1), character.equipmentIntegrity.duplicateArtifactSlots)
        assertEquals(listOf(2, 3, 4, 5), character.equipmentIntegrity.unoccupiedArtifactSlots)
        assertEquals(listOf(2), character.equipmentIntegrity.invalidArtifactEntryIndexes)
        assertEquals(listOf(0), character.equipmentIntegrity.invalidSubPropertyEntryIndexes)
    }

    @Test fun reportsMalformedAndDuplicateCharacterEntries() {
        val source = export().put("characters", org.json.JSONArray()
            .put(JSONObject().put("base", JSONObject().put("id", 7)))
            .put(JSONObject().put("base", JSONObject().put("id", 7)))
            .put(JSONObject().put("base", JSONObject().put("name", "missing id")))
            .put("not-an-object"))
        val model = AccountExportMapper.map(source)
        assertEquals(2, (model.characters as DataValue.Available).value.size)
        assertEquals(4, model.characterCollectionIntegrity.sourceEntryCount)
        assertEquals(2, model.characterCollectionIntegrity.mappedEntryCount)
        assertEquals(listOf(2, 3), model.characterCollectionIntegrity.invalidCharacterEntryIndexes)
        assertEquals(listOf(7), model.characterCollectionIntegrity.duplicateCharacterIds)
    }

    @Test fun malformedCharacterContainerIsFailedRatherThanMissing() {
        val source = export().put("characters", "not-an-array")
        assertTrue(AccountExportMapper.map(source).characters is DataValue.Failed)
    }


    @Test fun mapperAttachesStatClassificationAndPreservesCharacterSectionState() {
        val source = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 10000002))
            .put("selected_properties", org.json.JSONArray()
                .put(JSONObject().put("property_type", 3).put("stat_name", "FIGHT_PROP_HP_PERCENT")))))
        val model = AccountExportMapper.map(source)
        val report = (model.characterStatClassification as DataValue.Available).value
        assertEquals(1, report.classifiedCount)
        assertEquals(0, report.conflictCount)
        val failed = export().put("export_status", JSONObject().put("overall_status", "partial")
            .put("sections", JSONObject().put("characters", JSONObject().put("status", "failed").put("message", "offline"))))
        assertTrue(AccountExportMapper.map(failed).characterStatClassification is DataValue.Failed)
    }

    @Test fun mapperAttachesStatReconciliationAndPreservesCharacterSectionState() {
        val source = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 10000002).put("level", 90).put("rarity", 5))
            .put("selected_properties", org.json.JSONArray()
                .put(JSONObject().put("property_type", 2000).put("stat_name", "ATK")
                    .put("base", "100").put("add", "20").put("final", "120")))))
        val model = AccountExportMapper.map(source)
        val report = (model.characterStatReconciliation as DataValue.Available).value
        assertEquals(1, report.matchCount)
        assertEquals(0, report.mismatchCount)
        val failed = export().put("export_status", JSONObject().put("overall_status", "partial")
            .put("sections", JSONObject().put("characters", JSONObject().put("status", "failed").put("message", "offline"))))
        assertTrue(AccountExportMapper.map(failed).characterStatReconciliation is DataValue.Failed)
    }

    @Test fun mapperAttachesBuildValidationAndPreservesCharacterSectionState() {
        val source = export().put("characters", org.json.JSONArray().put(JSONObject()
            .put("base", JSONObject().put("id", 1).put("level", 90).put("rarity", 5)
                .put("fetter", 10).put("actived_constellation_num", 6))
            .put("weapon", JSONObject().put("id", 11501).put("level", 90).put("rarity", 5)
                .put("promote_level", 6).put("affix_level", 1))
            .put("relics", org.json.JSONArray())))
        val report = (AccountExportMapper.map(source).characterBuildValidation as DataValue.Available).value
        assertEquals(1, report.characters.size)
        assertEquals(0, report.totalErrors)
        assertTrue(report.characters.single().warningCount == 0)
        val failed = export().put("export_status", JSONObject().put("overall_status", "partial")
            .put("sections", JSONObject().put("characters", JSONObject().put("status", "failed").put("message", "offline"))))
        assertTrue(AccountExportMapper.map(failed).characterBuildValidation is DataValue.Failed)
    }

}
