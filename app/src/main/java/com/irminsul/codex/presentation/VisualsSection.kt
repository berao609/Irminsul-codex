package com.irminsul.codex.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.irminsul.codex.domain.model.AccountIntelligenceOverview
import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData
import com.irminsul.codex.domain.model.StygianOnslaughtSnapshot

@Composable
internal fun VisualsSection(
    modifier: Modifier = Modifier,
    destination: VisualsDestination,
    onNavigate: (VisualsDestination) -> Unit,
    onBack: () -> Unit,
    snapshot: AccountSnapshot?,
    intelligenceOverview: AccountIntelligenceOverview?,
    isExporting: Boolean,
    isRestoringSnapshot: Boolean,
    isAnalyzing: Boolean,
    errorMessage: String?,
    hasCredentials: Boolean,
    selectedCharacterId: Int?,
    onCharacterSelected: (Int) -> Unit,
    showAllRecommendations: Boolean,
    onShowAllRecommendationsChange: (Boolean) -> Unit,
    onAnalyzeAccount: () -> Unit
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    saveableStateHolder.SaveableStateProvider(destination.name) {
        Column(modifier = modifier) {
            if (destination != VisualsDestination.OVERVIEW) {
                VisualsPageHeader(title = destination.title, onBack = onBack)
            }
            Column(
                modifier = Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(CodexSpacing.xl)
            ) {
                when (destination) {
                    VisualsDestination.OVERVIEW -> {
                        Text(
                            "Battle Chronicles",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        snapshot?.let { account ->
                            ChronicleOverviewSection(account.accountCard, account.profileAndExploration)
                        }

                        val rosterValue = snapshot?.characters
                        val roster = rosterValue.charactersOrEmpty()
                        CharacterShowcasePreview(
                            characters = roster,
                            selectedCharacterId = selectedCharacterId,
                            onCharacterSelected = onCharacterSelected,
                            onOpenCharacter = { characterId ->
                                onCharacterSelected(characterId)
                                onNavigate(VisualsDestination.CHARACTER_DETAILS)
                            },
                            onViewAll = { onNavigate(VisualsDestination.CHARACTER_ROSTER) },
                            emptyMessage = if (snapshot == null) null else rosterAvailabilityText(rosterValue)
                        )

                        snapshot?.let { account ->
                            ChronicleExplorationSection(
                                value = account.profileAndExploration,
                                compact = true,
                                onViewMore = { onNavigate(VisualsDestination.WORLD_EXPLORATION) }
                            )
                            ChronicleModePreview(
                                title = "Spiral Abyss",
                                subtitle = "Current cycle",
                                summary = abyssPreview(account.spiralAbyssCurrent),
                                onClick = { onNavigate(VisualsDestination.SPIRAL_ABYSS_CURRENT) }
                            )
                            ChronicleModePreview(
                                title = "Spiral Abyss",
                                subtitle = "Previous cycle",
                                summary = abyssPreview(account.spiralAbyssPrevious),
                                onClick = { onNavigate(VisualsDestination.SPIRAL_ABYSS_PREVIOUS) }
                            )
                            ChronicleModePreview(
                                title = "Imaginarium Theater",
                                subtitle = "Recorded performances",
                                summary = theaterPreview(account.imaginariumTheater),
                                onClick = { onNavigate(VisualsDestination.IMAGINARIUM_THEATER) }
                            )
                        }
                        ChronicleModePreview(
                            title = "Stygian Onslaught",
                            subtitle = "Best recorded challenge",
                            summary = stygianPreview(snapshot?.stygianOnslaught ?: DataValue.Missing),
                            onClick = { onNavigate(VisualsDestination.STYGIAN_ONSLAUGHT) }
                        )

                        if (hasCredentials) {
                            AccountAnalysisPreview(
                                report = intelligenceOverview,
                                isAnalyzing = isAnalyzing,
                                isExporting = isExporting,
                                isRestoringSnapshot = isRestoringSnapshot,
                                onClick = { onNavigate(VisualsDestination.ACCOUNT_ANALYSIS) },
                                onAnalyze = onAnalyzeAccount
                            )
                        }
                        errorMessage?.let { ScreenErrorBanner(it) }
                    }

                    VisualsDestination.CHARACTER_ROSTER -> {
                        val rosterValue = snapshot?.characters
                        val roster = rosterValue.charactersOrEmpty()
                        if (roster.isEmpty()) {
                            Text(
                                if (snapshot == null) "Analyze your account to load the character roster."
                                else rosterAvailabilityText(rosterValue),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                CharacterShowcaseSection(
                                    characters = roster,
                                    selectedCharacterId = selectedCharacterId,
                                    onCharacterSelected = onCharacterSelected,
                                    initialRosterExpanded = true,
                                    showSelectedDetail = false
                                )
                                TextButton(
                                    onClick = { onNavigate(VisualsDestination.CHARACTER_DETAILS) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View Selected Character")
                                }
                            }
                        }
                    }

                    VisualsDestination.CHARACTER_DETAILS -> {
                        val availableCharacters = snapshot?.characters?.charactersOrEmpty() ?: emptyList()
                        val character = availableCharacters.firstOrNull { it.id == selectedCharacterId }
                            ?: availableCharacters.firstOrNull()
                        if (character == null) {
                            Text("Select a character from your roster to view its details.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            TextButton(onClick = { onNavigate(VisualsDestination.CHARACTER_ROSTER) }) {
                                Text("Open Character Roster")
                            }
                        } else {
                            CharacterDetailSelector(
                                characters = availableCharacters,
                                selectedCharacterId = character.id,
                                onCharacterSelected = onCharacterSelected
                            )
                            CharacterShowcaseDetail(character)
                        }
                    }

                    VisualsDestination.WORLD_EXPLORATION -> {
                        snapshot?.let {
                            ChronicleExplorationSection(it.profileAndExploration, compact = false)
                        } ?: Text("Analyze your account to load World Exploration.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    VisualsDestination.SPIRAL_ABYSS_CURRENT -> {
                        snapshot?.let {
                            ChronicleRecordSection("Current Cycle", it.spiralAbyssCurrent, ChronicleRecordMode.SPIRAL_ABYSS)
                        } ?: Text("Analyze your account to load Spiral Abyss records.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    VisualsDestination.SPIRAL_ABYSS_PREVIOUS -> {
                        snapshot?.let {
                            ChronicleRecordSection("Previous Cycle", it.spiralAbyssPrevious, ChronicleRecordMode.SPIRAL_ABYSS)
                        } ?: Text("Analyze your account to load Spiral Abyss records.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    VisualsDestination.IMAGINARIUM_THEATER -> {
                        snapshot?.let {
                            ChronicleRecordSection("Imaginarium Theater", it.imaginariumTheater, ChronicleRecordMode.IMAGINARIUM_THEATER)
                        } ?: Text("Analyze your account to load Imaginarium Theater records.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    VisualsDestination.STYGIAN_ONSLAUGHT -> {
                        ChronicleStygianOnslaughtSection(snapshot?.stygianOnslaught ?: DataValue.Missing)
                    }

                    VisualsDestination.ACCOUNT_ANALYSIS -> {
                        if (hasCredentials) {
                            AccountAnalysisSection(
                                report = intelligenceOverview,
                                characters = snapshot?.characters?.charactersOrEmpty() ?: emptyList(),
                                isExporting = isExporting,
                                isRestoringSnapshot = isRestoringSnapshot,
                                isAnalyzing = isAnalyzing,
                                errorMessage = errorMessage,
                                hasSnapshot = snapshot != null,
                                showAllRecommendations = showAllRecommendations,
                                onShowAllRecommendationsChange = onShowAllRecommendationsChange,
                                onAnalyzeAccount = onAnalyzeAccount
                            )
                        } else {
                            Text("Sign in with HoYoLAB to analyze your account.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualsPageHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onBack) {
            AppIcon(AppIconName.BACK, contentDescription = "Back")
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun CharacterShowcasePreview(
    characters: List<CharacterSnapshot>,
    selectedCharacterId: Int?,
    onCharacterSelected: (Int) -> Unit,
    onOpenCharacter: (Int) -> Unit,
    onViewAll: () -> Unit,
    emptyMessage: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("My Characters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onViewAll, enabled = characters.isNotEmpty()) { Text("View All") }
        }
        if (characters.isEmpty()) {
            if (emptyMessage != null) {
                Text(emptyMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text("Your character collection will appear here after account retrieval.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            val selected = characters.firstOrNull { it.id == selectedCharacterId } ?: characters.first()
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                sortCharactersByRelease(characters).forEach { character ->
                    val active = character.id == selected.id
                    Column(
                        modifier = Modifier.width(58.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { onOpenCharacter(character.id) }
                            .padding(5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        AsyncImage(
                            model = character.avatarIcon,
                            contentDescription = "${character.name ?: "Character"} portrait",
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(9.dp))
                        )
                        Text(character.name ?: "Character #${character.id}", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
            Card(
                onClick = { onOpenCharacter(selected.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = selected.avatarIcon,
                        contentDescription = "${selected.name ?: "Character"} portrait",
                        modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(selected.name ?: "Character #${selected.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${selected.element ?: "Element unavailable"} · Level ${selected.level ?: "—"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Friendship ${selected.friendshipLevel ?: "—"} · ${selected.activeConstellationCount ?: "—"}/6 constellations",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AppIcon(AppIconName.CHEVRON_RIGHT, contentDescription = "Open character details")
                }
            }
        }
    }
}


@Composable
private fun CharacterDetailSelector(
    characters: List<CharacterSnapshot>,
    selectedCharacterId: Int,
    onCharacterSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        sortCharactersByRelease(characters).forEach { character ->
            val selected = character.id == selectedCharacterId
            Column(
                modifier = Modifier.width(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onCharacterSelected(character.id) }
                    .padding(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AsyncImage(
                    model = character.avatarIcon,
                    contentDescription = "${character.name ?: "Character"} portrait",
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(9.dp))
                )
                Text(character.name ?: "Character #${character.id}", style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}

@Composable
private fun ChronicleModePreview(
    title: String,
    subtitle: String,
    summary: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(summary, style = MaterialTheme.typography.bodyMedium)
            }
            AppIcon(AppIconName.CHEVRON_RIGHT, contentDescription = "View $title details")
        }
    }
}

@Composable
private fun AccountAnalysisPreview(
    report: AccountIntelligenceOverview?,
    isAnalyzing: Boolean,
    isExporting: Boolean,
    isRestoringSnapshot: Boolean,
    onClick: () -> Unit,
    onAnalyze: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Account Analysis", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    report?.let { "${it.assessedCharacterCount ?: "—"} assessed · ${it.recommendationCount} recommendations" }
                        ?: when {
                            isAnalyzing -> "Analyzing account data..."
                            isRestoringSnapshot -> "Restoring saved analysis..."
                            else -> "Review character development priorities and account coverage."
                        },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text("View analysis", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }
        Button(onClick = onAnalyze, enabled = !isExporting && !isAnalyzing, modifier = Modifier.fillMaxWidth()) {
            Text(
                when {
                    isExporting -> "Retrieving account..."
                    isAnalyzing -> "Analyzing account..."
                    isRestoringSnapshot -> "Restoring analysis..."
                    report == null -> "Analyze Account"
                    else -> "Refresh Analysis"
                }
            )
        }
    }
}

private fun DataValue<List<CharacterSnapshot>>?.charactersOrEmpty(): List<CharacterSnapshot> = when (this) {
    is DataValue.Available -> value
    is DataValue.Stale -> value
    else -> emptyList()
}

private fun rosterAvailabilityText(value: DataValue<List<CharacterSnapshot>>?): String = when (value) {
    DataValue.Missing -> "The character roster was not included in this export."
    DataValue.Unavailable -> "Character data is not available from the source."
    is DataValue.Failed -> "Character retrieval failed: ${value.reason}"
    is DataValue.Stale -> if (value.value.isEmpty()) "No characters are available in the saved roster." else "Showing previously retrieved characters."
    is DataValue.Available -> if (value.value.isEmpty()) "No characters were returned by the source." else "Character data is available."
    null -> "Character data is not available yet."
}

private fun abyssPreview(value: DataValue<StructuredData>): String {
    val metrics = ChroniclePresentationMapper.recordMetrics(value, ChronicleRecordMode.SPIRAL_ABYSS)
    val primary = metrics.firstOrNull { it.label == "Best Floor" } ?: metrics.firstOrNull { it.label == "Stars" }
    return primary?.let { "${it.value} ${it.label}" } ?: structuredAvailability(value)
}

private fun theaterPreview(value: DataValue<StructuredData>): String {
    val cycle = ChroniclePresentationMapper.theaterCycles(value).firstOrNull()
        ?: return structuredAvailability(value)
    val details = listOfNotNull(
        cycle.title,
        cycle.roundsCompleted?.let { "$it rounds" },
        cycle.medals?.let { "$it medals" }
    )
    return details.joinToString(" · ")
}

private fun structuredAvailability(value: DataValue<StructuredData>): String = when (value) {
    is DataValue.Available -> "No supported summary fields were found."
    is DataValue.Stale -> "Showing previously retrieved data."
    DataValue.Missing -> "Not included in this account export."
    DataValue.Unavailable -> "Not available from the source."
    is DataValue.Failed -> value.reason
}

private fun stygianPreview(value: DataValue<StygianOnslaughtSnapshot>): String = when (value) {
    is DataValue.Available -> listOfNotNull(
        value.value.bestRecordName,
        value.value.difficulty?.let { "Difficulty $it" }
    ).takeIf { it.isNotEmpty() }?.joinToString(" · ") ?: "No supported record fields were found."
    is DataValue.Stale -> listOfNotNull(
        value.value.bestRecordName,
        value.value.difficulty?.let { "Difficulty $it" },
        "Previously retrieved"
    ).joinToString(" · ")
    DataValue.Missing -> "Not included in this account export."
    DataValue.Unavailable -> "Not available from the source."
    is DataValue.Failed -> value.reason
}
