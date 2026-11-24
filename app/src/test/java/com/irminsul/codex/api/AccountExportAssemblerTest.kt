package com.irminsul.codex.api

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountExportAssemblerTest {

    private fun requiredSections(): LinkedHashMap<String, ExportSectionResult> =
        linkedMapOf(
            "profile_and_exploration" to ExportSectionResult.success(JSONObject().put("level", 60)),
            "daily_note" to ExportSectionResult.success(JSONObject().put("current_resin", 120)),
            "characters" to ExportSectionResult.success(JSONArray()),
            "spiral_abyss_current" to ExportSectionResult.success(JSONObject()),
            "spiral_abyss_previous" to ExportSectionResult.success(JSONObject()),
            "imaginarium_theater" to ExportSectionResult.success(JSONObject())
        )

    @Test
    fun completeExportAllowsSkippedOptionalSection() {
        val sections = requiredSections()
        sections["account_card"] = ExportSectionResult.skipped(
            "Optional account ID cookie unavailable.",
            "HoYoLAB game record card"
        )
        val export = AccountExportAssembler.assemble(
            uid = "800000001",
            server = "os_asia",
            exportedAt = "2026-11-25T00:00:00Z",
            sections = sections
        )

        assertEquals(1, export.optInt("export_format_version"))
        assertEquals("complete", export.getJSONObject("export_status").optString("overall_status"))
        assertEquals(60, export.getJSONObject("profile_and_exploration").optInt("level"))
        assertFalse(export.has("account_card"))
        val cardStatus = export.getJSONObject("export_status")
            .getJSONObject("sections").getJSONObject("account_card")
        assertEquals("skipped", cardStatus.optString("status"))
        assertEquals("HoYoLAB game record card", cardStatus.optString("source"))
    }

    @Test
    fun skippedRequiredSectionMakesExportPartial() {
        val sections = requiredSections()
        sections["daily_note"] = ExportSectionResult.skipped("Not retrieved")
        val export = AccountExportAssembler.assemble(
            "800000001", "os_asia", "2026-11-25T00:00:00Z", sections
        )
        assertEquals("partial", export.getJSONObject("export_status").optString("overall_status"))
    }

    @Test
    fun failedSectionDoesNotRemoveSuccessfulSections() {
        val sections = requiredSections()
        sections["daily_note"] = ExportSectionResult.failed(
            JSONObject().put("retcode", -1).put("message", "Request timed out"),
            "Request timed out",
            "HoYoLAB dailyNote",
            "2026-11-25T00:00:00Z"
        )
        val export = AccountExportAssembler.assemble(
            "800000001", "os_asia", "2026-11-25T00:00:00Z", sections
        )

        assertEquals("partial", export.getJSONObject("export_status").optString("overall_status"))
        assertEquals(60, export.getJSONObject("profile_and_exploration").optInt("level"))
        assertEquals(-1, export.getJSONObject("daily_note").optInt("retcode"))
        val dailyStatus = export.getJSONObject("export_status")
            .getJSONObject("sections").getJSONObject("daily_note")
        assertEquals("failed", dailyStatus.optString("status"))
        assertEquals("HoYoLAB dailyNote", dailyStatus.optString("source"))
        assertEquals("2026-11-25T00:00:00Z", dailyStatus.optString("retrieved_at"))
        assertFalse(dailyStatus.has("last_successful_retrieval_at"))
    }

    @Test
    fun successfulSectionIncludesRetrievalMetadata() {
        val sections = requiredSections()
        sections["daily_note"] = ExportSectionResult.success(
            JSONObject().put("current_resin", 120),
            "HoYoLAB dailyNote",
            "2026-11-25T00:00:00Z"
        )
        val export = AccountExportAssembler.assemble(
            "800000001", "os_asia", "2026-11-25T00:00:00Z", sections
        )
        val status = export.getJSONObject("export_status")
            .getJSONObject("sections").getJSONObject("daily_note")
        assertEquals("HoYoLAB dailyNote", status.optString("source"))
        assertEquals("2026-11-25T00:00:00Z", status.optString("retrieved_at"))
        assertEquals(
            "2026-11-25T00:00:00Z",
            status.optString("last_successful_retrieval_at")
        )
    }

    @Test
    fun partialCharacterDetailsRetainAvailableCharactersAndExplainMissingBatches() {
        val characters = JSONArray().put(JSONObject().put("id", 10000002))
        val sections = requiredSections()
        sections["characters"] = ExportSectionResult.partial(
            characters,
            "Character detail batch 2: Request failed",
            "HoYoLAB character/detail",
            "2026-11-25T00:00:00Z"
        )
        val export = AccountExportAssembler.assemble(
            "800000001", "os_asia", "2026-11-25T00:00:00Z", sections
        )

        assertEquals("partial", export.getJSONObject("export_status").optString("overall_status"))
        assertEquals(1, export.getJSONArray("characters").length())
        assertTrue(
            export.getJSONObject("export_status").getJSONObject("sections")
                .getJSONObject("characters").optString("message").contains("batch 2")
        )
    }

    @Test
    fun reportsFailedWhenNoSectionContainsUsableData() {
        val sections = mapOf(
            "daily_note" to ExportSectionResult.failed(
                JSONObject().put("retcode", -1).put("message", "Unavailable"),
                "Unavailable"
            ),
            "characters" to ExportSectionResult.failed(
                JSONObject().put("retcode", -1).put("message", "Unavailable"),
                "Unavailable"
            )
        )
        val export = AccountExportAssembler.assemble(
            "800000001", "os_asia", "2026-11-25T00:00:00Z", sections
        )
        assertEquals("failed", export.getJSONObject("export_status").optString("overall_status"))
        assertTrue(export.has("daily_note"))
        assertTrue(export.has("characters"))
    }
}
