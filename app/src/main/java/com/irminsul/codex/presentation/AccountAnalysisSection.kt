package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.irminsul.codex.domain.model.AccountIntelligenceOverview
import com.irminsul.codex.domain.model.CharacterSnapshot

@Composable
fun AccountAnalysisSection(
    report: AccountIntelligenceOverview?,
    characters: List<CharacterSnapshot>,
    isExporting: Boolean,
    isRestoringSnapshot: Boolean,
    isAnalyzing: Boolean,
    errorMessage: String?,
    hasSnapshot: Boolean,
    showAllRecommendations: Boolean,
    onShowAllRecommendationsChange: (Boolean) -> Unit,
    onAnalyzeAccount: () -> Unit
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(CodexSpacing.md)) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Account Analysis", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
            Text("Evidence-backed priorities from your retrieved account data.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(onClick = onAnalyzeAccount, enabled = !isExporting, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Text(when {
                isExporting -> "Retrieving account..."
                isRestoringSnapshot -> "Restoring saved analysis..."
                !hasSnapshot -> "Analyze Account"
                else -> "Refresh Analysis"
            })
        }
        if (report == null && !isAnalyzing && !isRestoringSnapshot && errorMessage == null) {
            Text("Analysis is separate from the optional JSON export.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (isAnalyzing) {
            LinearProgressIndicator(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
            Text("Analyzing account data...", style = MaterialTheme.typography.bodySmall)
        }
        errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

        report?.let { overview ->
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Character assessment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AnalysisCount("Assessed", overview.assessedCharacterCount?.toString() ?: "—")
                AnalysisCount("Attention", overview.needsAttentionCount.toString())
                AnalysisCount("Review", overview.reviewCharacterCount.toString())
                AnalysisCount("Clear", overview.clearCharacterCount.toString())
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Development recommendations · ${overview.recommendationCount}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (overview.goalAwareDevelopment.recommendations.isEmpty()) {
                Text(
                    if (overview.goalAwareDevelopment.availability.name in listOf("AVAILABLE", "STALE")) "No recommendations were identified by the available analysis."
                    else "Recommendations are not available from the current character data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val recommendations = overview.goalAwareDevelopment.recommendations
                val visible = if (showAllRecommendations) recommendations else recommendations.take(5)
                visible.forEach { item ->
                    CharacterRecommendationCard(
                        item = item,
                        character = characters.firstOrNull { it.id == item.recommendation.characterId },
                        expanded = showAllRecommendations
                    )
                }
                if (overview.recommendationCount > 5) {
                    Text(
                        if (showAllRecommendations) "Showing all ${overview.recommendationCount} recommendations."
                        else "Showing 5 of ${overview.recommendationCount} recommendations.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { onShowAllRecommendationsChange(!showAllRecommendations) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text(
                            if (showAllRecommendations) "Show fewer" else "Show all recommendations",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text("Account coverage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${overview.confirmedAccountGapCount} confirmed gaps · ${overview.accountDataReviewCount} data reviews", style = MaterialTheme.typography.bodyMedium)
            Text(
                "${overview.presentAccountSectionCount}/${overview.accountSectionCount} sections present · ${overview.missingAccountSectionCount} missing · ${overview.unavailableAccountSectionCount} unavailable · ${overview.failedAccountSectionCount} failed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (overview.hasStaleEvidence) Text("Some findings rely on stale account evidence.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            if (overview.hasUnverifiedSources) Text("Some sources are missing, unavailable, or failed. Treat this overview as partial.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            if (overview.hasDataConfidenceConcerns) Text("Account data coverage or freshness has unresolved concerns.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AnalysisCount(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
