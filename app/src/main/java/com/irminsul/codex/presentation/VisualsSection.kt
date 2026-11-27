package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.irminsul.codex.domain.model.AccountIntelligenceOverview
import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.model.DataValue

@Composable
fun VisualsSection(
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
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CodexSpacing.xl)) {
        snapshot?.let { account ->
            Text("Account Chronicle", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            ChronicleOverviewSection(account.accountCard, account.profileAndExploration)
        }

        val rosterValue = snapshot?.characters
        val roster = when (rosterValue) {
            is DataValue.Available -> rosterValue.value
            is DataValue.Stale -> rosterValue.value
            else -> emptyList()
        }
        if (roster.isNotEmpty()) {
            CharacterShowcaseSection(roster, selectedCharacterId, onCharacterSelected)
        } else if (snapshot != null) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Character Showcase", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    when (rosterValue) {
                        DataValue.Missing -> "The character roster was not included in this export."
                        DataValue.Unavailable -> "Character data is not available from the source."
                        is DataValue.Failed -> "Character retrieval failed: ${rosterValue.reason}"
                        is DataValue.Stale -> "No characters are available in the saved roster."
                        else -> "Run Analyze Account to retrieve your character roster and open detailed character profiles here."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        snapshot?.let { account ->
            ChronicleExplorationSection(account.profileAndExploration)
            ChronicleRecordSection("Spiral Abyss · Current Cycle", account.spiralAbyssCurrent, ChronicleRecordMode.SPIRAL_ABYSS)
            ChronicleRecordSection("Spiral Abyss · Previous Cycle", account.spiralAbyssPrevious, ChronicleRecordMode.SPIRAL_ABYSS)
            ChronicleRecordSection("Imaginarium Theater", account.imaginariumTheater, ChronicleRecordMode.IMAGINARIUM_THEATER)
            ChronicleStygianOnslaughtSection(account.stygianOnslaught)
        }

        if (hasCredentials) {
            AccountAnalysisSection(
                report = intelligenceOverview,
                characters = roster,
                isExporting = isExporting,
                isRestoringSnapshot = isRestoringSnapshot,
                isAnalyzing = isAnalyzing,
                errorMessage = errorMessage,
                hasSnapshot = snapshot != null,
                showAllRecommendations = showAllRecommendations,
                onShowAllRecommendationsChange = onShowAllRecommendationsChange,
                onAnalyzeAccount = onAnalyzeAccount
            )
        }
    }
}
