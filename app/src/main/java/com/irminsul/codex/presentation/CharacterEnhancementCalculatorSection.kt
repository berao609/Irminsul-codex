package com.irminsul.codex.presentation

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.irminsul.codex.data.catalogue.BundledCharacterProgressionCandidateRepository
import com.irminsul.codex.data.catalogue.CharacterUpgradePlanCandidateCalculator
import com.irminsul.codex.data.catalogue.CharacterFullUpgradePlanCandidateCalculator
import com.irminsul.codex.data.catalogue.CharacterTalentLevelRange
import com.irminsul.codex.data.catalogue.CharacterUpgradePlanCandidateRequest
import com.irminsul.codex.data.catalogue.CharacterUpgradePlanCandidateStatus
import com.irminsul.codex.data.catalogue.GenshinCharacterExperienceCurveCandidate
import com.irminsul.codex.domain.analysis.CharacterExperienceCalculator
import com.irminsul.codex.domain.model.CharacterSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun CharacterEnhancementCalculatorSection(
    character: CharacterSnapshot,
    currentLevel: String,
    targetLevel: String,
    talentRanges: List<CharacterTalentLevelRange>,
    preferences: SharedPreferences
) {
    val context = LocalContext.current.applicationContext
    var currentExperience by remember(character.id) {
        mutableStateOf(preferences.getString("character_plan_current_exp_${character.id}", "0") ?: "0")
    }
    LaunchedEffect(character.id, currentExperience) {
        preferences.edit()
            .putString("character_plan_current_exp_${character.id}", currentExperience)
            .apply()
    }

    var candidateLoadFailed by remember(character.id) { mutableStateOf(false) }
    var loadedCandidates by remember(character.id) {
        mutableStateOf<List<com.irminsul.codex.data.catalogue.GenshinDbUpgradeCostCandidate>?>(null)
    }
    LaunchedEffect(character.id) {
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                BundledCharacterProgressionCandidateRepository.loadCandidates(context, character.id.toString())
            }
        }
        loaded.onSuccess { result ->
            if (result == null) candidateLoadFailed = true
            else loadedCandidates = result.candidates
        }.onFailure { candidateLoadFailed = true }
    }

    val current = currentLevel.toIntOrNull()
    val target = targetLevel.toIntOrNull()
    val currentExp = currentExperience.toLongOrNull()
    val rank = character.ascensionLevel ?: current?.let { level ->
        when {
            level < 20 -> 0
            level < 40 -> 1
            level < 50 -> 2
            level < 60 -> 3
            level < 70 -> 4
            else -> 5
        }
    }
    val levelPlan = if (
        current != null && target != null && currentExp != null &&
        rank != null && loadedCandidates != null
    ) {
        CharacterUpgradePlanCandidateCalculator.calculate(
            CharacterUpgradePlanCandidateRequest(
                characterId = character.id.toString(),
                gameVersion = BundledCharacterProgressionCandidateRepository.GAME_VERSION,
                currentLevel = current,
                currentExperienceWithinLevel = currentExp,
                currentAscensionRank = rank,
                targetLevel = target
            ),
            GenshinCharacterExperienceCurveCandidate.current.cumulativeExperienceByLevel,
            loadedCandidates.orEmpty()
        )
    } else null
    val combinedPlan = levelPlan?.let {
        CharacterFullUpgradePlanCandidateCalculator.calculate(
            levelPlan = it,
            characterId = character.id.toString(),
            gameVersion = BundledCharacterProgressionCandidateRepository.GAME_VERSION,
            talentRanges = talentRanges,
            candidates = loadedCandidates.orEmpty()
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Character enhancement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${character.name ?: "Selected character"} · Level and ascension materials",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = currentExperience,
                onValueChange = { currentExperience = it.filter(Char::isDigit).take(9) },
                label = { Text("Current EXP within level") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            when {
                current == null || target == null || currentExp == null -> Text(
                    "Enter valid current and target levels, plus current EXP within the level.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                target !in current..90 -> Text(
                    "Target level must be between the current level and 90.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                loadedCandidates == null && !candidateLoadFailed -> Text(
                    "Loading offline character ascension materials…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                candidateLoadFailed -> Text(
                    "The bundled character progression dataset could not be read. Complete totals are unavailable.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                combinedPlan?.status == CharacterUpgradePlanCandidateStatus.INCOMPLETE -> Text(
                    combinedPlan.issue ?: "Some character or talent costs are unavailable. Combined totals are withheld.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                combinedPlan?.status == CharacterUpgradePlanCandidateStatus.INVALID -> Text(
                    combinedPlan.issue ?: "Character progression data failed validation. Material totals are withheld.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                combinedPlan?.status == CharacterUpgradePlanCandidateStatus.COMPLETE_CANDIDATE_DATA -> {
                    if (combinedPlan.requirements.isEmpty()) {
                        Text("No materials required for this level range.")
                    } else {
                        combinedPlan.requirements.forEach { requirement ->
                            val ownedKey = "character_plan_owned_${character.id}_${requirement.itemId}"
                            var owned by remember(character.id, requirement.itemId) {
                                mutableStateOf(preferences.getString(ownedKey, "") ?: "")
                            }
                            LaunchedEffect(ownedKey, owned) {
                                preferences.edit().putString(ownedKey, owned).apply()
                            }
                            val materialName = when (requirement.itemId) {
                                CharacterExperienceCalculator.HEROS_WIT_ITEM_ID -> "Hero's Wit"
                                CharacterExperienceCalculator.ADVENTURERS_EXPERIENCE_ITEM_ID -> "Adventurer's Experience"
                                CharacterExperienceCalculator.WANDERERS_ADVICE_ITEM_ID -> "Wanderer's Advice"
                                CharacterExperienceCalculator.MORA_ITEM_ID -> "Mora"
                                else -> requirement.itemName
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "$materialName · Required: ${requirement.quantity}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                OutlinedTextField(
                                    value = owned,
                                    onValueChange = { owned = it.filter(Char::isDigit).take(12) },
                                    label = { Text("Owned (optional)") },
                                    placeholder = { Text("Unknown") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                owned.toLongOrNull()?.let { quantity ->
                                    Text(
                                        "Remaining: ${(requirement.quantity - quantity).coerceAtLeast(0L)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Text(
                "Automatic requirements are calculated from the bundled Genshin-DB 7.2 progression data. Source: Genshin-DB, version-pinned offline dataset.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
