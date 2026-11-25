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

enum class BuildIssueSeverity { WARNING, ERROR }

data class BuildValidationIssue(
    val code: String,
    val severity: BuildIssueSeverity,
    val fieldPath: String,
    val message: String
)

data class CharacterBuildValidation(
    val characterId: Int,
    val issues: List<BuildValidationIssue>
) {
    val errorCount: Int get() = issues.count { it.severity == BuildIssueSeverity.ERROR }
    val warningCount: Int get() = issues.count { it.severity == BuildIssueSeverity.WARNING }
    val isValid: Boolean get() = errorCount == 0
}

data class CharacterBuildValidationReport(
    val characters: List<CharacterBuildValidation>
) {
    val totalErrors: Int get() = characters.sumOf { it.errorCount }
    val totalWarnings: Int get() = characters.sumOf { it.warningCount }
    val validCharacterCount: Int get() = characters.count { it.isValid }
    val invalidCharacterCount: Int get() = characters.size - validCharacterCount
}

enum class StatPropertyCategory {
    CORE_ATTRIBUTE, OFFENSIVE, DEFENSIVE, ELEMENTAL, HEALING, ENERGY, CONTROL, UTILITY, UNKNOWN
}

enum class StatValueKind { FLAT, PERCENTAGE, UNKNOWN }

enum class StatClassificationStatus { CLASSIFIED, UNKNOWN, CONFLICT }

enum class StatClassificationSource { SOURCE_NAME, PROPERTY_TYPE_ID, NONE }

data class StatPropertyDefinition(
    val canonicalId: String,
    val displayName: String,
    val category: StatPropertyCategory,
    val valueKind: StatValueKind,
    val element: String? = null
)

data class StatPropertyClassification(
    val characterId: Int,
    val fieldPath: String,
    val propertyType: Int?,
    val sourceStatName: String?,
    val definition: StatPropertyDefinition?,
    val status: StatClassificationStatus,
    val resolvedBy: StatClassificationSource
)

data class CharacterStatClassification(
    val characterId: Int,
    val results: List<StatPropertyClassification>
) {
    val classifiedCount: Int get() = results.count { it.status == StatClassificationStatus.CLASSIFIED }
    val unknownCount: Int get() = results.count { it.status == StatClassificationStatus.UNKNOWN }
    val conflictCount: Int get() = results.count { it.status == StatClassificationStatus.CONFLICT }
}

data class CharacterStatClassificationReport(
    val characters: List<CharacterStatClassification>
) {
    val classifiedCount: Int get() = characters.sumOf { it.classifiedCount }
    val unknownCount: Int get() = characters.sumOf { it.unknownCount }
    val conflictCount: Int get() = characters.sumOf { it.conflictCount }
}

enum class StatReconciliationStatus { MATCH, WITHIN_ROUNDING_TOLERANCE, MISMATCH, NOT_CHECKED }

data class StatReconciliationResult(
    val characterId: Int,
    val fieldPath: String,
    val propertyType: Int?,
    val statName: String?,
    val base: String?,
    val add: String?,
    val final: String?,
    val expectedFinal: String?,
    /** Signed difference: reported final minus calculated base + add. */
    val difference: String?,
    /** Half of the smallest displayed unit in the reported final value. */
    val roundingTolerance: String?,
    val status: StatReconciliationStatus,
    val reason: String? = null
)

data class CharacterStatReconciliation(
    val characterId: Int,
    val results: List<StatReconciliationResult>
) {
    val checkedCount: Int get() = results.count { it.status != StatReconciliationStatus.NOT_CHECKED }
    val mismatchCount: Int get() = results.count { it.status == StatReconciliationStatus.MISMATCH }
    val withinToleranceCount: Int get() = results.count { it.status == StatReconciliationStatus.WITHIN_ROUNDING_TOLERANCE }
}

data class CharacterStatReconciliationReport(
    val characters: List<CharacterStatReconciliation>
) {
    val checkedCount: Int get() = characters.sumOf { it.checkedCount }
    val matchCount: Int get() = characters.sumOf { c -> c.results.count { it.status == StatReconciliationStatus.MATCH } }
    val withinToleranceCount: Int get() = characters.sumOf { it.withinToleranceCount }
    val mismatchCount: Int get() = characters.sumOf { it.mismatchCount }
    val notCheckedCount: Int get() = characters.sumOf { c -> c.results.count { it.status == StatReconciliationStatus.NOT_CHECKED } }
}

sealed interface StructuredData {
    data class ObjectValue(val fields: Map<String, StructuredData>) : StructuredData
    data class ArrayValue(val items: List<StructuredData>) : StructuredData
    data class StringValue(val value: String) : StructuredData
    data class NumberValue(val value: String) : StructuredData
    data class BooleanValue(val value: Boolean) : StructuredData
    data object NullValue : StructuredData
}


enum class SnapshotPayloadState { PRESENT, STALE, MISSING, UNAVAILABLE, FAILED }

enum class SnapshotCoverageIssueCode {
    SUCCESS_WITHOUT_PAYLOAD,
    FAILED_WITH_PAYLOAD,
    SKIPPED_WITH_PAYLOAD,
    PAYLOAD_WITHOUT_METADATA
}

data class SnapshotCoverageIssue(val code: SnapshotCoverageIssueCode, val sectionName: String)

data class AccountSectionCoverage(
    val sectionName: String,
    val declaredStatus: SectionStatus?,
    val payloadState: SnapshotPayloadState,
    val source: String?,
    val retrievedAt: Instant?,
    val issues: List<SnapshotCoverageIssue>
)

data class AccountSnapshotCoverageReport(val sections: List<AccountSectionCoverage>) {
    val sectionCount: Int get() = sections.size
    val presentCount: Int get() = sections.count { it.payloadState == SnapshotPayloadState.PRESENT }
    val staleCount: Int get() = sections.count { it.payloadState == SnapshotPayloadState.STALE }
    val missingCount: Int get() = sections.count { it.payloadState == SnapshotPayloadState.MISSING }
    val unavailableCount: Int get() = sections.count { it.payloadState == SnapshotPayloadState.UNAVAILABLE }
    val failedCount: Int get() = sections.count { it.payloadState == SnapshotPayloadState.FAILED }
    val issueCount: Int get() = sections.sumOf { it.issues.size }
    val isConsistent: Boolean get() = issueCount == 0
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
    val characterCollectionIntegrity: CharacterCollectionIntegrity = CharacterCollectionIntegrity(),
    val characterBuildValidation: DataValue<CharacterBuildValidationReport> = DataValue.Missing,
    val characterStatReconciliation: DataValue<CharacterStatReconciliationReport> = DataValue.Missing,
    val characterStatClassification: DataValue<CharacterStatClassificationReport> = DataValue.Missing,
    val sectionCoverage: AccountSnapshotCoverageReport = AccountSnapshotCoverageReport(emptyList())
)
