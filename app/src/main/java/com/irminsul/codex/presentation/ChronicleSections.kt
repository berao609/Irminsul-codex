package com.irminsul.codex.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData

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
            Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = if (expanded) "Collapse $title" else "Expand $title", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (expanded) content()
    }
}

@Composable
fun ChronicleOverviewSection(accountCard: DataValue<StructuredData>, profileAndExploration: DataValue<StructuredData>) {
    val overview = ChroniclePresentationMapper.accountOverview(accountCard, profileAndExploration)
    val identity = overview.filter { it.label == "Player" || it.label == "Adventure Rank" }
    val statistics = overview.filterNot { it.label == "Player" || it.label == "Adventure Rank" }
    ChronicleSection("Account Overview", "Your journey and collection at a glance") {
        if (overview.isEmpty()) {
            Text(chronicleAvailabilityText(profileAndExploration), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            if (identity.isNotEmpty()) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
            if (statistics.isNotEmpty()) {
                Text("Progress & collectibles", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                statistics.chunked(3).forEach { row ->
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

@Composable
fun ChronicleExplorationSection(value: DataValue<StructuredData>) {
    val regions = ChroniclePresentationMapper.explorationRegions(value)
    ChronicleSection("World Exploration", "Regional completion") {
        if (regions.isEmpty()) {
            Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                regions.forEachIndexed { index, region ->
                    val alternate = index % 2 != 0
                    val foreground = if (alternate) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                    Column(
                        Modifier.width(196.dp).background(
                            if (alternate) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(14.dp)
                        ).padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(region.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = foreground, maxLines = 2, minLines = 2)
                        Text(region.percentageLabel, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = foreground)
                        LinearProgressIndicator(region.progress, Modifier.fillMaxWidth().height(6.dp), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.outlineVariant)
                        Text("Main areas", style = MaterialTheme.typography.labelSmall, color = if (alternate) foreground else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ChronicleRecordSection(title: String, value: DataValue<StructuredData>, mode: ChronicleRecordMode) {
    val metrics = ChroniclePresentationMapper.recordMetrics(value, mode)
    val stars = metrics.firstOrNull { it.label == "Stars" }
    ChronicleSection(title, when (mode) {
        ChronicleRecordMode.SPIRAL_ABYSS -> "Spiral Abyss battle record"
        ChronicleRecordMode.IMAGINARIUM_THEATER -> "Imaginarium Theater record"
    }) {
        if (metrics.isEmpty()) {
            Text(chronicleAvailabilityText(value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            stars?.let {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(it.value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(it.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Recorded result", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            val supporting = metrics.filterNot { it == stars }
            if (supporting.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                supporting.forEach { metric ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(metric.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(metric.value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
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
