package com.irminsul.codex.api

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountExportAssemblerTest {

    @Test
    fun assemblesCompleteExportAndAllowsSkippedOptionalSection() {
        val profile = JSONObject().put("level", 60)
        val export = AccountExportAssembler.assemble(
            uid = "800000001",
            server = "os_asia",
            exportedAt = "2026-11-25T00:00:00Z",
            sections = linkedMapOf(
                "profile_and_exploration" to ExportSectionResult.success(profile),
                "daily_note" to ExportSectionResult.success(JSONObject().put("current_resin", 120)),
                "account_card" to ExportSectionResult.skipped("Optional account ID cookie unavailable.")
            )
        )

        assertEquals(1, export.optInt("export_format_version"))
        val status = export.getJSONObject("export_status")
        assertEquals("complete", status.optString("overall_status"))
        assertEquals(60, export.getJSONObject("profile_and_exploration").optInt("level"))
        assertFalse(export.has("account_card"))
        assertEquals(
            "skipped",
            status.getJSONObject("sections").getJSONObject("account_card").optString("status")
        )
    }

    @Test
    fun failedSectionDoesNotRemoveSuccessfulSections() {
        val failure = JSONObject()
            .put("retcode", -1)
            .put("message", "Request timed out")
        val export = AccountExportAssembler.assemble(
            uid = "800000001",
            server = "os_asia",
            exportedAt = "2026-11-25T00:00:00Z",
            sections = linkedMapOf(
                "profile_and_exploration" to ExportSectionResult.success(JSONObject().put("level", 60)),
                "daily_note" to ExportSectionResult.failed(failure, "Request timed out"),
                "spiral_abyss_current" to ExportSectionResult.success(JSONObject().put("total_battles", 12))
            )
        )

        assertEquals(1, export.optInt("export_format_version"))
        assertEquals("partial", export.getJSONObject("export_status").optString("overall_status"))
        assertEquals(60, export.getJSONObject("profile_and_exploration").optInt("level"))
        assertEquals(12, export.getJSONObject("spiral_abyss_current").optInt("total_battles"))
        assertEquals(-1, export.getJSONObject("daily_note").optInt("retcode"))
        assertEquals(
            "failed",
            export.getJSONObject("export_status")
                .getJSONObject("sections")
                .getJSONObject("daily_note")
                .optString("status")
        )
    }

    @Test
    fun partialCharacterDetailsRetainAvailableCharactersAndExplainMissingBatches() {
        val characters = JSONArray().put(JSONObject().put("id", 10000002))
        val export = AccountExportAssembler.assemble(
            uid = "800000001",
            server = "os_asia",
            exportedAt = "2026-11-25T00:00:00Z",
            sections = mapOf(
                "characters" to ExportSectionResult.partial(
                    characters,
                    "Character detail batch 2: Request failed (SocketTimeoutException)"
                )
            )
        )

        assertEquals("partial", export.getJSONObject("export_status").optString("overall_status"))
        assertEquals(1, export.getJSONArray("characters").length())
        assertTrue(
            export.getJSONObject("export_status")
                .getJSONObject("sections")
                .getJSONObject("characters")
                .optString("message")
                .contains("batch 2")
        )
    }

    @Test
    fun reportsFailedWhenEveryRequestedSectionFails() {
        val export = AccountExportAssembler.assemble(
            uid = "800000001",
            server = "os_asia",
            exportedAt = "2026-11-25T00:00:00Z",
            sections = mapOf(
                "daily_note" to ExportSectionResult.failed(
                    JSONObject().put("retcode", -1).put("message", "Unavailable"),
                    "Unavailable"
                ),
                "characters" to ExportSectionResult.failed(
                    JSONObject().put("retcode", -1).put("message", "Unavailable"),
                    "Unavailable"
                )
            )
        )

        assertEquals("failed", export.getJSONObject("export_status").optString("overall_status"))
        assertTrue(export.has("daily_note"))
        assertTrue(export.has("characters"))
    }
}
