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

data class CharacterSnapshot(
    val id: Int,
    val name: String?,
    val level: Int?,
    val rarity: Int?,
    val element: String?,
    val friendshipLevel: Int?,
    val activeConstellationCount: Int?
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
    val sections: Map<String, AccountSectionMetadata>
)
