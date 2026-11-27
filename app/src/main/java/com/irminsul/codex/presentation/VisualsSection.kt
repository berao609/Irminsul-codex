package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irminsul.codex.domain.model.AccountIntelligenceOverview
import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.model.CharacterSnapshot
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
    snapshot?.let { account ->
        Text(
            text = "Account Chronicle",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        ChronicleOverviewSection(account.accountCard, account.profileAndExploration)
        ChronicleExplorationSection(account.profileAndExploration)
        ChronicleRecordSection(
            "Spiral Abyss · Current Cycle",
            account.spiralAbyssCurrent,
            ChronicleRecordMode.SPIRAL_ABYSS
        )
        ChronicleRecordSection(
            "Spiral Abyss · Previous Cycle",
            account.spiralAbyssPrevious,
            ChronicleRecordMode.SPIRAL_ABYSS
        )
        ChronicleRecordSection(
            "Imaginarium Theater",
            account.imaginariumTheater,
            ChronicleRecordMode.IMAGINARIUM_THEATER
        )
    }

    val roster = when (val value = snapshot?.characters) {
        is DataValue.Available -> value.value
        is DataValue.Stale -> value.value
        else -> emptyList()
    }
    if (roster.isNotEmpty()) {
        CharacterShowcaseSection(
            characters = roster,
            selectedCharacterId = selectedCharacterId,
            onCharacterSelected = onCharacterSelected
        )
    } else {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Character Showcase", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Run Analyze Account to retrieve your character roster and open detailed character profiles here.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (hasCredentials) {
        AccountAnalysisSection(
            report = intelligenceOverview,
            characters = (snapshot?.characters as? DataValue.Available)?.value.orEmpty(),
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
