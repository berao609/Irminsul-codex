package com.irminsul.codex

import com.irminsul.codex.presentation.CodexBottomNavigationBar
import com.irminsul.codex.presentation.CodexTab

import com.irminsul.codex.presentation.VisualDashboardSection
import com.irminsul.codex.presentation.formatDuration

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.SideEffect
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.CharacterDevelopmentRecommendationType
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.GoalAwareCharacterRecommendation
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irminsul.codex.api.DailyNote
import com.irminsul.codex.api.HoYoLabClient
import com.irminsul.codex.data.repository.HoYoLabAccountRepository
import com.irminsul.codex.data.export.JsonDocumentWriter
import com.irminsul.codex.data.local.IrminsulDatabase
import com.irminsul.codex.data.local.repository.RoomAccountSnapshotStore
import com.irminsul.codex.presentation.ChroniclePresentationRules
import com.irminsul.codex.presentation.ChronicleRecordMode
import com.irminsul.codex.presentation.ChronicleOverviewSection
import com.irminsul.codex.presentation.ChronicleExplorationSection
import com.irminsul.codex.presentation.ChronicleRecordSection
import com.irminsul.codex.presentation.LineupSimulatorSection
import com.irminsul.codex.presentation.CharacterShowcaseSection
import com.irminsul.codex.presentation.CharacterRecommendationCard
import com.irminsul.codex.presentation.MaterialsPlannerSection
import com.irminsul.codex.presentation.formatGameDescription
import com.irminsul.codex.presentation.sortCharactersByRelease
import com.irminsul.codex.presentation.viewmodel.AccountViewModel
import com.irminsul.codex.ui.HoYoLabLoginDialog
import com.irminsul.codex.integration.security.HoYoLabCredentialStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.irminsul.codex.domain.analysis.LineupSelectionRules

private val CodexDarkColors = darkColorScheme(
    primary = Color(0xFF80CBC4),
    background = Color(0xFF0F1115),
    surface = Color(0xFF171A21),
    surfaceVariant = Color(0xFF1E222B),
    onBackground = Color(0xFFE6E8EC),
    onSurface = Color(0xFFE6E8EC),
    onSurfaceVariant = Color(0xFF9AA0A6),
    error = Color(0xFFEF5350)
)

private val CodexLightColors = lightColorScheme(
    primary = Color(0xFF276B65),
    onPrimary = Color.White,
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE9EDF1),
    onBackground = Color(0xFF171A21),
    onSurface = Color(0xFF171A21),
    onSurfaceVariant = Color(0xFF626B75),
    error = Color(0xFFB3261E)
)

private enum class ThemeMode(val label: String, val preferenceValue: String) {
    SYSTEM("System", "system"),
    LIGHT("Light", "light"),
    DARK("Dark", "dark");

    companion object {
        fun fromPreference(value: String?): ThemeMode =
            entries.firstOrNull { it.preferenceValue == value } ?: SYSTEM
    }
}

