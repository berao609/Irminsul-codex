package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.irminsul.codex.api.DailyNote

@Composable
fun HomeDailyNoteSection(
    note: DailyNote?,
    isLoading: Boolean,
    canSync: Boolean,
    isExporting: Boolean,
    onRefresh: () -> Unit
) {
    if (note == null) {
        if (!isLoading && canSync) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CodexSpacing.md)
            ) {
                HomeSectionHeading(title = "Real-Time Notes")
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(CodexSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(CodexSpacing.sm)
                    ) {
                        Text("Daily activity not loaded", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Sync to view Resin, commissions, expeditions, and Realm Currency.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = onRefresh, enabled = !isLoading && !isExporting) {
                            Text("Sync Daily Note")
                        }
                    }
                }
            }
        }
        return
    }

    val resinProgress = if (note.currentResin != null && note.maxResin != null && note.maxResin > 0) {
        (note.currentResin.toFloat() / note.maxResin).coerceIn(0f, 1f)
    } else null

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CodexSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HomeSectionHeading(title = "Real-Time Notes")
            TextButton(onClick = onRefresh, enabled = !isLoading && !isExporting) {
                Text(if (isLoading) "Refreshing…" else "Refresh")
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(CodexSpacing.md),
                verticalArrangement = Arrangement.spacedBy(CodexSpacing.lg)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier.padding(CodexSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(CodexSpacing.xs)
                    ) {
                        Text(
                            text = "ORIGINAL RESIN",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(CodexSpacing.xs)
                        ) {
                            Text(
                                text = note.currentResin?.toString() ?: "—",
                                style = MaterialTheme.typography.displaySmall.merge(CodexTabularNumericStyle),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "/ " + (note.maxResin?.toString() ?: "—"),
                                style = MaterialTheme.typography.titleMedium.merge(CodexTabularNumericStyle),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        if (resinProgress != null) {
                            LinearProgressIndicator(
                                progress = { resinProgress },
                                modifier = Modifier.fillMaxWidth().height(7.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )
                        }
                        Text(
                            text = when {
                                note.resinRecoveryTime == null -> "Recovery unavailable"
                                note.resinRecoveryTime > 0L -> "Full in " + formatDuration(note.resinRecoveryTime)
                                note.currentResin != null && note.maxResin != null && note.currentResin >= note.maxResin -> "Resin is full"
                                else -> "Recovery unavailable"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(CodexSpacing.md)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CodexSpacing.lg)
                    ) {
                        HomeDailyNoteMetric(
                            modifier = Modifier.weight(1f),
                            label = "Expeditions",
                            value = (note.currentExpeditionNum?.toString() ?: "—") + "/" + (note.maxExpeditionNum?.toString() ?: "—")
                        )
                        HomeDailyNoteMetric(
                            modifier = Modifier.weight(1f),
                            label = "Realm Currency",
                            value = (note.currentHomeCoin?.toString() ?: "—") + "/" + (note.maxHomeCoin?.toString() ?: "—")
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(CodexSpacing.lg)
                    ) {
                        HomeDailyNoteMetric(
                            modifier = Modifier.weight(1f),
                            label = "Daily Commissions",
                            value = (note.finishedTaskNum?.toString() ?: "—") + "/" + (note.totalTaskNum?.toString() ?: "—"),
                            status = when (note.isExtraTaskRewardReceived) {
                                true -> "All claimed"
                                false -> "Reward unclaimed"
                                null -> "Claim status unavailable"
                            }
                        )
                        HomeDailyNoteMetric(
                            modifier = Modifier.weight(1f),
                            label = "Weekly Discounts",
                            value = (note.remainResinDiscountNum?.toString() ?: "—") + "/" + (note.resinDiscountNumLimit?.toString() ?: "—"),
                            status = "Remaining"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeSectionHeading(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun HomeDailyNoteMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    status: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CodexSpacing.xxs)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.merge(CodexTabularNumericStyle),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        status?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
