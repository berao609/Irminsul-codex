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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData

@Composable
private fun ChronicleSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    var expanded by remember(title) { mutableStateOf(true) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse $title" else "Expand $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) content()
        }
    }
}

@Composable
fun ChronicleOverviewSection(
    accountCard: DataValue<StructuredData>,
    profileAndExploration: DataValue<StructuredData>
) {
    val overview = ChroniclePresentationMapper.accountOverview(accountCard, profileAndExploration)
    ChronicleSectionCard("Account Overview") {
        if (overview.isEmpty()) {
            Text(chronicleAvailabilityText(profileAndExploration), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            overview.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { metric ->
                        Column(
                            modifier = Modifier.weight(1f).background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(10.dp)
                            ).padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(metric.label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(metric.value, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ChronicleExplorationSection(value: DataValue<StructuredData>) {
    val regions = ChroniclePresentationMapper.explorationRegions(value)
    ChronicleSectionCard("World Exploration & Collectibles") {
        if (regions.isEmpty()) {
            Text(chronicleAvailabilityText(value), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                regions.forEach { region ->
                    Card(
                        modifier = Modifier.width(184.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(region.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                            Text(region.percentageLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(
                                progress = region.progress,
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text("Main Areas Only", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChronicleRecordSection(title: String, value: DataValue<StructuredData>, mode: ChronicleRecordMode) {
    val metrics = ChroniclePresentationMapper.recordMetrics(value, mode)
    ChronicleSectionCard(title) {
        if (metrics.isEmpty()) {
            Text(chronicleAvailabilityText(value), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            metrics.forEach { metric ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(metric.label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(metric.value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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

