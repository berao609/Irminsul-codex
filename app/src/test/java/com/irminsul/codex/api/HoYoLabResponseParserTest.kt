package com.irminsul.codex.api

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HoYoLabResponseParserTest {

    @Test
    fun preservesSuccessfulHttpResponse() {
        val response = HoYoLabResponseParser.parse(
            body = """{"retcode":0,"data":{"value":42}}""",
            httpStatus = 200
        )

        assertEquals(0, response.optInt("retcode", -1))
        assertEquals(42, response.getJSONObject("data").optInt("value"))
    }

    @Test
    fun preservesHoYoLabApiErrorOnSuccessfulHttpResponse() {
        val response = HoYoLabResponseParser.parse(
            body = """{"retcode":-100,"message":"Invalid authentication"}""",
            httpStatus = 200
        )

        assertEquals(-100, response.optInt("retcode"))
        assertEquals("Invalid authentication", response.optString("message"))
    }

    @Test
    fun convertsHttpFailureWithSuccessfulApiEnvelopeIntoFailure() {
        val response = HoYoLabResponseParser.parse(
            body = """{"retcode":0,"message":"Request succeeded"}""",
            httpStatus = 401
        )

        assertEquals(-1, response.optInt("retcode"))
        assertTrue(response.optString("message").startsWith("HTTP 401"))
    }

    @Test
    fun retainsApiErrorCodeAndAddsHttpStatus() {
        val response = HoYoLabResponseParser.parse(
            body = """{"retcode":-100,"message":"Invalid authentication"}""",
            httpStatus = 403
        )

        assertEquals(-100, response.optInt("retcode"))
        assertTrue(response.optString("message").startsWith("HTTP 403"))
        assertTrue(response.optString("message").contains("Invalid authentication"))
    }

    @Test
    fun convertsEmptyAndMalformedBodiesIntoExplicitFailures() {
        val empty = HoYoLabResponseParser.parse(body = null, httpStatus = 204)
        val malformed = HoYoLabResponseParser.parse(body = "<html>not json</html>", httpStatus = 502)

        assertEquals(-1, empty.optInt("retcode"))
        assertEquals("Empty response (HTTP 204)", empty.optString("message"))
        assertEquals(-1, malformed.optInt("retcode"))
        assertEquals("Invalid JSON response (HTTP 502)", malformed.optString("message"))
        assertFalse(malformed.has("data"))
    }

    @Test
    fun truncatesLongServerErrorMessages() {
        val longMessage = "x".repeat(400)
        val response = HoYoLabResponseParser.parse(
            body = JSONObject()
                .put("retcode", -1)
                .put("message", longMessage)
                .toString(),
            httpStatus = 500
        )

        assertTrue(response.optString("message").length <= 250)
    }
}
