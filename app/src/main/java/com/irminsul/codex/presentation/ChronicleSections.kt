package com.irminsul.codex.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData
import com.irminsul.codex.domain.model.StygianOnslaughtSnapshot

@Composable
private fun ChronicleSection(title: String, subtitle: String? = null, content: @Composable () -> Unit) {
    var expanded by remember(title) { mutableStateOf(true) }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth().clickable(role = Role.Button) { expanded = !expanded }.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                subtitle?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Icon(if (expanded) AppIconName.EXPAND_LESS else AppIconName.EXPAND_MORE, contentDescription = if (expanded) "Collapse $title" else "Expand $title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (expanded) content()
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
                    Icon(
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
fun ChronicleExplorationSection(value: DataValue<StructuredData>) {
    val regions = ChroniclePresentationMapper.explorationRegions(value)
    var showAllRegions by remember(value) { mutableStateOf(false) }
    ChronicleSection("World Exploration", "Regional completion") {
        if (regions.isEmpty()) {
            Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            if (showAllRegions) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    regions.forEach { region -> RegionDetailCard(region) }
                }
            } else {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    regions.forEach { region -> RegionOverviewCard(region) }
                }
            }
            TextButton(onClick = { showAllRegions = !showAllRegions }, modifier = Modifier.fillMaxWidth()) {
                Text(if (showAllRegions) "Show fewer regions" else "View more regions")
            }
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
private fun RegionDetailCard(region: ChronicleRegion) {
    Row(
        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RegionEmblem(region, Modifier.size(56.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(region.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            LinearProgressIndicator(region.progress, Modifier.fillMaxWidth().height(6.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outlineVariant)
        }
        Text(region.percentageLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
fun ChronicleRecordSection(title: String, value: DataValue<StructuredData>, mode: ChronicleRecordMode) {
    if (mode == ChronicleRecordMode.IMAGINARIUM_THEATER) {
        ChronicleTheaterSection(title, value)
        return
    }
    val metrics = ChroniclePresentationMapper.recordMetrics(value, mode)
    val primaryMetric = metrics.firstOrNull { it.label == "Best Floor" }
        ?: metrics.firstOrNull { it.label == "Stars" }
    ChronicleSection(title, "Spiral Abyss battle record") {
        if (metrics.isEmpty()) {
            Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            primaryMetric?.let {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(it.value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(it.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Recorded result", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            metrics.filterNot { it == primaryMetric }.forEach { metric ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(metric.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(metric.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ChronicleTheaterSection(title: String, value: DataValue<StructuredData>) {
    val cycles = ChroniclePresentationMapper.theaterCycles(value)
    ChronicleSection(title, "Imaginarium Theater · recorded cycles") {
        if (cycles.isEmpty()) {
            Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            cycles.forEachIndexed { index, cycle ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    if (cycle.rounds.isNotEmpty()) {
                        Text("Round results", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        cycle.rounds.forEach { round ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Round ${round.roundNumber}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    round.medalEarned?.let {
                                        Text(if (it) "Medal earned" else "No medal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                if (round.characters.isNotEmpty()) {
                                    Text(round.characters.joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    ChronicleSection("Stygian Onslaught", "Best recorded challenge") {
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
