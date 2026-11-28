package com.irminsul.codex.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData
import com.irminsul.codex.domain.model.StygianOnslaughtSnapshot

@Composable
private fun ChronicleSection(
    title: String,
    subtitle: String? = null,
    collapsible: Boolean = true,
    content: @Composable () -> Unit
) {
    var expanded by remember(title) { mutableStateOf(true) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth()
                .then(if (collapsible) Modifier.clickable(role = Role.Button) { expanded = !expanded } else Modifier)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            if (collapsible) {
                AppIcon(
                    if (expanded) AppIconName.EXPAND_LESS else AppIconName.EXPAND_MORE,
                    contentDescription = if (expanded) "Collapse $title" else "Expand $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!collapsible || expanded) content()
    }
}

@Composable
fun ChronicleOverviewSection(accountCard: DataValue<StructuredData>, profileAndExploration: DataValue<StructuredData>) {
    val overview = ChroniclePresentationMapper.accountOverview(accountCard, profileAndExploration)
    val identity = overview.filter { it.label == "Player" || it.label == "Adventure Rank" }
    val statistics = overview.filterNot { it.label == "Player" || it.label == "Adventure Rank" }
    val primaryStatistics = ChroniclePresentationMapper.primaryOverviewMetrics(statistics)
    val secondaryStatistics = ChroniclePresentationMapper.secondaryOverviewMetrics(statistics)
    var secondaryExpanded by remember(accountCard, profileAndExploration) { mutableStateOf(false) }

    ChronicleSection("Account Overview", "Your journey and collection at a glance") {
        if (overview.isEmpty()) {
            Text(chronicleAvailabilityText(profileAndExploration), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            if (identity.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    identity.forEach { metric ->
                        Column(
                            Modifier.weight(1f).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(14.dp)).padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(metric.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(metric.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 2)
                        }
                    }
                }
            }
            if (primaryStatistics.isNotEmpty()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    primaryStatistics.forEach { metric ->
                        Column(
                            Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)).padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(metric.value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(metric.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                    }
                    repeat(3 - primaryStatistics.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            if (secondaryStatistics.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) { secondaryExpanded = !secondaryExpanded }.padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (secondaryExpanded) "Additional statistics · ${secondaryStatistics.size}" else "View additional statistics · ${secondaryStatistics.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AppIcon(
                        if (secondaryExpanded) AppIconName.EXPAND_LESS else AppIconName.EXPAND_MORE,
                        contentDescription = if (secondaryExpanded) "Collapse additional statistics" else "Expand additional statistics",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (secondaryExpanded) {
                    secondaryStatistics.chunked(3).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { metric ->
                                Column(
                                    Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp)).padding(9.dp),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(metric.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                    Text(metric.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChronicleExplorationSection(
    value: DataValue<StructuredData>,
    compact: Boolean = true,
    onViewMore: (() -> Unit)? = null
) {
    val regions = ChroniclePresentationMapper.explorationRegions(value)
    if (compact) {
        ChronicleSection("World Exploration", "Regional completion") {
            if (regions.isEmpty()) {
                Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    regions.forEach { region -> RegionOverviewCard(region) }
                }
                onViewMore?.let {
                    TextButton(onClick = it, modifier = Modifier.fillMaxWidth()) {
                        Text("View more regions")
                    }
                }
            }
        }
    } else if (regions.isEmpty()) {
        Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            regions.forEach { region -> RegionDetailCard(region) }
        }
    }
}

@Composable
private fun RegionOverviewCard(region: ChronicleRegion) {
    Column(
        Modifier.width(196.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RegionEmblem(region, Modifier.size(40.dp))
            Text(region.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, maxLines = 2, minLines = 2)
        }
        Text("Main Areas Only", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(region.percentageLabel, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(region.progress, Modifier.fillMaxWidth().height(6.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun RegionDetailCard(region: ChronicleRegion, nested: Boolean = false) {
    var expanded by rememberSaveable(region.id, region.name) { mutableStateOf(false) }
    val cardColor = if (nested) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardColor)
    ) {
        if (!region.backgroundImageUrl.isNullOrBlank()) {
            AsyncImage(
                model = region.backgroundImageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
            Box(
                Modifier.matchParentSize()
                    .background(cardColor.copy(alpha = 0.88f))
            )
        }
        Column(
            Modifier.fillMaxWidth().padding(if (nested) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RegionEmblem(region, Modifier.size(if (nested) 42.dp else 56.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(region.name, style = if (nested) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    LinearProgressIndicator(
                        region.progress,
                        Modifier.fillMaxWidth().height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Text(region.percentageLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (expanded) {
                val progression = buildList {
                    region.reputationLevel?.let { add("Reputation · Lv. $it") }
                    region.statueLevel?.let { add("Statue of The Seven · Lv. $it") }
                }
                progression.forEach { label ->
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (region.offerings.isNotEmpty()) {
                    Text("Offerings", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    region.offerings.forEach { offering ->
                        RegionProgressionRow(offering.name, offering.level, offering.iconUrl)
                    }
                }

                if (region.tribalReputations.isNotEmpty()) {
                    Text("Tribal Reputation", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    region.tribalReputations.forEach { tribe ->
                        RegionProgressionRow(tribe.name, tribe.level, tribe.iconUrl)
                    }
                }

                if (region.subareas.isNotEmpty()) {
                    Text("Subareas", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    region.subareas.chunked(2).forEach { areaRow ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            areaRow.forEach { area ->
                                Column(
                                    Modifier.weight(1f)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f), RoundedCornerShape(9.dp))
                                        .padding(horizontal = 9.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(area.name, style = MaterialTheme.typography.labelSmall, maxLines = 2, minLines = 2)
                                    Text(area.percentageLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    LinearProgressIndicator(
                                        area.progress,
                                        Modifier.fillMaxWidth().height(3.dp),
                                        color = MaterialTheme.colorScheme.secondary,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                            if (areaRow.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                if (region.childRegions.isNotEmpty()) {
                    Text("Subregions", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    region.childRegions.forEach { child -> RegionDetailCard(child, nested = true) }
                }
            }

            Row(
                Modifier.fillMaxWidth()
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .semantics { this.selected = expanded }
                    .padding(top = 1.dp, bottom = 0.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (expanded) "Show less" else "Show details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(3.dp))
                AppIcon(
                    if (expanded) AppIconName.EXPAND_LESS else AppIconName.EXPAND_MORE,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RegionProgressionRow(name: String, level: Int?, iconUrl: String?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        if (!iconUrl.isNullOrBlank()) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        level?.let { Text("Lv. $it", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun RegionEmblem(region: ChronicleRegion, modifier: Modifier = Modifier) {
    if (!region.iconUrl.isNullOrBlank()) {
        AsyncImage(
            model = region.iconUrl,
            contentDescription = "${region.name} emblem",
            contentScale = ContentScale.Fit,
            modifier = modifier
        )
    }
}

@Composable
fun ChronicleRecordSection(
    title: String,
    value: DataValue<StructuredData>,
    mode: ChronicleRecordMode,
    roster: List<CharacterSnapshot> = emptyList()
) {
    if (mode == ChronicleRecordMode.IMAGINARIUM_THEATER) {
        ChronicleTheaterSection(title, value, roster)
    } else {
        ChronicleSection(title, "Spiral Abyss · floor and chamber records") {
            ChronicleAbyssCycleContent(value, roster)
        }
    }
}

@Composable
fun ChronicleSpiralAbyssSection(
    current: DataValue<StructuredData>,
    previous: DataValue<StructuredData>,
    roster: List<CharacterSnapshot>
) {
    var selectedCycle by rememberSaveable { mutableIntStateOf(0) }
    ChronicleSection("Spiral Abyss", "Floor and chamber records", collapsible = false) {
        ChronicleCycleSelector(
            selectedIndex = selectedCycle,
            labels = listOf("Current Cycle", "Previous Cycle"),
            onSelected = { selectedCycle = it }
        )
        ChronicleAbyssCycleContent(
            value = if (selectedCycle == 0) current else previous,
            roster = roster
        )
    }
}

@Composable
private fun ChronicleAbyssCycleContent(
    value: DataValue<StructuredData>,
    roster: List<CharacterSnapshot>
) {
    val metrics = ChroniclePresentationMapper.recordMetrics(value, ChronicleRecordMode.SPIRAL_ABYSS)
    val cycle = ChroniclePresentationMapper.spiralAbyss(value, roster)
    if (metrics.isEmpty() && cycle == null) {
            Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            if (value is DataValue.Stale) Text("Showing previously retrieved data.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            metrics.forEach { metric ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(metric.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(metric.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
            cycle?.floors.orEmpty().forEach { floor ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Floor ${floor.floorNumber}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${floor.stars ?: "—"}/${floor.maxStars ?: "—"} ★", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        floor.leyLineDisorders.forEach { disorder ->
                            Text(disorder, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        floor.chambers.forEach { chamber ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Chamber ${chamber.chamberNumber}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    Text("${chamber.stars ?: "—"}/${chamber.maxStars ?: "—"} ★", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                                chamber.battles.forEach { battle ->
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text(battle.half, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                                            battle.completedAt?.let {
                                                Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        ChronicleRecordCharacterLineup(battle.characters)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
}

@Composable
private fun ChronicleCycleSelector(
    selectedIndex: Int,
    labels: List<String>,
    onSelected: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                Modifier.weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable(role = Role.Tab) { onSelected(index) }
                    .semantics { selected = isSelected }
                    .padding(horizontal = 8.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ChronicleRecordCharacterLineup(characters: List<ChronicleRecordCharacter>) {
    if (characters.isEmpty()) {
        Text("Character lineup was not included.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        characters.forEach { character ->
            Row(
                Modifier.widthIn(min = 112.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                if (character.iconUrl != null) {
                    AsyncImage(
                        model = character.iconUrl,
                        contentDescription = "${character.name ?: "Character"} portrait",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(character.name ?: "Character", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    val detail = listOfNotNull(character.element, character.level?.let { "Lv. $it" }).joinToString(" · ")
                    if (detail.isNotBlank()) {
                        Text(detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChronicleTheaterSection(
    title: String,
    value: DataValue<StructuredData>,
    roster: List<CharacterSnapshot>
) {
    val cycles = ChroniclePresentationMapper.theaterCycles(value, roster)
    var selectedCycle by rememberSaveable { mutableIntStateOf(0) }
    ChronicleSection(title, "Imaginarium Theater · recorded cycles and round lineups", collapsible = false) {
        if (cycles.isEmpty()) {
            Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            if (value is DataValue.Stale) Text("Showing previously retrieved data.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ChronicleCycleSelector(
                selectedIndex = selectedCycle,
                labels = listOf("Current Cycle", "Previous Cycle"),
                onSelected = { selectedCycle = it }
            )
            val cycle = cycles.getOrNull(selectedCycle)
            if (cycle == null) {
                Text(
                    if (selectedCycle == 0) "No current Theater cycle record was included."
                    else "No previous Theater cycle record was included.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (!cycle.hasData) {
                Text(
                    "No recorded performance is available for this cycle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(cycle.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    val metrics = listOfNotNull(
                        cycle.difficulty?.let { ChronicleMetric("Difficulty", it) },
                        cycle.medals?.let { ChronicleMetric("Medals", it) },
                        cycle.roundsCompleted?.let { ChronicleMetric("Rounds", it) },
                        cycle.coins?.let { ChronicleMetric("Fantasia Flowers", it) },
                        cycle.bonusCharacters?.let { ChronicleMetric("Bonus Characters", it) },
                        cycle.rentedCharacters?.let { ChronicleMetric("Rented Characters", it) }
                    )
                    metrics.forEach { metric ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(metric.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(metric.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (cycle.performanceMetrics.isNotEmpty()) {
                        Text("Performance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        cycle.performanceMetrics.forEach { metric ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(metric.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(metric.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    if (cycle.rounds.isNotEmpty()) {
                        Text("Round results", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        cycle.rounds.forEach { round ->
                            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Round ${round.roundNumber}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    round.completedAt?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                    round.medalEarned?.let {
                                        Text(if (it) "Medal earned" else "No medal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                ChronicleRecordCharacterLineup(round.characters)
                                if (round.buffs.isNotEmpty()) {
                                    Text("Blessings: ${round.buffs.joinToString(" · ")}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChronicleStygianOnslaughtSection(value: DataValue<StygianOnslaughtSnapshot>) {
    ChronicleSection("Stygian Onslaught", "Best recorded challenge", collapsible = false) {
        when (value) {
            is DataValue.Available -> StygianOnslaughtRecord(value.value, stale = false)
            is DataValue.Stale -> StygianOnslaughtRecord(value.value, stale = true)
            DataValue.Missing -> Text(
                "No Stygian Onslaught summary was included in this profile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DataValue.Unavailable -> Text(
                "Stygian Onslaught data is not available from the source.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            is DataValue.Failed -> Text(
                value.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StygianOnslaughtRecord(value: StygianOnslaughtSnapshot, stale: Boolean) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        value.bestRecordName?.let {
            Text(it, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text("Best Record", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        value.difficulty?.let {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Difficulty", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }
        if (stale) {
            Text("Showing previously retrieved data.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun chronicleAvailabilityText(value: DataValue<StructuredData>): String = when (value) {
    is DataValue.Available -> "No supported summary fields were found in this section."
    is DataValue.Stale -> "Showing previously retrieved data. No supported summary fields were found."
    DataValue.Missing -> "Not included in this account export."
    DataValue.Unavailable -> "Not available from the source."
    is DataValue.Failed -> value.reason
}
