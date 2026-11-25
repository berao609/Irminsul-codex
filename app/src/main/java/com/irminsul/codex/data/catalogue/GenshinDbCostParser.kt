package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.UpgradeCostStep
import com.irminsul.codex.domain.analysis.UpgradeMaterialCost
import org.json.JSONArray
import org.json.JSONObject

enum class CatalogueCostParseStatus {
    COMPLETE,
    MISSING_STAGE,
    INVALID_DATA
}

data class CatalogueCostParseResult(
    val status: CatalogueCostParseStatus,
    val steps: List<UpgradeCostStep> = emptyList(),
    val missingStageKeys: List<String> = emptyList()
)

/**
 * Converts the documented genshin-db `costs` object into normalized domain
 * records. This parser does not fetch data or assume omitted stages are free.
 */
object GenshinDbCostParser {

    private val characterAndWeaponAscensionStages = linkedMapOf(
        "ascend1" to 20,
        "ascend2" to 40,
        "ascend3" to 50,
        "ascend4" to 60,
        "ascend5" to 70,
        "ascend6" to 80
    )

    private val talentStages = (2..10).associate { "lvl$it" to it }

    fun parseCharacterAscensionCosts(costs: JSONObject): CatalogueCostParseResult =
        parseStages(costs, characterAndWeaponAscensionStages)

    fun parseWeaponAscensionCosts(costs: JSONObject): CatalogueCostParseResult =
        parseStages(costs, characterAndWeaponAscensionStages)

    fun parseTalentCosts(costs: JSONObject): CatalogueCostParseResult =
        parseStages(costs, talentStages)

    private fun parseStages(
        costs: JSONObject,
        stageThresholds: Map<String, Int>
    ): CatalogueCostParseResult {
        val missing = stageThresholds.keys.filterNot(costs::has)
        if (missing.isNotEmpty()) {
            return CatalogueCostParseResult(
                status = CatalogueCostParseStatus.MISSING_STAGE,
                missingStageKeys = missing
            )
        }

        val steps = mutableListOf<UpgradeCostStep>()
        for ((stageKey, targetLevel) in stageThresholds) {
            val rawStage = costs.opt(stageKey)
            if (rawStage !is JSONArray) {
                return CatalogueCostParseResult(CatalogueCostParseStatus.INVALID_DATA)
            }
            val materials = mutableListOf<UpgradeMaterialCost>()
            for (index in 0 until rawStage.length()) {
                val rawMaterial = rawStage.opt(index)
                if (rawMaterial !is JSONObject) {
                    return CatalogueCostParseResult(CatalogueCostParseStatus.INVALID_DATA)
                }
                val idValue = rawMaterial.opt("id")
                val countValue = rawMaterial.opt("count")
                val nameValue = rawMaterial.opt("name")
                if (idValue !is Number || countValue !is Number || nameValue !is String) {
                    return CatalogueCostParseResult(CatalogueCostParseStatus.INVALID_DATA)
                }
                val id = idValue.toInt()
                val quantity = countValue.toLong()
                if (id <= 0 || quantity <= 0L || nameValue.isBlank()) {
                    return CatalogueCostParseResult(CatalogueCostParseStatus.INVALID_DATA)
                }
                materials += UpgradeMaterialCost(
                    materialId = id,
                    materialName = nameValue,
                    quantity = quantity
                )
            }
            steps += UpgradeCostStep(targetLevel = targetLevel, materials = materials)
        }
        return CatalogueCostParseResult(
            status = CatalogueCostParseStatus.COMPLETE,
            steps = steps
        )
    }
}
