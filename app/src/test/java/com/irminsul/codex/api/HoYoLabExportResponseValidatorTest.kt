package com.irminsul.codex.api

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HoYoLabExportResponseValidatorTest {

    @Test
    fun rejectsMissingData() {
        val response = JSONObject().put("retcode", 0)
        assertEquals(
            "Daily Note response did not contain an account data object.",
            HoYoLabExportResponseValidator.validationError(response, "Daily Note")
        )
    }

    @Test
    fun rejectsNullData() {
        val response = JSONObject().put("retcode", 0).put("data", JSONObject.NULL)
        assertEquals(
            "Daily Note response did not contain an account data object.",
            HoYoLabExportResponseValidator.validationError(response, "Daily Note")
        )
    }

    @Test
    fun rejectsWrongDataType() {
        val response = JSONObject().put("retcode", 0).put("data", JSONArray())
        assertEquals(
            "Daily Note response did not contain an account data object.",
            HoYoLabExportResponseValidator.validationError(response, "Daily Note")
        )
    }

    @Test
    fun rejectsMissingRequiredList() {
        val response = JSONObject().put("retcode", 0).put("data", JSONObject())
        assertEquals(
            "Character list response did not contain a valid 'list' array.",
            HoYoLabExportResponseValidator.validationError(
                response,
                "Character list",
                "list"
            )
        )
    }

    @Test
    fun acceptsEmptyObjectForObjectShapedEndpoint() {
        val response = JSONObject().put("retcode", 0).put("data", JSONObject())
        assertNull(
            HoYoLabExportResponseValidator.validationError(response, "Daily Note")
        )
    }

    @Test
    fun acceptsEmptyArrayForListShapedEndpoint() {
        val response = JSONObject()
            .put("retcode", 0)
            .put("data", JSONObject().put("list", JSONArray()))
        assertNull(
            HoYoLabExportResponseValidator.validationError(
                response,
                "Character list",
                "list"
            )
        )
    }

    @Test
    fun rejectsNonZeroRetcodeEvenWhenDataExists() {
        val response = JSONObject()
            .put("retcode", -1)
            .put("message", "Unavailable")
            .put("data", JSONObject())
        assertEquals(
            "Error -1: Unavailable",
            HoYoLabExportResponseValidator.validationError(response, "Daily Note")
        )
    }
}
