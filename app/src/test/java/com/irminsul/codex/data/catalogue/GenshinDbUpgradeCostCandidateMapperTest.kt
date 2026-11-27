package com.irminsul.codex.data.catalogue

import com.irminsul.codex.domain.model.KnowledgeSourceReference
import com.irminsul.codex.domain.model.UpgradeCostSubjectType
import com.irminsul.codex.domain.model.UpgradeCostTrack
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class GenshinDbUpgradeCostCandidateMapperTest {
    private val retrievedAt = Instant.parse("2026-11-27T00:00:00Z")
    private val source = KnowledgeSourceReference(
        sourceId = "genshin-db-7.2",
        canonicalUrl = "https://github.com/theBowja/genshin-db/blob/main/src/data/English/characters/albedo.json",
        publisherLabel = "Genshin-DB"
    )

    private fun context(
        subjectType: UpgradeCostSubjectType = UpgradeCostSubjectType.CHARACTER,
        track: UpgradeCostTrack = UpgradeCostTrack.CHARACTER_ASCENSION,
        talentSlot: String? = null
    ) = GenshinDbCostCandidateContext(
        subjectType = subjectType,
        subjectId = "10000038",
        track = track,
        gameVersion = "7.2",
        sourceReference = source,
        retrievedAt = retrievedAt,
        talentSlot = talentSlot
    )

    private fun segments() = listOf(
        GenshinDbParsedCostSegment(
            fromLevel = 20,
            toLevel = 40,
            costs = listOf(GenshinDbParsedCostItem("202", "Mora", 20_000))
        ),
        GenshinDbParsedCostSegment(
            fromLevel = 40,
            toLevel = 50,
            costs = listOf(GenshinDbParsedCostItem("202", "Mora", 40_000))
        )
    )

    @Test fun mapsStableEntityAndMaterialIdsWithoutPromotingCandidateTrust() {
        val result = GenshinDbUpgradeCostCandidateMapper.map(context(), segments())

        assertEquals(2, result.size)
        assertEquals("10000038", result.first().subjectId)
        assertEquals(UpgradeCostSubjectType.CHARACTER, result.first().subjectType)
        assertEquals(UpgradeCostTrack.CHARACTER_ASCENSION, result.first().track)
        assertEquals(20, result.first().fromLevel)
        assertEquals(40, result.first().toLevel)
        assertEquals("202", result.first().costs.single().itemId)
        assertEquals("Mora", result.first().costs.single().itemName)
        assertEquals(20_000L, result.first().costs.single().quantity)
        assertEquals(source, result.first().sourceReference)
        assertEquals(retrievedAt, result.first().retrievedAt)
        assertTrue(result.first().candidateId.contains("10000038"))
    }

    @Test fun candidateIdsAreStableForTheSameEntityTrackVersionAndRange() {
        val first = GenshinDbUpgradeCostCandidateMapper.map(context(), segments())
        val second = GenshinDbUpgradeCostCandidateMapper.map(context(), segments())
        assertEquals(first.map { it.candidateId }, second.map { it.candidateId })
        assertNotEquals(first[0].candidateId, first[1].candidateId)
    }

    @Test fun talentCandidatesRequireAndRetainTheirSlot() {
        val talentContext = context(track = UpgradeCostTrack.TALENT_LEVEL, talentSlot = "normal")
        val result = GenshinDbUpgradeCostCandidateMapper.map(talentContext, segments())
        assertEquals("normal", result.first().talentSlot)
        assertTrue(result.first().candidateId.contains(":normal:"))
    }

    @Test fun rejectsMismatchedEntityTypesAndMissingTalentSlots() {
        try {
            GenshinDbUpgradeCostCandidateMapper.map(
                context(subjectType = UpgradeCostSubjectType.WEAPON),
                segments()
            )
            fail("Expected subject and track mismatch to be rejected")
        } catch (_: IllegalArgumentException) { }

        try {
            GenshinDbUpgradeCostCandidateMapper.map(
                context(track = UpgradeCostTrack.TALENT_LEVEL),
                segments()
            )
            fail("Expected missing talent slot to be rejected")
        } catch (_: IllegalArgumentException) { }
    }

    @Test fun preservesExplicitlyEmptySourceStagesForReview() {
        val emptyStage = listOf(
            GenshinDbParsedCostSegment(50, 60, emptyList())
        )
        val result = GenshinDbUpgradeCostCandidateMapper.map(context(), emptyStage)
        assertTrue(result.single().costs.isEmpty())
        assertEquals(50, result.single().fromLevel)
        assertEquals(60, result.single().toLevel)
    }

    @Test fun rejectsInvalidSourceUrlsAndInvalidMaterialQuantities() {
        val invalidSource = context().copy(
            sourceReference = source.copy(canonicalUrl = "http://example.com/data")
        )
        try {
            GenshinDbUpgradeCostCandidateMapper.map(invalidSource, segments())
            fail("Expected non-HTTPS source URL to be rejected")
        } catch (_: IllegalArgumentException) { }

        val invalidItems = listOf(
            GenshinDbParsedCostSegment(
                20, 40,
                listOf(GenshinDbParsedCostItem("202", "Mora", 0))
            )
        )
        try {
            GenshinDbUpgradeCostCandidateMapper.map(context(), invalidItems)
            fail("Expected invalid candidate quantity to be rejected")
        } catch (_: IllegalArgumentException) { }
    }
}
