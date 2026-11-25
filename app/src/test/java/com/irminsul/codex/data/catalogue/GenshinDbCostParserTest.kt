package com.irminsul.codex.data.catalogue

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class GenshinDbCostParserTest {

    @Test fun parsesCharacterAscensionSegmentsIntoExpectedLevelRanges() {
        val result = GenshinDbCostParser.parseCharacterAscensionCosts(ascensionCosts())
        assertEquals(CatalogueCostParseStatus.COMPLETE, result.status)
        assertEquals(
            listOf(1 to 20, 20 to 40, 40 to 50, 50 to 60, 60 to 70, 70 to 80),
            result.segments.map { it.fromLevel to it.toLevel }
        )
        assertEquals("202", result.segments.first().costs.first().itemId)
        assertEquals("Mora", result.segments.first().costs.first().itemName)
        assertEquals(10_000L, result.segments.first().costs.first().quantity)
    }

    @Test fun weaponAscensionUsesTheSameAscensionRanges() {
        val result = GenshinDbCostParser.parseWeaponAscensionCosts(ascensionCosts())
        assertEquals(CatalogueCostParseStatus.COMPLETE, result.status)
        assertEquals(6, result.segments.size)
    }

    @Test fun parsesTalentLevelsAsContiguousSegments() {
        val costs = JSONObject()
        for (level in 2..10) {
            costs.put("lvl$level", org.json.JSONArray().put(
                JSONObject().put("id", 202).put("name", "Mora").put("count", level * 1000)
            ))
        }
        val result = GenshinDbCostParser.parseTalentCosts(costs)
        assertEquals(CatalogueCostParseStatus.COMPLETE, result.status)
        assertEquals((2..10).map { (it - 1) to it }, result.segments.map { it.fromLevel to it.toLevel })
        assertEquals(10_000L, result.segments.last().costs.single().quantity)
    }

    @Test fun missingStageIsNotTreatedAsAFreeUpgrade() {
        val costs = ascensionCosts().apply { remove("ascend4") }
        val result = GenshinDbCostParser.parseCharacterAscensionCosts(costs)
        assertEquals(CatalogueCostParseStatus.MISSING_STAGE, result.status)
        assertEquals(listOf("ascend4"), result.missingStageKeys)
        assertTrue(result.segments.isEmpty())
    }

    @Test fun explicitlyEmptyStageIsPreservedForCuration() {
        val costs = ascensionCosts().apply { put("ascend3", org.json.JSONArray()) }
        val result = GenshinDbCostParser.parseWeaponAscensionCosts(costs)
        assertEquals(CatalogueCostParseStatus.COMPLETE, result.status)
        assertTrue(result.segments[2].costs.isEmpty())
        assertEquals(40 to 50, result.segments[2].fromLevel to result.segments[2].toLevel)
    }

    @Test fun rejectsMalformedStageAndMaterialRecords() {
        val malformedStage = ascensionCosts().apply { put("ascend2", "not-an-array") }
        assertEquals(
            CatalogueCostParseStatus.INVALID_DATA,
            GenshinDbCostParser.parseCharacterAscensionCosts(malformedStage).status
        )

        val malformedMaterial = ascensionCosts().apply {
            put("ascend1", org.json.JSONArray().put(JSONObject().put("id", 202).put("name", "Mora")))
        }
        assertEquals(
            CatalogueCostParseStatus.INVALID_DATA,
            GenshinDbCostParser.parseCharacterAscensionCosts(malformedMaterial).status
        )
    }

    @Test fun rejectsFractionalAndOverflowingIdsOrQuantities() {
        val fractional = ascensionCosts().apply {
            put("ascend1", org.json.JSONArray().put(
                JSONObject().put("id", 202.5).put("name", "Mora").put("count", 10)
            ))
        }
        assertEquals(
            CatalogueCostParseStatus.INVALID_DATA,
            GenshinDbCostParser.parseCharacterAscensionCosts(fractional).status
        )

        val overflow = ascensionCosts().apply {
            put("ascend1", org.json.JSONArray().put(
                JSONObject().put("id", 202).put("name", "Mora").put("count", "9223372036854775808")
            ))
        }
        assertEquals(
            CatalogueCostParseStatus.INVALID_DATA,
            GenshinDbCostParser.parseCharacterAscensionCosts(overflow).status
        )
    }

    private fun ascensionCosts(): JSONObject {
        val costs = JSONObject()
        val names = listOf("ascend1", "ascend2", "ascend3", "ascend4", "ascend5", "ascend6")
        names.forEachIndexed { index, stage ->
            costs.put(stage, org.json.JSONArray().put(
                JSONObject()
                    .put("id", 202)
                    .put("name", "Mora")
                    .put("count", (index + 1) * 10_000)
            ))
        }
        return costs
    }
}
