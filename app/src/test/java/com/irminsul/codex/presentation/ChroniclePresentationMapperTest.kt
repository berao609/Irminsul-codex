package com.irminsul.codex.presentation

import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChroniclePresentationMapperTest {
    private fun obj(vararg fields: Pair<String, StructuredData>) = StructuredData.ObjectValue(mapOf(*fields))

    @Test fun accountOverviewUsesOnlyApprovedSummaryFields() {
        val data = DataValue.Available(obj(
            "nickname" to StructuredData.StringValue("Traveler"),
            "level" to StructuredData.NumberValue("60"),
            "uid" to StructuredData.StringValue("800000001"),
            "avatar_url" to StructuredData.StringValue("https://example.invalid/a.png"),
            "unknown_internal_field" to StructuredData.StringValue("do not show")
        ), null)
        assertEquals(
            listOf(ChronicleMetric("Player", "Traveler"), ChronicleMetric("Adventure Rank", "60")),
            ChroniclePresentationMapper.accountOverview(data)
        )
    }

    @Test fun explorationMapsNamedRegionsAndNormalizesBasisPointPercentages() {
        val data = DataValue.Available(obj(
            "exploration" to StructuredData.ArrayValue(listOf(
                obj("name" to StructuredData.StringValue("Mondstadt"),
                    "exploration_percentage" to StructuredData.NumberValue("8750"),
                    "id" to StructuredData.NumberValue("1"),
                    "icon" to StructuredData.StringValue("https://example.invalid/icon.png")),
                obj("name" to StructuredData.StringValue("Liyue"),
                    "exploration_percentage" to StructuredData.NumberValue("82"))
            ))
        ), null)
        val regions = ChroniclePresentationMapper.explorationRegions(data)
        assertEquals(2, regions.size)
        assertEquals("Mondstadt", regions[0].name)
        assertEquals("87%", regions[0].percentageLabel)
        assertEquals(0.875f, regions[0].progress, 0.001f)
        assertEquals("82%", regions[1].percentageLabel)
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
        assertTrue(ChroniclePresentationMapper.accountOverview(DataValue.Missing).isEmpty())
        assertTrue(ChroniclePresentationMapper.explorationRegions(DataValue.Unavailable).isEmpty())
    }
}
