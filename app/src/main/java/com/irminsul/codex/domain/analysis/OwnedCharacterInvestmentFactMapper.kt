package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*
import java.time.Instant

/**
 * Extracts only account-observed character investment facts.
 *
 * This mapper does not interpret combat roles, team synergy, build quality,
 * patch relevance, redundancy, resource cost, affordability, or pull value.
 */
object OwnedCharacterInvestmentFactMapper {

    fun map(snapshot: AccountSnapshot): OwnedCharacterInvestmentFacts {
        val section = snapshot.sections["characters"]
        val roster = when (val source = snapshot.characters) {
            is DataValue.Available -> OwnedCharacterInvestmentRosterFacts(
                state = InvestmentFactState.AVAILABLE,
                observedAt = source.retrievedAt,
                sectionStatus = section?.status,
                sectionSource = section?.source,
                sectionRetrievedAt = section?.retrievedAt,
                lastSuccessfulRetrievedAt = section?.lastSuccessfulRetrievalAt,
                characters = source.value.map { mapCharacter(it, stale = false, source.retrievedAt) },
                collectionIntegrity = snapshot.characterCollectionIntegrity
            )
            is DataValue.Stale -> OwnedCharacterInvestmentRosterFacts(
                state = InvestmentFactState.STALE,
                observedAt = source.lastRetrievedAt,
                sectionStatus = section?.status,
                sectionSource = section?.source,
                sectionRetrievedAt = section?.retrievedAt,
                lastSuccessfulRetrievedAt = section?.lastSuccessfulRetrievalAt,
                characters = source.value.map { mapCharacter(it, stale = true, source.lastRetrievedAt) },
                collectionIntegrity = snapshot.characterCollectionIntegrity
            )
            DataValue.Missing -> OwnedCharacterInvestmentRosterFacts(
                state = InvestmentFactState.MISSING,
                observedAt = null,
                sectionStatus = section?.status,
                sectionSource = section?.source,
                sectionRetrievedAt = section?.retrievedAt,
                lastSuccessfulRetrievedAt = section?.lastSuccessfulRetrievalAt,
                collectionIntegrity = snapshot.characterCollectionIntegrity
            )
            DataValue.Unavailable -> OwnedCharacterInvestmentRosterFacts(
                state = InvestmentFactState.UNAVAILABLE,
                observedAt = null,
                sectionStatus = section?.status,
                sectionSource = section?.source,
                sectionRetrievedAt = section?.retrievedAt,
                lastSuccessfulRetrievedAt = section?.lastSuccessfulRetrievalAt,
                collectionIntegrity = snapshot.characterCollectionIntegrity
            )
            is DataValue.Failed -> OwnedCharacterInvestmentRosterFacts(
                state = InvestmentFactState.FAILED,
                observedAt = null,
                sectionStatus = section?.status,
                sectionSource = section?.source,
                sectionRetrievedAt = section?.retrievedAt,
                lastSuccessfulRetrievedAt = section?.lastSuccessfulRetrievalAt,
                collectionIntegrity = snapshot.characterCollectionIntegrity,
                failureReason = source.reason
            )
        }
        return OwnedCharacterInvestmentFacts(
            snapshotExportedAt = snapshot.metadata.exportedAt,
            roster = roster
        )
    }

