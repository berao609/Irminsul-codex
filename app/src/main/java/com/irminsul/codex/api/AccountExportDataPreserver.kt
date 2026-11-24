package com.irminsul.codex.api

import org.json.JSONArray
import org.json.JSONObject

/**
 * Copies account data into the export without dropping source fields.
 *
 * The export is an account-data interchange format, not a presentation model.
 * Preserve nulls, URLs, descriptions, and other source fields so future
 * intelligence features can use data that the current UI does not display.
 */
internal object AccountExportDataPreserver {

    fun preserve(node: Any?, propertyMap: Map<Int, String> = emptyMap()): Any? {
        return when (node) {
            null -> null
            JSONObject.NULL -> JSONObject.NULL
            is JSONObject -> {
                val copy = JSONObject()
                val propertyType = node.optInt("property_type", -1)
                if (propertyType != -1 && !node.has("stat_name")) {
                    propertyMap[propertyType]?.let { copy.put("stat_name", it) }
                }

                val keys = node.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = node.opt(key)
                    copy.put(key, preserve(value, propertyMap))
                }
                copy
            }
            is JSONArray -> {
                val copy = JSONArray()
                for (index in 0 until node.length()) {
                    copy.put(preserve(node.opt(index), propertyMap))
                }
                copy
            }
            else -> node
        }
    }
}
