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
        val sectionsJson = root.optJSONObject("export_status")?.optJSONObject("sections") ?: JSONObject()
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
        val characterMapping = characterArray?.let(::mapCharacters)
        val characterValue: DataValue<List<CharacterSnapshot>> = when {
            characterMeta?.status == SectionStatus.FAILED -> DataValue.Failed(characterMeta.message ?: "Character retrieval failed.")
            characterArray == null -> when {
                characterMeta?.status == SectionStatus.SKIPPED -> DataValue.Unavailable
                root.has("characters") && !root.isNull("characters") -> DataValue.Failed("Character section has an invalid container type.")
                else -> DataValue.Missing
            }
            else -> DataValue.Available(characterMapping!!.first, characterMeta?.retrievedAt)
        }

        return AccountSnapshot(
            identity = identity,
            metadata = SnapshotMetadata(exportedAt, root.optInt("export_format_version", 0)),
            overallStatus = parseStatus(root.optJSONObject("export_status")?.optString("overall_status")),
            dailyNote = dailyValue,
            characters = characterValue,
            accountCard = mapStructuredSection(root, "account_card", sections),
            characterRosterSource = mapStructuredSection(root, "character_roster_source", sections),
            characterDetailSource = mapStructuredSection(root, "character_detail_source", sections),
            profileAndExploration = mapStructuredSection(root, "profile_and_exploration", sections),
            spiralAbyssCurrent = mapStructuredSection(root, "spiral_abyss_current", sections),
            spiralAbyssPrevious = mapStructuredSection(root, "spiral_abyss_previous", sections),
            imaginariumTheater = mapStructuredSection(root, "imaginarium_theater", sections),
            sections = sections,
            characterCollectionIntegrity = characterMapping?.second ?: CharacterCollectionIntegrity()
        )
    }

    private fun mapStructuredSection(root: JSONObject, key: String, sections: Map<String, AccountSectionMetadata>): DataValue<StructuredData> {
        val metadata = sections[key]
        if (metadata?.status == SectionStatus.FAILED) return DataValue.Failed(metadata.message ?: "$key retrieval failed.")
        val raw = root.opt(key)
        if (raw == null || raw == JSONObject.NULL) {
            return if (metadata?.status == SectionStatus.SKIPPED) DataValue.Unavailable else DataValue.Missing
        }
        return DataValue.Available(toStructuredData(raw), metadata?.retrievedAt)
    }

    private fun toStructuredData(value: Any?): StructuredData = when (value) {
        null, JSONObject.NULL -> StructuredData.NullValue
        is JSONObject -> {
            val fields = linkedMapOf<String, StructuredData>()
            val keys = value.keys()
            while (keys.hasNext()) { val key = keys.next(); fields[key] = toStructuredData(value.opt(key)) }
            StructuredData.ObjectValue(fields)
        }
        is JSONArray -> StructuredData.ArrayValue((0 until value.length()).map { toStructuredData(value.opt(it)) })
        is String -> StructuredData.StringValue(value)
        is Boolean -> StructuredData.BooleanValue(value)
        is Number -> StructuredData.NumberValue(value.toString())
        else -> StructuredData.StringValue(value.toString())
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

    private fun mapCharacters(array: JSONArray): Pair<List<CharacterSnapshot>, CharacterCollectionIntegrity> {
        val mapped = mutableListOf<CharacterSnapshot>()
        val invalidIndexes = mutableListOf<Int>()
        val ids = mutableListOf<Int>()

        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index)
            if (item == null) {
                invalidIndexes += index
                continue
            }
            val base = item.optJSONObject("base") ?: item
            val id = base.intOrNull("id")
            if (id == null) {
                invalidIndexes += index
                continue
            }
            ids += id
            mapped += mapCharacter(item, base, id)
        }

        val duplicates = ids.groupingBy { it }.eachCount()
            .filterValues { it > 1 }.keys.sorted()
        return mapped to CharacterCollectionIntegrity(
            sourceEntryCount = array.length(),
            mappedEntryCount = mapped.size,
            invalidCharacterEntryIndexes = invalidIndexes,
            duplicateCharacterIds = duplicates
        )
    }

    private fun mapCharacter(item: JSONObject, base: JSONObject, id: Int): CharacterSnapshot {
        val weapon = when {
            !item.has("weapon") || item.isNull("weapon") -> DataValue.Missing
            item.opt("weapon") !is JSONObject -> DataValue.Failed("Character weapon has an invalid object shape.")
            else -> mapWeapon(item.optJSONObject("weapon")!!)
                ?.let { DataValue.Available(it, null) }
                ?: DataValue.Failed("Character weapon is missing a valid identifier.")
        }

        val relicValue: DataValue<List<ArtifactSnapshot>>
        val equipmentIntegrity: EquipmentIntegritySnapshot
        when {
            !item.has("relics") || item.isNull("relics") -> {
                relicValue = DataValue.Missing
                equipmentIntegrity = EquipmentIntegritySnapshot()
            }
            item.opt("relics") !is JSONArray -> {
                relicValue = DataValue.Failed("Character artifact collection has an invalid array shape.")
                equipmentIntegrity = EquipmentIntegritySnapshot()
            }
            else -> {
                val (artifacts, integrity) = mapArtifacts(item.optJSONArray("relics")!!)
                relicValue = DataValue.Available(artifacts, null)
                equipmentIntegrity = integrity
            }
        }

        return CharacterSnapshot(
            id = id,
            name = base.stringOrNull("name"),
            level = base.intOrNull("level"),
            rarity = base.intOrNull("rarity"),
            element = base.stringOrNull("element"),
            friendshipLevel = base.intOrNull("fetter"),
            activeConstellationCount = base.intOrNull("actived_constellation_num"),
            weapon = weapon,
            artifacts = relicValue,
            equipmentIntegrity = equipmentIntegrity,
            constellations = item.optJSONArray("constellations")?.let(::mapConstellations).orEmpty(),
            skills = item.optJSONArray("skills")?.let(::mapSkills).orEmpty(),
            selectedProperties = item.optJSONArray("selected_properties")?.let(::mapStats).orEmpty(),
            baseProperties = item.optJSONArray("base_properties")?.let(::mapStats).orEmpty(),
            extraProperties = item.optJSONArray("extra_properties")?.let(::mapStats).orEmpty(),
            elementProperties = item.optJSONArray("element_properties")?.let(::mapStats).orEmpty()
        )
    }

    private fun mapWeapon(json: JSONObject): WeaponSnapshot? {
        val id = json.intOrNull("id") ?: return null
        return WeaponSnapshot(
            id = id,
            name = json.stringOrNull("name"),
            icon = json.stringOrNull("icon"),
            type = json.intOrNull("type"),
            rarity = json.intOrNull("rarity"),
            level = json.intOrNull("level"),
            ascensionLevel = json.intOrNull("promote_level"),
            typeName = json.stringOrNull("type_name"),
            description = json.stringOrNull("desc"),
            refinementLevel = json.intOrNull("affix_level"),
            mainProperty = json.optJSONObject("main_property")?.let(::mapStat),
            subProperty = json.optJSONObject("sub_property")?.let(::mapStat)
        )
    }

    private fun mapArtifacts(array: JSONArray): Pair<List<ArtifactSnapshot>, EquipmentIntegritySnapshot> {
        val mapped = mutableListOf<ArtifactSnapshot>()
        val invalidIndexes = mutableListOf<Int>()
        val invalidSubPropertyIndexes = mutableListOf<Int>()

        for (index in 0 until array.length()) {
            val json = array.optJSONObject(index)
            val id = json?.intOrNull("id")
            if (json == null || id == null) {
                invalidIndexes += index
                continue
            }
            val subProperties = json.optJSONArray("sub_property_list")
            if (subProperties != null) {
                for (subIndex in 0 until subProperties.length()) {
                    val stat = subProperties.optJSONObject(subIndex)
                    if (stat == null || stat.intOrNull("property_type") == null) {
                        invalidSubPropertyIndexes += index
                        break
                    }
                }
            }
            mapped += ArtifactSnapshot(
                id = id,
                name = json.stringOrNull("name"),
                icon = json.stringOrNull("icon"),
                slot = json.intOrNull("pos"),
                slotName = json.stringOrNull("pos_name"),
                rarity = json.intOrNull("rarity"),
                level = json.intOrNull("level"),
                set = json.opt("set")?.takeUnless { it == JSONObject.NULL }?.let(::toStructuredData),
                mainProperty = json.optJSONObject("main_property")?.let(::mapStat),
                subProperties = subProperties?.let(::mapStats).orEmpty()
            )
        }

        val duplicateIds = mapped.groupingBy { it.id }.eachCount()
            .filterValues { it > 1 }.keys.sorted()
        val validSlots = mapped.mapNotNull { it.slot?.takeIf { slot -> slot in 1..5 } }
        val duplicateSlots = validSlots.groupingBy { it }.eachCount()
            .filterValues { it > 1 }.keys.sorted()
        val invalidSlots = mapped.mapNotNull { artifact ->
            artifact.slot?.takeIf { it !in 1..5 } ?: if (artifact.slot == null) -1 else null
        }.distinct().sorted()
        val presentSlots = validSlots.toSet()

        return mapped to EquipmentIntegritySnapshot(
            artifactSourceEntryCount = array.length(),
            mappedArtifactCount = mapped.size,
            invalidArtifactEntryIndexes = invalidIndexes,
            duplicateArtifactIds = duplicateIds,
            duplicateArtifactSlots = duplicateSlots,
            invalidArtifactSlots = invalidSlots,
            unoccupiedArtifactSlots = (1..5).filterNot { it in presentSlots },
            invalidSubPropertyEntryIndexes = invalidSubPropertyIndexes.distinct().sorted()
        )
    }

    private fun mapConstellations(array: JSONArray): List<ConstellationSnapshot> =
        (0 until array.length()).mapNotNull { i ->
            val j = array.optJSONObject(i) ?: return@mapNotNull null
            val id = j.intOrNull("id") ?: return@mapNotNull null
            ConstellationSnapshot(id, j.stringOrNull("name"), j.stringOrNull("icon"),
                j.stringOrNull("effect"), j.booleanOrNull("is_actived"), j.intOrNull("pos"),
                j.booleanOrNull("is_enhanced"), j.stringOrNull("enhanced_effect"), j.booleanOrNull("can_enhanced"))
        }

    private fun mapSkills(array: JSONArray): List<CharacterSkillSnapshot> =
        (0 until array.length()).mapNotNull { i ->
            val j = array.optJSONObject(i) ?: return@mapNotNull null
            val id = j.intOrNull("skill_id") ?: return@mapNotNull null
            CharacterSkillSnapshot(
                id, j.intOrNull("skill_type"), j.stringOrNull("name"), j.intOrNull("level"),
                j.stringOrNull("desc"), j.stringOrNull("icon"), j.booleanOrNull("is_unlock"),
                j.booleanOrNull("is_enhanced"), j.stringOrNull("enhanced_desc"),
                j.booleanOrNull("can_enhanced"),
                j.opt("skill_affix_list")?.takeUnless { it == JSONObject.NULL }?.let(::toStructuredData),
                toStructuredData(JSONObject().put("before", j.opt("before_enhanced_skill_attr_index"))
                    .put("after", j.opt("after_enhanced_skill_attr_index")))
            )
        }

    private fun mapStats(array: JSONArray): List<StatValueSnapshot> =
        (0 until array.length()).mapNotNull { array.optJSONObject(it)?.let(::mapStat) }

    private fun mapStat(j: JSONObject) = StatValueSnapshot(
        propertyType = j.intOrNull("property_type"),
        statName = j.stringOrNull("stat_name"),
        base = j.stringOrNull("base"),
        add = j.stringOrNull("add"),
        final = j.stringOrNull("final"),
        value = j.stringOrNull("value"),
        rolls = j.intOrNull("times")
    )

    private fun intValue(data: JSONObject, key: String, at: Instant?): DataValue<Int> {
        if (!data.has(key) || data.isNull(key)) return DataValue.Missing
        val value = data.opt(key)
        val number = (value as? Number)?.toLong()?.takeIf { it in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() }?.toInt()
            ?: return DataValue.Missing
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
        return (data.opt(key) as? Boolean)?.let { DataValue.Available(it, at) } ?: DataValue.Missing
    }

    private fun JSONObject.intOrNull(key: String): Int? {
        if (!has(key) || isNull(key)) return null
        val number = opt(key) as? Number ?: return null
        return number.toLong().takeIf { number.toDouble() == it.toDouble() && it in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() }?.toInt()
    }

    private fun JSONObject.stringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else opt(key)?.toString()?.takeIf { it.isNotBlank() }

    private fun JSONObject.booleanOrNull(key: String): Boolean? =
        if (!has(key) || isNull(key)) null else opt(key) as? Boolean

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
