package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import java.net.URI

data class WeaponAscensionCandidateCalculationRequest(
    val weaponId: String,
    val rarity: Int,
    val currentLevel: Int,
    val currentAscensionRank: Int,
    val targetLevel: Int,
    val gameVersion: String
)

data class WeaponAscensionCandidateMaterial(
    val itemId: String,
    val itemName: String,
    val quantity: Long
)

enum class WeaponAscensionCandidateCalculationStatus {
    CANDIDATE_DATA,
    NO_ASCENSION_REQUIRED,
    INVALID_REQUEST,
    COST_DATA_INCOMPLETE,
    INVALID_CANDIDATE_DATA,
    OVERLAPPING_SEGMENTS,
    QUANTITY_OVERFLOW
}

data class WeaponAscensionCandidateCalculationResult(
    val status: WeaponAscensionCandidateCalculationStatus,
    val request: WeaponAscensionCandidateCalculationRequest,
    val materials: List<WeaponAscensionCandidateMaterial> = emptyList(),
    val usedCandidateIds: List<String> = emptyList(),
    val missingFromLevel: Int? = null,
    val issue: String? = null
)

/**
 * Calculates weapon-specific ascension requirements from the existing
 * Genshin-DB importer output. Results remain candidate data and are not
 * promoted into the trusted upgrade-cost registry.
 */
object WeaponAscensionCandidateCalculator {
    private val levelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

    fun calculate(
        request: WeaponAscensionCandidateCalculationRequest,
        candidates: List<GenshinDbUpgradeCostCandidate>
    ): WeaponAscensionCandidateCalculationResult {
        fun result(
            status: WeaponAscensionCandidateCalculationStatus,
            materials: List<WeaponAscensionCandidateMaterial> = emptyList(),
            used: List<String> = emptyList(),
            missing: Int? = null,
            issue: String? = null
        ) = WeaponAscensionCandidateCalculationResult(
            status, request, materials, used, missing, issue
        )

        if (request.weaponId.isBlank() || request.gameVersion.isBlank() ||
            request.rarity !in 4..5 ||
            request.currentLevel !in 1..90 ||
            request.targetLevel !in request.currentLevel..90 ||
            request.currentAscensionRank !in 0..6 ||
            request.currentLevel > levelCaps[request.currentAscensionRank]
        ) {
            return result(
                WeaponAscensionCandidateCalculationStatus.INVALID_REQUEST,
                issue = "Weapon identity, rarity, levels, or current ascension rank is invalid."
            )
        }

        val derivedTargetRank = levelCaps.indexOfFirst { request.targetLevel <= it }
        if (derivedTargetRank < 0) {
            return result(
                WeaponAscensionCandidateCalculationStatus.INVALID_REQUEST,
                issue = "Target level is outside the supported weapon progression."
            )
        }
        // A weapon may be ascended but still remain at the previous level
        // boundary, such as 20/40 immediately after its first ascension.
        val targetRank = maxOf(request.currentAscensionRank, derivedTargetRank)
        if (targetRank == request.currentAscensionRank) {
            return result(WeaponAscensionCandidateCalculationStatus.NO_ASCENSION_REQUIRED)
        }

        val matching = candidates.filter {
            it.subjectType == UpgradeCostSubjectType.WEAPON &&
                it.subjectId == request.weaponId &&
                it.track == UpgradeCostTrack.WEAPON_ASCENSION &&
                it.talentSlot == null &&
                it.gameVersion == request.gameVersion
        }

        val totals = linkedMapOf<String, Long>()
        val names = linkedMapOf<String, String>()
        val used = mutableListOf<String>()

        for (rank in (request.currentAscensionRank + 1)..targetRank) {
            val expectedFrom = levelCaps[rank - 1]
            val expectedTo = levelCaps[rank]
            val stage = matching.filter { it.fromLevel == expectedFrom && it.toLevel == expectedTo }
            if (stage.isEmpty()) {
                return result(
                    WeaponAscensionCandidateCalculationStatus.COST_DATA_INCOMPLETE,
                    used = used,
                    missing = expectedFrom
                )
            }
            if (stage.size > 1) {
                return result(
                    WeaponAscensionCandidateCalculationStatus.OVERLAPPING_SEGMENTS,
                    used = used,
                    missing = expectedFrom,
                    issue = "Multiple candidate records cover the same ascension stage."
                )
            }

            val candidate = stage.single()
            if (candidate.candidateId.isBlank() ||
                candidate.sourceReference.sourceId.isBlank() ||
                !isHttps(candidate.sourceReference.canonicalUrl) ||
                candidate.costs.isEmpty()
            ) {
                return result(
                    WeaponAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
                    used = used,
                    missing = expectedFrom,
                    issue = "Candidate identity, provenance, or stage costs are incomplete."
                )
            }

            for (item in candidate.costs) {
                if (item.itemId.isBlank() || item.itemName.isBlank() || item.quantity <= 0L) {
                    return result(
                        WeaponAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
                        used = used,
                        missing = expectedFrom,
                        issue = "Candidate material ID, name, and quantity must be valid."
                    )
                }
                val existingName = names[item.itemId]
                if (existingName != null && existingName != item.itemName) {
                    return result(
                        WeaponAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
                        used = used,
                        missing = expectedFrom,
                        issue = "One material ID has conflicting display names."
                    )
                }
                names[item.itemId] = item.itemName
                try {
                    totals[item.itemId] = Math.addExact(totals[item.itemId] ?: 0L, item.quantity)
                } catch (_: ArithmeticException) {
                    return result(
                        WeaponAscensionCandidateCalculationStatus.QUANTITY_OVERFLOW,
                        used = used,
                        missing = expectedFrom
                    )
                }
            }
            used += candidate.candidateId
        }

        return result(
            WeaponAscensionCandidateCalculationStatus.CANDIDATE_DATA,
            materials = totals.map { (id, quantity) ->
                WeaponAscensionCandidateMaterial(id, names.getValue(id), quantity)
            },
            used = used
        )
    }

    private fun isHttps(value: String): Boolean = runCatching {
        val uri = URI(value)
        uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
