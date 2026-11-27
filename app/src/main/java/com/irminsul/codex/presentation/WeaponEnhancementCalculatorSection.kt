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
import com.irminsul.codex.data.catalogue.BundledWeaponAscensionCandidateRepository
import com.irminsul.codex.data.catalogue.GenshinDbUpgradeCostCandidate
import com.irminsul.codex.data.catalogue.GenshinWeaponExperienceCurveCandidate
import com.irminsul.codex.data.catalogue.WeaponAscensionCandidateCalculationRequest
import com.irminsul.codex.data.catalogue.WeaponAscensionCandidateCalculationStatus
import com.irminsul.codex.data.catalogue.WeaponAscensionCandidateCalculator
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculationRequest
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculationStatus
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculator
import com.irminsul.codex.domain.analysis.WeaponExperienceRarity
import com.irminsul.codex.domain.model.WeaponSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val weaponAscensionLevelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

private data class WeaponMaterialDisplayRequirement(
    val itemId: String,
    val itemName: String,
    val quantity: Long
)

private fun weaponMaterialName(itemId: String): String = when (itemId) {
    WeaponExperienceCalculator.MYSTIC_ENHANCEMENT_ORE_ITEM_ID -> "Mystic Enhancement Ore"
    WeaponExperienceCalculator.FINE_ENHANCEMENT_ORE_ITEM_ID -> "Fine Enhancement Ore"
    WeaponExperienceCalculator.ENHANCEMENT_ORE_ITEM_ID -> "Enhancement Ore"
    WeaponExperienceCalculator.MORA_ITEM_ID -> "Mora"
    else -> "Unknown material ($itemId)"
}

