package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData

data class ChronicleMetric(val label: String, val value: String)
data class ChronicleTheaterRound(val roundNumber: String, val medalEarned: Boolean?, val characters: List<String>)
data class ChronicleTheaterCycle(
    val title: String,
    val difficulty: String?,
    val medals: String?,
    val roundsCompleted: String?,
    val coins: String?,
    val bonusCharacters: String?,
    val rentedCharacters: String?,
    val rounds: List<ChronicleTheaterRound>
)
data class ChronicleRegion(val name: String, val progress: Float, val percentageLabel: String, val iconUrl: String? = null)
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

    private val primaryOverviewLabels = setOf("Active Days", "Achievements", "Characters")
    private val identityOverviewLabels = setOf("Player", "Adventure Rank")

    fun primaryOverviewMetrics(metrics: List<ChronicleMetric>): List<ChronicleMetric> =
        metrics.filter { it.label in primaryOverviewLabels }

    fun secondaryOverviewMetrics(metrics: List<ChronicleMetric>): List<ChronicleMetric> =
        metrics.filterNot { it.label in primaryOverviewLabels || it.label in identityOverviewLabels }

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
                percentageLabel = if (percent % 1.0 == 0.0) "${percent.toInt()}%" else "${"%.1f".format(java.util.Locale.ROOT, percent)}%",
                iconUrl = sequenceOf(region.fields["icon"], region.fields["image"])
                    .filterIsInstance<StructuredData.StringValue>()
                    .map { it.value.trim() }
                    .firstOrNull { it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true) }
            )
        }
    }

    private fun StructuredData?.objectFields(): Map<String, StructuredData> =
        (this as? StructuredData.ObjectValue)?.fields.orEmpty()

    @kotlin.jvm.JvmName("displayNullableScalar")
    private fun StructuredData?.displayScalar(): String? = when (this) {
        is StructuredData.StringValue -> value.takeIf { it.isNotBlank() }
        is StructuredData.NumberValue -> value
        is StructuredData.BooleanValue -> if (value) "Yes" else "No"
        else -> null
    }

    fun theaterCycles(value: DataValue<StructuredData>): List<ChronicleTheaterCycle> {
        val root = availableObject(value) ?: return emptyList()
        val records = root["data"] as? StructuredData.ArrayValue ?: return emptyList()
        return records.items.mapNotNull { item ->
            val record = item.objectFields()
            if (record["has_data"] == StructuredData.BooleanValue(false)) return@mapNotNull null
            val stat = record["stat"].objectFields()
            val schedule = record["schedule"].objectFields()
            val start = schedule["start_date_time"].objectFields()
            val month = start["month"].displayScalar()?.toIntOrNull()
            val year = start["year"].displayScalar()
            val title = if (month != null && year != null) {
                "${monthName(month)} $year"
            } else {
                schedule["schedule_id"].displayScalar()?.let { "Cycle $it" } ?: "Theater cycle"
            }
            val detail = record["detail"].objectFields()
            val rounds = (detail["rounds_data"] as? StructuredData.ArrayValue)?.items.orEmpty().mapNotNull { roundValue ->
                val round = roundValue.objectFields()
                val roundId = round["round_id"].displayScalar() ?: return@mapNotNull null
                val avatars = (round["avatars"] as? StructuredData.ArrayValue)?.items.orEmpty()
                    .mapNotNull { avatar -> avatar.objectFields()["name"].displayScalar()?.takeIf { it.isNotBlank() } }
                ChronicleTheaterRound(
                    roundNumber = roundId,
                    medalEarned = (round["is_get_medal"] as? StructuredData.BooleanValue)?.value,
                    characters = avatars
                )
            }
            ChronicleTheaterCycle(
                title = title,
                difficulty = stat["difficulty_id"].displayScalar(),
                medals = stat["medal_num"].displayScalar(),
                roundsCompleted = stat["max_round_id"].displayScalar(),
                coins = stat["coin_num"].displayScalar(),
                bonusCharacters = stat["avatar_bonus_num"].displayScalar(),
                rentedCharacters = stat["rent_cnt"].displayScalar(),
                rounds = rounds
            )
        }
    }

    private fun monthName(month: Int): String = when (month) {
        1 -> "January"; 2 -> "February"; 3 -> "March"; 4 -> "April"
        5 -> "May"; 6 -> "June"; 7 -> "July"; 8 -> "August"
        9 -> "September"; 10 -> "October"; 11 -> "November"; 12 -> "December"
        else -> "Unknown month"
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
