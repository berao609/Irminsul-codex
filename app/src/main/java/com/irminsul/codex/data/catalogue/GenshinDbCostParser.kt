package com.irminsul.codex.data.catalogue

import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

enum class CatalogueCostParseStatus {
    COMPLETE,
    MISSING_STAGE,
    INVALID_DATA
}

/**
 * Parsed source data is an untrusted candidate. It is not an UpgradeCostRecord
 * and cannot enter UpgradeCostCalculator until it receives source attribution,
 * permission approval, and an explicit human verification review.
 */
data class GenshinDbParsedCostItem(
    val itemId: String,
    val itemName: String,
    val quantity: Long
)

data class GenshinDbParsedCostSegment(
    val fromLevel: Int,
    val toLevel: Int,
    val costs: List<GenshinDbParsedCostItem>
)

data class CatalogueCostParseResult(
    val status: CatalogueCostParseStatus,
    val segments: List<GenshinDbParsedCostSegment> = emptyList(),
    val missingStageKeys: List<String> = emptyList()
)

/**
 * Converts genshin-db's documented costs JSON into untrusted progression
 * segments. This parser neither fetches data nor grants trust to source facts.
 */
object GenshinDbCostParser {

    private val ascensionStages = linkedMapOf(
        "ascend1" to (1 to 20),
        "ascend2" to (20 to 40),
        "ascend3" to (40 to 50),
        "ascend4" to (50 to 60),
        "ascend5" to (60 to 70),
        "ascend6" to (70 to 80)
    )

    private val talentStages = (2..10).associate { "lvl$it" to ((it - 1) to it) }

    fun parseCharacterAscensionCosts(costs: JSONObject): CatalogueCostParseResult =
        parseStages(costs, ascensionStages)

    fun parseWeaponAscensionCosts(costs: JSONObject): CatalogueCostParseResult =
        parseStages(costs, ascensionStages)

    fun parseTalentCosts(costs: JSONObject): CatalogueCostParseResult =
        parseStages(costs, talentStages)

    private fun parseStages(
        costs: JSONObject,
        stageRanges: Map<String, Pair<Int, Int>>
    ): CatalogueCostParseResult {
        val missing = stageRanges.keys.filterNot(costs::has)
        if (missing.isNotEmpty()) {
            return CatalogueCostParseResult(
                status = CatalogueCostParseStatus.MISSING_STAGE,
                missingStageKeys = missing
            )
        }

        val segments = mutableListOf<GenshinDbParsedCostSegment>()
        for ((stageKey, range) in stageRanges) {
            val rawStage = costs.opt(stageKey)
            if (rawStage !is JSONArray) {
                return CatalogueCostParseResult(CatalogueCostParseStatus.INVALID_DATA)
            }
            val materials = mutableListOf<GenshinDbParsedCostItem>()
            for (index in 0 until rawStage.length()) {
                val rawMaterial = rawStage.opt(index)
                if (rawMaterial !is JSONObject) {
                    return CatalogueCostParseResult(CatalogueCostParseStatus.INVALID_DATA)
                }
                val id = exactLong(rawMaterial.opt("id")) ?: return invalid()
                val quantity = exactLong(rawMaterial.opt("count")) ?: return invalid()
                val name = rawMaterial.opt("name") as? String ?: return invalid()
                if (id !in 1..Int.MAX_VALUE.toLong() || quantity <= 0L || name.isBlank()) {
                    return invalid()
                }
                materials += GenshinDbParsedCostItem(itemId = id.toString(), itemName = name, quantity = quantity)
            }
            segments += GenshinDbParsedCostSegment(
                fromLevel = range.first,
                toLevel = range.second,
                costs = materials
            )
        }
        return CatalogueCostParseResult(
            status = CatalogueCostParseStatus.COMPLETE,
            segments = segments
        )
    }

    private fun exactLong(value: Any?): Long? {
        if (value !is Number) return null
        return runCatching { BigDecimal(value.toString()).longValueExact() }.getOrNull()
    }

    private fun invalid() = CatalogueCostParseResult(CatalogueCostParseStatus.INVALID_DATA)
}