@Composable
internal fun WeaponEnhancementCalculatorSection(
    weapon: WeaponSnapshot,
    currentLevel: String,
    targetLevel: String,
    preferences: SharedPreferences
) {
    val context = LocalContext.current.applicationContext
    val rarity = when (weapon.rarity) {
        4 -> WeaponExperienceRarity.FOUR_STAR
        5 -> WeaponExperienceRarity.FIVE_STAR
        else -> null
    }
    val maximumUnlockedLevel = weapon.ascensionLevel?.let { rank ->
        weaponAscensionLevelCaps.getOrNull(rank)
    }
    var currentExperience by remember(weapon.id) {
        mutableStateOf(preferences.getString("weapon_plan_current_exp_${weapon.id}", "0") ?: "0")
    }
    LaunchedEffect(weapon.id, currentExperience) {
        preferences.edit().putString("weapon_plan_current_exp_${weapon.id}", currentExperience).apply()
    }

    var ascensionCandidates by remember(weapon.id) {
        mutableStateOf<List<GenshinDbUpgradeCostCandidate>?>(null)
    }
    var candidateLoadFailed by remember(weapon.id) { mutableStateOf(false) }
    LaunchedEffect(weapon.id) {
        val loaded = withContext(Dispatchers.IO) {
            runCatching {
                BundledWeaponAscensionCandidateRepository.loadCandidates(context, weapon.id.toString())
            }
        }
        loaded.onSuccess { ascensionCandidates = it }
            .onFailure { candidateLoadFailed = true }
    }

    val current = currentLevel.toIntOrNull()
    val target = targetLevel.toIntOrNull()
    val targetMaximumUnlockedLevel = target?.let { requested ->
        weaponAscensionLevelCaps.firstOrNull { requested <= it }?.let { targetCap ->
            maximumUnlockedLevel?.let { maxOf(it, targetCap) } ?: targetCap
        }
    }
    val currentExp = currentExperience.toLongOrNull()
    val candidate = when (weapon.rarity) {
        4 -> GenshinWeaponExperienceCurveCandidate.fourStar
        5 -> GenshinWeaponExperienceCurveCandidate.fiveStar
        else -> null
    }
    val calculation = if (
        rarity != null && maximumUnlockedLevel != null &&
        current != null && target != null && targetMaximumUnlockedLevel != null &&
        currentExp != null && candidate != null
    ) {
        WeaponExperienceCalculator.calculate(
            WeaponExperienceCalculationRequest(
                rarity = rarity,
                currentLevel = current,
                currentExperienceWithinLevel = currentExp,
                targetLevel = target,
                maximumUnlockedLevel = maximumUnlockedLevel,
                targetMaximumUnlockedLevel = targetMaximumUnlockedLevel
            ),
            candidate.cumulativeExperienceByLevel
        )
    } else null

    val ascensionCalculation = if (
        weapon.rarity != null && weapon.rarity in 4..5 &&
        weapon.ascensionLevel != null &&
        current != null && target != null &&
        current in 1..90 && target in current..90 &&
        ascensionCandidates != null
    ) {
        WeaponAscensionCandidateCalculator.calculate(
            WeaponAscensionCandidateCalculationRequest(
                weaponId = weapon.id.toString(),
                rarity = weapon.rarity,
                currentLevel = current,
                currentAscensionRank = weapon.ascensionLevel,
                targetLevel = target,
                gameVersion = BundledWeaponAscensionCandidateRepository.GAME_VERSION
            ),
            ascensionCandidates.orEmpty()
        )
    } else null

    val ascensionDataComplete = ascensionCalculation?.status in setOf(
        WeaponAscensionCandidateCalculationStatus.CANDIDATE_DATA,
        WeaponAscensionCandidateCalculationStatus.NO_ASCENSION_REQUIRED
    )
    val displayRequirements = if (calculation != null) {
        val totals = linkedMapOf<String, Pair<String, Long>>()
        calculation.materials.forEach { item ->
            totals[item.itemId] = weaponMaterialName(item.itemId) to item.quantity
        }
        if (ascensionDataComplete) {
            ascensionCalculation?.materials.orEmpty().forEach { item ->
                val existing = totals[item.itemId]
                totals[item.itemId] = item.itemName to Math.addExact(existing?.second ?: 0L, item.quantity)
            }
        }
        totals.map { (id, value) -> WeaponMaterialDisplayRequirement(id, value.first, value.second) }
    } else emptyList()

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Weapon enhancement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${weapon.name ?: "Selected weapon"} · ${weapon.rarity ?: "?"}-star · 4-star/5-star calculator",
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
                rarity == null -> Text(
                    "Only 4-star and 5-star weapons are supported.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                maximumUnlockedLevel == null -> Text(
                    "Ascension data is unavailable. The calculator cannot verify the weapon's current level cap.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                current == null || target == null || currentExp == null -> Text(
                    "Enter valid current and target levels, plus the current EXP within the level.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                targetMaximumUnlockedLevel == null -> Text(
                    "Target level must be between 1 and 90.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                calculation?.status == WeaponExperienceCalculationStatus.INVALID_REQUEST -> Text(
                    when (calculation.issue) {
                        com.irminsul.codex.domain.analysis.WeaponExperienceCalculationIssue.TARGET_EXCEEDS_ASCENSION_CAP ->
                            "Target exceeds the planned ascension cap of level $targetMaximumUnlockedLevel."
                        com.irminsul.codex.domain.analysis.WeaponExperienceCalculationIssue.INVALID_CURRENT_EXPERIENCE ->
                            "Current EXP is invalid for the selected level or ascension cap."
                        com.irminsul.codex.domain.analysis.WeaponExperienceCalculationIssue.INVALID_LEVEL_RANGE ->
                            "Enter levels from 1 to 90, with target not below current."
                        else -> "The weapon progression data is invalid."
                    },
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
                calculation != null -> {
                    Text(
                        "Required EXP: ${calculation.requiredExperience}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    when {
                        weapon.ascensionLevel == null -> Text(
                            "Ascension rank is missing from the account snapshot. Only enhancement ore costs can be calculated.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        ascensionCandidates == null && !candidateLoadFailed -> Text(
                            "Loading offline ascension materials…",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                        candidateLoadFailed -> Text(
                            "The bundled ascension dataset could not be read. Ascension costs are unavailable.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        ascensionCalculation?.status == WeaponAscensionCandidateCalculationStatus.COST_DATA_INCOMPLETE -> Text(
                            "Ascension costs are incomplete for this weapon at the level-${ascensionCalculation.missingFromLevel} gate. Listed totals exclude all ascension materials and ascension Mora.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        ascensionCalculation?.status == WeaponAscensionCandidateCalculationStatus.OVERLAPPING_SEGMENTS ||
                            ascensionCalculation?.status == WeaponAscensionCandidateCalculationStatus.INVALID_CANDIDATE_DATA ||
                            ascensionCalculation?.status == WeaponAscensionCandidateCalculationStatus.QUANTITY_OVERFLOW -> Text(
                            "Ascension source data failed validation. Ascension costs are withheld.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (displayRequirements.isEmpty()) {
                        Text("No materials required for this level range.")
                    } else {
                        displayRequirements.forEach { requirement ->
                            val ownedKey = "weapon_plan_owned_${weapon.id}_${requirement.itemId}"
                            var owned by remember(weapon.id, requirement.itemId) {
                                mutableStateOf(preferences.getString(ownedKey, "") ?: "")
                            }
                            LaunchedEffect(ownedKey, owned) {
                                preferences.edit().putString(ownedKey, owned).apply()
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "${requirement.itemName} · Required: ${requirement.quantity}",
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
                                val ownedQuantity = owned.toLongOrNull()
                                if (ownedQuantity != null) {
                                    Text(
                                        "Remaining: ${(requirement.quantity - ownedQuantity).coerceAtLeast(0L)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        "Weapon EXP and ascension requirements use version-pinned Genshin-DB 7.2 candidate data. This source is not yet permission-reviewed or promoted to trusted knowledge. Weapon fodder is not included; enhancement ore is calculated instead.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
