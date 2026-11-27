package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.irminsul.codex.domain.model.AccountIntelligenceOverview

@Composable
fun VisualDashboardSection(report: AccountIntelligenceOverview?) {
    Text("Visual Dashboard", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
    Text(
    "Account analysis, evidence-backed recommendations, and visual summaries in one place.",
    fontSize = 13.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val report = accountExportState.intelligenceOverview
    if (report == null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text("No analysis available yet", fontWeight = FontWeight.SemiBold)
    Text(
    "Use the Analyze Account button above to populate this dashboard.",
    fontSize = 12.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    }
    }
    } else {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text("Character assessment", fontWeight = FontWeight.SemiBold)
    AssessmentBar("Needs attention", report.needsAttentionCount, report.assessedCharacterCount ?: 0)
    AssessmentBar("Review", report.reviewCharacterCount, report.assessedCharacterCount ?: 0)
    AssessmentBar("Clear", report.clearCharacterCount, report.assessedCharacterCount ?: 0)
    }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
    MetricTile("Recommendations", report.recommendationCount.toString(), "Evidence-backed", Modifier.weight(1f))
    MetricTile("Confirmed gaps", report.confirmedAccountGapCount.toString(), "Roster / equipment", Modifier.weight(1f))
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text("Account data coverage", fontWeight = FontWeight.SemiBold)
    Text(
    "${report.presentAccountSectionCount} of ${report.accountSectionCount} sections present",
    fontSize = 20.sp,
    fontWeight = FontWeight.SemiBold,
    color = MaterialTheme.colorScheme.primary
    )
    Text(
    "${report.missingAccountSectionCount} missing • ${report.unavailableAccountSectionCount} unavailable • ${report.failedAccountSectionCount} failed",
    fontSize = 12.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (report.hasDataConfidenceConcerns) {
    Text("Freshness or coverage needs review.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
    } else {
    Text("No reported coverage or freshness concerns.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    }
    }
    }
}
