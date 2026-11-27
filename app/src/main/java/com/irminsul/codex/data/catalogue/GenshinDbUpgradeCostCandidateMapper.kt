package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import java.net.URI
import java.time.Instant

/**
 * Identity and provenance supplied by the importer for one Genshin-DB entity.
 * This context does not imply source permission or human verification.
 */
data class GenshinDbCostCandidateContext(
    val subjectType: UpgradeCostSubjectType,
    val subjectId: String,
    val track: UpgradeCostTrack,
    val gameVersion: String,
    val sourceReference: KnowledgeSourceReference,
    val retrievedAt: Instant,
    val talentSlot: String? = null
)

/**
 * Untrusted, source-attributed progression segment. Candidate items retain the
 * upstream names for review; they are not material-catalogue entries and cannot
 * be passed directly to the deterministic upgrade calculators.
 */
data class GenshinDbUpgradeCostCandidate(
    val candidateId: String,
    val subjectType: UpgradeCostSubjectType,
    val subjectId: String,
    val track: UpgradeCostTrack,
    val fromLevel: Int,
    val toLevel: Int,
    val talentSlot: String?,
    val gameVersion: String,
    val costs: List<GenshinDbParsedCostItem>,
    val sourceReference: KnowledgeSourceReference,
    val retrievedAt: Instant
)

object GenshinDbUpgradeCostCandidateMapper {

    fun map(
        context: GenshinDbCostCandidateContext,
        segments: List<GenshinDbParsedCostSegment>
    ): List<GenshinDbUpgradeCostCandidate> {
        require(context.subjectId.isNotBlank()) { "A stable Genshin entity ID is required." }
        require(context.gameVersion.isNotBlank()) { "A game version is required." }
        require(context.sourceReference.sourceId.isNotBlank()) { "A source registry ID is required." }
        require(isHttpsUrl(context.sourceReference.canonicalUrl)) {
            "The candidate source reference must be an absolute HTTPS URL."
        }

        val expectedSubject = when (context.track) {
            UpgradeCostTrack.CHARACTER_LEVEL,
            UpgradeCostTrack.CHARACTER_ASCENSION,
            UpgradeCostTrack.TALENT_LEVEL -> UpgradeCostSubjectType.CHARACTER
            UpgradeCostTrack.WEAPON_LEVEL,
            UpgradeCostTrack.WEAPON_ASCENSION -> UpgradeCostSubjectType.WEAPON
        }
        require(context.subjectType == expectedSubject) {
            "The upgrade track does not match the Genshin entity type."
        }
        if (context.track == UpgradeCostTrack.TALENT_LEVEL) {
            require(!context.talentSlot.isNullOrBlank()) {
                "Talent cost candidates require a stable talent slot."
            }
        } else {
            require(context.talentSlot == null) {
                "Talent slots are only valid for talent cost candidates."
            }
        }
        require(segments.isNotEmpty()) { "At least one parsed progression segment is required." }

        val ranges = segments.map { it.fromLevel to it.toLevel }
        require(ranges.distinct().size == ranges.size) { "Duplicate progression segments are not allowed." }
        segments.forEach { segment ->
            require(segment.fromLevel >= 1 && segment.toLevel > segment.fromLevel) {
                "Progression segments must have a valid increasing level range."
            }
            segment.costs.forEach { item ->
                require(item.itemId.isNotBlank() && item.itemName.isNotBlank() && item.quantity > 0L) {
                    "Candidate material IDs, names, and quantities must be valid."
                }
            }
        }

        return segments.map { segment ->
            val candidateId = listOf(
                context.sourceReference.sourceId,
                context.gameVersion,
                context.subjectType.name.lowercase(),
                context.subjectId,
                context.track.name.lowercase(),
                context.talentSlot ?: "none",
                "${segment.fromLevel}-${segment.toLevel}"
            ).joinToString(":")

            GenshinDbUpgradeCostCandidate(
                candidateId = candidateId,
                subjectType = context.subjectType,
                subjectId = context.subjectId,
                track = context.track,
                fromLevel = segment.fromLevel,
                toLevel = segment.toLevel,
                talentSlot = context.talentSlot,
                gameVersion = context.gameVersion,
                costs = segment.costs.toList(),
                sourceReference = context.sourceReference,
                retrievedAt = context.retrievedAt
            )
        }
    }

    private fun isHttpsUrl(value: String): Boolean = runCatching {
        val uri = URI(value)
        uri.scheme.equals("https", ignoreCase = true) && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
