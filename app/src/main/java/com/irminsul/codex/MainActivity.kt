package com.irminsul.codex

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage
import com.irminsul.codex.domain.model.CharacterSnapshot
import com.irminsul.codex.domain.model.CharacterDevelopmentRecommendationType
import com.irminsul.codex.domain.model.DataValue
import com.irminsul.codex.domain.model.StructuredData
import com.irminsul.codex.domain.model.GoalAwareCharacterRecommendation
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.DoorFront
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.InsertChart
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
import com.irminsul.codex.data.local.IrminsulDatabase
import com.irminsul.codex.data.local.repository.RoomAccountSnapshotStore
import com.irminsul.codex.presentation.viewmodel.AccountViewModel
import com.irminsul.codex.ui.HoYoLabLoginDialog
import com.irminsul.codex.integration.security.HoYoLabCredentialStore
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import com.irminsul.codex.domain.analysis.ManualMaterialPlanCalculator
import com.irminsul.codex.domain.analysis.ManualPlanCalculationStatus
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

private enum class CodexTab(val label: String) {
    HOME("Home"),
    VISUALS("Visuals"),
    ACCOUNT("Account")
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
            Box {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    NavigationBarItem(
                        selected = selectedTab == CodexTab.HOME,
                        onClick = { selectedTab = CodexTab.HOME },
                        icon = { Icon(Icons.Outlined.DoorFront, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 10.sp) },
                        alwaysShowLabel = true
                    )
                    NavigationBarItem(
                        selected = selectedTab == CodexTab.VISUALS,
                        onClick = { selectedTab = CodexTab.VISUALS },
                        icon = { Icon(Icons.Outlined.InsertChart, contentDescription = "Visuals") },
                        label = { Text("Visuals", fontSize = 10.sp) },
                        alwaysShowLabel = true
                    )
                    Spacer(Modifier.weight(1f))
                    NavigationBarItem(
                        selected = selectedTab == CodexTab.ACCOUNT,
                        onClick = {
                            selectedTab = CodexTab.ACCOUNT
                            showCredentials = true
                        },
                        icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Account") },
                        label = { Text("Account", fontSize = 10.sp) },
                        alwaysShowLabel = true
                    )
                }
                FloatingActionButton(
                    onClick = { showAiSheet = true },
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = (-18).dp),
                    shape = RoundedCornerShape(50),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Outlined.AutoAwesome, contentDescription = "Open AI assistant")
                }
            }
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
            TextButton(onClick = {
                selectedTab = CodexTab.ACCOUNT
                showCredentials = true
            }) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Account")
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
                ChronicleDataSection("Account Overview", snapshot.accountCard)
                ChronicleDataSection("World Exploration & Collectibles", snapshot.profileAndExploration)
                ChronicleDataSection("Spiral Abyss · Current Cycle", snapshot.spiralAbyssCurrent)
                ChronicleDataSection("Spiral Abyss · Previous Cycle", snapshot.spiralAbyssPrevious)
                ChronicleDataSection("Imaginarium Theater", snapshot.imaginariumTheater)
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

        if (selectedTab == CodexTab.VISUALS) {
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

            val materialCharacters = lineupCharacters
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
                        Text("Materials Planner", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Plan character and weapon upgrades",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        if (showMaterialsPlanner) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                        contentDescription = if (showMaterialsPlanner) "Collapse Materials Planner" else "Expand Materials Planner"
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

private data class ManualMaterialPlanRow(
    val id: String,
    val materialName: String = "",
    val requiredQuantity: String = "",
    val ownedQuantity: String = ""
)

private data class MaterialTargetOption(
    val id: String,
    val label: String,
    val currentLevel: Int?
)

@Composable
private fun MaterialsPlannerSection(
    characters: List<CharacterSnapshot>,
    preferences: SharedPreferences
) {
    var selectedTargetId by remember {
        mutableStateOf(preferences.getString("material_target_id", "custom") ?: "custom")
    }
    var customTargetName by remember {
        mutableStateOf(preferences.getString("material_custom_target", "") ?: "")
    }
    val targetOptions = buildList {
        characters.forEach { character ->
            add(
                MaterialTargetOption(
                    id = "character:" + character.id,
                    label = "Character · " + (character.name ?: "Character #" + character.id),
                    currentLevel = character.level
                )
            )
        }
        characters.forEach { character ->
            val weapon = when (val value = character.weapon) {
                is DataValue.Available -> value.value
                is DataValue.Stale -> value.value
                else -> null
            }
            if (weapon != null) {
                add(
                    MaterialTargetOption(
                        id = "weapon:" + character.id + ":" + weapon.id,
                        label = "Weapon · " + (weapon.name ?: "Weapon #" + weapon.id) + " (" + (character.name ?: "Equipped") + ")",
                        currentLevel = weapon.level
                    )
                )
            }
        }
    }
    val selectedTarget = targetOptions.firstOrNull { it.id == selectedTargetId }
    val legacyTargetId = preferences.getString("material_target_id", "custom") ?: "custom"
    var currentLevel by remember(selectedTargetId) {
        val stored = preferences.getString("material_plan_current_$selectedTargetId", null)
        val legacy = if (selectedTargetId == legacyTargetId) {
            preferences.getString("material_current_level", "")
        } else {
            selectedTarget?.currentLevel?.toString() ?: ""
        }
        mutableStateOf(stored ?: legacy ?: "")
    }
    var targetLevel by remember(selectedTargetId) {
        val stored = preferences.getString("material_plan_target_$selectedTargetId", null)
        val legacy = if (selectedTargetId == legacyTargetId) {
            preferences.getString("material_target_level", "")
        } else {
            ""
        }
        mutableStateOf(stored ?: legacy ?: "")
    }
    val planStorageKey = remember(selectedTargetId) {
        "manual_material_plan_rows_" + selectedTargetId
    }
    val rows = remember { mutableStateListOf<ManualMaterialPlanRow>() }
    var loadedPlanKey by remember { mutableStateOf<String?>(null) }
    var targetMenuExpanded by remember { mutableStateOf(false) }

    val characterId = selectedTargetId.takeIf { it.startsWith("character:") }
        ?.removePrefix("character:")
    var normalTalentCurrent by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_normal_current_$characterId", "") ?: "")
    }
    var normalTalentTarget by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_normal_target_$characterId", "") ?: "")
    }
    var skillTalentCurrent by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_skill_current_$characterId", "") ?: "")
    }
    var skillTalentTarget by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_skill_target_$characterId", "") ?: "")
    }
    var burstTalentCurrent by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_burst_current_$characterId", "") ?: "")
    }
    var burstTalentTarget by remember(characterId) {
        mutableStateOf(preferences.getString("material_talent_burst_target_$characterId", "") ?: "")
    }
    val talentValues = listOf(
        Triple("normal", "Normal Attack", normalTalentCurrent to normalTalentTarget),
        Triple("skill", "Elemental Skill", skillTalentCurrent to skillTalentTarget),
        Triple("burst", "Elemental Burst", burstTalentCurrent to burstTalentTarget)
    )
    val weaponPlanKey = characterId ?: "global"
    var plannedWeaponId by remember(weaponPlanKey) {
        mutableStateOf(preferences.getString("material_planned_weapon_id_$weaponPlanKey", "custom") ?: "custom")
    }
    var plannedWeaponName by remember(weaponPlanKey) {
        mutableStateOf(preferences.getString("material_planned_weapon_name_$weaponPlanKey", "") ?: "")
    }
    val plannedWeapon = targetOptions.firstOrNull { it.id == plannedWeaponId }
    var plannedWeaponCurrent by remember(weaponPlanKey, plannedWeaponId) {
        mutableStateOf(
            preferences.getString("material_planned_weapon_current_$weaponPlanKey-$plannedWeaponId", null)
                ?: plannedWeapon?.currentLevel?.toString().orEmpty()
        )
    }
    var plannedWeaponTarget by remember(weaponPlanKey, plannedWeaponId) {
        mutableStateOf(preferences.getString("material_planned_weapon_target_$weaponPlanKey-$plannedWeaponId", "") ?: "")
    }
    var weaponMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(planStorageKey) {
        rows.clear()
        rows.addAll(loadManualMaterialPlanRows(preferences, planStorageKey))
        loadedPlanKey = planStorageKey
    }
    LaunchedEffect(planStorageKey, rows.toList(), loadedPlanKey) {
        if (loadedPlanKey == planStorageKey) {
            preferences.edit().putString(planStorageKey, saveManualMaterialPlanRows(rows)).apply()
        }
    }
    LaunchedEffect(selectedTargetId, currentLevel, targetLevel, customTargetName) {
        preferences.edit()
            .putString("material_target_id", selectedTargetId)
            .putString("material_custom_target", customTargetName)
            .putString("material_plan_current_$selectedTargetId", currentLevel)
            .putString("material_plan_target_$selectedTargetId", targetLevel)
            // Keep the original keys readable for a safe, one-way migration from older builds.
            .putString("material_current_level", currentLevel)
            .putString("material_target_level", targetLevel)
            .apply()
    }
    LaunchedEffect(
        characterId, normalTalentCurrent, normalTalentTarget,
        skillTalentCurrent, skillTalentTarget, burstTalentCurrent, burstTalentTarget
    ) {
        if (characterId != null) {
            preferences.edit()
                .putString("material_talent_normal_current_$characterId", normalTalentCurrent)
                .putString("material_talent_normal_target_$characterId", normalTalentTarget)
                .putString("material_talent_skill_current_$characterId", skillTalentCurrent)
                .putString("material_talent_skill_target_$characterId", skillTalentTarget)
                .putString("material_talent_burst_current_$characterId", burstTalentCurrent)
                .putString("material_talent_burst_target_$characterId", burstTalentTarget)
                .apply()
        }
    }
    LaunchedEffect(weaponPlanKey, plannedWeaponId, plannedWeaponName) {
        preferences.edit()
            .putString("material_planned_weapon_id_$weaponPlanKey", plannedWeaponId)
            .putString("material_planned_weapon_name_$weaponPlanKey", plannedWeaponName)
            .apply()
    }
    LaunchedEffect(weaponPlanKey, plannedWeaponId, plannedWeaponCurrent, plannedWeaponTarget) {
        preferences.edit()
            .putString("material_planned_weapon_current_$weaponPlanKey-$plannedWeaponId", plannedWeaponCurrent)
            .putString("material_planned_weapon_target_$weaponPlanKey-$plannedWeaponId", plannedWeaponTarget)
            .apply()
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Materials Planner",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Build upgrade plans for your characters and weapons.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("1. Choose an upgrade target", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Each character and weapon keeps its own level range.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    OutlinedButton(
                        onClick = { targetMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            selectedTarget?.label ?: if (selectedTargetId == "custom") "Custom target" else "Choose target",
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text("⌄")
                    }
                    DropdownMenu(expanded = targetMenuExpanded, onDismissRequest = { targetMenuExpanded = false }) {
                        targetOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = { selectedTargetId = option.id; targetMenuExpanded = false }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Custom target") },
                            onClick = { selectedTargetId = "custom"; targetMenuExpanded = false }
                        )
                    }
                }
                if (selectedTargetId == "custom") {
                    OutlinedTextField(
                        value = customTargetName,
                        onValueChange = { customTargetName = it },
                        label = { Text("Character or weapon name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                Text(
                    if (selectedTargetId.startsWith("character:")) "Character level" else "Weapon level",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentLevel,
                        onValueChange = { currentLevel = it.filter(Char::isDigit).take(3) },
                        label = { Text("Current") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = targetLevel,
                        onValueChange = { targetLevel = it.filter(Char::isDigit).take(3) },
                        label = { Text("Target") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        if (characterId != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("2. Talent investment", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Set each talent track independently.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    talentValues.forEach { (track, label, levels) ->
                        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = levels.first,
                                    onValueChange = { next ->
                                        val level = next.filter(Char::isDigit).take(2)
                                        when (track) {
                                            "normal" -> normalTalentCurrent = level
                                            "skill" -> skillTalentCurrent = level
                                            "burst" -> burstTalentCurrent = level
                                        }
                                    },
                                    label = { Text("Current") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = levels.second,
                                    onValueChange = { next ->
                                        val level = next.filter(Char::isDigit).take(2)
                                        when (track) {
                                            "normal" -> normalTalentTarget = level
                                            "skill" -> skillTalentTarget = level
                                            "burst" -> burstTalentTarget = level
                                        }
                                    },
                                    label = { Text("Target") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("3. Planned weapon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Choose the weapon this character is intended to equip.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box {
                        OutlinedButton(
                            onClick = { weaponMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                plannedWeapon?.label ?: if (plannedWeaponId == "custom") "Choose or enter a weapon" else "Custom weapon",
                                modifier = Modifier.weight(1f),
                                maxLines = 1
                            )
                            Text("⌄")
                        }
                        DropdownMenu(expanded = weaponMenuExpanded, onDismissRequest = { weaponMenuExpanded = false }) {
                            targetOptions.filter { it.id.startsWith("weapon:") }.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = { plannedWeaponId = option.id; weaponMenuExpanded = false }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Custom weapon") },
                                onClick = { plannedWeaponId = "custom"; weaponMenuExpanded = false }
                            )
                        }
                    }
                    if (plannedWeaponId == "custom") {
                        OutlinedTextField(
                            value = plannedWeaponName,
                            onValueChange = { plannedWeaponName = it },
                            label = { Text("Weapon name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = plannedWeaponCurrent,
                            onValueChange = { plannedWeaponCurrent = it.filter(Char::isDigit).take(3) },
                            label = { Text("Current level") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = plannedWeaponTarget,
                            onValueChange = { plannedWeaponTarget = it.filter(Char::isDigit).take(3) },
                            label = { Text("Target level") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Automatic costs are not connected yet", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Only reviewed, approved cost data can populate automatic requirements. Manual tracking is available in the meantime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Material requirements", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (rows.isEmpty()) "No materials tracked" else "${rows.size} materials tracked",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { rows.add(ManualMaterialPlanRow(id = UUID.randomUUID().toString())) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add material")
                }
            }

            if (rows.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Your material list is empty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Add a material to track its required and owned quantities. Existing saved rows are loaded automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            rows.forEachIndexed { index, row ->
                val calculation = ManualMaterialPlanCalculator.calculate(row.requiredQuantity, row.ownedQuantity)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Material ${index + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { rows.removeAt(index) }) { Text("Remove") }
                        }
                        OutlinedTextField(
                            value = row.materialName,
                            onValueChange = { rows[index] = row.copy(materialName = it) },
                            label = { Text("Material name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = row.requiredQuantity,
                                onValueChange = { rows[index] = row.copy(requiredQuantity = it.filter(Char::isDigit)) },
                                label = { Text("Required") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = row.ownedQuantity,
                                onValueChange = { rows[index] = row.copy(ownedQuantity = it.filter(Char::isDigit)) },
                                label = { Text("Owned") },
                                placeholder = { Text("Not entered") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        when (calculation.status) {
                            ManualPlanCalculationStatus.COMPLETE -> {
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Remaining", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        calculation.remainingQuantity.toString(),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            ManualPlanCalculationStatus.INVENTORY_NOT_ENTERED -> Text(
                                "Enter your owned quantity to calculate what remains.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ManualPlanCalculationStatus.REQUIRED_NOT_ENTERED -> Text(
                                "Enter the required quantity.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ManualPlanCalculationStatus.INVALID_QUANTITY -> Text(
                                "Enter valid non-negative whole numbers.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
        Text(
            "Plans are saved locally. A blank owned quantity means unknown; 0 means none owned.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun loadManualMaterialPlanRows(preferences: SharedPreferences, storageKey: String): List<ManualMaterialPlanRow> {
    val raw = preferences.getString(storageKey, null) ?: return emptyList()
    return runCatching {
        val array = JSONArray(raw)
        buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                add(
                    ManualMaterialPlanRow(
                        id = item.optString("id").ifBlank { UUID.randomUUID().toString() },
                        materialName = item.optString("materialName"),
                        requiredQuantity = item.optString("requiredQuantity"),
                        ownedQuantity = item.optString("ownedQuantity")
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

private fun saveManualMaterialPlanRows(rows: List<ManualMaterialPlanRow>): String {
    val array = JSONArray()
    rows.forEach { row ->
        array.put(
            JSONObject()
                .put("id", row.id)
                .put("materialName", row.materialName)
                .put("requiredQuantity", row.requiredQuantity)
                .put("ownedQuantity", row.ownedQuantity)
        )
    }
    return array.toString()
}


@Composable
private fun LineupSimulatorSection(
    characters: List<CharacterSnapshot>,
    selectedCharacterIds: List<Int>,
    rosterIsStale: Boolean,
    rosterIsPartial: Boolean,
    onToggleCharacter: (Int) -> Unit
) {
    val selectedCharacters = selectedCharacterIds.mapNotNull { id ->
        characters.firstOrNull { it.id == id }
    }
    val elementSummary = selectedCharacters
        .groupingBy { it.element?.takeIf(String::isNotBlank) ?: "Unknown" }
        .eachCount()
        .entries
        .joinToString(" • ") { (element, count) -> "$element ×$count" }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Choose up to four characters you own. This sandbox helps you arrange a team; it does not yet rate combat performance or claim unsupported synergies.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (rosterIsStale) {
            Text(
                "This lineup uses a previously retrieved roster. Refresh account data before relying on it as current.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (rosterIsPartial) {
            Text(
                "The retrieved character roster is marked partial. Some owned characters may be missing.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(LineupSelectionRules.MAX_TEAM_SIZE) { index ->
                val character = selectedCharacters.getOrNull(index)
                Card(
                    modifier = Modifier.width(88.dp).heightIn(min = 126.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (character != null) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (character == null) {
                            Text("＋", fontSize = 26.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Slot ${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("Empty", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            ShowcaseArtwork(
                                url = character.avatarIcon,
                                description = "${character.name ?: "Character"} portrait",
                                fallback = elementGlyph(character.element),
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                character.name ?: "Character #${character.id}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                            TextButton(
                                onClick = { onToggleCharacter(character.id) },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                            ) {
                                Text("Remove", fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
        if (selectedCharacters.isNotEmpty()) {
            Text(
                "Elements represented: $elementSummary",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Text(
            "Element counts are descriptive only. Role coverage, elemental reaction quality, rotations, and team synergy will require verified game knowledge before recommendations are shown.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (characters.isEmpty()) {
            Text(
                "Analyze your account to load your owned character roster.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text("Your roster", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                characters.forEach { character ->
                    val isSelected = character.id in selectedCharacterIds
                    val isFull = selectedCharacterIds.size >= LineupSelectionRules.MAX_TEAM_SIZE
                    Card(
                        onClick = {
                            if (isSelected || !isFull) onToggleCharacter(character.id)
                        },
                        modifier = Modifier.width(104.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            ShowcaseArtwork(
                                url = character.avatarIcon,
                                description = "${character.name ?: "Character"} portrait",
                                fallback = elementGlyph(character.element),
                                modifier = Modifier.size(58.dp)
                            )
                            Text(
                                character.name ?: "Character #${character.id}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2
                            )
                            Text(
                                when {
                                    isSelected -> "Selected"
                                    isFull -> "Team full"
                                    else -> "Tap to add"
                                },
                                fontSize = 10.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterShowcaseSection(
    characters: List<CharacterSnapshot>,
    selectedCharacterId: Int?,
    onCharacterSelected: (Int) -> Unit
) {
    var rosterExpanded by remember { mutableStateOf(false) }
    var filtersExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var minimumLevel by remember { mutableStateOf("") }
    var maximumLevel by remember { mutableStateOf("") }
    var selectedElement by remember { mutableStateOf("All") }
    var selectedWeaponType by remember { mutableStateOf("All") }
    var selectedRarity by remember { mutableStateOf("All") }
    var sortBy by remember { mutableStateOf("Name") }
    var descending by remember { mutableStateOf(false) }

    val selected = characters.firstOrNull { it.id == selectedCharacterId } ?: characters.first()
    val elements = listOf("All") + characters.map { it.element?.takeIf(String::isNotBlank) ?: "Unknown" }.distinct().sorted()
    val weaponTypes = listOf("All") + characters.map { it.rosterWeaponType() ?: "Unknown" }.distinct().sorted()
    val rarities = listOf("All", "5★", "4★", "3★", "2★", "1★", "Unknown")

    val filteredCharacters = characters.filter { character ->
        val name = character.name ?: "Character #${character.id}"
        val level = character.level
        val rarity = character.rarity
        val weaponType = character.rosterWeaponType() ?: "Unknown"
        val element = character.element?.takeIf(String::isNotBlank) ?: "Unknown"
        val minLevel = minimumLevel.toIntOrNull()
        val maxLevel = maximumLevel.toIntOrNull()
        name.contains(searchQuery.trim(), ignoreCase = true) &&
            (selectedElement == "All" || element.equals(selectedElement, ignoreCase = true)) &&
            (selectedWeaponType == "All" || weaponType == selectedWeaponType) &&
            (selectedRarity == "All" || if (selectedRarity == "Unknown") rarity == null else rarity?.let { "${it}★" == selectedRarity } == true) &&
            (minLevel == null || level != null && level >= minLevel) &&
            (maxLevel == null || level != null && level <= maxLevel)
    }.let { matches ->
        val sorted = when (sortBy) {
            "Level" -> if (descending) matches.sortedByDescending { it.level ?: Int.MIN_VALUE } else matches.sortedBy { it.level ?: Int.MAX_VALUE }
            "Rarity" -> if (descending) matches.sortedByDescending { it.rarity ?: Int.MIN_VALUE } else matches.sortedBy { it.rarity ?: Int.MAX_VALUE }
            "Friendship" -> if (descending) matches.sortedByDescending { it.friendshipLevel ?: Int.MIN_VALUE } else matches.sortedBy { it.friendshipLevel ?: Int.MAX_VALUE }
            else -> if (descending) matches.sortedByDescending { it.name ?: "Character #${it.id}" } else matches.sortedBy { it.name ?: "Character #${it.id}" }
        }
        sorted
    }

    val hasActiveFilters = searchQuery.isNotBlank() || minimumLevel.isNotBlank() || maximumLevel.isNotBlank() ||
        selectedElement != "All" || selectedWeaponType != "All" || selectedRarity != "All"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(
            onClick = { rosterExpanded = !rosterExpanded },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Character Roster", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (rosterExpanded) "${filteredCharacters.size} of ${characters.size} characters shown"
                        else "${characters.size} characters • Selected: ${selected.name ?: "Character #${selected.id}"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    if (rosterExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (rosterExpanded) "Collapse character roster" else "Expand character roster"
                )
            }
        }

        if (rosterExpanded) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Search characters") },
                    textStyle = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = { filtersExpanded = !filtersExpanded }) {
                    Text(if (filtersExpanded) "Hide filters" else "Filters")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RosterChoiceMenu(
                    label = "Sort: $sortBy",
                    options = listOf("Name", "Level", "Rarity", "Friendship"),
                    selected = sortBy,
                    onSelected = { sortBy = it }
                )
                TextButton(onClick = { descending = !descending }) {
                    Text(if (descending) "Descending ↓" else "Ascending ↑")
                }
            }

            if (filtersExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Filter roster", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RosterChoiceMenu("Element: $selectedElement", elements, selectedElement) { selectedElement = it }
                        RosterChoiceMenu("Weapon: $selectedWeaponType", weaponTypes, selectedWeaponType) { selectedWeaponType = it }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        RosterChoiceMenu("Rarity: $selectedRarity", rarities, selectedRarity) { selectedRarity = it }
                    }
                    Text("Character level", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = minimumLevel,
                            onValueChange = { minimumLevel = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Minimum") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = maximumLevel,
                            onValueChange = { maximumLevel = it.filter(Char::isDigit).take(2) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            label = { Text("Maximum") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    if (hasActiveFilters) {
                        TextButton(
                            onClick = {
                                searchQuery = ""
                                minimumLevel = ""
                                maximumLevel = ""
                                selectedElement = "All"
                                selectedWeaponType = "All"
                                selectedRarity = "All"
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) { Text("Clear filters") }
                    }
                }
            }

            if (filteredCharacters.isEmpty()) {
                Text(
                    "No characters match these filters. Adjust or clear the filters.",
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredCharacters.chunked(3).forEach { rowCharacters ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCharacters.forEach { character ->
                                val isSelected = character.id == selected.id
                                Card(
                                    onClick = { onCharacterSelected(character.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(13.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(7.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        ShowcaseArtwork(
                                            url = character.avatarIcon,
                                            description = "${character.name ?: "Character"} portrait",
                                            fallback = elementGlyph(character.element),
                                            modifier = Modifier.size(66.dp)
                                        )
                                        Text(
                                            character.name ?: "Character #${character.id}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                        Text(
                                            "Lv. ${character.level ?: "—"} • ${character.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "—"}",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            repeat(3 - rowCharacters.size) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        CharacterShowcaseDetail(character = selected)
    }
}

@Composable
private fun RosterChoiceMenu(
    label: String,
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(label, maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun CharacterSnapshot.rosterWeaponType(): String? {
    val weapon = when (val value = weapon) {
        is DataValue.Available -> value.value
        is DataValue.Stale -> value.value
        else -> null
    } ?: return null
    return weapon.typeName?.takeIf(String::isNotBlank)
        ?: weapon.type?.let { "Type $it" }
}

@Composable
private fun CharacterShowcaseDetail(character: CharacterSnapshot) {
    var expandedDetailKey by remember(character.id) { mutableStateOf<String?>(null) }
    fun toggleDetail(key: String) {
        expandedDetailKey = if (expandedDetailKey == key) null else key
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ShowcaseArtwork(
                    url = character.avatarIcon,
                    description = "${character.name ?: "Character"} artwork",
                    fallback = elementGlyph(character.element),
                    modifier = Modifier.width(112.dp).height(142.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                    Text(character.name ?: "Character #${character.id}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${character.element ?: "Element unavailable"} • ${character.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Level ${character.level ?: "—"}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Friendship ${character.friendshipLevel ?: "—"} • Constellations ${character.activeConstellationCount ?: "—"}/6",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Talents", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (character.skills.isEmpty()) {
                    ShowcaseUnavailable("Talent details are not available.")
                } else {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        character.skills.forEach { skill ->
                            val key = "talent:${skill.id}"
                            Column(
                                modifier = Modifier
                                    .width(78.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (expandedDetailKey == key) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { toggleDetail(key) }
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                ShowcaseArtwork(skill.icon, skill.name ?: "Talent", "✦", Modifier.size(48.dp))
                                Text(skill.name ?: "Talent", style = MaterialTheme.typography.labelSmall, maxLines = 2)
                                Text("Lv. ${skill.level ?: "—"}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    character.skills.firstOrNull { "talent:${it.id}" == expandedDetailKey }?.let { skill ->
                        ShowcaseDetailSurface {
                            Text(skill.name ?: "Talent", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text(
                                when {
                                    skill.isUnlocked == false -> "Locked"
                                    skill.isUnlocked == true -> "Unlocked • Level ${skill.level ?: "—"}"
                                    else -> "Level ${skill.level ?: "—"}"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            skill.description?.takeIf { it.isNotBlank() }?.let {
                                Text(formatGameDescription(it), style = MaterialTheme.typography.bodySmall)
                            }
                            skill.enhancedDescription?.takeIf { it.isNotBlank() }?.let {
                                Text(formatGameDescription(it), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Constellations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (character.constellations.isEmpty()) {
                    ShowcaseUnavailable("Constellation details are not available.")
                } else {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        character.constellations.sortedBy { it.position ?: it.id }.forEach { constellation ->
                            val key = "constellation:${constellation.id}"
                            Column(
                                modifier = Modifier
                                    .width(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (expandedDetailKey == key) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { toggleDetail(key) }
                                    .padding(7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                ShowcaseArtwork(
                                    constellation.icon,
                                    constellation.name ?: "Constellation",
                                    "C${constellation.position ?: constellation.id}",
                                    Modifier.size(46.dp)
                                )
                                Text("C${constellation.position ?: constellation.id}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    when (constellation.isActive) {
                                        true -> "Unlocked"
                                        false -> "Locked"
                                        null -> "Unknown"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (constellation.isActive == true) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    character.constellations.firstOrNull {
                        "constellation:${it.id}" == expandedDetailKey
                    }?.let { constellation ->
                        ShowcaseDetailSurface {
                            Text(
                                constellation.name ?: "Constellation ${constellation.position ?: constellation.id}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                when (constellation.isActive) {
                                    true -> "Unlocked"
                                    false -> "Locked"
                                    null -> "Unlock status unavailable"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            constellation.effect?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                            constellation.enhancedEffect?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("Build inspection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            when (val weapon = character.weapon) {
                is DataValue.Available -> WeaponShowcaseRow(
                    weapon.value,
                    expanded = expandedDetailKey == "weapon",
                    onClick = { toggleDetail("weapon") }
                )
                is DataValue.Stale -> WeaponShowcaseRow(
                    weapon.value,
                    stale = true,
                    expanded = expandedDetailKey == "weapon",
                    onClick = { toggleDetail("weapon") }
                )
                DataValue.Missing -> ShowcaseUnavailable("Weapon data was not included.")
                DataValue.Unavailable -> ShowcaseUnavailable("Weapon data is unavailable.")
                is DataValue.Failed -> ShowcaseUnavailable("Weapon data could not be read.")
            }

            when (val artifacts = character.artifacts) {
                is DataValue.Available -> ArtifactShowcaseRow(
                    artifacts.value,
                    expandedDetailKey = expandedDetailKey,
                    onArtifactSelected = ::toggleDetail
                )
                is DataValue.Stale -> ArtifactShowcaseRow(
                    artifacts.value,
                    stale = true,
                    expandedDetailKey = expandedDetailKey,
                    onArtifactSelected = ::toggleDetail
                )
                DataValue.Missing -> ShowcaseUnavailable("Artifact data was not included.")
                DataValue.Unavailable -> ShowcaseUnavailable("Artifact data is unavailable.")
                is DataValue.Failed -> ShowcaseUnavailable("Artifact data could not be read.")
            }

            ShowcaseExpandableSection(
                title = "Attributes",
                summary = if (character.selectedProperties.isEmpty()) "No attribute data" else "${character.selectedProperties.size} stats",
                expanded = expandedDetailKey == "attributes",
                onClick = { toggleDetail("attributes") }
            ) {
                if (character.selectedProperties.isEmpty()) {
                    ShowcaseUnavailable("Character attributes are not available.")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        character.selectedProperties.take(12).forEach { stat ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(stat.statName ?: "Unknown stat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(stat.final ?: stat.value ?: stat.add ?: stat.base ?: "—", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }


}

@Composable
private fun ShowcaseExpandableSection(
    title: String,
    summary: String,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(summary, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse $title" else "Expand $title"
            )
        }
        if (expanded) ShowcaseDetailSurface(content)
    }
}

private fun formatGameDescription(raw: String): String {
    return raw
        .replace("\\r\\n", "\\n")
        .replace("\\n", "\n")
        .replace(Regex("<color=[^>]*>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("</color>", RegexOption.IGNORE_CASE), "")
        .replace(Regex("<[^>]+>"), "")
        .replace(Regex("\\{LINK#[^}]+}"), "")
        .replace(Regex("\\{/LINK}"), "")
        .replace(Regex("\\n{3,}"), "\\n\\n")
        .trim()
}

@Composable
private fun ShowcaseDetailSurface(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
        content = content
    )
}

@Composable
private fun WeaponShowcaseRow(
    weapon: com.irminsul.codex.domain.model.WeaponSnapshot,
    stale: Boolean = false,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(if (expanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                .clickable(onClick = onClick)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShowcaseArtwork(weapon.icon, weapon.name ?: "Weapon", "⚔", Modifier.size(58.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
                Text(weapon.name ?: "Weapon #${weapon.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Lv. ${weapon.level ?: "—"} • R${weapon.refinementLevel ?: "—"} • ${weapon.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (stale) Text("Using older account data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
            }
            Icon(
                imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = if (expanded) "Collapse weapon details" else "Expand weapon details"
            )
        }
        if (expanded) {
            ShowcaseDetailSurface {
                weapon.typeName?.let { Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                weapon.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                weapon.mainProperty?.let { stat ->
                    Text(
                        "${stat.statName ?: "Main stat"}: ${stat.value ?: stat.final ?: stat.base ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                weapon.subProperty?.let { stat ->
                    Text(
                        "${stat.statName ?: "Secondary stat"}: ${stat.value ?: stat.final ?: stat.base ?: "—"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    "Ascension ${weapon.ascensionLevel ?: "—"} • Level ${weapon.level ?: "—"} • Refinement ${weapon.refinementLevel ?: "—"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ArtifactShowcaseRow(
    artifacts: List<com.irminsul.codex.domain.model.ArtifactSnapshot>,
    stale: Boolean = false,
    expandedDetailKey: String?,
    onArtifactSelected: (String) -> Unit
) {
    if (artifacts.isEmpty()) {
        ShowcaseUnavailable("No equipped artifact entries were returned.")
        return
    }
    val orderedArtifacts = artifacts.sortedBy { it.slot ?: Int.MAX_VALUE }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Artifacts", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            orderedArtifacts.forEach { artifact ->
                val key = "artifact:${artifact.id}"
                Column(
                    modifier = Modifier
                        .width(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (expandedDetailKey == key) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { onArtifactSelected(key) }
                        .padding(7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ShowcaseArtwork(artifact.icon, artifact.name ?: "Artifact", "◇", Modifier.size(48.dp))
                    Text(artifact.slotName ?: artifact.name ?: "Artifact", style = MaterialTheme.typography.labelSmall, maxLines = 2)
                    Text("+${artifact.level ?: "—"}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        orderedArtifacts.firstOrNull { "artifact:${it.id}" == expandedDetailKey }?.let { artifact ->
            ShowcaseDetailSurface {
                Text(artifact.name ?: artifact.slotName ?: "Artifact", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "${artifact.slotName ?: "Artifact slot"} • ${artifact.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"} • +${artifact.level ?: "—"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                artifact.mainProperty?.let { stat ->
                    Text(
                        "${stat.statName ?: "Main stat"}: ${stat.value ?: stat.final ?: stat.base ?: "—"}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (artifact.subProperties.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    artifact.subProperties.forEach { stat ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stat.statName ?: "Substat", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stat.value ?: stat.final ?: stat.base ?: "—", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
        if (stale) Text("Using older account data", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ShowcaseArtwork(url: String?, description: String, fallback: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!url.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = description,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Text(fallback, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                error = { Text(fallback, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            )
        } else {
            Text(fallback, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ShowcaseUnavailable(message: String) {
    Text(message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

private fun elementGlyph(element: String?): String = when (element?.lowercase()) {
    "wind", "anemo" -> "🌪"
    "fire", "pyro" -> "🔥"
    "water", "hydro" -> "💧"
    "ice", "cryo" -> "❄"
    "electric", "electro" -> "⚡"
    "rock", "geo" -> "🪨"
    "grass", "dendro" -> "🌿"
    else -> "✦"
}

@Composable
private fun CharacterAvatarFallback(symbol: String) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(76.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, fontSize = 30.sp)
        }
    }
}

@Composable
private fun CharacterRecommendationCard(
    item: GoalAwareCharacterRecommendation,
    character: CharacterSnapshot?,
    expanded: Boolean
) {
    val recommendation = item.recommendation
    val name = recommendation.characterName ?: "Character"
    val artifactGap = recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW &&
        recommendation.evidence.any { it.contains("Artifact collection", ignoreCase = true) }
    val title = when {
        artifactGap -> "Check $name's artifacts"
        recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW -> "Check $name's character details"
        recommendation.type == CharacterDevelopmentRecommendationType.BUILD_ERRORS -> "Fix a build issue"
        recommendation.type == CharacterDevelopmentRecommendationType.STAT_MISMATCHES -> "Check $name's stats"
        recommendation.type == CharacterDevelopmentRecommendationType.BUILD_WARNINGS -> "Review $name's build"
        else -> recommendation.title
    }
    val artifactCount = (character?.artifacts as? DataValue.Available)?.value?.size
        ?: character?.equipmentIntegrity?.artifactSourceEntryCount
    val explanation = when {
        artifactGap && artifactCount != null ->
            "We found $artifactCount of 5 equipped artifact entries in your account data."
        artifactGap ->
            "We couldn't confirm all five equipped artifacts from your account data."
        recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW ->
            "Some information for $name is missing, so we can't fully check this character yet."
        else -> recommendation.reason
    }
    val nextStep = when {
        artifactGap ->
            "Refresh your account data, then check whether all five artifacts appear in $name's character details."
        recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW ->
            "Refresh your account data and check that $name's character details are available."
        else -> recommendation.action
    }
    val priorityLabel = when (recommendation.priority.name) {
        "P1" -> "Urgent"
        "P2" -> "High priority"
        "P3" -> "Review"
        else -> "Information needed"
    }
    val elementLabel = character?.element.orEmpty()
    val elementSymbol = when (elementLabel.lowercase()) {
        "wind", "anemo" -> "🌪️"
        "fire", "pyro" -> "🔥"
        "water", "hydro" -> "💧"
        "ice", "cryo" -> "❄️"
        "electric", "electro" -> "⚡"
        "rock", "geo" -> "🪨"
        "grass", "dendro" -> "🌿"
        else -> name.firstOrNull()?.uppercase() ?: "?"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (!character?.avatarIcon.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = character?.avatarIcon,
                    contentDescription = "$name portrait",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(76.dp).clip(RoundedCornerShape(12.dp)),
                    loading = { CharacterAvatarFallback(elementSymbol) },
                    error = { CharacterAvatarFallback(elementSymbol) }
                )
            } else {
                CharacterAvatarFallback(elementSymbol)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            priorityLabel,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                Text("What to do", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(nextStep, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (expanded) {
                    if (artifactGap || recommendation.type == CharacterDevelopmentRecommendationType.DATA_REVIEW) {
                        Text(
                            "This is an information check, not a rating of how strong this character is.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(recommendation.reason, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        recommendation.evidence.forEach { evidence ->
                            Text("• $evidence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    if (recommendation.basedOnStaleData) {
                        Text(
                            "Your account data may be out of date. Refresh it to confirm this finding.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AssessmentBar(label: String, value: Int, total: Int) {
    val fraction = if (total > 0) (value.toFloat() / total).coerceIn(0f, 1f) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun ChronicleDataSection(
    title: String,
    value: DataValue<StructuredData>
) {
    var expanded by remember(title) { mutableStateOf(false) }
    val payload = when (value) {
        is DataValue.Available -> value.value
        is DataValue.Stale -> value.value
        else -> null
    }
    val statusText = when (value) {
        is DataValue.Available -> null
        is DataValue.Stale -> "Showing previously retrieved data"
        DataValue.Missing -> "Not included in this account export"
        DataValue.Unavailable -> "Not available from the source"
        is DataValue.Failed -> value.reason
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    Text(
                        text = when {
                            payload != null -> "Account data available"
                            else -> statusText ?: "No data"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse $title" else "Expand $title",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                if (payload != null) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ChronicleStructuredNode(payload)
                } else {
                    Text(
                        text = statusText ?: "No data",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ChronicleStructuredNode(
    value: StructuredData,
    depth: Int = 0
) {
    when (value) {
        is StructuredData.ObjectValue -> {
            value.fields.forEach { (key, child) ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = (depth * 8).dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    when (child) {
                        is StructuredData.ObjectValue, is StructuredData.ArrayValue -> {
                            Text(
                                text = chronicleLabel(key),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            ChronicleStructuredNode(child, depth + 1)
                        }
                        else -> Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = chronicleLabel(key),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = chronicleValue(child),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        is StructuredData.ArrayValue -> {
            value.items.forEachIndexed { index, item ->
                Text(
                    text = "Entry ${index + 1}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ChronicleStructuredNode(item, depth + 1)
                if (index < value.items.lastIndex) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
        else -> Text(
            text = chronicleValue(value),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun chronicleLabel(raw: String): String =
    raw.replace(Regex("([a-z0-9])([A-Z])"), "$1 $2")
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
        .split(Regex("\\s+"))
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }

private fun chronicleValue(value: StructuredData): String = when (value) {
    is StructuredData.StringValue -> value.value.ifBlank { "—" }
    is StructuredData.NumberValue -> value.value
    is StructuredData.BooleanValue -> if (value.value) "Yes" else "No"
    StructuredData.NullValue -> "—"
    is StructuredData.ObjectValue -> "${value.fields.size} fields"
    is StructuredData.ArrayValue -> "${value.items.size} entries"
}

@Composable
private fun DailyNoteMetricRow(
    label: String,
    value: String,
    status: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        status?.let {
            Text(
                text = it,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.primary
            )
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
