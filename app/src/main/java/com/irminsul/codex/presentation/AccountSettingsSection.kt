package com.irminsul.codex.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ThemeMode(val label: String, val preferenceValue: String) {
    SYSTEM("System", "system"),
    LIGHT("Light", "light"),
    DARK("Dark", "dark");

    companion object {
        fun fromPreference(value: String?): ThemeMode =
            entries.firstOrNull { it.preferenceValue == value } ?: SYSTEM
    }
}

@Composable
fun AccountCredentialsSection(
    uid: String,
    onUidChange: (String) -> Unit,
    cookie: String,
    onCookieChange: (String) -> Unit,
    credentialLoadError: Boolean,
    isLoading: Boolean,
    isExporting: Boolean,
    onSignIn: () -> Unit,
    onClearSavedCredentials: () -> Unit,
    onSaveAndSync: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSignIn,
                enabled = !isLoading && !isExporting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Sign in with HoYoLAB")
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Genshin UID", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(uid.ifBlank { "Automatically filled after HoYoLAB sign-in" }, style = MaterialTheme.typography.bodyMedium)
                Text(
                    if (cookie.isNotBlank()) "HoYoLAB session is saved securely on this device."
                    else "No saved HoYoLAB session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Account credentials are managed through HoYoLAB sign-in. Manual text entry is disabled to keep the app keyboard-free.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (credentialLoadError) {
                Text(
                    "The saved session could not be unlocked. Clear it before signing in again.",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
                TextButton(onClick = onClearSavedCredentials) {
                    Text("Clear saved credentials")
                }
            }
            OutlinedButton(
                onClick = onSaveAndSync,
                enabled = !isLoading && !isExporting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isLoading) "Syncing..." else "Save & Sync")
            }
        }
    }
}

@Composable
fun AccountSettingsSection(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    uid: String,
    cookie: String,
    showCredentials: Boolean,
    onClearSavedCredentials: () -> Unit,
    onManageAccount: () -> Unit,
    onHideAccountCredentials: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Appearance", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Choose how Irminsul Codex looks. System follows your device setting.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = themeMode == mode,
                        onClick = { onThemeModeChange(mode) },
                        label = { Text(mode.label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Irminsul Codex", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text("Genshin Impact account intelligence", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "The HoYoLAB session cookie is encrypted with Android Keystore. The UID is stored locally.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (uid.isNotBlank() || cookie.isNotBlank()) {
                OutlinedButton(
                    onClick = onClearSavedCredentials,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign out and clear saved credentials")
                }
                Text(
                    "Saved account snapshots remain on this device.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (uid.isNotBlank() && cookie.isNotBlank() && !showCredentials) {
                OutlinedButton(
                    onClick = onManageAccount,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Manage HoYoLAB account")
                }
            }
            if (showCredentials && uid.isNotBlank() && cookie.isNotBlank()) {
                TextButton(onClick = onHideAccountCredentials) {
                    Text("Hide account credentials")
                }
            }
        }
    }
}
