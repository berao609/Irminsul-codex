package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class OwnedCharacterInvestmentFactMapperTest {
    private val exportedAt = Instant.parse("2026-11-25T10:00:00Z")
    private val retrievedAt = Instant.parse("2026-11-25T09:59:00Z")
    private val staleAt = Instant.parse("2026-11-24T10:00:00Z")

    private fun snapshot(
        characters: DataValue<List<CharacterSnapshot>>,
        integrity: CharacterCollectionIntegrity = CharacterCollectionIntegrity(),
        sections: Map<String, AccountSectionMetadata> = emptyMap()
    ) = AccountSnapshot(
        identity = AccountIdentity("800000001", "os_asia"),
        metadata = SnapshotMetadata(exportedAt, 1),
        overallStatus = SectionStatus.SUCCESS,
        dailyNote = DataValue.Missing,
        characters = characters,
        accountCard = DataValue.Missing,
        characterRosterSource = DataValue.Missing,
        characterDetailSource = DataValue.Missing,
        profileAndExploration = DataValue.Missing,
        spiralAbyssCurrent = DataValue.Missing,
        spiralAbyssPrevious = DataValue.Missing,
        imaginariumTheater = DataValue.Missing,
        sections = sections,
        characterCollectionIntegrity = integrity
    )

    private fun character(
        weapon: DataValue<WeaponSnapshot> = DataValue.Missing,
        artifacts: DataValue<List<ArtifactSnapshot>> = DataValue.Missing,
        level: Int? = 90,
        skills: List<CharacterSkillSnapshot> = emptyList(),
        constellations: List<ConstellationSnapshot> = emptyList()
    ) = CharacterSnapshot(
        id = 10000002,
        name = "Jean",
        level = level,
        rarity = 5,
        element = "Wind",
        friendshipLevel = 10,
        activeConstellationCount = 2,
        weapon = weapon,
        artifacts = artifacts,
        skills = skills,
        constellations = constellations
    )

    @Test fun mapsObservedCharacterAndEquipmentValuesWithoutAddingInterpretations() {
        val weaponStat = StatValueSnapshot(2000, "ATK", "100", "20", "120", null, null)
        val artifactStat = StatValueSnapshot(1, "HP", null, null, null, "4780", null)
        val weapon = WeaponSnapshot(
            id = 11501, name = "Favonius Sword", icon = null, type = 1, rarity = 4,
            level = 90, ascensionLevel = 6, typeName = "Sword", description = null,
            refinementLevel = 3, mainProperty = weaponStat, subProperty = null
        )
        val artifact = ArtifactSnapshot(
            id = 34501, name = "Flower", icon = null, slot = 1, slotName = "Flower",
            rarity = 5, level = 20,
            set = StructuredData.ObjectValue(mapOf("set_id" to StructuredData.StringValue("set-1"))),
            mainProperty = artifactStat, subProperties = listOf(weaponStat)
        )
        val skill = CharacterSkillSnapshot(
            id = 10, type = 1, name = "Normal Attack", level = 8, description = null,
            icon = null, isUnlocked = true, isEnhanced = false, enhancedDescription = null,
            canBeEnhanced = false, rawAffixes = null, rawEnhancementIndexes = null
        )
        val constellation = ConstellationSnapshot(
            id = 1, name = "C1", icon = null, effect = null, isActive = true,
            position = 1, isEnhanced = false, enhancedEffect = null, canBeEnhanced = false
        )
        val source = character(
            weapon = DataValue.Available(weapon, null),
            artifacts = DataValue.Available(listOf(artifact), null),
            skills = listOf(skill),
            constellations = listOf(constellation)
        )
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Available(listOf(source), retrievedAt))
        )

        assertEquals(InvestmentFactState.AVAILABLE, result.roster.state)
        assertEquals(retrievedAt, result.roster.observedAt)
        val mapped = result.roster.characters.single()
        assertEquals(10000002, mapped.characterId)
        assertEquals(90, mapped.level.value)
        assertEquals(InvestmentFactState.AVAILABLE, mapped.level.state)
        assertEquals(11501, mapped.weapon.value?.id)
        assertEquals(90, mapped.weapon.value?.level?.value)
        assertEquals(3, mapped.weapon.value?.refinementLevel?.value)
        assertEquals(weaponStat, mapped.weapon.value?.mainProperty?.value)
        assertEquals(34501, mapped.artifacts.value?.single()?.id)
        assertEquals(20, mapped.artifacts.value?.single()?.level?.value)
        assertEquals(artifactStat, mapped.artifacts.value?.single()?.mainProperty?.value)
        assertEquals(weaponStat, mapped.artifacts.value?.single()?.subProperties?.value?.single())
        assertTrue(mapped.artifacts.value?.single()?.set?.value is StructuredData.ObjectValue)
        assertEquals(8, mapped.skills.value?.single()?.level?.value)
        assertEquals(true, mapped.constellations.value?.single()?.isActive?.value)
        assertEquals("Jean", mapped.name.value)
        assertEquals(InvestmentFactState.AVAILABLE, mapped.name.state)
    }

    @Test fun missingFieldsRemainNotReportedInsteadOfBecomingZeroOrFalse() {
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Available(listOf(character(level = null)), retrievedAt))
        )
        val mapped = result.roster.characters.single()
        assertEquals(InvestmentFactState.NOT_REPORTED, mapped.level.state)
        assertNull(mapped.level.value)
        assertEquals(InvestmentFactState.NOT_REPORTED, mapped.skills.state)
        assertEquals(InvestmentFactState.NOT_REPORTED, mapped.constellations.state)
        assertEquals(InvestmentFactState.MISSING, mapped.weapon.state)
        assertEquals(InvestmentFactState.MISSING, mapped.artifacts.state)
        assertNull(mapped.weapon.value)
    }

    @Test fun emptyEquippedArtifactListIsPreservedAsAvailableAndNotAsMissing() {
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Available(
                listOf(character(artifacts = DataValue.Available(emptyList(), null))),
                retrievedAt
            ))
        )
        val mapped = result.roster.characters.single()
        assertEquals(InvestmentFactState.AVAILABLE, mapped.artifacts.state)
        assertNotNull(mapped.artifacts.value)
        assertTrue(mapped.artifacts.value!!.isEmpty())
        assertEquals(InvestmentFactState.AVAILABLE, mapped.artifactIntegrity.state)
        assertEquals((1..5).toList(), mapped.artifactIntegrity.value?.unoccupiedArtifactSlots)
    }

    @Test fun staleRosterMarksKnownCharacterAndNestedEquipmentFactsStale() {
        val source = character(
            weapon = DataValue.Available(
                WeaponSnapshot(
                    id = 11501, name = "Weapon", icon = null, type = 1, rarity = 4,
                    level = 80, ascensionLevel = 5, typeName = "Sword", description = null,
                    refinementLevel = 2, mainProperty = null, subProperty = null
                ),
                null
            ),
            artifacts = DataValue.Available(emptyList(), null)
        )
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Stale(listOf(source), staleAt))
        )
        val mapped = result.roster.characters.single()
        assertEquals(InvestmentFactState.STALE, result.roster.state)
        assertEquals(staleAt, result.roster.observedAt)
        assertEquals(InvestmentFactState.STALE, mapped.level.state)
        assertEquals(staleAt, mapped.level.observedAt)
        assertEquals(InvestmentFactState.STALE, mapped.weapon.state)
        assertEquals(InvestmentFactState.STALE, mapped.weapon.value?.level?.state)
        assertEquals(InvestmentFactState.STALE, mapped.artifacts.state)
        assertTrue(mapped.artifacts.value!!.isEmpty())
    }

    @Test fun staleNestedEquipmentRetainsItsOwnTimestamp() {
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Available(
                listOf(character(artifacts = DataValue.Stale(emptyList(), staleAt))),
                retrievedAt
            ))
        )
        val mapped = result.roster.characters.single()
        assertEquals(InvestmentFactState.STALE, mapped.artifacts.state)
        assertEquals(staleAt, mapped.artifacts.observedAt)
        assertEquals(InvestmentFactState.STALE, mapped.artifactIntegrity.state)
    }

    @Test fun unavailableNestedEquipmentRemainsUnavailable() {
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Available(
                listOf(character(
                    weapon = DataValue.Unavailable,
                    artifacts = DataValue.Unavailable
                )),
                retrievedAt
            ))
        )
        val mapped = result.roster.characters.single()
        assertEquals(InvestmentFactState.UNAVAILABLE, mapped.weapon.state)
        assertEquals(InvestmentFactState.UNAVAILABLE, mapped.artifacts.state)
        assertNull(mapped.weapon.value)
        assertNull(mapped.artifacts.value)
        assertEquals(InvestmentFactState.UNAVAILABLE, mapped.artifactIntegrity.state)
    }

    @Test fun failedNestedEquipmentRetainsFailureReason() {
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Available(
                listOf(character(weapon = DataValue.Failed("invalid weapon object"))),
                retrievedAt
            ))
        )
        val weapon = result.roster.characters.single().weapon
        assertEquals(InvestmentFactState.FAILED, weapon.state)
        assertEquals("invalid weapon object", weapon.failureReason)
    }

    @Test fun rosterAvailabilityStatesRemainDistinct() {
        assertEquals(
            InvestmentFactState.MISSING,
            OwnedCharacterInvestmentFactMapper.map(snapshot(DataValue.Missing)).roster.state
        )
        val unavailableSection = AccountSectionMetadata(
            status = SectionStatus.SKIPPED,
            source = "character-roster",
            retrievedAt = retrievedAt,
            lastSuccessfulRetrievalAt = staleAt
        )
        val unavailable = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Unavailable, sections = mapOf("characters" to unavailableSection))
        ).roster
        assertEquals(InvestmentFactState.UNAVAILABLE, unavailable.state)
        assertEquals(SectionStatus.SKIPPED, unavailable.sectionStatus)
        assertEquals("character-roster", unavailable.sectionSource)

        val failedSection = AccountSectionMetadata(
            status = SectionStatus.FAILED,
            source = "character-roster",
            retrievedAt = retrievedAt,
            lastSuccessfulRetrievalAt = staleAt
        )
        val failed = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Failed("roster request failed"), sections = mapOf("characters" to failedSection))
        ).roster
        assertEquals(InvestmentFactState.FAILED, failed.state)
        assertEquals("roster request failed", failed.failureReason)
        assertEquals(SectionStatus.FAILED, failed.sectionStatus)
        assertEquals(retrievedAt, failed.sectionRetrievedAt)
        assertEquals(staleAt, failed.lastSuccessfulRetrievedAt)
        assertTrue(failed.characters.isEmpty())
    }

    @Test fun partialSectionStatusAndSourceTimestampsRemainVisible() {
        val sectionTime = Instant.parse("2026-11-25T09:58:00Z")
        val lastSuccess = Instant.parse("2026-11-24T09:58:00Z")
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(
                DataValue.Available(listOf(character()), retrievedAt),
                sections = mapOf("characters" to AccountSectionMetadata(
                    status = SectionStatus.PARTIAL,
                    source = "character-details",
                    retrievedAt = sectionTime,
                    lastSuccessfulRetrievalAt = lastSuccess,
                    message = "Some detail batches were unavailable"
                ))
            )
        )
        assertEquals(SectionStatus.PARTIAL, result.roster.sectionStatus)
        assertEquals("character-details", result.roster.sectionSource)
        assertEquals(sectionTime, result.roster.sectionRetrievedAt)
        assertEquals(lastSuccess, result.roster.lastSuccessfulRetrievedAt)
        assertTrue(result.roster.isPartial)
    }

    @Test fun collectionIntegrityIsPreservedWithoutDiscardingMappedCharacters() {
        val integrity = CharacterCollectionIntegrity(
            sourceEntryCount = 3,
            mappedEntryCount = 2,
            invalidCharacterEntryIndexes = listOf(2),
            duplicateCharacterIds = listOf(10000002)
        )
        val result = OwnedCharacterInvestmentFactMapper.map(
            snapshot(DataValue.Available(listOf(character(), character()), retrievedAt), integrity)
        )
        assertEquals(integrity, result.roster.collectionIntegrity)
        assertTrue(result.roster.isPartial)
        assertEquals(2, result.roster.characters.size)
    }
}
