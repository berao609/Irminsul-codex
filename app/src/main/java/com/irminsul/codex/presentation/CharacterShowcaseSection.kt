package com.irminsul.codex.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.DataValue

@Composable
fun CharacterShowcaseSection(
    characters: List<CharacterSnapshot>,
    selectedCharacterId: Int?,
    onCharacterSelected: (Int) -> Unit
) {
    var rosterExpanded by remember { mutableStateOf(false) }
    var filtersExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var minimumLevel by remember { mutableStateOf("") }
    var maximumLevel by remember { mutableStateOf("") }
    var selectedElement by remember { mutableStateOf("All") }
    var selectedWeaponType by remember { mutableStateOf("All") }

    val selected = characters.firstOrNull { it.id == selectedCharacterId } ?: characters.first()
    val elements = remember(characters) {
        listOf("All") + characters.map { it.element?.takeIf(String::isNotBlank) ?: "Unknown" }.distinct().sorted()
    }
    val weaponTypes = remember(characters) {
        listOf("All") + characters.map { it.rosterWeaponType() ?: "Unknown" }.distinct().sorted()
    }
    val filteredCharacters = remember(
        characters,
        searchQuery,
        minimumLevel,
        maximumLevel,
        selectedElement,
        selectedWeaponType
    ) {
        sortCharactersByRelease(characters.filter { character ->
            val name = character.name ?: "Character #${character.id}"
            val level = character.level
            val weaponType = character.rosterWeaponType() ?: "Unknown"
            val element = character.element?.takeIf(String::isNotBlank) ?: "Unknown"
            val minLevel = minimumLevel.toIntOrNull()
            val maxLevel = maximumLevel.toIntOrNull()
            name.contains(searchQuery.trim(), ignoreCase = true) &&
                (selectedElement == "All" || element.equals(selectedElement, ignoreCase = true)) &&
                (selectedWeaponType == "All" || weaponType == selectedWeaponType) &&
                (minLevel == null || level != null && level >= minLevel) &&
                (maxLevel == null || level != null && level <= maxLevel)
        })
    }

    val hasActiveFilters = searchQuery.isNotBlank() || minimumLevel.isNotBlank() || maximumLevel.isNotBlank() ||
        selectedElement != "All" || selectedWeaponType != "All"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            onClick = { rosterExpanded = !rosterExpanded },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Character Roster", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (rosterExpanded) "${filteredCharacters.size} of ${characters.size} characters shown"
                        else "${characters.size} characters • Selected: ${selected.name ?: "Character #${selected.id}"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (rosterExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (rosterExpanded) "Collapse character roster" else "Expand character roster"
                )
            }
        }

        if (rosterExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Search characters") },
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                    Text(if (filtersExpanded) "Hide filters" else "Filters")
                }
            }

            if (filtersExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Filter roster", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RosterChoiceMenu("Element: $selectedElement", elements, selectedElement) { selectedElement = it }
                        RosterChoiceMenu("Weapon: $selectedWeaponType", weaponTypes, selectedWeaponType) { selectedWeaponType = it }
                    }
                    Text("Character level", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = minimumLevel,
                            onValueChange = { minimumLevel = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Minimum") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = maximumLevel,
                            onValueChange = { maximumLevel = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Maximum") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    if (hasActiveFilters) {
                        TextButton(
                            onClick = {
                                searchQuery = ""
                                minimumLevel = ""
                                maximumLevel = ""
                                selectedElement = "All"
                                selectedWeaponType = "All"
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Clear filters") }
                    }
                }
            }

            if (filteredCharacters.isEmpty()) {
                Text(
                    "No characters match these filters. Adjust or clear the filters.",
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val visibleRowCount = ((filteredCharacters.size + 2) / 3).coerceAtMost(4)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = (visibleRowCount * 124).dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredCharacters,
                        key = { character -> character.id }
                    ) { character ->
                        val isSelected = character.id == selected.id
                        Card(
                            onClick = { onCharacterSelected(character.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when (character.rarity) {
                                    5 -> Color(0xFFFFC857).copy(alpha = 0.24f)
                                    4 -> Color(0xFFB388FF).copy(alpha = 0.24f)
                                    else -> MaterialTheme.colorScheme.surface
                                }
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            ) else null,
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ShowcaseArtwork(
                                    url = character.avatarIcon,
                                    description = "${character.name ?: "Character"} portrait",
                                    fallback = elementGlyph(character.element),
                                    modifier = Modifier.size(66.dp)
                                )
                                Text(
                                    character.name ?: "Character #${character.id}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                                Text(
                                    "Lv. ${character.level ?: "—"} • ${character.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "—"}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        CharacterShowcaseDetail(character = selected)
    }
}

@Composable
private fun RosterChoiceMenu(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
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

private fun CharacterSnapshot.rosterWeaponType(): String? {
    val weapon = when (val value = weapon) {
        is DataValue.Available -> value.value
        is DataValue.Stale -> value.value
        else -> null
    } ?: return null
    return weapon.typeName?.takeIf(String::isNotBlank)
        ?: weapon.type?.let { "Type $it" }
}

@Composable
private fun CharacterShowcaseDetail(character: CharacterSnapshot) {
    var expandedDetailKey by remember(character.id) { mutableStateOf<String?>(null) }
    fun toggleDetail(key: String) {
        expandedDetailKey = if (expandedDetailKey == key) null else key
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ShowcaseArtwork(
                    url = character.avatarIcon,
                    description = "${character.name ?: "Character"} artwork",
                    fallback = elementGlyph(character.element),
                    modifier = Modifier.width(112.dp).height(142.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                    Text(character.name ?: "Character #${character.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${character.element ?: "Element unavailable"} • ${character.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Level ${character.level ?: "—"}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Friendship ${character.friendshipLevel ?: "—"} • Constellations ${character.activeConstellationCount ?: "—"}/6",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Talents", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (character.skills.isEmpty()) {
                    ShowcaseUnavailable("Talent details are not available.")
                } else {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        character.skills.forEach { skill ->
                            val key = "talent:${skill.id}"
                            Column(
                                modifier = Modifier
                                    .width(78.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (expandedDetailKey == key) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { toggleDetail(key) }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                ShowcaseArtwork(skill.icon, skill.name ?: "Talent", "✦", Modifier.size(48.dp))
                                Text(skill.name ?: "Talent", style = MaterialTheme.typography.labelSmall, maxLines = 2)
                                Text("Lv. ${skill.level ?: "—"}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    character.skills.firstOrNull { "talent:${it.id}" == expandedDetailKey }?.let { skill ->
                        ShowcaseDetailSurface {
                            Text(skill.name ?: "Talent", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                when {
                                    skill.isUnlocked == false -> "Locked"
                                    skill.isUnlocked == true -> "Unlocked • Level ${skill.level ?: "—"}"
                                    else -> "Level ${skill.level ?: "—"}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            skill.description?.takeIf { it.isNotBlank() }?.let {
                                Text(formatGameDescription(it), style = MaterialTheme.typography.bodySmall)
                            }
                            skill.enhancedDescription?.takeIf { it.isNotBlank() }?.let {
                                Text(formatGameDescription(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Constellations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (character.constellations.isEmpty()) {
                    ShowcaseUnavailable("Constellation details are not available.")
                } else {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        character.constellations.sortedBy { it.position ?: it.id }.forEach { constellation ->
                            val key = "constellation:${constellation.id}"
                            Column(
                                modifier = Modifier
                                    .width(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (expandedDetailKey == key) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { toggleDetail(key) }
                                    .padding(7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                ShowcaseArtwork(
                                    constellation.icon,
                                    constellation.name ?: "Constellation",
                                    "C${constellation.position ?: constellation.id}",
                                    Modifier.size(46.dp)
                                )
                                Text("C${constellation.position ?: constellation.id}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    when (constellation.isActive) {
                                        true -> "Unlocked"
                                        false -> "Locked"
                                        null -> "Unknown"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (constellation.isActive == true) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    character.constellations.firstOrNull {
                        "constellation:${it.id}" == expandedDetailKey
                    }?.let { constellation ->
                        ShowcaseDetailSurface {
                            Text(
                                constellation.name ?: "Constellation ${constellation.position ?: constellation.id}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                when (constellation.isActive) {
                                    true -> "Unlocked"
                                    false -> "Locked"
                                    null -> "Unlock status unavailable"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            constellation.effect?.takeIf { it.isNotBlank() }?.let {
                                Text(formatGameDescription(it), style = MaterialTheme.typography.bodySmall)
                            }
                            constellation.enhancedEffect?.takeIf { it.isNotBlank() }?.let {
                                Text(formatGameDescription(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("Build inspection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            when (val weapon = character.weapon) {
                is DataValue.Available -> WeaponShowcaseRow(
                    weapon.value,
                    expanded = expandedDetailKey == "weapon",
                    onClick = { toggleDetail("weapon") }
                )
                is DataValue.Stale -> WeaponShowcaseRow(
                    weapon.value,
                    stale = true,
                    expanded = expandedDetailKey == "weapon",
                    onClick = { toggleDetail("weapon") }
                )
                DataValue.Missing -> ShowcaseUnavailable("Weapon data was not included.")
                DataValue.Unavailable -> ShowcaseUnavailable("Weapon data is unavailable.")
                is DataValue.Failed -> ShowcaseUnavailable("Weapon data could not be read.")
            }

            when (val artifacts = character.artifacts) {
                is DataValue.Available -> ArtifactShowcaseRow(
                    artifacts.value,
                    expandedDetailKey = expandedDetailKey,
                    onArtifactSelected = ::toggleDetail
                )
                is DataValue.Stale -> ArtifactShowcaseRow(
                    artifacts.value,
                    stale = true,
                    expandedDetailKey = expandedDetailKey,
                    onArtifactSelected = ::toggleDetail
                )
                DataValue.Missing -> ShowcaseUnavailable("Artifact data was not included.")
                DataValue.Unavailable -> ShowcaseUnavailable("Artifact data is unavailable.")
                is DataValue.Failed -> ShowcaseUnavailable("Artifact data could not be read.")
            }

            ShowcaseExpandableSection(
                title = "Attributes",
                summary = if (character.selectedProperties.isEmpty()) "No attribute data" else "${character.selectedProperties.size} stats",
                expanded = expandedDetailKey == "attributes",
                onClick = { toggleDetail("attributes") }
            ) {
                if (character.selectedProperties.isEmpty()) {
                    ShowcaseUnavailable("Character attributes are not available.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        character.selectedProperties.take(12).forEach { stat ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stat.statName ?: "Unknown stat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stat.final ?: stat.value ?: stat.add ?: stat.base ?: "—", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }


}

@Composable
private fun ShowcaseExpandableSection(
    title: String,
    summary: String,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(summary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse $title" else "Expand $title"
            )
        }
        if (expanded) ShowcaseDetailSurface(content)
    }
}

@Composable
private fun ShowcaseDetailSurface(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        content = content
    )
}

@Composable
private fun WeaponShowcaseRow(
    weapon: com.irminsul.codex.domain.model.WeaponSnapshot,
    stale: Boolean = false,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val passiveKey = "${weapon.id}:${weapon.refinementLevel ?: 1}"
    var passive by remember(passiveKey) { mutableStateOf<com.irminsul.codex.presentation.WeaponPassive?>(null) }
    var passiveLoading by remember(passiveKey) { mutableStateOf(false) }
    var passiveLoaded by remember(passiveKey) { mutableStateOf(false) }

    LaunchedEffect(expanded, passiveKey) {
        if (expanded && !passiveLoaded && !passiveLoading) {
            passiveLoading = true
            try {
                passive = withContext(Dispatchers.IO) {
                    com.irminsul.codex.presentation.WeaponPassiveLookup.fetch(
                        weapon.name,
                        weapon.refinementLevel ?: 1
                    )
                }
                passiveLoaded = true
            } finally {
                passiveLoading = false
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(if (expanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShowcaseArtwork(weapon.icon, weapon.name ?: "Weapon", "⚔", Modifier.size(58.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
                Text(weapon.name ?: "Weapon #${weapon.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Lv. ${weapon.level ?: "—"} • R${weapon.refinementLevel ?: "—"} • ${weapon.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (stale) Text("Using older account data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse weapon details" else "Expand weapon details"
            )
        }
        if (expanded) {
            ShowcaseDetailSurface {
                weapon.typeName?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                weapon.description?.takeIf { it.isNotBlank() }?.let {
                    Text("Weapon description", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Text("Weapon passive", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                when {
                    passiveLoading -> Text("Loading passive description…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    passive != null -> {
                        passive?.name?.let { name ->
                            Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Text(passive!!.description, style = MaterialTheme.typography.bodySmall)
                    }
                    passiveLoaded -> Text("No passive description is available from the game-data source.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                weapon.mainProperty?.let { stat ->
                    Text(
                        "${stat.statName ?: "Main stat"}: ${stat.value ?: stat.final ?: stat.base ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                weapon.subProperty?.let { stat ->
                    Text(
                        "${stat.statName ?: "Secondary stat"}: ${stat.value ?: stat.final ?: stat.base ?: "—"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "Ascension ${weapon.ascensionLevel ?: "—"} • Level ${weapon.level ?: "—"} • Refinement ${weapon.refinementLevel ?: "—"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ArtifactShowcaseRow(
    artifacts: List<com.irminsul.codex.domain.model.ArtifactSnapshot>,
    stale: Boolean = false,
    expandedDetailKey: String?,
    onArtifactSelected: (String) -> Unit
) {
    if (artifacts.isEmpty()) {
        ShowcaseUnavailable("No equipped artifact entries were returned.")
        return
    }
    val orderedArtifacts = artifacts.sortedBy { it.slot ?: Int.MAX_VALUE }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Artifacts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            orderedArtifacts.forEach { artifact ->
                val key = "artifact:${artifact.id}"
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (expandedDetailKey == key) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onArtifactSelected(key) }
                        .padding(7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ShowcaseArtwork(artifact.icon, artifact.name ?: "Artifact", "◇", Modifier.size(48.dp))
                    Text(artifact.slotName ?: artifact.name ?: "Artifact", style = MaterialTheme.typography.labelSmall, maxLines = 2)
                    Text("+${artifact.level ?: "—"}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        orderedArtifacts.firstOrNull { "artifact:${it.id}" == expandedDetailKey }?.let { artifact ->
            ShowcaseDetailSurface {
                Text(artifact.name ?: artifact.slotName ?: "Artifact", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "${artifact.slotName ?: "Artifact slot"} • ${artifact.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"} • +${artifact.level ?: "—"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                artifact.mainProperty?.let { stat ->
                    Text(
                        "${stat.statName ?: "Main stat"}: ${stat.value ?: stat.final ?: stat.base ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (artifact.subProperties.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    artifact.subProperties.forEach { stat ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stat.statName ?: "Substat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stat.value ?: stat.final ?: stat.base ?: "—", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        if (stale) Text("Using older account data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ShowcaseArtwork(url: String?, description: String, fallback: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = description,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Text(fallback, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                error = { Text(fallback, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )
        } else {
            Text(fallback, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ShowcaseUnavailable(message: String) {
    Text(message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun elementGlyph(element: String?): String = when (element?.lowercase()) {
    "wind", "anemo" -> "🌪"
    "fire", "pyro" -> "🔥"
    "water", "hydro" -> "💧"
    "ice", "cryo" -> "❄"
    "electric", "electro" -> "⚡"
    "rock", "geo" -> "🪨"
    "grass", "dendro" -> "🌿"
    else -> "✦"
}

@Composable
private fun CharacterAvatarFallback(symbol: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(76.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, fontSize = 30.sp)
        }
    }
}
