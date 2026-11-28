package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChroniclePresentationMapperTest {
    private fun obj(vararg fields: Pair<String, StructuredData>) = StructuredData.ObjectValue(mapOf(*fields))

    @Test fun accountOverviewCombinesAccountCardIdentityWithProfileStatisticsAndOnlyApprovedFields() {
        val accountCard = DataValue.Available(obj(
            "role" to obj("nickname" to StructuredData.StringValue("Leywin"),
                "level" to StructuredData.NumberValue("60"))
        ), null)
        val profileAndExploration = DataValue.Available(obj(
            "stats" to obj(
                "active_day_number" to StructuredData.NumberValue("474"),
                "achievement_number" to StructuredData.NumberValue("1337"),
                "avatar_number" to StructuredData.NumberValue("67"),
                "uid" to StructuredData.StringValue("865157206"),
                "field_ext_map" to obj("image_url" to StructuredData.StringValue("https://example.invalid/a.png"))
            )
        ), null)
        assertEquals(
            listOf(ChronicleMetric("Player", "Leywin"), ChronicleMetric("Adventure Rank", "60"),
                ChronicleMetric("Active Days", "474"), ChronicleMetric("Achievements", "1337"),
                ChronicleMetric("Characters", "67")),
            ChroniclePresentationMapper.accountOverview(accountCard, profileAndExploration)
        )
    }

    @Test fun explorationMapsActualWorldExplorationsAndClampsBonusProgress() {
        val data = DataValue.Available(obj(
            "world_explorations" to StructuredData.ArrayValue(listOf(
                obj("name" to StructuredData.StringValue("Snezhnaya"),
                    "exploration_percentage" to StructuredData.NumberValue("620"),
                    "id" to StructuredData.NumberValue("20"),
                    "area_exploration_list" to StructuredData.ArrayValue(emptyList())),
                obj("name" to StructuredData.StringValue("Nod-Krai"),
                    "exploration_percentage" to StructuredData.NumberValue("1000")),
                obj("name" to StructuredData.StringValue("Sumeru"),
                    "exploration_percentage" to StructuredData.NumberValue("1183"))
            ))
        ), null)
        val regions = ChroniclePresentationMapper.explorationRegions(data)
        assertEquals(3, regions.size)
        assertEquals("Snezhnaya", regions[0].name)
        assertEquals("62%", regions[0].percentageLabel)
        assertEquals(0.62f, regions[0].progress, 0.001f)
        assertEquals("100%", regions[1].percentageLabel)
        assertEquals("118.3%", regions[2].percentageLabel)
        assertEquals(1f, regions[2].progress, 0.001f)
    }

    @Test fun explorationGroupsParentRegionsAndNestedAreasWithoutCombiningProgress() {
        val data = DataValue.Available(obj(
            "world_explorations" to StructuredData.ArrayValue(listOf(
                obj(
                    "id" to StructuredData.NumberValue("2"),
                    "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Liyue"),
                    "exploration_percentage" to StructuredData.NumberValue("1000"),
                    "area_exploration_list" to StructuredData.ArrayValue(listOf(
                        obj("name" to StructuredData.StringValue("Bishui Plain"),
                            "exploration_percentage" to StructuredData.NumberValue("1042")),
                        obj("name" to StructuredData.StringValue("Minlin"),
                            "exploration_percentage" to StructuredData.NumberValue("1080"))
                    ))
                ),
                obj(
                    "id" to StructuredData.NumberValue("10"),
                    "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Chenyu Vale"),
                    "exploration_percentage" to StructuredData.NumberValue("0")
                ),
                obj(
                    "id" to StructuredData.NumberValue("13"),
                    "parent_id" to StructuredData.NumberValue("10"),
                    "name" to StructuredData.StringValue("Chenyu Vale: Upper Vale"),
                    "exploration_percentage" to StructuredData.NumberValue("1000")
                )
            ))
        ), null)

        val regions = ChroniclePresentationMapper.explorationRegions(data)

        assertEquals(listOf("Liyue"), regions.map { it.name })
        val liyue = regions.single()
        assertEquals(listOf("Bishui Plain", "Minlin"), liyue.subareas.map { it.name })
        assertEquals("104.2%", liyue.subareas[0].percentageLabel)
        assertEquals(1.0f, liyue.progress, 0.001f)
        assertEquals(listOf("Chenyu Vale"), liyue.childRegions.map { it.name })
        val chenyuVale = liyue.childRegions.single()
        assertEquals(0f, chenyuVale.progress, 0.001f)
        assertEquals(1, chenyuVale.childRegions.size)
        assertEquals("Chenyu Vale: Upper Vale", chenyuVale.childRegions.single().name)
        assertEquals("100%", chenyuVale.childRegions.single().percentageLabel)
        assertEquals(0f, chenyuVale.progress, 0.001f)
    }

    @Test fun explorationMapsOfficialNatlanAndNodKraiEmblemsFromSourceIcons() {
        val natlanIcon = "https://act-webstatic.hoyoverse.com/game_record/genshin/city_icon/UI_ChapterIcon_Nata.png"
        val nodKraiIcon = "https://act-webstatic.hoyoverse.com/game_record/genshin/city_icon/UI_ChapterIcon_Nordkalei.png"
        val data = DataValue.Available(obj(
            "world_explorations" to StructuredData.ArrayValue(listOf(
                obj("id" to StructuredData.NumberValue("15"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Natlan"),
                    "exploration_percentage" to StructuredData.NumberValue("280"),
                    "icon" to StructuredData.StringValue(natlanIcon)),
                obj("id" to StructuredData.NumberValue("17"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Nod-Krai"),
                    "exploration_percentage" to StructuredData.NumberValue("1000"),
                    "icon" to StructuredData.StringValue(nodKraiIcon))
            ))
        ), null)

        val regions = ChroniclePresentationMapper.explorationRegions(data)

        assertEquals(natlanIcon, regions.single { it.name == "Natlan" }.iconUrl)
        assertEquals(nodKraiIcon, regions.single { it.name == "Nod-Krai" }.iconUrl)
    }

    @Test fun explorationUsesCorrectRegionalEmblemsAndTempleOfferingFallback() {
        val natlanIcon = "https://act-webstatic.hoyoverse.com/game_record/genshin/city_icon/UI_ChapterIcon_Nata.png"
        val natlanInner = "https://act-webstatic.hoyoverse.com/game_record/genshin/city_icon/UI_ChapterInnerIcon_Nata.png"
        val nodKraiIcon = "https://act-webstatic.hoyoverse.com/game_record/genshin/city_icon/UI_ChapterIcon_Nordkalei.png"
        val nodKraiInner = "https://act-webstatic.hoyoverse.com/game_record/genshin/city_icon/UI_ChapterInnerIcon_Nordkalei.png"
        val templeOffering = "https://act-webstatic.hoyoverse.com/game_record/genshin/city_icon/UI_ChapterOffering_SkyTempleMemoryCore.png"
        val data = DataValue.Available(obj(
            "world_explorations" to StructuredData.ArrayValue(listOf(
                obj("id" to StructuredData.NumberValue("15"), "name" to StructuredData.StringValue("Natlan"),
                    "exploration_percentage" to StructuredData.NumberValue("280"),
                    "icon" to StructuredData.StringValue(natlanIcon),
                    "inner_icon" to StructuredData.StringValue(natlanInner),
                    "cover" to StructuredData.StringValue("https://example.invalid/natlan-cover.png")),
                obj("id" to StructuredData.NumberValue("17"), "name" to StructuredData.StringValue("Nod-Krai"),
                    "exploration_percentage" to StructuredData.NumberValue("1000"),
                    "icon" to StructuredData.StringValue(nodKraiIcon),
                    "inner_icon" to StructuredData.StringValue(nodKraiInner),
                    "cover" to StructuredData.StringValue("https://example.invalid/nod-krai-cover.png")),
                obj("id" to StructuredData.NumberValue("19"), "name" to StructuredData.StringValue("Temple of Space"),
                    "exploration_percentage" to StructuredData.NumberValue("1000"),
                    "icon" to StructuredData.StringValue(""),
                    "inner_icon" to StructuredData.StringValue(""),
                    "cover" to StructuredData.StringValue(""),
                    "offerings" to StructuredData.ArrayValue(listOf(
                        obj("name" to StructuredData.StringValue("Temple of Space: Memory Core"),
                            "level" to StructuredData.NumberValue("10"),
                            "icon" to StructuredData.StringValue(templeOffering))
                    )))
            ))
        ), null)

        val regions = ChroniclePresentationMapper.explorationRegions(data)
        val natlan = regions.single { it.name == "Natlan" }
        val nodKrai = regions.single { it.name == "Nod-Krai" }
        val temple = regions.single { it.name == "Temple of Space" }

        assertEquals(natlanIcon, natlan.iconUrl)
        assertEquals(listOf(natlanIcon, natlanInner), natlan.iconUrlCandidates)
        assertEquals(nodKraiIcon, nodKrai.iconUrl)
        assertEquals(listOf(nodKraiIcon, nodKraiInner), nodKrai.iconUrlCandidates)
        assertEquals(templeOffering, temple.iconUrl)
        assertEquals(listOf(templeOffering), temple.iconUrlCandidates)
    }

    @Test fun explorationProvidesVerifiedTempleEmblemWhenOfferingDataIsMissing() {
        val data = DataValue.Available(obj(
            "world_explorations" to StructuredData.ArrayValue(listOf(
                obj("id" to StructuredData.NumberValue("19"), "name" to StructuredData.StringValue("Temple of Space"),
                    "exploration_percentage" to StructuredData.NumberValue("1000"))
            ))
        ), null)

        val temple = ChroniclePresentationMapper.explorationRegions(data).single()

        assertEquals(
            "https://act-webstatic.hoyoverse.com/game_record/genshin/city_icon/UI_ChapterOffering_SkyTempleMemoryCore.png",
            temple.iconUrl
        )
    }

    @Test fun explorationPlacesNamedAreasUnderTheirRequestedRegions() {
        val data = DataValue.Available(obj(
            "world_explorations" to StructuredData.ArrayValue(listOf(
                obj("id" to StructuredData.NumberValue("1"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Mondstadt"), "exploration_percentage" to StructuredData.NumberValue("500")),
                obj("id" to StructuredData.NumberValue("2"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Liyue"), "exploration_percentage" to StructuredData.NumberValue("600")),
                obj("id" to StructuredData.NumberValue("9"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Fontaine"), "exploration_percentage" to StructuredData.NumberValue("650")),
                obj("id" to StructuredData.NumberValue("14"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Sea of Bygone Eras"), "exploration_percentage" to StructuredData.NumberValue("750")),
                obj("id" to StructuredData.NumberValue("3"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Windrest Peak"), "exploration_percentage" to StructuredData.NumberValue("700")),
                obj("id" to StructuredData.NumberValue("4"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Dragonspine"), "exploration_percentage" to StructuredData.NumberValue("800")),
                obj("id" to StructuredData.NumberValue("5"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("The Chasm"), "exploration_percentage" to StructuredData.NumberValue("900")),
                obj("id" to StructuredData.NumberValue("6"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Chenyu Vale"), "exploration_percentage" to StructuredData.NumberValue("1000")),
                obj("id" to StructuredData.NumberValue("15"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Natlan"), "exploration_percentage" to StructuredData.NumberValue("280")),
                obj("id" to StructuredData.NumberValue("16"), "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Ancient Sacred Mountain"), "exploration_percentage" to StructuredData.NumberValue("406"))
            ))
        ), null)

        val regions = ChroniclePresentationMapper.explorationRegions(data)
        val mondstadt = regions.single { it.name == "Mondstadt" }
        val liyue = regions.single { it.name == "Liyue" }

        assertEquals(listOf("Windrest Peak", "Dragonspine"), mondstadt.childRegions.map { it.name })
        assertEquals(listOf("The Chasm", "Chenyu Vale"), liyue.childRegions.map { it.name })
        val fontaine = regions.single { it.name == "Fontaine" }
        assertEquals(listOf("Sea of Bygone Eras"), fontaine.childRegions.map { it.name })
        assertEquals(65f, fontaine.progress * 100f, 0.001f)
        assertEquals(75f, fontaine.childRegions.single().progress * 100f, 0.001f)
        val natlan = regions.single { it.name == "Natlan" }
        assertEquals(listOf("Ancient Sacred Mountain"), natlan.childRegions.map { it.name })
        assertEquals(40.6f, natlan.childRegions.single().progress * 100f, 0.001f)
        assertEquals(50f, mondstadt.progress * 100f, 0.001f)
        assertEquals(60f, liyue.progress * 100f, 0.001f)
        assertEquals(70f, mondstadt.childRegions[0].progress * 100f, 0.001f)
    }

    @Test fun explorationMapsSourceBackedProgressionAndTribalLevels() {
        val data = DataValue.Available(obj(
            "world_explorations" to StructuredData.ArrayValue(listOf(
                obj(
                    "id" to StructuredData.NumberValue("15"),
                    "parent_id" to StructuredData.NumberValue("0"),
                    "name" to StructuredData.StringValue("Natlan"),
                    "background_image" to StructuredData.StringValue("https://example.invalid/natlan-bg.png"),
                    "type" to StructuredData.StringValue("Reputation"),
                    "level" to StructuredData.NumberValue("0"),
                    "seven_statue_level" to StructuredData.NumberValue("1"),
                    "exploration_percentage" to StructuredData.NumberValue("280"),
                    "offerings" to StructuredData.ArrayValue(listOf(
                        obj("name" to StructuredData.StringValue("Tona's Flame"),
                            "level" to StructuredData.NumberValue("11"),
                            "open_state" to StructuredData.StringValue("OfferingOpenStateUnknow"))
                    )),
                    "natan_reputation" to obj(
                        "tribal_list" to StructuredData.ArrayValue(listOf(
                            obj("name" to StructuredData.StringValue("\"People of the Springs\""),
                                "level" to StructuredData.NumberValue("4"),
                                "icon" to StructuredData.StringValue("https://example.invalid/tribe.png"))
                        ))
                    )
                )
            ))
        ), null)

        val natlan = ChroniclePresentationMapper.explorationRegions(data).single()

        assertEquals(0, natlan.reputationLevel)
        assertEquals(1, natlan.statueLevel)
        assertEquals("https://example.invalid/natlan-bg.png", natlan.backgroundImageUrl)
        assertEquals("Tona's Flame", natlan.offerings.single().name)
        assertEquals(11, natlan.offerings.single().level)
        assertEquals("\"People of the Springs\"", natlan.tribalReputations.single().name)
        assertEquals(4, natlan.tribalReputations.single().level)
        assertEquals("https://example.invalid/tribe.png", natlan.tribalReputations.single().iconUrl)
    }

    @Test fun explorationSupportsLegacyFlatShapeAndHidesTechnicalValues() {
        val data = DataValue.Available(obj(
            "exploration" to StructuredData.ArrayValue(listOf(
                obj("name" to StructuredData.StringValue("Mondstadt"),
                    "exploration_percentage" to StructuredData.NumberValue("8750"),
                    "id" to StructuredData.NumberValue("1"),
                    "icon" to StructuredData.StringValue("https://example.invalid/icon.png"))
            ))
        ), null)
        val regions = ChroniclePresentationMapper.explorationRegions(data)
        assertEquals(1, regions.size)
        assertEquals("Mondstadt", regions[0].name)
        assertEquals("87.5%", regions[0].percentageLabel)
        assertEquals(0.875f, regions[0].progress, 0.001f)
    }

    @Test fun accountOverviewSeparatesPrimaryAndSecondaryStatisticsWithoutInventingMissingValues() {
        val metrics = listOf(
            ChronicleMetric("Player", "Traveler"),
            ChronicleMetric("Active Days", "120"),
            ChronicleMetric("Waypoints", "80"),
            ChronicleMetric("Achievements", "500"),
            ChronicleMetric("Characters", "30")
        )
        assertEquals(
            listOf(ChronicleMetric("Active Days", "120"), ChronicleMetric("Achievements", "500"), ChronicleMetric("Characters", "30")),
            ChroniclePresentationMapper.primaryOverviewMetrics(metrics)
        )
        assertEquals(listOf(ChronicleMetric("Waypoints", "80")), ChroniclePresentationMapper.secondaryOverviewMetrics(metrics))
        assertTrue(ChroniclePresentationMapper.primaryOverviewMetrics(listOf(ChronicleMetric("Waypoints", "0"))).isEmpty())
    }

    @Test fun explorationDoesNotExposeNonHttpImageValuesAsEmblems() {
        val data = DataValue.Available(obj(
            "world_explorations" to StructuredData.ArrayValue(listOf(
                obj("name" to StructuredData.StringValue("Fontaine"),
                    "exploration_percentage" to StructuredData.NumberValue("500"),
                    "icon" to StructuredData.StringValue("data:image/png;base64,hidden"))
            ))
        ), null)
        assertEquals(null, ChroniclePresentationMapper.explorationRegions(data).single().iconUrl)
    }

    @Test fun modeMetricsAreAllowlistedAndNeverExposeNestedOrTechnicalFields() {
        val data = DataValue.Available(obj(
            "total_battle_times" to StructuredData.NumberValue("12"),
            "total_star" to StructuredData.NumberValue("36"),
            "uid" to StructuredData.StringValue("800000001"),
            "url" to StructuredData.StringValue("https://example.invalid")
        ), null)
        val metrics = ChroniclePresentationMapper.recordMetrics(data, ChronicleRecordMode.SPIRAL_ABYSS)
        assertEquals(listOf(ChronicleMetric("Battles", "12"), ChronicleMetric("Stars", "36")), metrics)
        assertTrue(metrics.none { it.value.contains("http") || it.value.contains("800000001") })
    }

    @Test fun missingAndFailedSectionsProduceNoInventedMetrics() {
        assertTrue(ChroniclePresentationMapper.accountOverview(DataValue.Missing, DataValue.Missing).isEmpty())
        assertTrue(ChroniclePresentationMapper.explorationRegions(DataValue.Unavailable).isEmpty())
    }
    @Test fun mapsEveryNestedImaginariumTheaterCycleAndRoundLineups() {
        val value = DataValue.Available(obj(
            "data" to StructuredData.ArrayValue(listOf(
                obj(
                    "has_data" to StructuredData.BooleanValue(true),
                    "stat" to obj(
                        "difficulty_id" to StructuredData.NumberValue("4"),
                        "max_round_id" to StructuredData.NumberValue("10"),
                        "medal_num" to StructuredData.NumberValue("10"),
                        "coin_num" to StructuredData.NumberValue("1140"),
                        "avatar_bonus_num" to StructuredData.NumberValue("4"),
                        "rent_cnt" to StructuredData.NumberValue("2")
                    ),
                    "schedule" to obj("schedule_id" to StructuredData.NumberValue("31"),
                        "start_date_time" to obj("year" to StructuredData.NumberValue("2026"), "month" to StructuredData.NumberValue("11"))),
                    "detail" to obj("rounds_data" to StructuredData.ArrayValue(listOf(
                        obj("round_id" to StructuredData.NumberValue("1"),
                            "is_get_medal" to StructuredData.BooleanValue(true),
                            "avatars" to StructuredData.ArrayValue(listOf(
                                obj("name" to StructuredData.StringValue("Flins")),
                                obj("name" to StructuredData.StringValue("Xingqiu"))
                            )))
                    )))
                ),
                obj(
                    "has_data" to StructuredData.BooleanValue(true),
                    "stat" to obj("difficulty_id" to StructuredData.NumberValue("3"), "max_round_id" to StructuredData.NumberValue("8"), "medal_num" to StructuredData.NumberValue("8")),
                    "schedule" to obj("schedule_id" to StructuredData.NumberValue("30"),
                        "start_date_time" to obj("year" to StructuredData.NumberValue("2026"), "month" to StructuredData.NumberValue("10"))),
                    "detail" to obj("rounds_data" to StructuredData.ArrayValue(emptyList()))
                )
            ))
        ), null)
        val cycles = ChroniclePresentationMapper.theaterCycles(value)
        assertEquals(2, cycles.size)
        assertEquals("November 2026", cycles[0].title)
        assertEquals("4", cycles[0].difficulty)
        assertEquals("10", cycles[0].medals)
        assertEquals("1140", cycles[0].coins)
        assertEquals(listOf("Flins", "Xingqiu"), cycles[0].rounds.single().characters.mapNotNull { it.name })
        assertEquals(true, cycles[0].rounds.single().medalEarned)
        assertEquals("October 2026", cycles[1].title)
    }

    @Test fun theaterPreservesCyclePositionsWhenARecordIsExplicitlyUnavailable() {
        val value = DataValue.Available(obj(
            "data" to StructuredData.ArrayValue(listOf(
                obj(
                    "has_data" to StructuredData.BooleanValue(false),
                    "schedule" to obj("schedule_id" to StructuredData.NumberValue("31"))
                ),
                obj(
                    "has_data" to StructuredData.BooleanValue(true),
                    "stat" to obj("difficulty_id" to StructuredData.NumberValue("4")),
                    "schedule" to obj("schedule_id" to StructuredData.NumberValue("30"))
                )
            ))
        ), null)
        val cycles = ChroniclePresentationMapper.theaterCycles(value)
        assertEquals(2, cycles.size)
        assertEquals(false, cycles[0].hasData)
        assertEquals("Cycle 30", cycles[1].title)
        assertEquals(true, cycles[1].hasData)
    }

    @Test fun theaterDoesNotInventCyclesForMissingOrUnavailableData() {
        assertTrue(ChroniclePresentationMapper.theaterCycles(DataValue.Missing).isEmpty())
        assertTrue(ChroniclePresentationMapper.theaterCycles(DataValue.Unavailable).isEmpty())
        val empty = DataValue.Available(obj("data" to StructuredData.ArrayValue(emptyList())), null)
        assertTrue(ChroniclePresentationMapper.theaterCycles(empty).isEmpty())
    }


    @Test fun spiralAbyssMapsFloorsChambersTeamsAndUsesOwnedCharacterNames() {
        val roster = listOf(
            com.irminsul.codex.domain.model.CharacterSnapshot(id = 10000120, name = "Flins", level = 90, rarity = 5, element = "Electro", friendshipLevel = 0, activeConstellationCount = 0),
            com.irminsul.codex.domain.model.CharacterSnapshot(id = 10000025, name = "Xingqiu", level = 70, rarity = 4, element = "Hydro", friendshipLevel = 0, activeConstellationCount = 0)
        )
        val value = DataValue.Available(obj(
            "total_battle_times" to StructuredData.NumberValue("38"),
            "total_win_times" to StructuredData.NumberValue("6"),
            "total_star" to StructuredData.NumberValue("36"),
            "max_floor" to StructuredData.StringValue("12-3"),
            "floors" to StructuredData.ArrayValue(listOf(
                obj(
                    "index" to StructuredData.NumberValue("12"),
                    "star" to StructuredData.NumberValue("9"),
                    "max_star" to StructuredData.NumberValue("9"),
                    "ley_line_disorder" to StructuredData.ArrayValue(listOf(StructuredData.StringValue("All party members gain a 60% Electro DMG Bonus."))),
                    "levels" to StructuredData.ArrayValue(listOf(
                        obj(
                            "index" to StructuredData.NumberValue("1"),
                            "star" to StructuredData.NumberValue("3"),
                            "max_star" to StructuredData.NumberValue("3"),
                            "battles" to StructuredData.ArrayValue(listOf(
                                obj(
                                    "index" to StructuredData.NumberValue("1"),
                                    "settle_date_time" to obj(
                                        "year" to StructuredData.NumberValue("2026"),
                                        "month" to StructuredData.NumberValue("11"),
                                        "day" to StructuredData.NumberValue("16"),
                                        "hour" to StructuredData.NumberValue("9"),
                                        "minute" to StructuredData.NumberValue("8")
                                    ),
                                    "avatars" to StructuredData.ArrayValue(listOf(
                                        obj("id" to StructuredData.NumberValue("10000120"), "level" to StructuredData.NumberValue("90"), "rarity" to StructuredData.NumberValue("5")),
                                        obj("id" to StructuredData.NumberValue("10000025"), "level" to StructuredData.NumberValue("70"), "rarity" to StructuredData.NumberValue("4"))
                                    ))
                                )
                            ))
                        )
                    ))
                )
            ))
        ), null)
        val cycle = ChroniclePresentationMapper.spiralAbyss(value, roster)
        assertEquals("38", cycle?.battles)
        assertEquals("6", cycle?.wins)
        assertEquals("36", cycle?.stars)
        assertEquals("12-3", cycle?.bestFloor)
        assertEquals("12", cycle?.floors?.single()?.floorNumber)
        assertEquals("All party members gain a 60% Electro DMG Bonus.", cycle?.floors?.single()?.leyLineDisorders?.single())
        val chamber = cycle?.floors?.single()?.chambers?.single()
        assertEquals("1", chamber?.chamberNumber)
        assertEquals("3", chamber?.stars)
        assertEquals("First Half", chamber?.battles?.single()?.half)
        assertEquals("2026-11-16 · 9:08", chamber?.battles?.single()?.completedAt)
        assertEquals(listOf("Flins", "Xingqiu"), chamber?.battles?.single()?.characters?.mapNotNull { it.name })
        assertEquals(listOf("Electro", "Hydro"), chamber?.battles?.single()?.characters?.mapNotNull { it.element })
    }

    @Test fun spiralAbyssIgnoresMalformedEntriesAndPreservesMissingSectionState() {
        assertEquals(null, ChroniclePresentationMapper.spiralAbyss(DataValue.Missing))
        assertEquals(null, ChroniclePresentationMapper.spiralAbyss(DataValue.Unavailable))
        val malformed = DataValue.Available(obj(
            "floors" to StructuredData.ArrayValue(listOf(
                StructuredData.StringValue("not a floor"),
                obj("index" to StructuredData.StringValue(""), "levels" to StructuredData.ArrayValue(emptyList()))
            ))
        ), null)
        val cycle = ChroniclePresentationMapper.spiralAbyss(malformed)
        assertTrue(cycle?.floors.isNullOrEmpty())
        assertEquals(null, cycle?.stars)
    }

    @Test fun theaterMapsCharacterDetailsBuffNamesAndAvailablePerformanceStatistics() {
        val roster = listOf(
            com.irminsul.codex.domain.model.CharacterSnapshot(id = 10000111, name = "Varesa", level = 90, rarity = 5, element = "Electro", friendshipLevel = 0, activeConstellationCount = 0)
        )
        val value = DataValue.Available(obj(
            "data" to StructuredData.ArrayValue(listOf(
                obj(
                    "has_data" to StructuredData.BooleanValue(true),
                    "stat" to obj("difficulty_id" to StructuredData.NumberValue("4"), "max_round_id" to StructuredData.NumberValue("10"), "medal_num" to StructuredData.NumberValue("10")),
                    "schedule" to obj("schedule_id" to StructuredData.NumberValue("31"), "start_date_time" to obj("year" to StructuredData.NumberValue("2026"), "month" to StructuredData.NumberValue("11"))),
                    "detail" to obj(
                        "rounds_data" to StructuredData.ArrayValue(listOf(
                            obj(
                                "round_id" to StructuredData.NumberValue("1"),
                                "is_get_medal" to StructuredData.BooleanValue(true),
                                "avatars" to StructuredData.ArrayValue(listOf(
                                    obj("avatar_id" to StructuredData.NumberValue("10000111"), "name" to StructuredData.StringValue("Varesa"), "element" to StructuredData.StringValue("Electro"), "level" to StructuredData.NumberValue("90"), "rarity" to StructuredData.NumberValue("5"), "image" to StructuredData.StringValue("https://example.invalid/varesa.png"))
                                )),
                                "finish_date_time" to obj("year" to StructuredData.NumberValue("2026"), "month" to StructuredData.NumberValue("11"), "day" to StructuredData.NumberValue("2")),
                                "buffs" to StructuredData.ArrayValue(emptyList()),
                                "splendour_buff" to obj("buffs" to StructuredData.ArrayValue(listOf(obj("name" to StructuredData.StringValue("Electro Crystal Blessing")))))
                            )
                        )),
                        "fight_statisic" to obj(
                            "max_damage_avatar" to obj("avatar_id" to StructuredData.NumberValue("10000111"), "value" to StructuredData.StringValue("816241")),
                            "max_defeat_avatar" to obj("avatar_id" to StructuredData.NumberValue("10000111"), "value" to StructuredData.StringValue("9")),
                            "max_take_damage_avatar" to obj("avatar_id" to StructuredData.NumberValue("10000111"), "value" to StructuredData.StringValue("17892")),
                            "total_use_time" to StructuredData.NumberValue("679")
                        )
                    )
                )
            ))
        ), null)
        val cycle = ChroniclePresentationMapper.theaterCycles(value, roster).single()
        val round = cycle.rounds.single()
        assertEquals("Varesa", round.characters.single().name)
        assertEquals("Electro", round.characters.single().element)
        assertEquals(90, round.characters.single().level)
        assertEquals("2026-11-2", round.completedAt)
        assertEquals("Electro Crystal Blessing", round.buffs.single())
        assertEquals("816241 · Varesa", cycle.performanceMetrics.first().value)
        assertEquals("679 seconds", cycle.performanceMetrics.last().value)
    }


}
