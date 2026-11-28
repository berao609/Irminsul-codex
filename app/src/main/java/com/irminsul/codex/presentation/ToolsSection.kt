package com.irminsul.codex.presentation

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irminsul.codex.domain.model.CharacterSnapshot

/**
 * Presentation-only container for the Tools destination.
 * Calculator preferences and lineup selection state remain owned by the caller.
 */
@Composable
fun ToolsSection(
    characters: List<CharacterSnapshot>,
    preferences: SharedPreferences,
    showMaterialsPlanner: Boolean,
    showLineupSimulator: Boolean,
    selectedLineupCharacterIds: List<Int>,
    rosterIsStale: Boolean,
    rosterIsPartial: Boolean,
    onToggleMaterialsPlanner: () -> Unit,
    onToggleLineupSimulator: () -> Unit,
    onToggleLineupCharacter: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tools", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "Calculators and utilities for account progression.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AppIcon(AppIconName.TOOLS, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Enhancement Calculator", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Choose any 4-star or 5-star character or weapon and calculate upgrade materials",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onToggleMaterialsPlanner, modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp)) {
                    AppIcon(
                        if (showMaterialsPlanner) AppIconName.EXPAND_LESS else AppIconName.EXPAND_MORE,
                        contentDescription = if (showMaterialsPlanner) "Collapse Materials Calculator" else "Expand Materials Calculator",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }
        if (showMaterialsPlanner) {
            EnhancementCalculatorSection(
                characters = characters,
                preferences = preferences
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Lineup Simulator", fontWeight = FontWeight.SemiBold)
                    Text(
                        "${selectedLineupCharacterIds.size}/4 characters selected",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onToggleLineupSimulator, modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp)) {
                    AppIcon(
                        if (showLineupSimulator) AppIconName.EXPAND_LESS else AppIconName.EXPAND_MORE,
                        contentDescription = if (showLineupSimulator) "Collapse Lineup Simulator" else "Expand Lineup Simulator",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }
        }
        if (showLineupSimulator) {
            LineupSimulatorSection(
                characters = characters,
                selectedCharacterIds = selectedLineupCharacterIds,
                rosterIsStale = rosterIsStale,
                rosterIsPartial = rosterIsPartial,
                onToggleCharacter = onToggleLineupCharacter
            )
        }
    }
}
