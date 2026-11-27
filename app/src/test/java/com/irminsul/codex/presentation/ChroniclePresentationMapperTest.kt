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
}