    private fun mapCharacter(
        source: CharacterSnapshot,
        stale: Boolean,
        observedAt: Instant?
    ) = OwnedCharacterInvestmentFact(
        characterId = source.id,
        name = observed(source.name, stale, observedAt),
        level = observed(source.level, stale, observedAt),
        rarity = observed(source.rarity, stale, observedAt),
        element = observed(source.element, stale, observedAt),
        activeConstellationCount = observed(source.activeConstellationCount, stale, observedAt),
        weapon = mapWeapon(source.weapon, stale, observedAt),
        artifacts = mapArtifacts(source.artifacts, stale, observedAt),
        artifactIntegrity = mapArtifactIntegrity(source.artifacts, source.equipmentIntegrity, stale, observedAt),
        skills = observedCollection(
            source.skills.map { skill ->
                SkillInvestmentFacts(
                    id = skill.id,
                    name = observed(skill.name, stale, observedAt),
                    level = observed(skill.level, stale, observedAt),
                    isUnlocked = observed(skill.isUnlocked, stale, observedAt),
                    isEnhanced = observed(skill.isEnhanced, stale, observedAt)
                )
            }.takeIf { it.isNotEmpty() },
            stale,
            observedAt
        ),
        constellations = observedCollection(
            source.constellations.map { constellation ->
                ConstellationInvestmentFacts(
                    id = constellation.id,
                    name = observed(constellation.name, stale, observedAt),
                    position = observed(constellation.position, stale, observedAt),
                    isActive = observed(constellation.isActive, stale, observedAt),
                    isEnhanced = observed(constellation.isEnhanced, stale, observedAt)
                )
            }.takeIf { it.isNotEmpty() },
            stale,
            observedAt
        )
    )

    private fun mapWeapon(
        source: DataValue<WeaponSnapshot>,
        parentStale: Boolean,
        parentObservedAt: Instant?
    ): InvestmentObservedFact<WeaponInvestmentFacts> = when (source) {
        is DataValue.Available -> {
            val stale = parentStale
            val at = source.retrievedAt ?: parentObservedAt
            InvestmentObservedFact(
                state = if (stale) InvestmentFactState.STALE else InvestmentFactState.AVAILABLE,
                value = source.value.let { weapon ->
                    WeaponInvestmentFacts(
                        id = weapon.id,
                        name = observed(weapon.name, stale, at),
                        type = observed(weapon.type, stale, at),
                        typeName = observed(weapon.typeName, stale, at),
                        rarity = observed(weapon.rarity, stale, at),
                        level = observed(weapon.level, stale, at),
                        ascensionLevel = observed(weapon.ascensionLevel, stale, at),
                        refinementLevel = observed(weapon.refinementLevel, stale, at),
                        mainProperty = observed(weapon.mainProperty, stale, at),
                        subProperty = observed(weapon.subProperty, stale, at)
                    )
                },
                observedAt = at
            )
        }
        is DataValue.Stale -> {
            val at = source.lastRetrievedAt
            val weapon = source.value
            InvestmentObservedFact(
                state = InvestmentFactState.STALE,
                value = WeaponInvestmentFacts(
                    id = weapon.id,
                    name = observed(weapon.name, true, at),
                    type = observed(weapon.type, true, at),
                    typeName = observed(weapon.typeName, true, at),
                    rarity = observed(weapon.rarity, true, at),
                    level = observed(weapon.level, true, at),
                    ascensionLevel = observed(weapon.ascensionLevel, true, at),
                    refinementLevel = observed(weapon.refinementLevel, true, at),
                    mainProperty = observed(weapon.mainProperty, true, at),
                    subProperty = observed(weapon.subProperty, true, at)
                ),
                observedAt = at
            )
        }
        DataValue.Missing -> unavailableFact(InvestmentFactState.MISSING, parentObservedAt)
        DataValue.Unavailable -> unavailableFact(InvestmentFactState.UNAVAILABLE, parentObservedAt)
        is DataValue.Failed -> unavailableFact(InvestmentFactState.FAILED, parentObservedAt, source.reason)
    }

