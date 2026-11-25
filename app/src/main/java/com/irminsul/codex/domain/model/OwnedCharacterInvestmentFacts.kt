package com.irminsul.codex.domain.model

import java.time.Instant

/**
 * Availability of an observed account fact. NOT_REPORTED means the normalized
 * source object did not provide that field; it is not equivalent to zero/false.
 */
enum class InvestmentFactState {
    AVAILABLE,
    STALE,
    NOT_REPORTED,
    MISSING,
    UNAVAILABLE,
    FAILED
}

/**
 * A directly observed value with source availability and timestamp preserved.
 * A null value is valid only for states that do not contain an observed value.
 */
data class InvestmentObservedFact<T>(
    val state: InvestmentFactState,
    val value: T? = null,
    val observedAt: Instant? = null,
    val failureReason: String? = null
)

data class WeaponInvestmentFacts(
    val id: Int,
    val name: InvestmentObservedFact<String>,
    val type: InvestmentObservedFact<Int>,
    val typeName: InvestmentObservedFact<String>,
    val rarity: InvestmentObservedFact<Int>,
    val level: InvestmentObservedFact<Int>,
    val ascensionLevel: InvestmentObservedFact<Int>,
    val refinementLevel: InvestmentObservedFact<Int>
)

data class ArtifactInvestmentFacts(
    val id: Int,
    val name: InvestmentObservedFact<String>,
    val slot: InvestmentObservedFact<Int>,
    val slotName: InvestmentObservedFact<String>,
    val rarity: InvestmentObservedFact<Int>,
    val level: InvestmentObservedFact<Int>
)

data class SkillInvestmentFacts(
    val id: Int,
    val name: InvestmentObservedFact<String>,
    val level: InvestmentObservedFact<Int>,
    val isUnlocked: InvestmentObservedFact<Boolean>,
    val isEnhanced: InvestmentObservedFact<Boolean>
)

data class ConstellationInvestmentFacts(
    val id: Int,
    val name: InvestmentObservedFact<String>,
    val position: InvestmentObservedFact<Int>,
    val isActive: InvestmentObservedFact<Boolean>,
    val isEnhanced: InvestmentObservedFact<Boolean>
)

data class OwnedCharacterInvestmentFact(
    val characterId: Int,
    val name: InvestmentObservedFact<String>,
    val level: InvestmentObservedFact<Int>,
    val rarity: InvestmentObservedFact<Int>,
    val element: InvestmentObservedFact<String>,
    val activeConstellationCount: InvestmentObservedFact<Int>,
    val weapon: InvestmentObservedFact<WeaponInvestmentFacts>,
    val artifacts: InvestmentObservedFact<List<ArtifactInvestmentFacts>>,
    val artifactIntegrity: InvestmentObservedFact<EquipmentIntegritySnapshot>,
    val skills: InvestmentObservedFact<List<SkillInvestmentFacts>>,
    val constellations: InvestmentObservedFact<List<ConstellationInvestmentFacts>>
)

data class OwnedCharacterInvestmentRosterFacts(
    val state: InvestmentFactState,
    val observedAt: Instant?,
    val characters: List<OwnedCharacterInvestmentFact> = emptyList(),
    val collectionIntegrity: CharacterCollectionIntegrity = CharacterCollectionIntegrity(),
    val failureReason: String? = null
)

/**
 * Account observations only. This model deliberately contains no role, synergy,
 * meta, investment-priority, cost, affordability, or pull-value conclusions.
 */
data class OwnedCharacterInvestmentFacts(
    val snapshotExportedAt: Instant,
    val roster: OwnedCharacterInvestmentRosterFacts
)
