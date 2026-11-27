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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.irminsul.codex.data.catalogue.GenshinWeaponExperienceCurveCandidate
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculationRequest
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculationStatus
import com.irminsul.codex.domain.analysis.WeaponExperienceCalculator
import com.irminsul.codex.domain.analysis.WeaponExperienceRarity
import com.irminsul.codex.domain.model.WeaponSnapshot

private val weaponAscensionLevelCaps = listOf(20, 40, 50, 60, 70, 80, 90)

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
                    "${weapon.name ?: "Selected weapon"} · ${weapon.rarity ?: "?"}-star · Ore-only estimate",
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
                        "Required EXP: ${calculation.requiredExperience} · Mora: ${calculation.moraRequired}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (calculation.materials.isEmpty()) {
                        Text("No enhancement materials required for this level range.")
                    } else {
                        calculation.materials.forEach { requirement ->
                            val ownedKey = "weapon_plan_owned_${weapon.id}_${requirement.itemId}"
                            var owned by remember(weapon.id, requirement.itemId) {
                                mutableStateOf(preferences.getString(ownedKey, "") ?: "")
                            }
                            LaunchedEffect(ownedKey, owned) {
                                preferences.edit().putString(ownedKey, owned).apply()
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "${weaponMaterialName(requirement.itemId)} · Required: ${requirement.quantity}",
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
                        "Candidate data only. This preview uses ore materials, not weapon fodder. Ascension materials and enemy drops are not included. Source permission and review are pending.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
