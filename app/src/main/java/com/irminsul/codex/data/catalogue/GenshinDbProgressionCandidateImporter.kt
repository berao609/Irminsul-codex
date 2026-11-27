package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.json.JSONObject
import java.math.BigDecimal
import java.time.Instant

enum class GenshinDbProgressionImportStatus { COMPLETE, PARTIAL, INVALID_DATA }

data class GenshinDbProgressionImportResult(
    val status: GenshinDbProgressionImportStatus,
    val subjectId: String?,
    val subjectName: String?,
    val candidates: List<GenshinDbUpgradeCostCandidate>,
    val missingComponents: List<String>,
    val invalidComponents: List<String>
)

/**
 * Imports ascension and talent-cost candidates from the documented Genshin-DB
 * entity JSON shapes. Character/weapon level EXP is intentionally reported as
 * missing because those costs are not present in these entity cost objects.
 *
 * All output remains untrusted candidate data. This importer does not approve
 * source permissions, verify facts, or create calculation-ready cost records.
 */
object GenshinDbProgressionCandidateImporter {

    fun importCharacter(
        character: JSONObject,
        talents: JSONObject?,
        gameVersion: String,
        sourceId: String,
        characterSourceUrl: String,
        talentSourceUrl: String,
        retrievedAt: Instant
    ): GenshinDbProgressionImportResult {
        val subjectId = exactEntityId(character.opt("id"))
        val candidates = mutableListOf<GenshinDbUpgradeCostCandidate>()
        val missing = mutableListOf("character_level_experience")
        val invalid = mutableListOf<String>()

        if (subjectId == null) {
            invalid += "character_id"
        } else {
            val ascensionCosts = character.optJSONObject("costs")
            if (ascensionCosts == null) {
                missing += "character_ascension_costs"
            } else {
                collect(
                    parsed = GenshinDbCostParser.parseCharacterAscensionCosts(ascensionCosts),
                    component = "character_ascension_costs",
                    context = GenshinDbCostCandidateContext(
                        subjectType = UpgradeCostSubjectType.CHARACTER,
                        subjectId = subjectId,
                        track = UpgradeCostTrack.CHARACTER_ASCENSION,
                        gameVersion = gameVersion,
                        sourceReference = KnowledgeSourceReference(sourceId, characterSourceUrl, "Genshin-DB"),
                        retrievedAt = retrievedAt
                    ),
                    candidates = candidates,
                    missing = missing,
                    invalid = invalid
                )
            }

            val talentCosts = talents?.optJSONObject("costs")
            if (talentCosts == null) {
                missing += "talent_costs"
            } else {
                val parsed = GenshinDbCostParser.parseTalentCosts(talentCosts)
                listOf("normal", "skill", "burst").forEach { slot ->
                    collect(
                        parsed = parsed,
                        component = "talent_costs_$slot",
                        context = GenshinDbCostCandidateContext(
                            subjectType = UpgradeCostSubjectType.CHARACTER,
                            subjectId = subjectId,
                            track = UpgradeCostTrack.TALENT_LEVEL,
                            gameVersion = gameVersion,
                            sourceReference = KnowledgeSourceReference(sourceId, talentSourceUrl, "Genshin-DB"),
                            retrievedAt = retrievedAt,
                            talentSlot = slot
                        ),
                        candidates = candidates,
                        missing = missing,
                        invalid = invalid
                    )
                }
            }
        }

        return result(subjectId, character.optString("name").takeIf { it.isNotBlank() }, candidates, missing, invalid)
    }

    fun importWeapon(
        weapon: JSONObject,
        gameVersion: String,
        sourceId: String,
        weaponSourceUrl: String,
        retrievedAt: Instant
    ): GenshinDbProgressionImportResult {
        val subjectId = exactEntityId(weapon.opt("id"))
        val candidates = mutableListOf<GenshinDbUpgradeCostCandidate>()
        val missing = mutableListOf("weapon_level_experience")
        val invalid = mutableListOf<String>()

        if (subjectId == null) {
            invalid += "weapon_id"
        } else {
            val ascensionCosts = weapon.optJSONObject("costs")
            if (ascensionCosts == null) {
                missing += "weapon_ascension_costs"
            } else {
                collect(
                    parsed = GenshinDbCostParser.parseWeaponAscensionCosts(ascensionCosts),
                    component = "weapon_ascension_costs",
                    context = GenshinDbCostCandidateContext(
                        subjectType = UpgradeCostSubjectType.WEAPON,
                        subjectId = subjectId,
                        track = UpgradeCostTrack.WEAPON_ASCENSION,
                        gameVersion = gameVersion,
                        sourceReference = KnowledgeSourceReference(sourceId, weaponSourceUrl, "Genshin-DB"),
                        retrievedAt = retrievedAt
                    ),
                    candidates = candidates,
                    missing = missing,
                    invalid = invalid
                )
            }
        }

        return result(subjectId, weapon.optString("name").takeIf { it.isNotBlank() }, candidates, missing, invalid)
    }

    private fun collect(
        parsed: CatalogueCostParseResult,
        component: String,
        context: GenshinDbCostCandidateContext,
        candidates: MutableList<GenshinDbUpgradeCostCandidate>,
        missing: MutableList<String>,
        invalid: MutableList<String>
    ) {
        when (parsed.status) {
            CatalogueCostParseStatus.COMPLETE -> {
                runCatching {
                    GenshinDbUpgradeCostCandidateMapper.map(context, parsed.segments)
                }.onSuccess(candidates::addAll)
                    .onFailure { invalid += component }
            }
            CatalogueCostParseStatus.MISSING_STAGE -> missing += parsed.missingStageKeys.map { "$component:$it" }
            CatalogueCostParseStatus.INVALID_DATA -> invalid += component
        }
    }

    private fun result(
        subjectId: String?,
        subjectName: String?,
        candidates: List<GenshinDbUpgradeCostCandidate>,
        missing: List<String>,
        invalid: List<String>
    ) = GenshinDbProgressionImportResult(
        status = when {
            invalid.isNotEmpty() -> GenshinDbProgressionImportStatus.INVALID_DATA
            missing.isNotEmpty() -> GenshinDbProgressionImportStatus.PARTIAL
            else -> GenshinDbProgressionImportStatus.COMPLETE
        },
        subjectId = subjectId,
        subjectName = subjectName,
        candidates = candidates,
        missingComponents = missing.distinct(),
        invalidComponents = invalid.distinct()
    )

    private fun exactEntityId(value: Any?): String? {
        if (value !is Number) return null
        val id = runCatching { BigDecimal(value.toString()).longValueExact() }.getOrNull()
            ?: return null
        return id.takeIf { it > 0L }?.toString()
    }
}
