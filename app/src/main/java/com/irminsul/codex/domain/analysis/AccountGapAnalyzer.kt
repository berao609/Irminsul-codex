package com.irminsul.codex.domain.analysis

import com.irminsul.codex.domain.model.*

/**
 * Reports account roster and equipped-gear coverage gaps using only fields retained
 * in AccountSnapshot. A data gap is not treated as proof that investment is needed.
 */
object AccountGapAnalyzer {
    fun analyze(characters: DataValue<List<CharacterSnapshot>>, integrity: CharacterCollectionIntegrity): AccountGapAnalysisReport {
        val values: List<CharacterSnapshot>
        val availability: AccountGapAnalysisAvailability
        val stale: Boolean
        when (characters) {
            is DataValue.Available -> {
                values = characters.value
                availability = AccountGapAnalysisAvailability.AVAILABLE
                stale = false
            }
            is DataValue.Stale -> {
                values = characters.value
                availability = AccountGapAnalysisAvailability.STALE
                stale = true
            }
            DataValue.Missing -> return AccountGapAnalysisReport(AccountGapAnalysisAvailability.MISSING)
            DataValue.Unavailable -> return AccountGapAnalysisReport(AccountGapAnalysisAvailability.UNAVAILABLE)
            is DataValue.Failed -> return AccountGapAnalysisReport(
                availability = AccountGapAnalysisAvailability.FAILED,
                failureReason = characters.reason
            )
        }

        val findings = buildList {
            integrity.invalidCharacterEntryIndexes.sorted().forEach { index ->
                add(
                    AccountGapFinding(
                        findingId = "roster-invalid-entry-$index",
                        type = AccountGapType.INVALID_ROSTER_ENTRY,
                        confidence = AccountGapConfidence.CONFIRMED,
                        title = "Roster entry could not be mapped",
                        explanation = "An entry exists in the source roster but does not contain enough valid character data to map.",
                        evidence = listOf("Invalid source roster entry index: $index"),
                        basedOnStaleData = stale
                    )
                )
            }
            integrity.duplicateCharacterIds.sorted().forEach { id ->
                add(
                    AccountGapFinding(
                        findingId = "roster-duplicate-$id",
                        type = AccountGapType.DUPLICATE_CHARACTER_ID,
                        confidence = AccountGapConfidence.CONFIRMED,
                        characterId = id,
                        characterName = values.firstOrNull { it.id == id }?.name,
                        title = "Duplicate character identity in roster",
                        explanation = "The source roster contains the same character ID more than once.",
                        evidence = listOf("Duplicate character ID: $id"),
                        basedOnStaleData = stale
                    )
                )
            }

            values.sortedBy { it.id }.forEach { character ->
                when (val weapon = character.weapon) {
                    DataValue.Missing -> add(reviewFinding(character, AccountGapType.WEAPON_DATA_GAP, "Weapon data is missing", "The retrieved character record has no weapon payload.", stale, "Weapon payload: missing"))
                    DataValue.Unavailable -> add(reviewFinding(character, AccountGapType.WEAPON_DATA_GAP, "Weapon data is unavailable", "Weapon information was not available from the source.", stale, "Weapon payload: unavailable"))
                    is DataValue.Failed -> add(reviewFinding(character, AccountGapType.WEAPON_DATA_GAP, "Weapon data could not be mapped", "The weapon payload failed validation and cannot support an equipment assessment.", stale, "Weapon mapping failure: ${weapon.reason}"))
                    is DataValue.Stale -> add(reviewFinding(character, AccountGapType.WEAPON_DATA_GAP, "Weapon data is stale", "The weapon information may no longer represent the current character equipment.", true, "Weapon last retrieved: ${weapon.lastRetrievedAt}"))
                    is DataValue.Available -> Unit
                }

                when (val artifacts = character.artifacts) {
                    DataValue.Missing -> add(reviewFinding(character, AccountGapType.ARTIFACT_DATA_GAP, "Artifact data is missing", "The retrieved character record has no artifact collection.", stale, "Artifact payload: missing"))
                    DataValue.Unavailable -> add(reviewFinding(character, AccountGapType.ARTIFACT_DATA_GAP, "Artifact data is unavailable", "Artifact information was not available from the source.", stale, "Artifact payload: unavailable"))
                    is DataValue.Failed -> add(reviewFinding(character, AccountGapType.ARTIFACT_DATA_GAP, "Artifact data could not be mapped", "The artifact payload failed validation and cannot support an equipment assessment.", stale, "Artifact mapping failure: ${artifacts.reason}"))
                    is DataValue.Stale -> add(reviewFinding(character, AccountGapType.ARTIFACT_DATA_GAP, "Artifact data is stale", "The artifact collection may no longer represent current equipment.", true, "Artifact last retrieved: ${artifacts.lastRetrievedAt}"))
                    is DataValue.Available -> {
                        val slots = character.equipmentIntegrity.unoccupiedArtifactSlots.distinct().sorted()
                        if (slots.isNotEmpty()) {
                            add(
                                AccountGapFinding(
                                    findingId = "${character.id}-artifact-slots",
                                    type = AccountGapType.ARTIFACT_SLOT_GAP,
                                    confidence = AccountGapConfidence.CONFIRMED,
                                    characterId = character.id,
                                    characterName = character.name,
                                    title = "Artifact slots are unoccupied",
                                    explanation = "The mapped equipment data identifies artifact slots without an equipped artifact. This is an equipment observation, not a recommendation to invest in this character.",
                                    evidence = listOf("Unoccupied artifact slot positions: ${slots.joinToString()}"),
                                    basedOnStaleData = stale
                                )
                            )
                        }
                    }
                }

                val equipment = character.equipmentIntegrity
                if (equipment.invalidArtifactEntryIndexes.isNotEmpty() ||
                    equipment.duplicateArtifactIds.isNotEmpty() ||
                    equipment.duplicateArtifactSlots.isNotEmpty() ||
                    equipment.invalidArtifactSlots.isNotEmpty() ||
                    equipment.invalidSubPropertyEntryIndexes.isNotEmpty()
                ) {
                    val evidence = buildList {
                        if (equipment.invalidArtifactEntryIndexes.isNotEmpty()) add("Invalid artifact entry indexes: ${equipment.invalidArtifactEntryIndexes.sorted().joinToString()}")
                        if (equipment.duplicateArtifactIds.isNotEmpty()) add("Duplicate artifact IDs: ${equipment.duplicateArtifactIds.sorted().joinToString()}")
                        if (equipment.duplicateArtifactSlots.isNotEmpty()) add("Duplicate artifact slots: ${equipment.duplicateArtifactSlots.sorted().joinToString()}")
                        if (equipment.invalidArtifactSlots.isNotEmpty()) add("Invalid artifact slot values: ${equipment.invalidArtifactSlots.sorted().joinToString()}")
                        if (equipment.invalidSubPropertyEntryIndexes.isNotEmpty()) add("Artifacts with invalid sub-property entries: ${equipment.invalidSubPropertyEntryIndexes.sorted().joinToString()}")
                    }
                    add(
                        AccountGapFinding(
                            findingId = "${character.id}-equipment-integrity",
                            type = AccountGapType.EQUIPMENT_INTEGRITY_ISSUE,
                            confidence = AccountGapConfidence.CONFIRMED,
                            characterId = character.id,
                            characterName = character.name,
                            title = "Artifact equipment data has integrity issues",
                            explanation = "The mapped artifact collection contains invalid, duplicate, or structurally inconsistent entries.",
                            evidence = evidence,
                            basedOnStaleData = stale
                        )
                    )
                }
            }
        }.sortedWith(
            compareBy<AccountGapFinding> { it.type.ordinal }
                .thenBy { it.characterId ?: Int.MIN_VALUE }
                .thenBy { it.findingId }
        )

        return AccountGapAnalysisReport(
            availability = availability,
            assessedCharacterCount = values.size,
            confirmedGapCount = findings.count { it.confidence == AccountGapConfidence.CONFIRMED },
            dataReviewCount = findings.count { it.confidence == AccountGapConfidence.DATA_REVIEW },
            findings = findings
        )
    }

    private fun reviewFinding(
        character: CharacterSnapshot,
        type: AccountGapType,
        title: String,
        explanation: String,
        stale: Boolean,
        evidence: String
    ) = AccountGapFinding(
        findingId = "${character.id}-${type.name.lowercase()}",
        type = type,
        confidence = AccountGapConfidence.DATA_REVIEW,
        characterId = character.id,
        characterName = character.name,
        title = title,
        explanation = explanation,
        evidence = listOf(evidence),
        basedOnStaleData = stale
    )
}
