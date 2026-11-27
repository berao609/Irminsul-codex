package com.irminsul.codex.api

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BattleChronicleCollectorTest {
    private val apiBase = "https://example.invalid/game_record/genshin/api"
    private val retrievedAt = "2026-11-27T14:00:00Z"

    private fun success(source: String) = FetchedExportResponse(
        response = JSONObject()
            .put("retcode", 0)
            .put("message", "OK")
            .put("data", JSONObject().put("source_marker", source)),
        source = source,
        retrievedAt = retrievedAt
    )

    @Test
    fun collectsChronicleEndpointsSeparatelyAndMapsIndependentSections() = runBlocking {
        val requested = mutableListOf<Pair<String, String>>()
        val collector = BattleChronicleCollector(apiBase) { source, url ->
            synchronized(requested) { requested += source to url }
            success(source)
        }

        val responses = collector.collect("800000001", "os_asia")
        val sections = responses.toExportSections(emptyMap())

        assertEquals(4, requested.size)
        assertTrue(requested.any { it.second == "$apiBase/index?server=os_asia&role_id=800000001" })
        assertTrue(requested.any { it.second.endsWith("schedule_type=1") })
        assertTrue(requested.any { it.second.endsWith("schedule_type=2") })
        assertTrue(requested.any { it.second.endsWith("need_detail=true") })
        assertEquals(
            setOf(
                "profile_and_exploration",
                "spiral_abyss_current",
                "spiral_abyss_previous",
                "imaginarium_theater"
            ),
            sections.keys
        )
        assertTrue(sections.values.all { it.status == "success" })
        assertTrue(sections.values.all { it.retrievedAt == retrievedAt })
    }

    @Test
    fun oneFailedChronicleEndpointDoesNotDiscardSuccessfulSiblingSections() = runBlocking {
        val collector = BattleChronicleCollector(apiBase) { source, _ ->
            if (source == "HoYoLAB index") {
                FetchedExportResponse(
                    response = JSONObject()
                        .put("retcode", 10001)
                        .put("message", "Not logged in"),
                    source = source,
                    retrievedAt = retrievedAt
                )
            } else {
                success(source)
            }
        }

        val sections = collector.collect("800000001", "os_asia").toExportSections(emptyMap())

        assertEquals("failed", sections.getValue("profile_and_exploration").status)
        assertEquals("success", sections.getValue("spiral_abyss_current").status)
        assertEquals("success", sections.getValue("spiral_abyss_previous").status)
        assertEquals("success", sections.getValue("imaginarium_theater").status)
    }

    @Test
    fun cancellationFromAnEndpointIsNotConvertedIntoASectionFailure() = runBlocking {
        val collector = BattleChronicleCollector(apiBase) { source, _ ->
            if (source == "HoYoLAB index") throw CancellationException("cancelled")
            success(source)
        }

        try {
            collector.collect("800000001", "os_asia")
            throw AssertionError("Expected cancellation to propagate")
        } catch (_: CancellationException) {
            // Expected: cancellation is a control signal, not an ordinary endpoint failure.
        }
    }
}
