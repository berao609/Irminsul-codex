package com.irminsul.codex.api

import org.json.JSONArray
import org.json.JSONObject

/**
 * Validates the minimum response structure required by each export section.
 * Empty objects and arrays remain valid when their container shape is correct.
 */
internal object HoYoLabExportResponseValidator {

    fun validationError(
        response: JSONObject,
        sectionName: String,
        requiredArrayField: String? = null
    ): String? {
        if (!response.has("retcode") || response.optInt("retcode", Int.MIN_VALUE) != 0) {
            val retcode = response.optInt("retcode", -1)
            val message = response.optString("message", "Request failed")
                .takeIf { it.isNotBlank() } ?: "Request failed"
            return "Error $retcode: $message"
        }

        val data = response.opt("data")
        if (data !is JSONObject) {
            return "$sectionName response did not contain an account data object."
        }

        if (requiredArrayField != null && data.opt(requiredArrayField) !is JSONArray) {
            return "$sectionName response did not contain a valid '$requiredArrayField' array."
        }

        return null
    }
}
