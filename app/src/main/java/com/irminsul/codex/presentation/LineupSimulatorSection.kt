package com.irminsul.codex.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.SubcomposeAsyncImage
import com.irminsul.codex.domain.analysis.LineupSelectionRules
import com.irminsul.codex.domain.model.CharacterSnapshot

/**
 * Presentation-only UI for arranging up to four owned characters.
 * Selection state and persistence remain owned by the caller.
 */
@Composable
fun LineupSimulatorSection(
    characters: List<CharacterSnapshot>,
    selectedCharacterIds: List<Int>,
    rosterIsStale: Boolean,
    rosterIsPartial: Boolean,
    onToggleCharacter: (Int) -> Unit
) {
    val selectedCharacters = selectedCharacterIds.mapNotNull { id ->
        characters.firstOrNull { it.id == id }
    }
    val elementSummary = selectedCharacters
        .groupingBy { it.element?.takeIf(String::isNotBlank) ?: "Unknown" }
        .eachCount()
        .entries
        .joinToString(" • ") { (element, count) -> "$element ×$count" }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Choose up to four characters you own. This sandbox helps you arrange a team; it does not yet rate combat performance or claim unsupported synergies.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (rosterIsStale) {
            Text(
                "This lineup uses a previously retrieved roster. Refresh account data before relying on it as current.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (rosterIsPartial) {
            Text(
                "The retrieved character roster is marked partial. Some owned characters may be missing.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(LineupSelectionRules.MAX_TEAM_SIZE) { index ->
                val character = selectedCharacters.getOrNull(index)
                Card(
                    modifier = Modifier.width(88.dp).heightIn(min = 126.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (character != null) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (character == null) {
                            Text("＋", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Slot ${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Empty", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            LineupArtwork(
                                url = character.avatarIcon,
                                description = "${character.name ?: "Character"} portrait",
                                fallback = lineupElementGlyph(character.element),
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                character.name ?: "Character #${character.id}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                            TextButton(
                                onClick = { onToggleCharacter(character.id) },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Remove", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
        if (selectedCharacters.isNotEmpty()) {
            Text(
                "Elements represented: $elementSummary",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            "Element counts are descriptive only. Role coverage, elemental reaction quality, rotations, and team synergy will require verified game knowledge before recommendations are shown.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (characters.isEmpty()) {
            Text(
                "Analyze your account to load your owned character roster.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text("Your roster", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                characters.forEach { character ->
                    val isSelected = character.id in selectedCharacterIds
                    val isFull = selectedCharacterIds.size >= LineupSelectionRules.MAX_TEAM_SIZE
                    Card(
                        onClick = {
                            if (isSelected || !isFull) onToggleCharacter(character.id)
                        },
                        modifier = Modifier.width(104.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            LineupArtwork(
                                url = character.avatarIcon,
                                description = "${character.name ?: "Character"} portrait",
                                fallback = lineupElementGlyph(character.element),
                                modifier = Modifier.size(58.dp)
                            )
                            Text(
                                character.name ?: "Character #${character.id}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                            Text(
                                when {
                                    isSelected -> "Selected"
                                    isFull -> "Team full"
                                    else -> "Tap to add"
                                },
                                fontSize = 10.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineupArtwork(
    url: String?,
    description: String,
    fallback: String,
    modifier: Modifier = Modifier
) {
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

private fun lineupElementGlyph(element: String?): String = when (element?.lowercase()) {
    "wind", "anemo" -> "🌪"
    "fire", "pyro" -> "🔥"
    "water", "hydro" -> "💧"
    "ice", "cryo" -> "❄"
    "electric", "electro" -> "⚡"
    "rock", "geo" -> "🪨"
    "grass", "dendro" -> "🌿"
    else -> "✦"
}
