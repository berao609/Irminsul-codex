package com.irminsul.codex.api

import org.json.JSONException
import org.json.JSONObject

/**
 * Converts an HTTP response into the response envelope expected by the existing
 * HoYoLAB endpoint handlers. Transport and HTTP failures become explicit error
 * envelopes so independent export sections can retain their own failure details.
 */
internal object HoYoLabResponseParser {

    fun parse(body: String?, httpStatus: Int): JSONObject {
        if (body.isNullOrBlank()) {
            return errorResponse("Empty response (HTTP $httpStatus)")
        }

        val json = try {
            JSONObject(body)
        } catch (_: JSONException) {
            return errorResponse("Invalid JSON response (HTTP $httpStatus)")
        }

        if (httpStatus !in 200..299) {
            val apiMessage = json.optString("message", "")
                .trim()
                .takeIf { it.isNotEmpty() }
                ?.take(MAX_ERROR_MESSAGE_LENGTH)
            val message = if (apiMessage == null) {
                "HTTP $httpStatus"
            } else {
                "HTTP $httpStatus: $apiMessage"
            }

            val existingRetcode = json.optInt("retcode", -1)
            json.put(
                "retcode",
                if (!json.has("retcode") || existingRetcode == 0) -1 else existingRetcode
            )
            json.put("message", message)
        }

        return json
    }

    private fun errorResponse(message: String): JSONObject {
        return JSONObject()
            .put("retcode", -1)
            .put("message", message)
    }

    private const val MAX_ERROR_MESSAGE_LENGTH = 240
}
