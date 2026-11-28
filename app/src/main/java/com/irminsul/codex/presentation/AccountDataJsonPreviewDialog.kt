package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun AccountDataJsonPreviewDialog(
    json: String,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    val lines = remember(json) { json.lineSequence().toList() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account JSON Preview") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${lines.size} lines · read-only preview",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SelectionContainer {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)
                    ) {
                        itemsIndexed(lines, key = { index, _ -> index }) { index, line ->
                            Text(
                                text = line,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onCopy) { Text("Copy full JSON") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
