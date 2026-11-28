package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData

data class ChronicleMetric(val label: String, val value: String)
data class ChronicleRecordCharacter(
    val id: Int?,
    val name: String?,
    val element: String?,
    val level: Int?,
    val rarity: Int?,
    val iconUrl: String?
)
data class ChronicleAbyssBattle(val half: String, val completedAt: String?, val characters: List<ChronicleRecordCharacter>)
data class ChronicleAbyssChamber(
    val chamberNumber: String,
    val stars: String?,
    val maxStars: String?,
    val battles: List<ChronicleAbyssBattle>
)
data class ChronicleAbyssFloor(
    val floorNumber: String,
    val stars: String?,
    val maxStars: String?,
    val leyLineDisorders: List<String>,
    val chambers: List<ChronicleAbyssChamber>
)
data class ChronicleAbyssCycle(
    val battles: String?,
    val wins: String?,
    val stars: String?,
    val bestFloor: String?,
    val floors: List<ChronicleAbyssFloor>
)
data class ChronicleTheaterRound(
    val roundNumber: String,
    val completedAt: String?,
    val medalEarned: Boolean?,
    val characters: List<ChronicleRecordCharacter>,
    val buffs: List<String>
)
data class ChronicleTheaterCycle(
    val title: String,
    val difficulty: String?,
    val medals: String?,
    val roundsCompleted: String?,
    val coins: String?,
    val bonusCharacters: String?,
    val rentedCharacters: String?,
    val rounds: List<ChronicleTheaterRound>,
    val performanceMetrics: List<ChronicleMetric>,
    val hasData: Boolean = true
)
data class ChronicleExplorationSubarea(
    val name: String,
    val progress: Float,
    val percentageLabel: String
)
data class ChronicleExplorationOffering(val name: String, val level: Int?, val iconUrl: String?)
data class ChronicleTribalReputation(val name: String, val level: Int?, val iconUrl: String?)
data class ChronicleRegion(
    val id: Int?,
    val parentId: Int?,
    val name: String,
    val progress: Float,
    val percentageLabel: String,
    val iconUrl: String? = null,
    val iconUrlCandidates: List<String> = listOfNotNull(iconUrl),
    val backgroundImageUrl: String? = null,
    val reputationLevel: Int? = null,
    val statueLevel: Int? = null,
    val offerings: List<ChronicleExplorationOffering> = emptyList(),
    val tribalReputations: List<ChronicleTribalReputation> = emptyList(),
    val subareas: List<ChronicleExplorationSubarea> = emptyList(),
    val childRegions: List<ChronicleRegion> = emptyList()
)
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
        "total_win_times" to "Wins",
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
        val parsed = entries.items.mapNotNull { item ->
            val region = item as? StructuredData.ObjectValue ?: return@mapNotNull null
            val regionFields = region.fields
            val name = (regionFields["name"] ?: regionFields["area_name"])?.displayScalar()
                ?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val progress = explorationProgress(
                regionFields["exploration_percentage"] ?: regionFields["exploration_percent"]
            ) ?: return@mapNotNull null
            val progressionType = regionFields["type"].displayScalar()
            val offerings = (regionFields["offerings"] as? StructuredData.ArrayValue)?.items.orEmpty()
                .mapNotNull { offeringValue ->
                    val offering = offeringValue.objectFields()
                    val offeringName = offering["name"].displayScalar()?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                    ChronicleExplorationOffering(
                        name = offeringName,
                        level = offering["level"].displayScalar()?.toIntOrNull(),
                        iconUrl = offering["icon"].httpUrl() ?: offering["image"].httpUrl()
                    )
                }
            val tribalReputations = (regionFields["natan_reputation"].objectFields()["tribal_list"]
                as? StructuredData.ArrayValue)?.items.orEmpty().mapNotNull { tribeValue ->
                val tribe = tribeValue.objectFields()
                val tribeName = tribe["name"].displayScalar()?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                ChronicleTribalReputation(
                    name = tribeName,
                    level = tribe["level"].displayScalar()?.toIntOrNull(),
                    iconUrl = tribe["icon"].httpUrl() ?: tribe["image"].httpUrl()
                )
            }
            val subareas = (regionFields["area_exploration_list"] as? StructuredData.ArrayValue)?.items.orEmpty()
                .mapNotNull { areaValue ->
                    val area = areaValue.objectFields()
                    val areaName = (area["name"] ?: area["area_name"]).displayScalar()
                        ?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                    val areaProgress = explorationProgress(
                        area["exploration_percentage"] ?: area["exploration_percent"]
                    ) ?: return@mapNotNull null
                    ChronicleExplorationSubarea(areaName, areaProgress.first, areaProgress.second)
                }
            ChronicleRegion(
                id = regionFields["id"].displayScalar()?.toIntOrNull(),
                parentId = regionFields["parent_id"].displayScalar()?.toIntOrNull(),
                name = name,
                progress = progress.first,
                percentageLabel = progress.second,
                iconUrl = listOfNotNull(regionFields["icon"].httpUrl(), regionFields["inner_icon"].httpUrl(), regionFields["cover"].httpUrl(), regionFields["image"].httpUrl(), offerings.firstNotNullOfOrNull { it.iconUrl }).firstOrNull(),
                iconUrlCandidates = listOfNotNull(regionFields["icon"].httpUrl(), regionFields["inner_icon"].httpUrl(), regionFields["cover"].httpUrl(), regionFields["image"].httpUrl(), offerings.firstNotNullOfOrNull { it.iconUrl }).distinct(),
                backgroundImageUrl = regionFields["background_image"].httpUrl(),
                reputationLevel = if (progressionType == "Reputation") regionFields["level"].displayScalar()?.toIntOrNull() else null,
                statueLevel = regionFields["seven_statue_level"].displayScalar()?.toIntOrNull()?.takeIf { it > 0 },
                offerings = offerings,
                tribalReputations = tribalReputations,
                subareas = subareas
            )
        }
        val regionIdsByName = parsed.mapNotNull { region ->
            region.id?.let { region.name.trim().lowercase(java.util.Locale.ROOT) to it }
        }.toMap()
        val requestedParents = mapOf(
            "windrest peak" to "mondstadt",
            "dragonspine" to "mondstadt",
            "the chasm" to "liyue",
            "chasm" to "liyue",
            "chenyu vale" to "liyue",
            "ancient sacred mountain" to "natlan"
        )
        val grouped = parsed.map { region ->
            val parentName = requestedParents[region.name.trim().lowercase(java.util.Locale.ROOT)]
            val requestedParentId = parentName?.let(regionIdsByName::get)
            if (requestedParentId != null) region.copy(parentId = requestedParentId) else region
        }
        val regionsById = grouped.mapNotNull { region -> region.id?.let { it to region } }.toMap()
        fun attachChildren(region: ChronicleRegion, ancestors: Set<Int> = emptySet()): ChronicleRegion {
            val id = region.id ?: return region
            if (id in ancestors) return region
            val children = grouped.filter { it.parentId == id }
                .map { attachChildren(it, ancestors + id) }
            return region.copy(childRegions = children)
        }
        return grouped.filter { region ->
            region.parentId == null || region.parentId == 0 || region.parentId !in regionsById
        }.map { attachChildren(it) }
    }

    private fun explorationProgress(value: StructuredData?): Pair<Float, String>? {
        val rawProgress = value.displayScalar()?.toDoubleOrNull() ?: return null
        if (rawProgress < 0.0 || rawProgress > 10000.0) return null
        val percent = when {
            rawProgress <= 100.0 -> rawProgress
            rawProgress <= 2000.0 -> rawProgress / 10.0
            else -> rawProgress / 100.0
        }
        val label = if (percent % 1.0 == 0.0) {
            "${percent.toInt()}%"
        } else {
            "${"%.1f".format(java.util.Locale.ROOT, percent)}%"
        }
        return (percent / 100.0).coerceIn(0.0, 1.0).toFloat() to label
    }

    fun spiralAbyss(
        value: DataValue<StructuredData>,
        roster: List<CharacterSnapshot> = emptyList()
    ): ChronicleAbyssCycle? {
        val root = availableObject(value) ?: return null
        val rosterById = roster.associateBy { it.id }
        val floors = (root["floors"] as? StructuredData.ArrayValue)?.items.orEmpty().mapNotNull { floorValue ->
            val floor = floorValue.objectFields()
            val floorNumber = floor["index"].displayScalar()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val chambers = (floor["levels"] as? StructuredData.ArrayValue)?.items.orEmpty().mapNotNull { chamberValue ->
                val chamber = chamberValue.objectFields()
                val chamberNumber = chamber["index"].displayScalar()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val battles = (chamber["battles"] as? StructuredData.ArrayValue)?.items.orEmpty().mapNotNull { battleValue ->
                    val battle = battleValue.objectFields()
                    val halfNumber = battle["index"].displayScalar()?.toIntOrNull() ?: return@mapNotNull null
                    val team = (battle["avatars"] as? StructuredData.ArrayValue)?.items.orEmpty().mapNotNull { avatarValue ->
                        val avatar = avatarValue.objectFields()
                        val id = avatar["id"].displayScalar()?.toIntOrNull()
                        val owned = id?.let(rosterById::get)
                        val icon = avatar["icon"].httpUrl() ?: owned?.avatarIcon?.takeIf { it.isHttpUrl() }
                        if (id == null && icon == null) return@mapNotNull null
                        ChronicleRecordCharacter(
                            id = id,
                            name = owned?.name,
                            element = owned?.element,
                            level = avatar["level"].displayScalar()?.toIntOrNull() ?: owned?.level,
                            rarity = avatar["rarity"].displayScalar()?.toIntOrNull() ?: owned?.rarity,
                            iconUrl = icon
                        )
                    }
                    ChronicleAbyssBattle(
                        half = if (halfNumber == 1) "First Half" else "Second Half",
                        completedAt = battle["settle_date_time"].objectFields().let { date ->
                            val year = date["year"].displayScalar()
                            val month = date["month"].displayScalar()
                            val day = date["day"].displayScalar()
                            val hour = date["hour"].displayScalar()
                            val minute = date["minute"].displayScalar()
                            if (year != null && month != null && day != null) {
                                "$year-$month-$day" + (if (hour != null && minute != null) " · $hour:${minute.padStart(2, '0')}" else "")
                            } else null
                        },
                        characters = team
                    )
                }
                ChronicleAbyssChamber(
                    chamberNumber = chamberNumber,
                    stars = chamber["star"].displayScalar(),
                    maxStars = chamber["max_star"].displayScalar(),
                    battles = battles
                )
            }
            val disorders = (floor["ley_line_disorder"] as? StructuredData.ArrayValue)?.items.orEmpty()
                .mapNotNull { it.displayScalar()?.takeIf(String::isNotBlank) }
            ChronicleAbyssFloor(
                floorNumber = floorNumber,
                stars = floor["star"].displayScalar(),
                maxStars = floor["max_star"].displayScalar(),
                leyLineDisorders = disorders,
                chambers = chambers
            )
        }
        return ChronicleAbyssCycle(
            battles = root["total_battle_times"].displayScalar(),
            wins = root["total_win_times"].displayScalar(),
            stars = root["total_star"].displayScalar(),
            bestFloor = root["max_floor"].displayScalar(),
            floors = floors
        )
    }

    fun theaterCycles(
        value: DataValue<StructuredData>,
        roster: List<CharacterSnapshot> = emptyList()
    ): List<ChronicleTheaterCycle> {
        val root = availableObject(value) ?: return emptyList()
        val records = root["data"] as? StructuredData.ArrayValue ?: return emptyList()
        val rosterById = roster.associateBy { it.id }
        return records.items.mapNotNull { item ->
            val record = item.objectFields()
            val hasData = record["has_data"] != StructuredData.BooleanValue(false)
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
            if (!hasData) {
                return@mapNotNull ChronicleTheaterCycle(
                    title = title,
                    difficulty = null,
                    medals = null,
                    roundsCompleted = null,
                    coins = null,
                    bonusCharacters = null,
                    rentedCharacters = null,
                    rounds = emptyList(),
                    performanceMetrics = emptyList(),
                    hasData = false
                )
            }
            val detail = record["detail"].objectFields()
            val rounds = (detail["rounds_data"] as? StructuredData.ArrayValue)?.items.orEmpty().mapNotNull { roundValue ->
                val round = roundValue.objectFields()
                val roundId = round["round_id"].displayScalar() ?: return@mapNotNull null
                val avatars = (round["avatars"] as? StructuredData.ArrayValue)?.items.orEmpty().mapNotNull { avatarValue ->
                    val avatar = avatarValue.objectFields()
                    val id = (avatar["avatar_id"] ?: avatar["id"]).displayScalar()?.toIntOrNull()
                    val owned = id?.let(rosterById::get)
                    val name = avatar["name"].displayScalar() ?: owned?.name
                    val icon = avatar["image"].httpUrl() ?: avatar["icon"].httpUrl()
                        ?: owned?.avatarIcon?.takeIf { it.isHttpUrl() }
                    if (name.isNullOrBlank() && icon == null) return@mapNotNull null
                    ChronicleRecordCharacter(
                        id = id,
                        name = name,
                        element = avatar["element"].displayScalar() ?: owned?.element,
                        level = avatar["level"].displayScalar()?.toIntOrNull() ?: owned?.level,
                        rarity = avatar["rarity"].displayScalar()?.toIntOrNull() ?: owned?.rarity,
                        iconUrl = icon
                    )
                }
                val buffs = (round["buffs"] as? StructuredData.ArrayValue)?.items.orEmpty()
                    .mapNotNull { it.objectFields()["name"].displayScalar()?.takeIf(String::isNotBlank) }
                val splendourBuffs = round["splendour_buff"].objectFields()["buffs"] as? StructuredData.ArrayValue
                val allBuffs = (buffs + splendourBuffs?.items.orEmpty().mapNotNull { it.objectFields()["name"].displayScalar()?.takeIf(String::isNotBlank) }).distinct()
                val finishDate = round["finish_date_time"].objectFields()
                val completedAt = listOf("year", "month", "day").map { finishDate[it].displayScalar() }
                    .takeIf { values -> values.all { it != null } }
                    ?.joinToString("-")
                ChronicleTheaterRound(
                    roundNumber = roundId,
                    completedAt = completedAt,
                    medalEarned = (round["is_get_medal"] as? StructuredData.BooleanValue)?.value,
                    characters = avatars,
                    buffs = allBuffs
                )
            }
            val fightStats = detail["fight_statisic"].objectFields()
            val performance = listOfNotNull(
                fightStats["max_damage_avatar"].performanceMetric("Highest Damage", rosterById),
                fightStats["max_defeat_avatar"].performanceMetric("Most Defeats", rosterById),
                fightStats["max_take_damage_avatar"].performanceMetric("Most Damage Taken", rosterById),
                fightStats["total_use_time"].displayScalar()?.let { ChronicleMetric("Total Battle Time", "$it seconds") }
            )
            ChronicleTheaterCycle(
                title = title,
                difficulty = stat["difficulty_id"].displayScalar(),
                medals = stat["medal_num"].displayScalar(),
                roundsCompleted = stat["max_round_id"].displayScalar(),
                coins = stat["coin_num"].displayScalar(),
                bonusCharacters = stat["avatar_bonus_num"].displayScalar(),
                rentedCharacters = stat["rent_cnt"].displayScalar(),
                rounds = rounds,
                performanceMetrics = performance,
                hasData = true
            )
        }
    }

    private fun StructuredData?.performanceMetric(
        label: String,
        roster: Map<Int, CharacterSnapshot>
    ): ChronicleMetric? {
        val fields = objectFields()
        val value = fields["value"].displayScalar()?.takeIf { it.isNotBlank() } ?: return null
        val id = fields["avatar_id"].displayScalar()?.toIntOrNull()
        val name = id?.let(roster::get)?.name
        return ChronicleMetric(label, if (name == null) value else "$value · $name")
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

    private fun StructuredData?.objectFields(): Map<String, StructuredData> =
        (this as? StructuredData.ObjectValue)?.fields.orEmpty()

    private fun StructuredData?.displayScalar(): String? = when (this) {
        is StructuredData.StringValue -> value.takeIf { it.isNotBlank() && !it.looksTechnical() }
        is StructuredData.NumberValue -> value
        is StructuredData.BooleanValue -> if (value) "Yes" else "No"
        else -> null
    }

    private fun StructuredData?.httpUrl(): String? =
        (this as? StructuredData.StringValue)?.value?.trim()?.takeIf { it.isHttpUrl() }

    private fun String.isHttpUrl(): Boolean =
        startsWith("https://", ignoreCase = true) || startsWith("http://", ignoreCase = true)

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
