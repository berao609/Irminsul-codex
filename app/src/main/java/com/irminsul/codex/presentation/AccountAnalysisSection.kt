package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Account Analysis",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "One balanced review of your available account data. Recommendations keep a consistent priority order based on confirmed issues, stat discrepancies, build warnings, and information gaps.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAnalyzeAccount,
                enabled = !isExporting,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) {
                Text(
                    when {
                        isExporting -> "Retrieving account..."
                        isRestoringSnapshot -> "Restoring saved analysis..."
                        !hasSnapshot -> "Analyze Account"
                        else -> "Refresh Analysis"
                    }
                )
            }

            if (report == null && !isAnalyzing &&
                !isRestoringSnapshot &&
                errorMessage == null
            ) {
                Text(
                    text = "Run an account analysis here. This is separate from the optional Save JSON export.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isAnalyzing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Analyzing account data...", fontSize = 12.sp)
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            report?.let { overview ->
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Text(
                    text = "Character assessment",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Assessed: ${overview.assessedCharacterCount ?: "Unavailable"} • Need attention: ${overview.needsAttentionCount} • Review: ${overview.reviewCharacterCount} • Clear: ${overview.clearCharacterCount}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Development recommendations (${overview.recommendationCount})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (overview.goalAwareDevelopment.recommendations.isEmpty()) {
                    Text(
                        text = if (overview.goalAwareDevelopment.availability.name in listOf("AVAILABLE", "STALE")) {
                            "No recommendations were identified by the available analysis."
                        } else {
                            "Recommendations are not available from the current character data."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val recommendations = overview.goalAwareDevelopment.recommendations
                    val visibleRecommendations = if (showAllRecommendations) {
                        recommendations
                    } else {
                        recommendations.take(5)
                    }
                    visibleRecommendations.forEach { item ->
                        val character = characters.firstOrNull { it.id == item.recommendation.characterId }
                        CharacterRecommendationCard(
                            item = item,
                            character = character,
                            expanded = showAllRecommendations
                        )
                    }
                    if (overview.recommendationCount > 5) {
                        Text(
                            text = if (showAllRecommendations) {
                                "Showing all ${overview.recommendationCount} recommendations."
                            } else {
                                "Showing 5 of ${overview.recommendationCount} recommendations."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        androidx.compose.material3.TextButton(
                            onClick = { onShowAllRecommendationsChange(!showAllRecommendations) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (showAllRecommendations) "Show fewer recommendations" else "Show all recommendations")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Text(
                    text = "Account gaps: ${overview.confirmedAccountGapCount} confirmed • ${overview.accountDataReviewCount} data review",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Account data: ${overview.presentAccountSectionCount}/${overview.accountSectionCount} sections present • ${overview.missingAccountSectionCount} missing • ${overview.unavailableAccountSectionCount} unavailable • ${overview.failedAccountSectionCount} failed",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (overview.hasStaleEvidence) {
                    Text(
                        text = "Some findings rely on stale account evidence.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (overview.hasUnverifiedSources) {
                    Text(
                        text = "One or more analysis sources are missing, unavailable, or failed. Treat this overview as partial.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (overview.hasDataConfidenceConcerns) {
                    Text(
                        text = "Account data coverage or freshness has unresolved concerns.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
