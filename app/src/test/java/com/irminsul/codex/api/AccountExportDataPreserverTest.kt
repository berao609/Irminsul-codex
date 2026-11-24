package com.irminsul.codex.api

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountExportDataPreserverTest {

    @Test
    fun preservesSemanticFieldsUrlsAndNullValues() {
        val source = JSONObject()
            .put("name", "Example Character")
            .put("description", "A useful description")
            .put("icon", "https://example.com/icon.png")
            .put("optional_value", JSONObject.NULL)
            .put("skill_affix_list", JSONArray().put(JSONObject().put("effect", "Effect text")))

        val preserved = AccountExportDataPreserver.preserve(source) as JSONObject

        assertEquals("Example Character", preserved.optString("name"))
        assertEquals("A useful description", preserved.optString("description"))
        assertEquals("https://example.com/icon.png", preserved.optString("icon"))
        assertTrue(preserved.has("optional_value"))
        assertTrue(preserved.isNull("optional_value"))
        assertEquals(
            "Effect text",
            preserved.getJSONArray("skill_affix_list").getJSONObject(0).optString("effect")
        )
    }

    @Test
    fun preservesArrayOrderAndNestedValues() {
        val source = JSONArray()
            .put(JSONObject().put("level", 90))
            .put(JSONObject().put("level", 80))
        val preserved = AccountExportDataPreserver.preserve(source) as JSONArray

        assertEquals(2, preserved.length())
        assertEquals(90, preserved.getJSONObject(0).optInt("level"))
        assertEquals(80, preserved.getJSONObject(1).optInt("level"))
    }

    @Test
    fun addsReadableStatNameWithoutRemovingOriginalPropertyType() {
        val source = JSONObject().put("property_type", 2001).put("value", "100")
        val preserved = AccountExportDataPreserver.preserve(
            source,
            mapOf(2001 to "HP")
        ) as JSONObject

        assertEquals(2001, preserved.optInt("property_type"))
        assertEquals("HP", preserved.optString("stat_name"))
        assertEquals("100", preserved.optString("value"))
    }

    @Test
    fun retainsRawPropertyMapAndUnrecognizedDetailFields() {
        val source = JSONObject()
            .put("property_map", JSONObject().put("2001", JSONObject().put("name", "HP")))
            .put("future_field", JSONObject().put("nested", true))
        val preserved = AccountExportDataPreserver.preserve(source) as JSONObject

        assertEquals("HP", preserved.getJSONObject("property_map")
            .getJSONObject("2001").optString("name"))
        assertTrue(preserved.getJSONObject("future_field").optBoolean("nested"))
    }
}
