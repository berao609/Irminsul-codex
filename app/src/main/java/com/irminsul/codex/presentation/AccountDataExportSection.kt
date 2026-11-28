package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountDataExportSection(
    progress: String?,
    exportStatus: String?,
    hasExport: Boolean,
    isExporting: Boolean,
    isRestoringSnapshot: Boolean,
    onExport: () -> Unit,
    onPreview: () -> Unit,
    onSave: () -> Unit
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
                text = "Account Data Export",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Retrieve a portable account snapshot, inspect its JSON, and save it to a location you choose. Private profile and Daily Note cache data, plus downloaded images, are stored separately inside the app.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (progress != null) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = progress,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (exportStatus != null) {
                Text(
                    text = exportStatus,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onExport,
                enabled = !isExporting && !isRestoringSnapshot,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
            ) {
                Text(
                    when {
                        isExporting -> "Extracting..."
                        hasExport -> "Refresh JSON"
                        else -> "Retrieve JSON"
                    }
                )
            }

            if (hasExport) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = onPreview,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Preview JSON")
                    }
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
                    ) {
                        Text("Save JSON")
                    }
                }
            }
        }
    }
}
