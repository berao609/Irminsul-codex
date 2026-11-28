package com.irminsul.codex.data.catalogue

import java.net.URI

/**
 * Combines character level/ascension costs with three independent talent tracks.
 * All inputs remain version-pinned candidate data; this does not promote trust.
 */
data class CharacterTalentLevelRange(
    val slot: String,
    val currentLevel: Int,
    val targetLevel: Int
)

object CharacterFullUpgradePlanCandidateCalculator {
    fun calculate(
        levelPlan: CharacterUpgradePlanCandidateResult,
        characterId: String,
        gameVersion: String,
        talentRanges: List<CharacterTalentLevelRange>,
        candidates: List<GenshinDbUpgradeCostCandidate>
    ): CharacterUpgradePlanCandidateResult {
        if (levelPlan.status != CharacterUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA) {
            return levelPlan
        }
        val expectedSlots = setOf("normal", "skill", "burst")
        if (characterId.isBlank() || gameVersion.isBlank() ||
            talentRanges.map { it.slot }.toSet() != expectedSlots ||
            talentRanges.size != expectedSlots.size ||
            talentRanges.any { it.currentLevel !in 1..10 || it.targetLevel !in it.currentLevel..10 }
        ) {
            return levelPlan.copy(
                status = CharacterUpgradePlanCandidateStatus.INVALID,
                requirements = emptyList(),
                issue = "Talent tracks must contain valid independent Normal Attack, Skill, and Burst levels."
            )
        }

        val totals = linkedMapOf<String, Pair<String, Long>>()
        fun add(itemId: String, itemName: String, quantity: Long): Boolean {
            if (itemId.isBlank() || itemName.isBlank() || quantity <= 0L) return false
            val previous = totals[itemId]
            if (previous != null && previous.first != itemName) return false
            val next = try { Math.addExact(previous?.second ?: 0L, quantity) }
            catch (_: ArithmeticException) { return false }
            totals[itemId] = (previous?.first ?: itemName) to next
            return true
        }

        levelPlan.requirements.forEach { if (!add(it.itemId, it.itemName, it.quantity)) {
            return levelPlan.copy(status = CharacterUpgradePlanCandidateStatus.INVALID,
                requirements = emptyList(), issue = "Conflicting material identities or quantity overflow.")
        } }

        for (range in talentRanges) {
            for (toLevel in (range.currentLevel + 1)..range.targetLevel) {
                val matching = candidates.filter {
                    it.subjectType == com.irminsul.codex.domain.model.UpgradeCostSubjectType.CHARACTER &&
                        it.subjectId == characterId &&
                        it.track == com.irminsul.codex.domain.model.UpgradeCostTrack.TALENT_LEVEL &&
                        it.talentSlot == range.slot &&
                        it.gameVersion == gameVersion &&
                        it.fromLevel == toLevel - 1 && it.toLevel == toLevel
                }
                if (matching.isEmpty()) {
                    return levelPlan.copy(
                        status = CharacterUpgradePlanCandidateStatus.INCOMPLETE,
                        requirements = emptyList(),
                        issue = "Talent cost data is missing for ${range.slot} level $toLevel."
                    )
                }
                if (matching.size != 1) {
                    return levelPlan.copy(
                        status = CharacterUpgradePlanCandidateStatus.INVALID,
                        requirements = emptyList(),
                        issue = "Talent cost data overlaps for ${range.slot} level $toLevel."
                    )
                }
                val stageCandidate = matching.single()
                if (stageCandidate.candidateId.isBlank() ||
                    stageCandidate.sourceReference.sourceId.isBlank() ||
                    !isHttps(stageCandidate.sourceReference.canonicalUrl) ||
                    stageCandidate.costs.isEmpty()
                ) {
                    return levelPlan.copy(
                        status = CharacterUpgradePlanCandidateStatus.INVALID,
                        requirements = emptyList(),
                        issue = "Talent cost candidate has missing provenance or an empty material stage."
                    )
                }
                stageCandidate.costs.forEach { item ->
                    if (!add(item.itemId, item.itemName, item.quantity)) {
                        return levelPlan.copy(
                            status = CharacterUpgradePlanCandidateStatus.INVALID,
                            requirements = emptyList(),
                            issue = "Conflicting material identities or quantity overflow."
                        )
                    }
                }
            }
        }

        return levelPlan.copy(
            requirements = totals.map { (id, value) ->
                CharacterUpgradePlanCandidateRequirement(id, value.first, value.second)
            },
            issue = null
        )
    }

    private fun isHttps(value: String): Boolean = runCatching {
        val uri = URI(value)
        uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
