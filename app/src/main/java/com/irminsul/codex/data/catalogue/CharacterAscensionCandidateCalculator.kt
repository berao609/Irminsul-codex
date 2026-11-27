package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import java.net.URI

data class CharacterAscensionCandidateCalculationRequest(
    val characterId: String,
    val currentLevel: Int,
    val currentAscensionRank: Int,
    val targetLevel: Int,
    val gameVersion: String
)

data class CharacterAscensionCandidateMaterial(
    val itemId: String,
    val itemName: String,
    val quantity: Long
)

enum class CharacterAscensionCandidateCalculationStatus {
    CANDIDATE_DATA,
    NO_ASCENSION_REQUIRED,
    INVALID_REQUEST,
    COST_DATA_INCOMPLETE,
    INVALID_CANDIDATE_DATA,
    OVERLAPPING_SEGMENTS,
    QUANTITY_OVERFLOW
}

data class CharacterAscensionCandidateCalculationResult(
    val status: CharacterAscensionCandidateCalculationStatus,
    val request: CharacterAscensionCandidateCalculationRequest,
    val materials: List<CharacterAscensionCandidateMaterial> = emptyList(),
    val usedCandidateIds: List<String> = emptyList(),
    val missingFromLevel: Int? = null,
    val issue: String? = null
)

/** Calculates character ascension requirements without promoting unreviewed candidates. */
object CharacterAscensionCandidateCalculator {
    private val levelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

    fun calculate(
        request: CharacterAscensionCandidateCalculationRequest,
        candidates: List<GenshinDbUpgradeCostCandidate>
    ): CharacterAscensionCandidateCalculationResult {
        fun result(
            status: CharacterAscensionCandidateCalculationStatus,
            materials: List<CharacterAscensionCandidateMaterial> = emptyList(),
            used: List<String> = emptyList(),
            missing: Int? = null,
            issue: String? = null
        ) = CharacterAscensionCandidateCalculationResult(status, request, materials, used, missing, issue)

        if (request.characterId.isBlank() || request.gameVersion.isBlank() ||
            request.currentLevel !in 1..90 || request.targetLevel !in request.currentLevel..90 ||
            request.currentAscensionRank !in 0..6 ||
            request.currentLevel > levelCaps[request.currentAscensionRank]
        ) {
            return result(
                CharacterAscensionCandidateCalculationStatus.INVALID_REQUEST,
                issue = "Character identity, levels, or current ascension rank is invalid."
            )
        }

        val derivedTargetRank = levelCaps.indexOfFirst { request.targetLevel <= it }
        if (derivedTargetRank < 0) {
            return result(
                CharacterAscensionCandidateCalculationStatus.INVALID_REQUEST,
                issue = "Target level is outside the supported character progression."
            )
        }
        val targetRank = maxOf(request.currentAscensionRank, derivedTargetRank)
        if (targetRank == request.currentAscensionRank) {
            return result(CharacterAscensionCandidateCalculationStatus.NO_ASCENSION_REQUIRED)
        }

        val matching = candidates.filter {
            it.subjectType == UpgradeCostSubjectType.CHARACTER &&
                it.subjectId == request.characterId &&
                it.track == UpgradeCostTrack.CHARACTER_ASCENSION &&
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
                    CharacterAscensionCandidateCalculationStatus.COST_DATA_INCOMPLETE,
                    used = used,
                    missing = expectedFrom
                )
            }
            if (stage.size > 1) {
                return result(
                    CharacterAscensionCandidateCalculationStatus.OVERLAPPING_SEGMENTS,
                    used = used,
                    missing = expectedFrom,
                    issue = "Multiple candidate records cover the same ascension stage."
                )
            }

            val candidate = stage.single()
            if (candidate.candidateId.isBlank() || candidate.sourceReference.sourceId.isBlank() ||
                !isHttps(candidate.sourceReference.canonicalUrl) || candidate.costs.isEmpty()
            ) {
                return result(
                    CharacterAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
                    used = used,
                    missing = expectedFrom,
                    issue = "Candidate identity, provenance, or stage costs are incomplete."
                )
            }

            for (item in candidate.costs) {
                if (item.itemId.isBlank() || item.itemName.isBlank() || item.quantity <= 0L) {
                    return result(
                        CharacterAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
                        used = used,
                        missing = expectedFrom,
                        issue = "Candidate material ID, name, and quantity must be valid."
                    )
                }
                val existingName = names[item.itemId]
                if (existingName != null && existingName != item.itemName) {
                    return result(
                        CharacterAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA,
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
                        CharacterAscensionCandidateCalculationStatus.QUANTITY_OVERFLOW,
                        used = used,
                        missing = expectedFrom
                    )
                }
            }
            used += candidate.candidateId
        }

        return result(
            CharacterAscensionCandidateCalculationStatus.CANDIDATE_DATA,
            materials = totals.map { (id, quantity) ->
                CharacterAscensionCandidateMaterial(id, names.getValue(id), quantity)
            },
            used = used
        )
    }

    private fun isHttps(value: String): Boolean = runCatching {
        val uri = URI(value)
        uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
