package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData

data class ChronicleMetric(val label: String, val value: String)
data class ChronicleRegion(val name: String, val progress: Float, val percentageLabel: String)
enum class ChronicleRecordMode { SPIRAL_ABYSS, IMAGINARIUM_THEATER }

object ChroniclePresentationMapper {
    private val overviewStatFields = linkedMapOf(
        "active_day_number" to "Active Days",
        "achievement_number" to "Achievements",
        "avatar_number" to "Characters",
        "way_point_number" to "Waypoints",
        "domain_number" to "Domains",
        "anemoculus_number" to "Anemoculi",
        "geoculus_number" to "Geoculi",
        "electroculus_number" to "Electroculi",
        "dendroculus_number" to "Dendroculi",
        "hydroculus_number" to "Hydroculi",
        "pyroculus_number" to "Pyroculi",
        "precious_chest_number" to "Precious Chests",
        "luxurious_chest_number" to "Luxurious Chests",
        "exquisite_chest_number" to "Exquisite Chests",
        "common_chest_number" to "Common Chests",
        "magic_chest_number" to "Remarkable Chests"
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

    fun accountOverview(
        accountCard: DataValue<StructuredData>,
        profileAndExploration: DataValue<StructuredData>
    ): List<ChronicleMetric> {
        val accountFields = availableObject(accountCard).orEmpty()
        val profileFields = availableObject(profileAndExploration).orEmpty()
        if (accountFields.isEmpty() && profileFields.isEmpty()) return emptyList()
        // HoYoLAB separates identity/rank (account_card) from statistics (profile_and_exploration.stats).
        val role = (profileFields["role"] ?: accountFields["role"]).objectFields()
        val stats = (profileFields["stats"] ?: accountFields["stats"]).objectFields()
        val result = mutableListOf<ChronicleMetric>()
        (role["nickname"] ?: profileFields["nickname"] ?: accountFields["nickname"])?.displayScalar()
            ?.takeIf { it.isNotBlank() }?.let { result += ChronicleMetric("Player", it) }
        (role["level"] ?: profileFields["level"] ?: accountFields["level"])?.displayScalar()
            ?.takeIf { it.isNotBlank() }?.let { result += ChronicleMetric("Adventure Rank", it) }
        overviewStatFields.forEach { (key, label) ->
            stats[key]?.displayScalar()?.takeIf { it.isNotBlank() }?.let {
                result += ChronicleMetric(label, it)
            }
        }
        return result.distinctBy { it.label }
    }

    fun explorationRegions(value: DataValue<StructuredData>): List<ChronicleRegion> {
        val fields = availableObject(value) ?: return emptyList()
        val entries = (fields["world_explorations"] ?: fields["exploration"]
            ?: fields["exploration_list"]) as? StructuredData.ArrayValue ?: return emptyList()
        return entries.items.mapNotNull { item ->
            val region = item as? StructuredData.ObjectValue ?: return@mapNotNull null
            val name = (region.fields["name"] ?: region.fields["area_name"])?.displayScalar()
                ?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val rawProgress = (region.fields["exploration_percentage"]
                ?: region.fields["exploration_percent"])?.displayScalar()?.toDoubleOrNull()
                ?: return@mapNotNull null
            if (rawProgress < 0.0 || rawProgress > 10000.0) return@mapNotNull null
            // HoYoLAB's profile payload uses tenths of a percent (1000 = 100%).
            // Retain the displayed over-exploration value while capping the visual bar.
            val percent = when {
                rawProgress <= 100.0 -> rawProgress
                rawProgress <= 2000.0 -> rawProgress / 10.0
                else -> rawProgress / 100.0
            }
            ChronicleRegion(
                name = name,
                progress = (percent / 100.0).coerceIn(0.0, 1.0).toFloat(),
                percentageLabel = if (percent % 1.0 == 0.0) "${percent.toInt()}%" else "${"%.1f".format(java.util.Locale.ROOT, percent)}%"
            )
        }
    }

    private fun StructuredData?.objectFields(): Map<String, StructuredData> =
        (this as? StructuredData.ObjectValue)?.fields.orEmpty()

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
