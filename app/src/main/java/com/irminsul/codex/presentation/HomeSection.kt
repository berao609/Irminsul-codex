package com.irminsul.codex.presentation

import androidx.compose.runtime.Composable
import com.irminsul.codex.api.DailyNote
import com.irminsul.codex.domain.model.AccountIntelligenceOverview

@Composable
fun HomeSection(
    uid: String,
    serverName: String,
    rosterCount: Int?,
    exportedAt: String?,
    hasSnapshot: Boolean,
    dailyNote: DailyNote?,
    isLoading: Boolean,
    canSync: Boolean,
    isExporting: Boolean,
    intelligenceOverview: AccountIntelligenceOverview?,
    onOpenAnalysis: () -> Unit,
    onRefreshDailyNote: () -> Unit
) {
    HomeAccountOverviewSection(
        uid = uid,
        serverName = serverName,
        rosterCount = rosterCount,
        exportedAt = exportedAt,
        hasSnapshot = hasSnapshot,
        onOpenAnalysis = onOpenAnalysis
    )

    HomeDailyNoteSection(
        note = dailyNote,
        isLoading = isLoading,
        canSync = canSync,
        isExporting = isExporting,
        onRefresh = onRefreshDailyNote
    )

    VisualDashboardSection(intelligenceOverview)
}