class MainActivity : ComponentActivity() {
    private lateinit var accountViewModel: AccountViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountViewModel = ViewModelProvider(
            this,
            AccountViewModel.Factory(
                HoYoLabAccountRepository(),
                RoomAccountSnapshotStore(IrminsulDatabase.getInstance(applicationContext))
            )
        )[AccountViewModel::class.java]
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContent {
            val preferences = remember {
                getSharedPreferences("irminsul_prefs", Context.MODE_PRIVATE)
            }
            var themeMode by remember {
                mutableStateOf(ThemeMode.fromPreference(preferences.getString("theme_mode", "system")))
            }
            val systemDark = isSystemInDarkTheme()
            val useDarkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            val view = LocalView.current
            SideEffect {
                val controller = WindowInsetsControllerCompat(window, view)
                controller.isAppearanceLightStatusBars = !useDarkTheme
                controller.isAppearanceLightNavigationBars = !useDarkTheme
            }

            MaterialTheme(colorScheme = if (useDarkTheme) CodexDarkColors else CodexLightColors) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CodexScreen(
                        accountViewModel = accountViewModel,
                        themeMode = themeMode,
                        onThemeModeChange = { selected ->
                            themeMode = selected
                            preferences.edit().putString("theme_mode", selected.preferenceValue).apply()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodexScreen(
    accountViewModel: AccountViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("irminsul_prefs", Context.MODE_PRIVATE) }
    val credentialStore = remember(context) { HoYoLabCredentialStore(context.applicationContext) }
    val storedCookieResult = remember(credentialStore) { runCatching { credentialStore.getCookie() } }
    val scope = rememberCoroutineScope()
    val accountExportState by accountViewModel.uiState.collectAsState()

    var uid by remember { mutableStateOf(prefs.getString("uid", "") ?: "") }
    var credentialLoadError by remember { mutableStateOf(storedCookieResult.isFailure) }
    var cookie by remember { mutableStateOf(storedCookieResult.getOrNull().orEmpty()) }
    var showCredentials by remember { mutableStateOf(uid.isBlank() || cookie.isBlank()) }
    var showWebLogin by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(CodexTab.HOME) }
    var showAiSheet by remember { mutableStateOf(false) }
    var showMaterialsPlanner by remember { mutableStateOf(false) }
    var showLineupSimulator by remember { mutableStateOf(false) }
    var selectedLineupCharacterIds by remember { mutableStateOf(emptyList<Int>()) }
    val aiSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showAllRecommendations by remember { mutableStateOf(false) }
    var selectedCharacterId by remember { mutableStateOf<Int?>(null) }

    var dailyNote by remember { mutableStateOf<DailyNote?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var exportedJson by remember { mutableStateOf<String?>(null) }
    var exportStatus by remember { mutableStateOf<String?>(null) }

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val content = exportedJson
        when {
            uri == null -> {
                exportStatus = "Save cancelled."
            }
            content == null -> {
                exportStatus = null
                errorMessage = "No export data is available to save."
            }
            else -> {
                try {
                    val byteCount = JsonDocumentWriter.write(content) {
                        context.contentResolver.openOutputStream(uri)
                    }
                    val kb = (byteCount / 1024).coerceAtLeast(1)
                    errorMessage = null
                    exportStatus = "Saved JSON file ($kb KB)."
                } catch (e: Exception) {
                    exportStatus = null
                    errorMessage = "Could not save file: ${e.message ?: "Unknown error."}"
                }
            }
        }
    }

    fun refreshData(targetUid: String = uid, targetCookie: String = cookie) {
        if (targetUid.isBlank() || targetCookie.isBlank()) {
            errorMessage = "Enter both your UID and HoYoLAB cookie."
            return
        }
        try {
            credentialStore.saveCookie(targetCookie.trim())
        } catch (_: Exception) {
            errorMessage = "Could not securely save HoYoLAB credentials. Clear saved credentials and sign in again."
            return
        }
        prefs.edit().putString("uid", targetUid.trim()).apply()
        credentialLoadError = false

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

    fun extractAccountJson() {
        if (uid.isBlank() || cookie.isBlank()) {
            errorMessage = "Sign in with HoYoLAB first."
            return
        }
        errorMessage = null
        exportStatus = null
        accountViewModel.exportAccount(uid, cookie)
    }

    LaunchedEffect(accountExportState.pendingExport) {
        val completedExport = accountExportState.pendingExport ?: return@LaunchedEffect
        val json = completedExport.rawJson
        exportedJson = json
        val jsonByteCount = json.toByteArray(Charsets.UTF_8).size
        val kb = (jsonByteCount / 1024).coerceAtLeast(1)
        val exportLabel = when (completedExport.snapshot.overallStatus.name) {
            "SUCCESS" -> "Complete export"
            "PARTIAL" -> "Partial export. Some sections are unavailable."
            "FAILED" -> "Export contains no successfully retrieved account sections."
            else -> "Export status unavailable. Check export_status in the JSON."
        }
        exportStatus = "$exportLabel ($kb KB). Choose where to save."
        saveFileLauncher.launch("genshin_${uid.trim()}.json")
        accountViewModel.acknowledgeExport()
    }

    fun handleWebLoginSuccess(capturedCookie: String) {
        showWebLogin = false
        try {
            credentialStore.saveCookie(capturedCookie.trim())
        } catch (_: Exception) {
            cookie = ""
            credentialLoadError = true
            showCredentials = true
            errorMessage = "Could not securely save the HoYoLAB session. Clear saved credentials and sign in again."
            return
        }
        cookie = capturedCookie.trim()
        credentialLoadError = false

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

    fun clearSavedCredentials() {
        credentialStore.clearCookie()
        prefs.edit().remove("uid").apply()
        uid = ""
        cookie = ""
        dailyNote = null
        showCredentials = true
        showWebLogin = false
        credentialLoadError = false
        errorMessage = null
    }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            accountViewModel.restoreLatestSnapshot(uid)
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

    Scaffold(
        bottomBar = {
            CodexBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    if (tab == CodexTab.ACCOUNT) showCredentials = true
                },
                onOpenAi = { showAiSheet = true }
            )
        }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
        }

        if (showCredentials && (selectedTab == CodexTab.ACCOUNT || uid.isBlank() || cookie.isBlank())) {
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
                        enabled = !isLoading && !accountExportState.isExporting,
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
                    if (credentialLoadError) {
                        Text(
                            "The saved session could not be unlocked. Clear it before signing in again.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                        TextButton(onClick = { clearSavedCredentials() }) {
                            Text("Clear saved credentials")
                        }
                    }
                    OutlinedButton(
                        onClick = { refreshData() },
                        enabled = !isLoading && !accountExportState.isExporting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isLoading) "Syncing..." else "Save & Sync")
                    }
                }
            }
        }

        if (selectedTab == CodexTab.ACCOUNT && uid.isNotBlank() && cookie.isNotBlank()) {
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

                    if (accountExportState.progress != null) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = accountExportState.progress!!,
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

                    Button(
                        onClick = { extractAccountJson() },
                        enabled = !accountExportState.isExporting && !accountExportState.isRestoringSnapshot,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (accountExportState.isExporting) "Extracting..." else "Save JSON")
                    }
                }
            }
        }

        if (selectedTab == CodexTab.VISUALS) {
            accountExportState.snapshot?.let { snapshot ->
                Text(
                    text = "Account Chronicle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                ChronicleOverviewSection(snapshot.accountCard, snapshot.profileAndExploration)
                ChronicleExplorationSection(snapshot.profileAndExploration)
                ChronicleRecordSection("Spiral Abyss · Current Cycle", snapshot.spiralAbyssCurrent, ChronicleRecordMode.SPIRAL_ABYSS)
                ChronicleRecordSection("Spiral Abyss · Previous Cycle", snapshot.spiralAbyssPrevious, ChronicleRecordMode.SPIRAL_ABYSS)
                ChronicleRecordSection("Imaginarium Theater", snapshot.imaginariumTheater, ChronicleRecordMode.IMAGINARIUM_THEATER)
            }
        }

        if (selectedTab == CodexTab.VISUALS) {
            val roster = when (val value = accountExportState.snapshot?.characters) {
                is DataValue.Available -> value.value
                is DataValue.Stale -> value.value
                else -> emptyList()
            }
            if (roster.isNotEmpty()) {
                CharacterShowcaseSection(
                    characters = roster,
                    selectedCharacterId = selectedCharacterId,
                    onCharacterSelected = { selectedCharacterId = it }
                )
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Character Showcase", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Run Analyze Account to retrieve your character roster and open detailed character profiles here.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (selectedTab == CodexTab.VISUALS && uid.isNotBlank() && cookie.isNotBlank()) {
            val overview = accountExportState.intelligenceOverview
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
                        text = "Account Analysis",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "One balanced review of your available account data. Recommendations keep a consistent priority order based on confirmed issues, stat discrepancies, build warnings, and information gaps.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = { accountViewModel.analyzeAccount(uid, cookie) },
                        enabled = !accountExportState.isExporting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            when {
                                accountExportState.isExporting -> "Retrieving account..."
                                accountExportState.isRestoringSnapshot -> "Restoring saved analysis..."
                                accountExportState.snapshot == null -> "Analyze Account"
                                else -> "Refresh Analysis"
                            }
                        )
                    }

                    if (overview == null && !accountExportState.isAnalyzingIntelligence &&
                        !accountExportState.isRestoringSnapshot &&
                        accountExportState.intelligenceErrorMessage == null
                    ) {
                        Text(
                            text = "Run an account analysis here. This is separate from the optional Save JSON export.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (accountExportState.isAnalyzingIntelligence) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Analyzing account data...", fontSize = 12.sp)
                    }

                    accountExportState.intelligenceErrorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    overview?.let { report ->
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Text(
                            text = "Character assessment",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Assessed: ${report.assessedCharacterCount ?: "Unavailable"} • Need attention: ${report.needsAttentionCount} • Review: ${report.reviewCharacterCount} • Clear: ${report.clearCharacterCount}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Development recommendations (${report.recommendationCount})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (report.goalAwareDevelopment.recommendations.isEmpty()) {
                            Text(
                                text = if (report.goalAwareDevelopment.availability.name in listOf("AVAILABLE", "STALE")) {
                                    "No recommendations were identified by the available analysis."
                                } else {
                                    "Recommendations are not available from the current character data."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            val recommendations = report.goalAwareDevelopment.recommendations
                            val visibleRecommendations = if (showAllRecommendations) {
                                recommendations
                            } else {
                                recommendations.take(5)
                            }
                            val characterSnapshots = (accountExportState.snapshot?.characters as? DataValue.Available)?.value.orEmpty()
                            visibleRecommendations.forEach { item ->
                                val character = characterSnapshots.firstOrNull { it.id == item.recommendation.characterId }
                                CharacterRecommendationCard(
                                    item = item,
                                    character = character,
                                    expanded = showAllRecommendations
                                )
                            }
                            if (report.recommendationCount > 5) {
                                Text(
                                    text = if (showAllRecommendations) {
                                        "Showing all ${report.recommendationCount} recommendations."
                                    } else {
                                        "Showing 5 of ${report.recommendationCount} recommendations."
                                    },
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                TextButton(
                                    onClick = { showAllRecommendations = !showAllRecommendations },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (showAllRecommendations) "Show fewer recommendations" else "Show all recommendations")
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Text(
                            text = "Account gaps: ${report.confirmedAccountGapCount} confirmed • ${report.accountDataReviewCount} data review",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Account data: ${report.presentAccountSectionCount}/${report.accountSectionCount} sections present • ${report.missingAccountSectionCount} missing • ${report.unavailableAccountSectionCount} unavailable • ${report.failedAccountSectionCount} failed",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (report.hasStaleEvidence) {
                            Text(
                                text = "Some findings rely on stale account evidence.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (report.hasUnverifiedSources) {
                            Text(
                                text = "One or more analysis sources are missing, unavailable, or failed. Treat this overview as partial.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (report.hasDataConfidenceConcerns) {
                            Text(
                                text = "Account data coverage or freshness has unresolved concerns.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        VisualDashboardSection(accountExportState.intelligenceOverview)


        if (selectedTab == CodexTab.VISUALS) {
            val lineupCharacters = when (val value = accountExportState.snapshot?.characters) {
                is DataValue.Available -> value.value
                is DataValue.Stale -> value.value
                else -> emptyList()
            }
            val availableLineupIds = lineupCharacters.map { it.id }.toSet()
            val activeLineupIds = selectedLineupCharacterIds.filter { it in availableLineupIds }
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    showLineupSimulator = !showLineupSimulator
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Lineup Simulator", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${activeLineupIds.size}/4 characters selected",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        if (showLineupSimulator) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (showLineupSimulator) "Collapse Lineup Simulator" else "Expand Lineup Simulator"
                    )
                }
            }
            if (showLineupSimulator) {
                LineupSimulatorSection(
                    characters = lineupCharacters,
                    selectedCharacterIds = activeLineupIds,
                    rosterIsStale = accountExportState.snapshot?.characters is DataValue.Stale,
                    rosterIsPartial = accountExportState.snapshot?.sections?.get("characters")?.status?.name == "PARTIAL",
                    onToggleCharacter = { characterId ->
                        selectedLineupCharacterIds = LineupSelectionRules.toggle(activeLineupIds, characterId)
                    }
                )
            }


        }

        if (selectedTab == CodexTab.TOOLS) {
            Text("Tools", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Calculators and utilities for account progression.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val materialCharacters = when (val value = accountExportState.snapshot?.characters) {
                is DataValue.Available -> value.value
                is DataValue.Stale -> value.value
                else -> emptyList()
            }
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    showMaterialsPlanner = !showMaterialsPlanner
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Materials Calculator", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Automatically plan character and weapon upgrade materials",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        if (showMaterialsPlanner) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (showMaterialsPlanner) "Collapse Materials Calculator" else "Expand Materials Calculator"
                    )
                }
            }
            if (showMaterialsPlanner) {
                MaterialsPlannerSection(
                    characters = materialCharacters,
                    preferences = prefs
                )
            }
        }

        if (selectedTab == CodexTab.ACCOUNT) {
            Text("Account & settings", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            onClick = { clearSavedCredentials() },
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
                        OutlinedButton(onClick = { showCredentials = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("Manage HoYoLAB account")
                        }
                    }
                    if (showCredentials && uid.isNotBlank() && cookie.isNotBlank()) {
                        TextButton(onClick = { showCredentials = false }) { Text("Hide account credentials") }
                    }
                }
            }
        }

        if (errorMessage != null || accountExportState.errorMessage != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2B1618)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = (errorMessage ?: accountExportState.errorMessage)!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        if (selectedTab == CodexTab.HOME && isLoading && dailyNote == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        if (selectedTab == CodexTab.HOME) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Adventurer account", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (uid.isNotBlank()) "UID ${uid.trim()}" else "No account configured",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (uid.isNotBlank()) HoYoLabClient.detectServer(uid) else "Connect your HoYoLAB account in Account",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            accountExportState.snapshot?.let { snapshot ->
                val rosterCount = when (val roster = snapshot.characters) {
                    is com.irminsul.codex.domain.model.DataValue.Available -> roster.value.size
                    is com.irminsul.codex.domain.model.DataValue.Stale -> roster.value.size
                    else -> null
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Account snapshot", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = rosterCount?.let { "$it characters retrieved" } ?: "Character roster unavailable",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Last retrieved: ${snapshot.metadata.exportedAt}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { selectedTab = CodexTab.VISUALS }) {
                            Text("Analysis")
                        }
                    }
                }
            }

            if (dailyNote == null && !isLoading && uid.isNotBlank() && cookie.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Daily activity not loaded", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Sync your Daily Note to see Resin, commissions, expeditions, and Realm Currency.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = { refreshData() },
                            enabled = !isLoading && !accountExportState.isExporting,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Sync Daily Note") }
                    }
                }
            }
            dailyNote?.let { note ->
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
                    onClick = { refreshData() },
                    enabled = !isLoading && !accountExportState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (isLoading) "Refreshing..." else "Refresh Notes")
                }
            }
        }
        if (showAiSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAiSheet = false },
                sheetState = aiSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Irminsul AI", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = { showAiSheet = false }) { Text("Close") }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 280.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text("Your account-aware assistant", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Ask about your characters, builds, teams, and progression priorities.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "The chat interface is ready for the AI integration. A provider is not connected yet, so responses are not active in this build.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        enabled = false,
                        placeholder = { Text("AI chat will be available after provider setup") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Text(
                        "Your HoYoLAB authentication cookies will never be sent to an AI provider.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }


    }
    }
}








