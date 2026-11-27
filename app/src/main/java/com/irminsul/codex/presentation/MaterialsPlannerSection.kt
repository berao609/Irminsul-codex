package com.irminsul.codex.presentation

import android.content.SharedPreferences
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.analysis.ManualMaterialPlanCalculator
import com.irminsul.codex.domain.analysis.ManualPlanCalculationStatus
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private data class ManualMaterialPlanRow(
    val id: String,
    val materialName: String = "",
    val requiredQuantity: String = "",
    val ownedQuantity: String = ""
)

private data class MaterialTargetOption(
    val id: String,
    val label: String,
    val currentLevel: Int?
)

@Composable
fun MaterialsPlannerSection(
    characters: List<CharacterSnapshot>,
    preferences: SharedPreferences
) {
    var selectedTargetId by remember {
        mutableStateOf(preferences.getString("material_target_id", "custom") ?: "custom")
    }
    var customTargetName by remember {
        mutableStateOf(preferences.getString("material_custom_target", "") ?: "")
    }
    val targetOptions = buildList {
        characters.forEach { character ->
            add(
                MaterialTargetOption(
                    id = "character:" + character.id,
                    label = "Character · " + (character.name ?: "Character #" + character.id),
                    currentLevel = character.level
                )
            )
        }
        characters.forEach { character ->
            val weapon = when (val value = character.weapon) {
                is DataValue.Available -> value.value
                is DataValue.Stale -> value.value
                else -> null
            }
            if (weapon != null) {
                add(
                    MaterialTargetOption(
                        id = "weapon:" + character.id + ":" + weapon.id,
                        label = "Weapon · " + (weapon.name ?: "Weapon #" + weapon.id) + " (" + (character.name ?: "Equipped") + ")",
                        currentLevel = weapon.level
                    )
                )
            }
        }
    }
    val selectedTarget = targetOptions.firstOrNull { it.id == selectedTargetId }
    val legacyTargetId = preferences.getString("material_target_id", "custom") ?: "custom"
    var currentLevel by remember(selectedTargetId) {
        val stored = preferences.getString("material_plan_current_$selectedTargetId", null)
        val legacy = if (selectedTargetId == legacyTargetId) {
            preferences.getString("material_current_level", "")
        } else {
            selectedTarget?.currentLevel?.toString() ?: ""
        }
        mutableStateOf(stored ?: legacy ?: "")
    }
    var targetLevel by remember(selectedTargetId) {
        val stored = preferences.getString("material_plan_target_$selectedTargetId", null)
        val legacy = if (selectedTargetId == legacyTargetId) {
            preferences.getString("material_target_level", "")
        } else {
            ""
        }
        mutableStateOf(stored ?: legacy ?: "")
    }
    val planStorageKey = remember(selectedTargetId) {
        "manual_material_plan_rows_" + selectedTargetId
    }
    val rows = remember { mutableStateListOf<ManualMaterialPlanRow>() }
    var loadedPlanKey by remember { mutableStateOf<String?>(null) }
    var targetMenuExpanded by remember { mutableStateOf(false) }

    val characterId = selectedTargetId.takeIf { it.startsWith("character:") }
        ?.removePrefix("character:")
    var normalTalentCurrent by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_normal_current_$characterId", "") ?: "")
    }
    var normalTalentTarget by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_normal_target_$characterId", "") ?: "")
    }
    var skillTalentCurrent by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_skill_current_$characterId", "") ?: "")
    }
    var skillTalentTarget by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_skill_target_$characterId", "") ?: "")
    }
    var burstTalentCurrent by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_burst_current_$characterId", "") ?: "")
    }
    var burstTalentTarget by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_burst_target_$characterId", "") ?: "")
    }
    val talentValues = listOf(
        Triple("normal", "Normal Attack", normalTalentCurrent to normalTalentTarget),
        Triple("skill", "Elemental Skill", skillTalentCurrent to skillTalentTarget),
        Triple("burst", "Elemental Burst", burstTalentCurrent to burstTalentTarget)
    )
    val weaponPlanKey = characterId ?: "global"
    var plannedWeaponId by remember(weaponPlanKey) {
        mutableStateOf(preferences.getString("material_planned_weapon_id_$weaponPlanKey", "custom") ?: "custom")
    }
    var plannedWeaponName by remember(weaponPlanKey) {
        mutableStateOf(preferences.getString("material_planned_weapon_name_$weaponPlanKey", "") ?: "")
    }
    val plannedWeapon = targetOptions.firstOrNull { it.id == plannedWeaponId }
    var plannedWeaponCurrent by remember(weaponPlanKey, plannedWeaponId) {
        mutableStateOf(
            preferences.getString("material_planned_weapon_current_$weaponPlanKey-$plannedWeaponId", null)
                ?: plannedWeapon?.currentLevel?.toString().orEmpty()
        )
    }
    var plannedWeaponTarget by remember(weaponPlanKey, plannedWeaponId) {
        mutableStateOf(preferences.getString("material_planned_weapon_target_$weaponPlanKey-$plannedWeaponId", "") ?: "")
    }
    var weaponMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(planStorageKey) {
        rows.clear()
        rows.addAll(loadManualMaterialPlanRows(preferences, planStorageKey))
        loadedPlanKey = planStorageKey
    }
    LaunchedEffect(planStorageKey, rows.toList(), loadedPlanKey) {
        if (loadedPlanKey == planStorageKey) {
            preferences.edit().putString(planStorageKey, saveManualMaterialPlanRows(rows)).apply()
        }
    }
    LaunchedEffect(selectedTargetId, currentLevel, targetLevel, customTargetName) {
        preferences.edit()
            .putString("material_target_id", selectedTargetId)
            .putString("material_custom_target", customTargetName)
            .putString("material_plan_current_$selectedTargetId", currentLevel)
            .putString("material_plan_target_$selectedTargetId", targetLevel)
            // Keep the original keys readable for a safe, one-way migration from older builds.
            .putString("material_current_level", currentLevel)
            .putString("material_target_level", targetLevel)
            .apply()
    }
    LaunchedEffect(
        characterId, normalTalentCurrent, normalTalentTarget,
        skillTalentCurrent, skillTalentTarget, burstTalentCurrent, burstTalentTarget
    ) {
        if (characterId != null) {
            preferences.edit()
                .putString("material_talent_normal_current_$characterId", normalTalentCurrent)
                .putString("material_talent_normal_target_$characterId", normalTalentTarget)
                .putString("material_talent_skill_current_$characterId", skillTalentCurrent)
                .putString("material_talent_skill_target_$characterId", skillTalentTarget)
                .putString("material_talent_burst_current_$characterId", burstTalentCurrent)
                .putString("material_talent_burst_target_$characterId", burstTalentTarget)
                .apply()
        }
    }
    LaunchedEffect(weaponPlanKey, plannedWeaponId, plannedWeaponName) {
        preferences.edit()
            .putString("material_planned_weapon_id_$weaponPlanKey", plannedWeaponId)
            .putString("material_planned_weapon_name_$weaponPlanKey", plannedWeaponName)
            .apply()
    }
    LaunchedEffect(weaponPlanKey, plannedWeaponId, plannedWeaponCurrent, plannedWeaponTarget) {
        preferences.edit()
            .putString("material_planned_weapon_current_$weaponPlanKey-$plannedWeaponId", plannedWeaponCurrent)
            .putString("material_planned_weapon_target_$weaponPlanKey-$plannedWeaponId", plannedWeaponTarget)
            .apply()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Materials Planner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Build upgrade plans for your characters and weapons.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("1. Choose an upgrade target", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Each character and weapon keeps its own level range.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    OutlinedButton(
                        onClick = { targetMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            selectedTarget?.label ?: if (selectedTargetId == "custom") "Custom target" else "Choose target",
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text("⌄")
                    }
                    DropdownMenu(expanded = targetMenuExpanded, onDismissRequest = { targetMenuExpanded = false }) {
                        targetOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = { selectedTargetId = option.id; targetMenuExpanded = false }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Custom target") },
                            onClick = { selectedTargetId = "custom"; targetMenuExpanded = false }
                        )
                    }
                }
                if (selectedTargetId == "custom") {
                    OutlinedTextField(
                        value = customTargetName,
                        onValueChange = { customTargetName = it },
                        label = { Text("Character or weapon name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Text(
                    if (selectedTargetId.startsWith("character:")) "Character level" else "Weapon level",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentLevel,
                        onValueChange = { currentLevel = it.filter(Char::isDigit).take(3) },
                        label = { Text("Current") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = targetLevel,
                        onValueChange = { targetLevel = it.filter(Char::isDigit).take(3) },
                        label = { Text("Target") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        if (characterId != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("2. Talent investment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Set each talent track independently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    talentValues.forEach { (track, label, levels) ->
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = levels.first,
                                    onValueChange = { next ->
                                        val level = next.filter(Char::isDigit).take(2)
                                        when (track) {
                                            "normal" -> normalTalentCurrent = level
                                            "skill" -> skillTalentCurrent = level
                                            "burst" -> burstTalentCurrent = level
                                        }
                                    },
                                    label = { Text("Current") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = levels.second,
                                    onValueChange = { next ->
                                        val level = next.filter(Char::isDigit).take(2)
                                        when (track) {
                                            "normal" -> normalTalentTarget = level
                                            "skill" -> skillTalentTarget = level
                                            "burst" -> burstTalentTarget = level
                                        }
                                    },
                                    label = { Text("Target") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
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
                        Text("3. Planned weapon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Choose the weapon this character is intended to equip.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        OutlinedButton(
                            onClick = { weaponMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                plannedWeapon?.label ?: if (plannedWeaponId == "custom") "Choose or enter a weapon" else "Custom weapon",
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text("⌄")
                        }
                        DropdownMenu(expanded = weaponMenuExpanded, onDismissRequest = { weaponMenuExpanded = false }) {
                            targetOptions.filter { it.id.startsWith("weapon:") }.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = { plannedWeaponId = option.id; weaponMenuExpanded = false }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Custom weapon") },
                                onClick = { plannedWeaponId = "custom"; weaponMenuExpanded = false }
                            )
                        }
                    }
                    if (plannedWeaponId == "custom") {
                        OutlinedTextField(
                            value = plannedWeaponName,
                            onValueChange = { plannedWeaponName = it },
                            label = { Text("Weapon name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = plannedWeaponCurrent,
                            onValueChange = { plannedWeaponCurrent = it.filter(Char::isDigit).take(3) },
                            label = { Text("Current level") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = plannedWeaponTarget,
                            onValueChange = { plannedWeaponTarget = it.filter(Char::isDigit).take(3) },
                            label = { Text("Target level") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Automatic costs are not connected yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Only reviewed, approved cost data can populate automatic requirements. Manual tracking is available in the meantime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Material requirements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (rows.isEmpty()) "No materials tracked" else "${rows.size} materials tracked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { rows.add(ManualMaterialPlanRow(id = UUID.randomUUID().toString())) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add material")
                }
            }

            if (rows.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Your material list is empty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Add a material to track its required and owned quantities. Existing saved rows are loaded automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            rows.forEachIndexed { index, row ->
                val calculation = ManualMaterialPlanCalculator.calculate(row.requiredQuantity, row.ownedQuantity)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Material ${index + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { rows.removeAt(index) }) { Text("Remove") }
                        }
                        OutlinedTextField(
                            value = row.materialName,
                            onValueChange = { rows[index] = row.copy(materialName = it) },
                            label = { Text("Material name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = row.requiredQuantity,
                                onValueChange = { rows[index] = row.copy(requiredQuantity = it.filter(Char::isDigit)) },
                                label = { Text("Required") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = row.ownedQuantity,
                                onValueChange = { rows[index] = row.copy(ownedQuantity = it.filter(Char::isDigit)) },
                                label = { Text("Owned") },
                                placeholder = { Text("Not entered") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        when (calculation.status) {
                            ManualPlanCalculationStatus.COMPLETE -> {
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Remaining", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        calculation.remainingQuantity.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            ManualPlanCalculationStatus.INVENTORY_NOT_ENTERED -> Text(
                                "Enter your owned quantity to calculate what remains.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ManualPlanCalculationStatus.REQUIRED_NOT_ENTERED -> Text(
                                "Enter the required quantity.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ManualPlanCalculationStatus.INVALID_QUANTITY -> Text(
                                "Enter valid non-negative whole numbers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
        Text(
            "Plans are saved locally. A blank owned quantity means unknown; 0 means none owned.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun loadManualMaterialPlanRows(preferences: SharedPreferences, storageKey: String): List<ManualMaterialPlanRow> {
    val raw = preferences.getString(storageKey, null) ?: return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    ManualMaterialPlanRow(
                        id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                        materialName = item.optString("materialName"),
                        requiredQuantity = item.optString("requiredQuantity"),
                        ownedQuantity = item.optString("ownedQuantity")
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun saveManualMaterialPlanRows(rows: List<ManualMaterialPlanRow>): String {
    val array = JSONArray()
    rows.forEach { row ->
        array.put(
            JSONObject()
                .put("id", row.id)
                .put("materialName", row.materialName)
                .put("requiredQuantity", row.requiredQuantity)
                .put("ownedQuantity", row.ownedQuantity)
        )
    }
    return array.toString()
}
