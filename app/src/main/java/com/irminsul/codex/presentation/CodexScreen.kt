package com.irminsul.codex.presentation





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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.irminsul.codex.domain.model.DataValue
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.irminsul.codex.api.DailyNote
import com.irminsul.codex.api.HoYoLabClient
import com.irminsul.codex.data.repository.HoYoLabAccountRepository
import com.irminsul.codex.data.export.JsonDocumentWriter
import com.irminsul.codex.data.local.IrminsulDatabase
import com.irminsul.codex.data.local.repository.RoomAccountSnapshotStore
import com.irminsul.codex.ui.HoYoLabLoginDialog
import com.irminsul.codex.integration.security.HoYoLabCredentialStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.irminsul.codex.domain.analysis.LineupSelectionRules

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CodexScreen(
    accountViewModel: AccountViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("irminsul_prefs", Context.MODE_PRIVATE) }
    val credentialStore = remember(context) { HoYoLabCredentialStore(context.applicationContext) }
    val storedCookieResult = remember(credentialStore) { runCatching { credentialStore.getCookie() } }
    val scope = rememberCoroutineScope()
    val accountExportState by accountViewModel.uiState.collectAsStateWithLifecycle()

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
                        errorMessage = "Could not detect your Genshin UID from this HoYoLAB session. Open Account to enter your UID manually, then tap Save & Sync."
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

    val isInitialSignIn = selectedTab == CodexTab.HOME && (uid.isBlank() || cookie.isBlank())

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
        if (isInitialSignIn) {
            InitialSignInSection(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                isLoading = isLoading,
                credentialLoadError = credentialLoadError,
                errorMessage = errorMessage,
                onSignIn = { showWebLogin = true },
                onClearSavedCredentials = { clearSavedCredentials() }
            )
        } else {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CodexScreenHeader(
            uid = uid,
            serverName = if (uid.isNotBlank()) HoYoLabClient.detectServer(uid) else null
        )

        AccountSection(
            showCredentialsPanel = showCredentials && (
                selectedTab == CodexTab.ACCOUNT || uid.isBlank() || cookie.isBlank()
            ),
            uid = uid,
            onUidChange = { uid = it },
            cookie = cookie,
            onCookieChange = { cookie = it },
            credentialLoadError = credentialLoadError,
            isLoading = isLoading,
            isExporting = accountExportState.isExporting,
            onSignIn = { showWebLogin = true },
            onClearSavedCredentials = { clearSavedCredentials() },
            onSaveAndSync = { refreshData() },
            showExportPanel = selectedTab == CodexTab.ACCOUNT && uid.isNotBlank() && cookie.isNotBlank(),
            exportProgress = accountExportState.progress,
            exportStatus = exportStatus,
            isRestoringSnapshot = accountExportState.isRestoringSnapshot,
            onExport = { extractAccountJson() },
            showSettings = selectedTab == CodexTab.ACCOUNT,
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
            showCredentials = showCredentials,
            onManageAccount = { showCredentials = true },
            onHideAccountCredentials = { showCredentials = false }
        )


        if (selectedTab == CodexTab.VISUALS) {
            VisualsSection(
                snapshot = accountExportState.snapshot,
                intelligenceOverview = accountExportState.intelligenceOverview,
                isExporting = accountExportState.isExporting,
                isRestoringSnapshot = accountExportState.isRestoringSnapshot,
                isAnalyzing = accountExportState.isAnalyzingIntelligence,
                errorMessage = accountExportState.intelligenceErrorMessage,
                hasCredentials = uid.isNotBlank() && cookie.isNotBlank(),
                selectedCharacterId = selectedCharacterId,
                onCharacterSelected = { selectedCharacterId = it },
                showAllRecommendations = showAllRecommendations,
                onShowAllRecommendationsChange = { showAllRecommendations = it },
                onAnalyzeAccount = { accountViewModel.analyzeAccount(uid, cookie) }
            )
        }

        if (selectedTab == CodexTab.TOOLS) {
            val toolsCharacters = when (val value = accountExportState.snapshot?.characters) {
                is DataValue.Available -> value.value
                is DataValue.Stale -> value.value
                else -> emptyList()
            }
            val availableLineupIds = toolsCharacters.map { it.id }.toSet()
            val activeLineupIds = selectedLineupCharacterIds.filter { it in availableLineupIds }
            ToolsSection(
                characters = toolsCharacters,
                preferences = prefs,
                showMaterialsPlanner = showMaterialsPlanner,
                showLineupSimulator = showLineupSimulator,
                selectedLineupCharacterIds = activeLineupIds,
                rosterIsStale = accountExportState.snapshot?.characters is DataValue.Stale,
                rosterIsPartial = accountExportState.snapshot?.sections?.get("characters")?.status?.name == "PARTIAL",
                onToggleMaterialsPlanner = { showMaterialsPlanner = !showMaterialsPlanner },
                onToggleLineupSimulator = { showLineupSimulator = !showLineupSimulator },
                onToggleLineupCharacter = { characterId ->
                    selectedLineupCharacterIds = LineupSelectionRules.toggle(activeLineupIds, characterId)
                }
            )
        }

        (errorMessage ?: accountExportState.errorMessage)?.let { message ->
            ScreenErrorBanner(message)
        }

        if (selectedTab == CodexTab.HOME && isLoading && dailyNote == null) {
            HomeInitialLoadingIndicator()
        }

        if (selectedTab == CodexTab.HOME) {
            val homeSnapshot = accountExportState.snapshot
            val rosterCount = when (val roster = homeSnapshot?.characters) {
                is DataValue.Available -> roster.value.size
                is DataValue.Stale -> roster.value.size
                else -> null
            }
            HomeSection(
                uid = uid,
                serverName = HoYoLabClient.detectServer(uid),
                rosterCount = rosterCount,
                exportedAt = homeSnapshot?.metadata?.exportedAt?.toString(),
                hasSnapshot = homeSnapshot != null,
                dailyNote = dailyNote,
                isLoading = isLoading,
                canSync = uid.isNotBlank() && cookie.isNotBlank(),
                isExporting = accountExportState.isExporting,
                intelligenceOverview = accountExportState.intelligenceOverview,
                onOpenAnalysis = { selectedTab = CodexTab.VISUALS },
                onRefreshDailyNote = { refreshData() }
            )
        }
        }
        }
        if (showAiSheet) {
            AiAssistantSheet(
                sheetState = aiSheetState,
                onDismiss = { showAiSheet = false }
            )
        }
    }
}