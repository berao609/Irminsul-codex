package com.irminsul.codex.data.catalogue

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class GenshinDbCostParserTest {

    @Test fun parsesCharacterAscensionStagesIntoSparseLevelThresholds() {
        val costs = ascensionCosts()
        val result = GenshinDbCostParser.parseCharacterAscensionCosts(costs)
        assertEquals(CatalogueCostParseStatus.COMPLETE, result.status)
        assertEquals(listOf(20, 40, 50, 60, 70, 80), result.steps.map { it.targetLevel })
        assertEquals("Mora", result.steps.first().materials.first().materialName)
        assertEquals(20_000L, result.steps.first().materials.first().quantity)
    }

    @Test fun weaponAscensionUsesTheSameAscensionThresholds() {
        val result = GenshinDbCostParser.parseWeaponAscensionCosts(ascensionCosts())
        assertEquals(CatalogueCostParseStatus.COMPLETE, result.status)
        assertEquals(6, result.steps.size)
    }

    @Test fun parsesTalentLevelsTwoThroughTen() {
        val costs = JSONObject()
        for (level in 2..10) {
            costs.put("lvl$level", org.json.JSONArray().put(
                JSONObject().put("id", 202).put("name", "Mora").put("count", level * 1000)
            ))
        }
        val result = GenshinDbCostParser.parseTalentCosts(costs)
        assertEquals(CatalogueCostParseStatus.COMPLETE, result.status)
        assertEquals((2..10).toList(), result.steps.map { it.targetLevel })
        assertEquals(10_000L, result.steps.last().materials.single().quantity)
    }

    @Test fun missingStageIsNotTreatedAsAnEmptyFreeStage() {
        val costs = ascensionCosts().apply { remove("ascend4") }
        val result = GenshinDbCostParser.parseCharacterAscensionCosts(costs)
        assertEquals(CatalogueCostParseStatus.MISSING_STAGE, result.status)
        assertEquals(listOf("ascend4"), result.missingStageKeys)
        assertTrue(result.steps.isEmpty())
    }

    @Test fun explicitlyEmptyStageIsPreservedAsAValidStage() {
        val costs = ascensionCosts().apply { put("ascend3", org.json.JSONArray()) }
        val result = GenshinDbCostParser.parseWeaponAscensionCosts(costs)
        assertEquals(CatalogueCostParseStatus.COMPLETE, result.status)
        assertTrue(result.steps[2].materials.isEmpty())
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