    private fun mapArtifacts(
        source: DataValue<List<ArtifactSnapshot>>,
        parentStale: Boolean,
        parentObservedAt: Instant?
    ): InvestmentObservedFact<List<ArtifactInvestmentFacts>> = when (source) {
        is DataValue.Available -> {
            val stale = parentStale
            val at = source.retrievedAt ?: parentObservedAt
            InvestmentObservedFact(
                state = if (stale) InvestmentFactState.STALE else InvestmentFactState.AVAILABLE,
                value = source.value.map { artifact ->
                    ArtifactInvestmentFacts(
                        id = artifact.id,
                        name = observed(artifact.name, stale, at),
                        slot = observed(artifact.slot, stale, at),
                        slotName = observed(artifact.slotName, stale, at),
                        rarity = observed(artifact.rarity, stale, at),
                        level = observed(artifact.level, stale, at),
                        set = observed(artifact.set, stale, at),
                        mainProperty = observed(artifact.mainProperty, stale, at),
                        subProperties = observedCollection(artifact.subProperties.takeIf { it.isNotEmpty() }, stale, at)
                    )
                },
                observedAt = at
            )
        }
        is DataValue.Stale -> InvestmentObservedFact(
            state = InvestmentFactState.STALE,
            value = source.value.map { artifact ->
                ArtifactInvestmentFacts(
                    id = artifact.id,
                    name = observed(artifact.name, true, source.lastRetrievedAt),
                    slot = observed(artifact.slot, true, source.lastRetrievedAt),
                    slotName = observed(artifact.slotName, true, source.lastRetrievedAt),
                    rarity = observed(artifact.rarity, true, source.lastRetrievedAt),
                    level = observed(artifact.level, true, source.lastRetrievedAt),
                    set = observed(artifact.set, true, source.lastRetrievedAt),
                    mainProperty = observed(artifact.mainProperty, true, source.lastRetrievedAt),
                    subProperties = observedCollection(artifact.subProperties.takeIf { it.isNotEmpty() }, true, source.lastRetrievedAt)
                )
            },
            observedAt = source.lastRetrievedAt
        )
        DataValue.Missing -> unavailableFact(InvestmentFactState.MISSING, parentObservedAt)
        DataValue.Unavailable -> unavailableFact(InvestmentFactState.UNAVAILABLE, parentObservedAt)
        is DataValue.Failed -> unavailableFact(InvestmentFactState.FAILED, parentObservedAt, source.reason)
    }

    private fun mapArtifactIntegrity(
        artifacts: DataValue<List<ArtifactSnapshot>>,
        integrity: EquipmentIntegritySnapshot,
        parentStale: Boolean,
        parentObservedAt: Instant?
    ): InvestmentObservedFact<EquipmentIntegritySnapshot> = when (artifacts) {
        is DataValue.Available -> InvestmentObservedFact(
            state = if (parentStale) InvestmentFactState.STALE else InvestmentFactState.AVAILABLE,
            value = integrity,
            observedAt = artifacts.retrievedAt ?: parentObservedAt
        )
        is DataValue.Stale -> InvestmentObservedFact(
            state = InvestmentFactState.STALE,
            value = integrity,
            observedAt = artifacts.lastRetrievedAt
        )
        DataValue.Missing -> unavailableFact(InvestmentFactState.MISSING, parentObservedAt)
        DataValue.Unavailable -> unavailableFact(InvestmentFactState.UNAVAILABLE, parentObservedAt)
        is DataValue.Failed -> unavailableFact(InvestmentFactState.FAILED, parentObservedAt, artifacts.reason)
    }

    private fun <T> observed(
        value: T?,
        stale: Boolean,
        at: Instant?
    ): InvestmentObservedFact<T> = if (value == null) {
        InvestmentObservedFact(state = InvestmentFactState.NOT_REPORTED, observedAt = at)
    } else {
        InvestmentObservedFact(
            state = if (stale) InvestmentFactState.STALE else InvestmentFactState.AVAILABLE,
            value = value,
            observedAt = at
        )
    }

    private fun <T> observedCollection(
        values: List<T>?,
        stale: Boolean,
        at: Instant?
    ): InvestmentObservedFact<List<T>> = if (values == null) {
        InvestmentObservedFact(state = InvestmentFactState.NOT_REPORTED, observedAt = at)
    } else {
        InvestmentObservedFact(
            state = if (stale) InvestmentFactState.STALE else InvestmentFactState.AVAILABLE,
            value = values,
            observedAt = at
        )
    }

    private fun <T> unavailableFact(
        state: InvestmentFactState,
        at: Instant?,
        reason: String? = null
    ) = InvestmentObservedFact<T>(
        state = state,
        observedAt = at,
        failureReason = reason
    )
}
