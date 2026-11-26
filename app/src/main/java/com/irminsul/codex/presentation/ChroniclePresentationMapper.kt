package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData

data class ChronicleMetric(val label: String, val value: String)
data class ChronicleRegion(val name: String, val progress: Float, val percentageLabel: String)
enum class ChronicleRecordMode { SPIRAL_ABYSS, IMAGINARIUM_THEATER }

object ChroniclePresentationMapper {
    private val overviewFields = linkedMapOf(
        "nickname" to "Player",
        "level" to "Adventure Rank",
        "world_level" to "World Level",
        "finish_achievement_num" to "Achievements",
        "achievement_number" to "Achievements",
        "active_day_number" to "Active Days"
    )
    private val abyssFields = linkedMapOf(
        "total_battle_times" to "Battles",
        "total_star" to "Stars",
        "max_floor" to "Best Floor"
    )
    private val theaterFields = linkedMapOf(
        "difficulty" to "Difficulty",
        "total_star" to "Stars",
        "battle_count" to "Battles",
        "medal_num" to "Medals"
    )

    fun accountOverview(value: DataValue<StructuredData>): List<ChronicleMetric> {
        val fields = availableObject(value) ?: return emptyList()
        return overviewFields.mapNotNull { (key, label) ->
            fields[key]?.displayScalar()?.takeIf { it.isNotBlank() }?.let { ChronicleMetric(label, it) }
        }.distinctBy { it.label }
    }

    fun explorationRegions(value: DataValue<StructuredData>): List<ChronicleRegion> {
        val fields = availableObject(value) ?: return emptyList()
        val entries = fields["exploration"] as? StructuredData.ArrayValue
            ?: fields["exploration_list"] as? StructuredData.ArrayValue
            ?: return emptyList()
        return entries.items.mapNotNull { item ->
            val region = item as? StructuredData.ObjectValue ?: return@mapNotNull null
            val name = (region.fields["name"] ?: region.fields["area_name"])?.displayScalar()
                ?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val rawProgress = (region.fields["exploration_percentage"]
                ?: region.fields["exploration_percent"])?.displayScalar()?.toDoubleOrNull()
                ?: return@mapNotNull null
            val normalized = when {
                rawProgress < 0.0 -> return@mapNotNull null
                rawProgress > 100.0 && rawProgress <= 10000.0 -> rawProgress / 10000.0
                rawProgress <= 100.0 -> rawProgress / 100.0
                else -> return@mapNotNull null
            }.coerceIn(0.0, 1.0)
            ChronicleRegion(name, normalized.toFloat(), "${(normalized * 100).toInt()}%")
        }
    }

    fun recordMetrics(value: DataValue<StructuredData>, mode: ChronicleRecordMode): List<ChronicleMetric> {
        val fields = availableObject(value) ?: return emptyList()
        val allowlist = when (mode) {
            ChronicleRecordMode.SPIRAL_ABYSS -> abyssFields
            ChronicleRecordMode.IMAGINARIUM_THEATER -> theaterFields
        }
        return allowlist.mapNotNull { (key, label) ->
            fields[key]?.displayScalar()?.takeIf { it.isNotBlank() }?.let { ChronicleMetric(label, it) }
        }
    }

    private fun availableObject(value: DataValue<StructuredData>): Map<String, StructuredData>? = when (value) {
        is DataValue.Available -> (value.value as? StructuredData.ObjectValue)?.fields
        is DataValue.Stale -> (value.value as? StructuredData.ObjectValue)?.fields
        else -> null
    }

    private fun StructuredData.displayScalar(): String? = when (this) {
        is StructuredData.StringValue -> value.takeIf { it.isNotBlank() && !it.looksTechnical() }
        is StructuredData.NumberValue -> value
        is StructuredData.BooleanValue -> if (value) "Yes" else "No"
        StructuredData.NullValue, is StructuredData.ObjectValue, is StructuredData.ArrayValue -> null
    }

    private fun String.looksTechnical(): Boolean {
        val text = trim()
        return text.startsWith("https://", true) ||
            text.startsWith("http://", true) ||
            text.startsWith("//") ||
            text.startsWith("data:", true) ||
            text.matches(Regex("(?i).+\\.(png|jpe?g|webp|gif|svg|avif)(\\?.*)?")) ||
            text.matches(Regex("(?i)(?:uid|id|entry_id|avatar_url|image_url)\\s*[:=].*"))
    }
}
