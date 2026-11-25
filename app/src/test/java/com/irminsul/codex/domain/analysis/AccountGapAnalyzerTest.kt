package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class AccountGapAnalyzerTest {
    private fun character(
        id: Int,
        weapon: DataValue<WeaponSnapshot> = DataValue.Available(WeaponSnapshot(100 + id, "Weapon", null, null, 5, 90, 6, null, null, 1, null, null), null),
        artifacts: DataValue<List<ArtifactSnapshot>> = DataValue.Available(emptyList(), null),
        integrity: EquipmentIntegritySnapshot = EquipmentIntegritySnapshot(unoccupiedArtifactSlots = emptyList())
    ) = CharacterSnapshot(
        id = id,
        name = "Character$id",
        level = 90,
        rarity = 5,
        element = "Pyro",
        friendshipLevel = null,
        activeConstellationCount = 0,
        weapon = weapon,
        artifacts = artifacts,
        equipmentIntegrity = integrity
    )

    @Test fun cleanRosterAndEquipmentProduceNoGapFindings() {
        val result = AccountGapAnalyzer.analyze(
            DataValue.Available(listOf(character(1)), null),
            CharacterCollectionIntegrity(sourceEntryCount = 1, mappedEntryCount = 1)
        )
        assertEquals(AccountGapAnalysisAvailability.AVAILABLE, result.availability)
        assertEquals(1, result.assessedCharacterCount)
        assertTrue(result.findings.isEmpty())
    }

    @Test fun invalidAndDuplicateRosterEntriesAreConfirmedIntegrityGaps() {
        val result = AccountGapAnalyzer.analyze(
            DataValue.Available(listOf(character(7)), null),
            CharacterCollectionIntegrity(
                sourceEntryCount = 4,
                mappedEntryCount = 1,
                invalidCharacterEntryIndexes = listOf(3, 1),
                duplicateCharacterIds = listOf(7)
            )
        )
        assertEquals(3, result.confirmedGapCount)
        assertEquals(
            listOf(
                AccountGapType.INVALID_ROSTER_ENTRY,
                AccountGapType.INVALID_ROSTER_ENTRY,
                AccountGapType.DUPLICATE_CHARACTER_ID
            ),
            result.findings.map { it.type }
        )
        assertEquals(listOf("roster-invalid-entry-1", "roster-invalid-entry-3", "roster-duplicate-7"), result.findings.map { it.findingId })
    }

    @Test fun missingWeaponAndArtifactPayloadsAreDataReviewNotConfirmedGaps() {
        val result = AccountGapAnalyzer.analyze(
            DataValue.Available(listOf(character(2, weapon = DataValue.Missing, artifacts = DataValue.Missing)), null),
            CharacterCollectionIntegrity()
        )
        assertEquals(0, result.confirmedGapCount)
        assertEquals(2, result.dataReviewCount)
        assertTrue(result.findings.all { it.confidence == AccountGapConfidence.DATA_REVIEW })
    }

    @Test fun unavailableAndFailedEquipmentStatesRemainExplicit() {
        val result = AccountGapAnalyzer.analyze(
            DataValue.Available(listOf(
                character(1, weapon = DataValue.Unavailable),
                character(2, artifacts = DataValue.Failed("bad artifact shape"))
            ), null),
            CharacterCollectionIntegrity()
        )
        assertTrue(result.findings.any { it.evidence.any { e -> e.contains("unavailable") } })
        assertTrue(result.findings.any { it.evidence.any { e -> e.contains("bad artifact shape") } })
    }

    @Test fun unoccupiedArtifactSlotsAreReportedAsEquipmentObservations() {
        val result = AccountGapAnalyzer.analyze(
            DataValue.Available(
                listOf(character(4, integrity = EquipmentIntegritySnapshot(unoccupiedArtifactSlots = listOf(2, 5)))),
                null
            ),
            CharacterCollectionIntegrity()
        )
        val finding = result.findings.single()
        assertEquals(AccountGapType.ARTIFACT_SLOT_GAP, finding.type)
        assertEquals(AccountGapConfidence.CONFIRMED, finding.confidence)
        assertTrue(finding.evidence.single().contains("2, 5"))
        assertTrue(finding.explanation.contains("not a recommendation"))
    }

    @Test fun malformedArtifactIntegrityIsReportedWithSpecificEvidence() {
        val result = AccountGapAnalyzer.analyze(
            DataValue.Available(
                listOf(character(8, integrity = EquipmentIntegritySnapshot(
                    unoccupiedArtifactSlots = emptyList(),
                    duplicateArtifactIds = listOf(12),
                    invalidArtifactSlots = listOf(-1)
                ))),
                null
            ),
            CharacterCollectionIntegrity()
        )
        val finding = result.findings.single()
        assertEquals(AccountGapType.EQUIPMENT_INTEGRITY_ISSUE, finding.type)
        assertTrue(finding.evidence.any { it.contains("12") })
        assertTrue(finding.evidence.any { it.contains("-1") })
    }

    @Test fun staleCharacterCollectionMarksAllDerivedFindingsStale() {
        val result = AccountGapAnalyzer.analyze(
            DataValue.Stale(listOf(character(3, weapon = DataValue.Missing)), Instant.parse("2026-11-20T00:00:00Z")),
            CharacterCollectionIntegrity()
        )
        assertEquals(AccountGapAnalysisAvailability.STALE, result.availability)
        assertTrue(result.findings.all { it.basedOnStaleData })
    }

    @Test fun missingUnavailableAndFailedRosterStatesArePreserved() {
        val cases = listOf(
            DataValue.Missing to AccountGapAnalysisAvailability.MISSING,
            DataValue.Unavailable to AccountGapAnalysisAvailability.UNAVAILABLE,
            DataValue.Failed("roster request failed") to AccountGapAnalysisAvailability.FAILED
        )
        cases.forEach { (input, expected) ->
            val result = AccountGapAnalyzer.analyze(input, CharacterCollectionIntegrity())
            assertEquals(expected, result.availability)
            assertTrue(result.findings.isEmpty())
        }
        val failed = AccountGapAnalyzer.analyze(
            DataValue.Failed("roster request failed"), CharacterCollectionIntegrity()
        )
        assertEquals("roster request failed", failed.failureReason)
    }
}
