package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.irminsul.codex.domain.model.AccountIntelligenceOverview

@Composable
fun VisualDashboardSection(report: AccountIntelligenceOverview?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CodexSpacing.md)
    ) {
        Text(
            text = "Account Intelligence",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (report == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.padding(CodexSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(CodexSpacing.xs)
                ) {
                    Text(
                        text = "Analysis not available yet",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Analyze your account in Visuals to see character assessments, recommendations, and confirmed gaps here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(CodexSpacing.lg),
                    verticalArrangement = Arrangement.spacedBy(CodexSpacing.lg)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(CodexSpacing.sm)) {
                        Text(
                            text = "Character assessment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        AssessmentBar(
                            "Needs attention",
                            report.needsAttentionCount,
                            report.assessedCharacterCount ?: 0
                        )
                        AssessmentBar(
                            "Review",
                            report.reviewCharacterCount,
                            report.assessedCharacterCount ?: 0
                        )
                        AssessmentBar(
                            "Clear",
                            report.clearCharacterCount,
                            report.assessedCharacterCount ?: 0
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CodexSpacing.xl)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(CodexSpacing.xxs)
                        ) {
                            Text(
                                text = report.recommendationCount.toString(),
                                style = MaterialTheme.typography.headlineSmall.merge(CodexTabularNumericStyle),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Recommendations",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(CodexSpacing.xxs)
                        ) {
                            Text(
                                text = report.confirmedAccountGapCount.toString(),
                                style = MaterialTheme.typography.headlineSmall.merge(CodexTabularNumericStyle),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Confirmed gaps",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
