package com.irminsul.codex.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun AccountSection(
    showCredentialsPanel: Boolean,
    uid: String,
    onUidChange: (String) -> Unit,
    cookie: String,
    onCookieChange: (String) -> Unit,
    credentialLoadError: Boolean,
    isLoading: Boolean,
    isExporting: Boolean,
    onSignIn: () -> Unit,
    onClearSavedCredentials: () -> Unit,
    onSaveAndSync: () -> Unit,
    showExportPanel: Boolean,
    exportProgress: String?,
    exportStatus: String?,
    isRestoringSnapshot: Boolean,
    onExport: () -> Unit,
    showSettings: Boolean,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    showCredentials: Boolean,
    onManageAccount: () -> Unit,
    onHideAccountCredentials: () -> Unit
) {
    if (showCredentialsPanel) {
        AccountCredentialsSection(
            uid = uid,
            onUidChange = onUidChange,
            cookie = cookie,
            onCookieChange = onCookieChange,
            credentialLoadError = credentialLoadError,
            isLoading = isLoading,
            isExporting = isExporting,
            onSignIn = onSignIn,
            onClearSavedCredentials = onClearSavedCredentials,
            onSaveAndSync = onSaveAndSync
        )
    }

    if (showExportPanel) {
        AccountDataExportSection(
            progress = exportProgress,
            exportStatus = exportStatus,
            isExporting = isExporting,
            isRestoringSnapshot = isRestoringSnapshot,
            onExport = onExport
        )
    }

    if (showSettings) {
        Text("Account & settings", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        AccountSettingsSection(
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            uid = uid,
            cookie = cookie,
            showCredentials = showCredentials,
            onClearSavedCredentials = onClearSavedCredentials,
            onManageAccount = onManageAccount,
            onHideAccountCredentials = onHideAccountCredentials
        )
    }
}
