package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.analysis.WeaponAscensionCostCandidate
import com.irminsul.codex.domain.analysis.WeaponAscensionPhaseCost
import com.irminsul.codex.domain.analysis.WeaponExperienceMaterialRequirement
import com.irminsul.codex.domain.model.KnowledgePermissionState
import com.irminsul.codex.domain.model.KnowledgeReviewState
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack

data class GenshinDbWeaponAscensionCandidateMappingResult(
    val candidate: WeaponAscensionCostCandidate?,
    val issues: List<String>
)

/**
 * Adapts the existing Genshin-DB importer output to the domain calculator's
 * weapon-specific candidate model. This mapping preserves source labels and
 * material names but does not approve source reuse or verify game facts.
 */
object GenshinDbWeaponAscensionCandidateMapper {
    private val rankByRange = mapOf(
        (20 to 40) to 1,
        (40 to 50) to 2,
        (50 to 60) to 3,
        (60 to 70) to 4,
        (70 to 80) to 5,
        (80 to 90) to 6
    )

    fun map(
        weaponId: Int,
        rarity: Int,
        gameVersion: String,
        candidates: List<GenshinDbUpgradeCostCandidate>
    ): GenshinDbWeaponAscensionCandidateMappingResult {
        fun invalid(issue: String) = GenshinDbWeaponAscensionCandidateMappingResult(null, listOf(issue))

        if (weaponId <= 0 || rarity !in 4..5 || gameVersion.isBlank()) {
            return invalid("Weapon identity, rarity, or game version is invalid.")
        }
        if (candidates.isEmpty()) {
            return invalid("No weapon ascension candidate segments were supplied.")
        }
        if (candidates.any {
                it.subjectType != UpgradeCostSubjectType.WEAPON ||
                    it.subjectId != weaponId.toString() ||
                    it.track != UpgradeCostTrack.WEAPON_ASCENSION ||
                    it.gameVersion != gameVersion ||
                    it.talentSlot != null
            }
        ) {
            return invalid("Candidate segments do not all match the requested weapon and ascension track.")
        }

        val source = candidates.first().sourceReference
        if (candidates.any {
                it.sourceReference.sourceId != source.sourceId ||
                    it.sourceReference.canonicalUrl != source.canonicalUrl ||
                    it.sourceReference.publisherLabel != source.publisherLabel
            }
        ) {
            return invalid("Weapon ascension segments contain conflicting source provenance.")
        }

        val phases = candidates.map { candidate ->
            val rank = rankByRange[candidate.fromLevel to candidate.toLevel]
                ?: return invalid("Candidate contains an unsupported weapon ascension level range.")
            WeaponAscensionPhaseCost(
                ascensionLevel = rank,
                costs = candidate.costs.map { item ->
                    WeaponExperienceMaterialRequirement(
                        itemId = item.itemId,
                        quantity = item.quantity,
                        itemName = item.itemName
                    )
                }
            )
        }

        return GenshinDbWeaponAscensionCandidateMappingResult(
            candidate = WeaponAscensionCostCandidate(
                weaponId = weaponId,
                rarity = rarity,
                phaseCosts = phases,
                sourceLabel = source.publisherLabel,
                sourceUrl = source.canonicalUrl,
                sourceLicense = "Underlying game-data license and redistribution permission pending review",
                retrievedOn = candidates.maxOf { it.retrievedAt }.toString(),
                reviewState = KnowledgeReviewState.CANDIDATE,
                permissionState = KnowledgePermissionState.PENDING_REVIEW
            ),
            issues = emptyList()
        )
    }
}
