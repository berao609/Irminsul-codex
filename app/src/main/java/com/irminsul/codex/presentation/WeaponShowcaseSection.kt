package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.irminsul.codex.domain.model.CharacterSnapshot

@Composable
internal fun WeaponShowcaseSection(characters: List<CharacterSnapshot>) {
    val weapons = remember(characters) { WeaponRosterRules.entries(characters) }
    var selectedRarity by remember { mutableStateOf("All") }
    var selectedType by remember { mutableStateOf("All") }

    val rarities = remember(weapons) {
        listOf("All") + weapons.mapNotNull { it.weapon.rarity }
            .distinct().sortedDescending().map { "${it}★" }
    }
    val types = remember(weapons) {
        listOf("All") + weapons.map { it.weapon.typeName?.takeIf(String::isNotBlank) ?: "Unknown" }
            .distinct().sorted()
    }
    val filtered = remember(weapons, selectedRarity, selectedType) {
        WeaponRosterRules.filter(
            entries = weapons,
            rarity = selectedRarity.removeSuffix("★").toIntOrNull(),
            weaponType = selectedType.takeIf { it != "All" }
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Weapons equipped on retrieved characters. This is not a complete inventory of unequipped weapons.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeaponRosterChoiceMenu(
                label = "Rarity: $selectedRarity",
                options = rarities,
                selected = selectedRarity,
                modifier = Modifier.weight(1f)
            ) { selectedRarity = it }
            WeaponRosterChoiceMenu(
                label = "Type: $selectedType",
                options = types,
                selected = selectedType,
                modifier = Modifier.weight(1f)
            ) { selectedType = it }
        }
        if (selectedRarity != "All" || selectedType != "All") {
            TextButton(
                onClick = {
                    selectedRarity = "All"
                    selectedType = "All"
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Clear filters")
            }
        }
        Text(
            "${filtered.size} of ${weapons.size} weapons",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (weapons.isEmpty()) {
            Text(
                "No equipped weapon data is available in the retrieved character roster.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (filtered.isEmpty()) {
            Text(
                "No weapons match these filters.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxWidth().heightIn(max = 640.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.weapon.id }) { entry ->
                    WeaponRosterCard(entry)
                }
            }
        }
    }
}

@Composable
private fun WeaponRosterCard(entry: WeaponRosterEntry) {
    val weapon = entry.weapon
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            AsyncImage(
                model = weapon.icon,
                contentDescription = weapon.name ?: "Weapon",
                modifier = Modifier.size(68.dp),
                contentScale = ContentScale.Fit
            )
            Text(
                weapon.name ?: "Weapon #${weapon.id}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                minLines = 2
            )
            Text(
                "${weapon.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Lv. ${weapon.level ?: "—"} · R${weapon.refinementLevel ?: "—"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                entry.equippedBy.joinToString(", "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun WeaponRosterChoiceMenu(
    label: String,
    options: List<String>,
    selected: String,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(label, maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
