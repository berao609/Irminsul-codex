package com.irminsul.codex.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily activity not loaded", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Sync your Daily Note to see Resin, commissions, expeditions, and Realm Currency.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onRefresh,
                        enabled = !isLoading && !isExporting,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Sync Daily Note") }
                }
            }
        }
        return
    }

    val resinProgress = if (note.currentResin != null && note.maxResin != null && note.maxResin > 0) {
        (note.currentResin.toFloat() / note.maxResin).coerceIn(0f, 1f)
    } else null

    Text(
        text = "Real-Time Notes",
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onBackground
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Original Resin",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${note.currentResin ?: "—"}",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "/ ${note.maxResin ?: "—"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (resinProgress != null) {
                    LinearProgressIndicator(
                        progress = { resinProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                Text(
                    text = when {
                        note.resinRecoveryTime == null -> "Recovery unavailable"
                        note.resinRecoveryTime > 0L -> "Full in ${formatDuration(note.resinRecoveryTime)}"
                        note.currentResin != null && note.maxResin != null && note.currentResin >= note.maxResin -> "Resin is full"
                        else -> "Recovery unavailable"
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                modifier = Modifier.weight(1.5f),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                DailyNoteMetricRow(
                    label = "Dispatched Expeditions",
                    value = "${note.currentExpeditionNum ?: "—"}/${note.maxExpeditionNum ?: "—"}"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DailyNoteMetricRow(
                    label = "Realm Currency",
                    value = "${note.currentHomeCoin ?: "—"}/${note.maxHomeCoin ?: "—"}"
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DailyNoteMetricRow(
                    label = "Daily Commission Rewards",
                    value = "${note.finishedTaskNum ?: "—"}/${note.totalTaskNum ?: "—"}",
                    status = when (note.isExtraTaskRewardReceived) {
                        true -> "All claimed"
                        false -> "Reward unclaimed"
                        null -> "Claim status unavailable"
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DailyNoteMetricRow(
                    label = "Weekly Resin Discounts",
                    value = "${note.remainResinDiscountNum ?: "—"}/${note.resinDiscountNumLimit ?: "—"}",
                    status = "Remaining"
                )
            }
        }
    }

    OutlinedButton(
        onClick = onRefresh,
        enabled = !isLoading && !isExporting,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(if (isLoading) "Refreshing..." else "Refresh Notes")
    }
}
