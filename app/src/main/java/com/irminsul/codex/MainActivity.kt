package com.irminsul.codex

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irminsul.codex.api.DailyNote
import com.irminsul.codex.api.HoYoLabClient
import com.irminsul.codex.ui.HoYoLabLoginDialog
import kotlinx.coroutines.launch

private val CodexColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    background = Color(0xFF0F1115),
    surface = Color(0xFF171A21),
    surfaceVariant = Color(0xFF1E222B),
    onBackground = Color(0xFFE6E8EC),
    onSurface = Color(0xFFE6E8EC),
    onSurfaceVariant = Color(0xFF9AA0A6),
    error = Color(0xFFEF5350)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = CodexColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CodexScreen()
                }
            }
        }
    }
}

@Composable
fun CodexScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("irminsul_prefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()

    var uid by remember { mutableStateOf(prefs.getString("uid", "") ?: "") }
    var cookie by remember { mutableStateOf(prefs.getString("cookie", "") ?: "") }
    var showCredentials by remember { mutableStateOf(uid.isBlank() || cookie.isBlank()) }
    var showWebLogin by remember { mutableStateOf(false) }

    var dailyNote by remember { mutableStateOf<DailyNote?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableStateOf<String?>(null) }
    var exportedJson by remember { mutableStateOf<String?>(null) }
    var exportStatus by remember { mutableStateOf<String?>(null) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val content = exportedJson
        if (uri != null && content != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(content.toByteArray(Charsets.UTF_8))
                }
                val kb = (content.toByteArray(Charsets.UTF_8).size / 1024).coerceAtLeast(1)
                exportStatus = "Saved JSON file ($kb KB)."
            } catch (e: Exception) {
                errorMessage = "Could not save file: ${e.message}"
            }
        }
    }

    fun refreshData(targetUid: String = uid, targetCookie: String = cookie) {
        if (targetUid.isBlank() || targetCookie.isBlank()) {
            errorMessage = "Enter both your UID and HoYoLAB cookie."
            return
        }
        prefs.edit()
            .putString("uid", targetUid.trim())
            .putString("cookie", targetCookie.trim())
            .apply()

        scope.launch {
            isLoading = true
            errorMessage = null
            HoYoLabClient.fetchDailyNote(targetUid, targetCookie)
                .onSuccess { note ->
                    dailyNote = note
                    showCredentials = false
                }
                .onFailure { err ->
                    errorMessage = err.message ?: "Request failed."
                }
            isLoading = false
        }
    }

    fun extractAccountJson(saveImmediately: Boolean) {
        if (uid.isBlank() || cookie.isBlank()) {
            errorMessage = "Sign in with HoYoLAB first."
            return
        }
        scope.launch {
            isExporting = true
            errorMessage = null
            exportStatus = null
            exportProgress = "Starting extraction..."

            HoYoLabClient.exportFullAccountJson(uid, cookie) { step ->
                scope.launch { exportProgress = step }
            }.onSuccess { json ->
                exportedJson = json
                val kb = (json.toByteArray(Charsets.UTF_8).size / 1024).coerceAtLeast(1)
                exportProgress = null
                if (saveImmediately) {
                    exportStatus = "Extracted $kb KB. Choose where to save."
                    saveFileLauncher.launch("genshin_${uid.trim()}.json")
                } else {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Irminsul Codex JSON", json))
                    exportStatus = "Copied full JSON ($kb KB) to clipboard."
                }
            }.onFailure { err ->
                exportProgress = null
                errorMessage = err.message ?: "JSON extraction failed."
            }
            isExporting = false
        }
    }

    fun handleWebLoginSuccess(capturedCookie: String) {
        showWebLogin = false
        cookie = capturedCookie
        prefs.edit().putString("cookie", capturedCookie).apply()

        scope.launch {
            isLoading = true
            errorMessage = null
            HoYoLabClient.fetchGenshinUid(capturedCookie)
                .onSuccess { detectedUid ->
                    uid = detectedUid
                    refreshData(detectedUid, capturedCookie)
                }
                .onFailure {
                    isLoading = false
                    if (uid.isNotBlank()) {
                        refreshData(uid, capturedCookie)
                    } else {
                        errorMessage = "Signed in! Enter your Genshin UID below and tap Save & Sync."
                    }
                }
        }
    }

    LaunchedEffect(Unit) {
        if (uid.isNotBlank() && cookie.isNotBlank()) {
            refreshData()
        }
    }

    if (showWebLogin) {
        HoYoLabLoginDialog(
            onCookieCaptured = { handleWebLoginSuccess(it) },
            onDismiss = { showWebLogin = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Irminsul Codex",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (uid.isNotBlank()) {
                    Text(
                        text = "UID ${uid.trim()} • ${HoYoLabClient.detectServer(uid)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextButton(onClick = { showCredentials = !showCredentials }) {
                Text(if (showCredentials) "Hide" else "Account")
            }
        }

        if (showCredentials) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showWebLogin = true },
                        enabled = !isLoading && !isExporting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Sign in with HoYoLAB")
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    OutlinedTextField(
                        value = uid,
                        onValueChange = { uid = it },
                        label = { Text("Genshin UID (auto-filled on login)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cookie,
                        onValueChange = { cookie = it },
                        label = { Text("HoYoLAB Cookie (ltoken_v2 / ltuid_v2)") },
                        visualTransformation = PasswordVisualTransformation(),
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedButton(
                        onClick = { refreshData() },
                        enabled = !isLoading && !isExporting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isLoading) "Syncing..." else "Save & Sync")
                    }
                }
            }
        }

        if (uid.isNotBlank() && cookie.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
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
                        text = "Extracts profile, exploration, all character builds (weapons, artifacts, talents), Spiral Abyss, and Imaginarium Theater.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (exportProgress != null) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = exportProgress!!,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (exportStatus != null) {
                        Text(
                            text = exportStatus!!,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { extractAccountJson(saveImmediately = true) },
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isExporting) "Extracting..." else "Save JSON")
                        }
                        OutlinedButton(
                            onClick = { extractAccountJson(saveImmediately = false) },
                            enabled = !isExporting,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Copy JSON")
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1618)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        if (isLoading && dailyNote == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        dailyNote?.let { note ->
            val resinProgress = if (note.currentResin != null && note.maxResin != null && note.maxResin > 0) {
                (note.currentResin.toFloat() / note.maxResin).coerceIn(0f, 1f)
            } else null

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Original Resin",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${note.currentResin ?: "—"} / ${note.maxResin ?: "—"}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { resinProgress ?: 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = when {
                            note.resinRecoveryTime == null -> "Recovery time unavailable"
                            note.resinRecoveryTime > 0L -> "Full in ${formatDuration(note.resinRecoveryTime)}"
                            note.currentResin != null && note.maxResin != null && note.currentResin >= note.maxResin -> "Resin is full"
                            else -> "Recovery status unavailable"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = "Commissions",
                    value = "${note.finishedTaskNum ?: "—"} / ${note.totalTaskNum ?: "—"}",
                    subtext = when (note.isExtraTaskRewardReceived) { true -> "Bonus claimed"; false -> "Bonus unclaimed"; null -> "Bonus status unavailable" },
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Realm Currency",
                    value = "${note.currentHomeCoin ?: "—"}",
                    subtext = "Max ${note.maxHomeCoin ?: "—"}",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricTile(
                    label = "Weekly Discounts",
                    value = "${note.remainResinDiscountNum ?: "—"} / ${note.resinDiscountNumLimit ?: "—"}",
                    subtext = "Remaining",
                    modifier = Modifier.weight(1f)
                )
                MetricTile(
                    label = "Expeditions",
                    value = "${note.currentExpeditionNum ?: "—"} / ${note.maxExpeditionNum ?: "—"}",
                    subtext = "Dispatched",
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = { refreshData() },
                enabled = !isLoading && !isExporting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isLoading) "Refreshing..." else "Refresh")
            }
        }
    }
}

@Composable
private fun MetricTile(
    label: String,
    value: String,
    subtext: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    if (totalSeconds <= 0L) return "Full"
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
