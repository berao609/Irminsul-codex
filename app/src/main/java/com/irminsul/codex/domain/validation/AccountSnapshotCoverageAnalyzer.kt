package com.irminsul.codex.domain.validation

import com.irminsul.codex.domain.model.*

/** Compares declared export outcomes with payload states retained by the unified snapshot. */
object AccountSnapshotCoverageAnalyzer {
    private data class SectionInput(
        val name: String,
        val payload: DataValue<*>?,
        val metadata: AccountSectionMetadata?
    )

    fun assess(snapshot: AccountSnapshot): AccountSnapshotCoverageReport {
        val inputs = listOf(
            SectionInput("account_card", snapshot.accountCard, snapshot.sections["account_card"]),
            SectionInput("profile_and_exploration", snapshot.profileAndExploration, snapshot.sections["profile_and_exploration"]),
            SectionInput("daily_note", snapshot.dailyNote, snapshot.sections["daily_note"]),
            SectionInput("characters", snapshot.characters, snapshot.sections["characters"]),
            SectionInput("character_roster_source", snapshot.characterRosterSource, snapshot.sections["character_roster_source"]),
            SectionInput("character_detail_source", snapshot.characterDetailSource, snapshot.sections["character_detail_source"]),
            SectionInput("spiral_abyss_current", snapshot.spiralAbyssCurrent, snapshot.sections["spiral_abyss_current"]),
            SectionInput("spiral_abyss_previous", snapshot.spiralAbyssPrevious, snapshot.sections["spiral_abyss_previous"]),
            SectionInput("imaginarium_theater", snapshot.imaginariumTheater, snapshot.sections["imaginarium_theater"])
        )
        return AccountSnapshotCoverageReport(inputs.map(::assessSection))
    }

    private fun assessSection(input: SectionInput): AccountSectionCoverage {
        val state = when (input.payload) {
            is DataValue.Available<*> -> SnapshotPayloadState.PRESENT
            is DataValue.Stale<*> -> SnapshotPayloadState.STALE
            DataValue.Missing, null -> SnapshotPayloadState.MISSING
            DataValue.Unavailable -> SnapshotPayloadState.UNAVAILABLE
            is DataValue.Failed -> SnapshotPayloadState.FAILED
        }
        val status = input.metadata?.status
        val issues = buildList {
            when {
                status == SectionStatus.SUCCESS && state !in setOf(SnapshotPayloadState.PRESENT, SnapshotPayloadState.STALE) ->
                    add(SnapshotCoverageIssue(SnapshotCoverageIssueCode.SUCCESS_WITHOUT_PAYLOAD, input.name))
                status == SectionStatus.FAILED && state in setOf(SnapshotPayloadState.PRESENT, SnapshotPayloadState.STALE) ->
                    add(SnapshotCoverageIssue(SnapshotCoverageIssueCode.FAILED_WITH_PAYLOAD, input.name))
                status == SectionStatus.SKIPPED && state in setOf(SnapshotPayloadState.PRESENT, SnapshotPayloadState.STALE) ->
                    add(SnapshotCoverageIssue(SnapshotCoverageIssueCode.SKIPPED_WITH_PAYLOAD, input.name))
                status == null && state in setOf(SnapshotPayloadState.PRESENT, SnapshotPayloadState.STALE) ->
                    add(SnapshotCoverageIssue(SnapshotCoverageIssueCode.PAYLOAD_WITHOUT_METADATA, input.name))
            }
        }
        val retrievedAt = input.metadata?.retrievedAt ?: when (val value = input.payload) {
            is DataValue.Available<*> -> value.retrievedAt
            is DataValue.Stale<*> -> value.lastRetrievedAt
            else -> null
        }
        return AccountSectionCoverage(input.name, status, state, input.metadata?.source, retrievedAt, issues)
    }
}
