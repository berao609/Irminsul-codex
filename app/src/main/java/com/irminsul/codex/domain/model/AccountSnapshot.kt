package com.irminsul.codex.domain.model

import java.time.Instant

data class AccountIdentity(
    val uid: String,
    val server: String
)

data class SnapshotMetadata(
    val exportedAt: Instant,
    val exportFormatVersion: Int,
    val origin: String = "hoyolab_export"
)

enum class SectionStatus { SUCCESS, PARTIAL, FAILED, SKIPPED, UNKNOWN }

data class AccountSectionMetadata(
    val status: SectionStatus,
    val source: String?,
    val retrievedAt: Instant?,
    val lastSuccessfulRetrievalAt: Instant?,
    val message: String? = null
)

sealed interface DataValue<out T> {
    data class Available<T>(val value: T, val retrievedAt: Instant?) : DataValue<T>
    data object Missing : DataValue<Nothing>
    data object Unavailable : DataValue<Nothing>
    data class Failed(val reason: String) : DataValue<Nothing>
    data class Stale<T>(val value: T, val lastRetrievedAt: Instant) : DataValue<T>
}

data class DailyNoteSnapshot(
    val currentResin: DataValue<Int>,
    val maxResin: DataValue<Int>,
    val resinRecoveryTimeSeconds: DataValue<Long>,
    val finishedTaskCount: DataValue<Int>,
    val totalTaskCount: DataValue<Int>,
    val extraTaskRewardReceived: DataValue<Boolean>,
    val currentHomeCurrency: DataValue<Int>,
    val maxHomeCurrency: DataValue<Int>
)

data class StatValueSnapshot(
    val propertyType: Int?,
    val statName: String?,
    val base: String?,
    val add: String?,
    val final: String?,
    val value: String?,
    val rolls: Int?
)

data class WeaponSnapshot(
    val id: Int,
    val name: String?,
    val icon: String?,
    val type: Int?,
    val rarity: Int?,
    val level: Int?,
    val ascensionLevel: Int?,
    val typeName: String?,
    val description: String?,
    val refinementLevel: Int?,
    val mainProperty: StatValueSnapshot?,
    val subProperty: StatValueSnapshot?
)

data class ArtifactSnapshot(
    val id: Int,
    val name: String?,
    val icon: String?,
    val slot: Int?,
    val slotName: String?,
    val rarity: Int?,
    val level: Int?,
    val set: StructuredData?,
    val mainProperty: StatValueSnapshot?,
    val subProperties: List<StatValueSnapshot>
)

data class ConstellationSnapshot(
    val id: Int,
    val name: String?,
    val icon: String?,
    val effect: String?,
    val isActive: Boolean?,
    val position: Int?,
    val isEnhanced: Boolean?,
    val enhancedEffect: String?,
    val canBeEnhanced: Boolean?
)

data class CharacterSkillSnapshot(
    val id: Int,
    val type: Int?,
    val name: String?,
    val level: Int?,
    val description: String?,
    val icon: String?,
    val isUnlocked: Boolean?,
    val isEnhanced: Boolean?,
    val enhancedDescription: String?,
    val canBeEnhanced: Boolean?,
    val rawAffixes: StructuredData?,
    val rawEnhancementIndexes: StructuredData?
)

data class CharacterSnapshot(
    val id: Int,
    val name: String?,
    val level: Int?,
    val rarity: Int?,
    val element: String?,
    val friendshipLevel: Int?,
    val activeConstellationCount: Int?,
    val weapon: DataValue<WeaponSnapshot> = DataValue.Missing,
    val artifacts: DataValue<List<ArtifactSnapshot>> = DataValue.Missing,
    val equipmentIntegrity: EquipmentIntegritySnapshot = EquipmentIntegritySnapshot(),
    val constellations: List<ConstellationSnapshot> = emptyList(),
    val skills: List<CharacterSkillSnapshot> = emptyList(),
    val selectedProperties: List<StatValueSnapshot> = emptyList(),
    val baseProperties: List<StatValueSnapshot> = emptyList(),
    val extraProperties: List<StatValueSnapshot> = emptyList(),
    val elementProperties: List<StatValueSnapshot> = emptyList()
)

data class EquipmentIntegritySnapshot(
    val artifactSourceEntryCount: Int? = null,
    val mappedArtifactCount: Int = 0,
    val invalidArtifactEntryIndexes: List<Int> = emptyList(),
    val duplicateArtifactIds: List<Int> = emptyList(),
    val duplicateArtifactSlots: List<Int> = emptyList(),
    val invalidArtifactSlots: List<Int> = emptyList(),
    val unoccupiedArtifactSlots: List<Int> = (1..5).toList(),
    val invalidSubPropertyEntryIndexes: List<Int> = emptyList()
)

data class CharacterCollectionIntegrity(
    val sourceEntryCount: Int? = null,
    val mappedEntryCount: Int = 0,
    val invalidCharacterEntryIndexes: List<Int> = emptyList(),
    val duplicateCharacterIds: List<Int> = emptyList()
)

sealed interface StructuredData {
    data class ObjectValue(val fields: Map<String, StructuredData>) : StructuredData
    data class ArrayValue(val items: List<StructuredData>) : StructuredData
    data class StringValue(val value: String) : StructuredData
    data class NumberValue(val value: String) : StructuredData
    data class BooleanValue(val value: Boolean) : StructuredData
    data object NullValue : StructuredData
}

data class AccountSnapshot(
    val identity: AccountIdentity,
    val metadata: SnapshotMetadata,
    val overallStatus: SectionStatus,
    val dailyNote: DataValue<DailyNoteSnapshot>,
    val characters: DataValue<List<CharacterSnapshot>>,
    val accountCard: DataValue<StructuredData>,
    val characterRosterSource: DataValue<StructuredData>,
    val characterDetailSource: DataValue<StructuredData>,
    val profileAndExploration: DataValue<StructuredData>,
    val spiralAbyssCurrent: DataValue<StructuredData>,
    val spiralAbyssPrevious: DataValue<StructuredData>,
    val imaginariumTheater: DataValue<StructuredData>,
    val sections: Map<String, AccountSectionMetadata>,
    val characterCollectionIntegrity: CharacterCollectionIntegrity = CharacterCollectionIntegrity()
)
