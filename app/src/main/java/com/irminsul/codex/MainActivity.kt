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
    val scope = rememberCoroutineScope()
    val accountExportState by accountViewModel.uiState.collectAsState()

    var uid by remember { mutableStateOf(prefs.getString("uid", "") ?: "") }
    var cookie by remember { mutableStateOf(prefs.getString("cookie", "") ?: "") }
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
                    Text("HoYoLAB account credentials are stored in the current app preferences. They are not encrypted by this implementation.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    if (resinProgress != null) {
                        LinearProgressIndicator(
                            progress = { resinProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    } else {
                        Text(
                            text = "Resin progress unavailable",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                enabled = !isLoading && !accountExportState.isExporting,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isLoading) "Refreshing..." else "Refresh")
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
    val planStorageKey = remember(selectedTargetId) {
        "manual_material_plan_rows_" + selectedTargetId
    }
    val rows = remember { mutableStateListOf<ManualMaterialPlanRow>() }
    var loadedPlanKey by remember { mutableStateOf<String?>(null) }
    var currentLevel by remember {
        mutableStateOf(preferences.getString("material_current_level", "") ?: "")
    }
    var targetLevel by remember {
        mutableStateOf(preferences.getString("material_target_level", "") ?: "")
    }
    var targetMenuExpanded by remember { mutableStateOf(false) }

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
    LaunchedEffect(selectedTargetId, customTargetName, currentLevel, targetLevel) {
        preferences.edit()
            .putString("material_target_id", selectedTargetId)
            .putString("material_custom_target", customTargetName)
            .putString("material_current_level", currentLevel)
            .putString("material_target_level", targetLevel)
            .apply()
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Materials Planner", fontSize = 23.sp, fontWeight = FontWeight.Bold)
            Text(
                "Plan an upgrade and track the materials you still need.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Upgrade target", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Box {
                    OutlinedButton(
                        onClick = { targetMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            selectedTarget?.label ?: if (selectedTargetId == "custom") "Custom target" else "Choose target",
                            modifier = Modifier.weight(1f)
                        )
                        Text("⌄")
                    }
                    DropdownMenu(
                        expanded = targetMenuExpanded,
                        onDismissRequest = { targetMenuExpanded = false }
                    ) {
                        targetOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    selectedTargetId = option.id
                                    currentLevel = option.currentLevel?.toString() ?: ""
                                    targetLevel = ""
                                    targetMenuExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Custom target") },
                            onClick = {
                                selectedTargetId = "custom"
                                currentLevel = ""
                                targetLevel = ""
                                targetMenuExpanded = false
                            }
                        )
                    }
                }
                if (selectedTargetId == "custom") {
                    OutlinedTextField(
                        value = customTargetName,
                        onValueChange = { customTargetName = it },
                        label = { Text("Character or weapon name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentLevel,
                        onValueChange = { currentLevel = it.filter(Char::isDigit).take(3) },
                        label = { Text("Current level") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetLevel,
                        onValueChange = { targetLevel = it.filter(Char::isDigit).take(3) },
                        label = { Text("Target level") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "Current levels are prefilled when available. You can adjust both values.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Manual planning mode", fontWeight = FontWeight.SemiBold)
                Text(
                    "A redistribution-approved material-cost catalogue is not connected yet. For now, enter the required quantities from a source you trust. These requirements are manual, not game-verified.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Material requirements", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = {
                rows.add(ManualMaterialPlanRow(id = UUID.randomUUID().toString()))
            }) {
                Text("+ Add material")
            }
        }

        if (rows.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("No materials added", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Add a material to enter its required amount and how many you already own.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = {
                        rows.add(ManualMaterialPlanRow(id = UUID.randomUUID().toString()))
                    }) {
                        Text("Add first material")
                    }
                }
            }
        }

        rows.forEachIndexed { index, row ->
            val calculation = ManualMaterialPlanCalculator.calculate(
                row.requiredQuantity,
                row.ownedQuantity
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Material " + (index + 1), fontWeight = FontWeight.SemiBold)
                        TextButton(onClick = { rows.removeAt(index) }) {
                            Text("Remove")
                        }
                    }
                    OutlinedTextField(
                        value = row.materialName,
                        onValueChange = { value -> rows[index] = row.copy(materialName = value) },
                        label = { Text("Material name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = row.requiredQuantity,
                            onValueChange = { value ->
                                rows[index] = row.copy(requiredQuantity = value.filter(Char::isDigit))
                            },
                            label = { Text("Required") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = row.ownedQuantity,
                            onValueChange = { value ->
                                rows[index] = row.copy(ownedQuantity = value.filter(Char::isDigit))
                            },
                            label = { Text("Owned") },
                            placeholder = { Text("Not entered") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    when (calculation.status) {
                        ManualPlanCalculationStatus.COMPLETE -> {
                            Text(
                                "Remaining: " + calculation.remainingQuantity + "  •  Required: " + calculation.requiredQuantity + "  •  Owned: " + calculation.ownedQuantity,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        ManualPlanCalculationStatus.INVENTORY_NOT_ENTERED -> {
                            Text(
                                "Required: " + calculation.requiredQuantity + "  •  Enter owned quantity to calculate what remains.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        ManualPlanCalculationStatus.REQUIRED_NOT_ENTERED -> {
                            Text("Enter the required quantity.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ManualPlanCalculationStatus.INVALID_QUANTITY -> {
                            Text("Enter valid non-negative whole numbers.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Text(
            "Your plan is saved locally on this device. Blank owned quantities remain unknown; entering 0 means you have none.",
            fontSize = 11.sp,
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
    val selected = characters.firstOrNull { it.id == selectedCharacterId } ?: characters.first()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Character Showcase", fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text(
                "${characters.size} characters retrieved • Select a character to inspect their build.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            characters.forEach { character ->
                val isSelected = character.id == selected.id
                Card(
                    onClick = { onCharacterSelected(character.id) },
                    modifier = Modifier.width(112.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ShowcaseArtwork(
                            url = character.avatarIcon,
                            description = "${character.name ?: "Character"} portrait",
                            fallback = elementGlyph(character.element),
                            modifier = Modifier.size(88.dp)
                        )
                        Text(
                            character.name ?: "Character #${character.id}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            "Lv. ${character.level ?: "—"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        CharacterShowcaseDetail(character = selected)
    }
}

@Composable
private fun CharacterShowcaseDetail(character: CharacterSnapshot) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ShowcaseArtwork(
                    url = character.avatarIcon,
                    description = "${character.name ?: "Character"} artwork",
                    fallback = elementGlyph(character.element),
                    modifier = Modifier.width(112.dp).height(142.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.weight(1f)) {
                    Text(character.name ?: "Character #${character.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${character.element ?: "Element unavailable"} • ${character.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("Level ${character.level ?: "—"}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Friendship ${character.friendshipLevel ?: "—"} • Constellations ${character.activeConstellationCount ?: "—"}/6",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("Weapon", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            when (val weapon = character.weapon) {
                is DataValue.Available -> WeaponShowcaseRow(weapon.value)
                is DataValue.Stale -> WeaponShowcaseRow(weapon.value, stale = true)
                DataValue.Missing -> ShowcaseUnavailable("Weapon data was not included.")
                DataValue.Unavailable -> ShowcaseUnavailable("Weapon data is unavailable.")
                is DataValue.Failed -> ShowcaseUnavailable("Weapon data could not be read.")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("Artifacts", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            when (val artifacts = character.artifacts) {
                is DataValue.Available -> ArtifactShowcaseRow(artifacts.value)
                is DataValue.Stale -> ArtifactShowcaseRow(artifacts.value, stale = true)
                DataValue.Missing -> ShowcaseUnavailable("Artifact data was not included.")
                DataValue.Unavailable -> ShowcaseUnavailable("Artifact data is unavailable.")
                is DataValue.Failed -> ShowcaseUnavailable("Artifact data could not be read.")
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("Attributes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            if (character.selectedProperties.isEmpty()) {
                ShowcaseUnavailable("Character attributes are not available.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    character.selectedProperties.take(8).forEach { stat ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(stat.statName ?: "Unknown stat", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(stat.final ?: stat.value ?: stat.add ?: stat.base ?: "—", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("Talents", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            if (character.skills.isEmpty()) {
                ShowcaseUnavailable("Talent details are not available.")
            } else {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    character.skills.forEach { skill ->
                        Column(
                            modifier = Modifier.width(88.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            ShowcaseArtwork(
                                skill.icon,
                                skill.name ?: "Talent",
                                "✦",
                                Modifier.size(54.dp)
                            )
                            Text(skill.name ?: "Talent", fontSize = 10.sp, maxLines = 2)
                            Text("Lv. ${skill.level ?: "—"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text("Constellations", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            if (character.constellations.isEmpty()) {
                ShowcaseUnavailable("Constellation details are not available.")
            } else {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    character.constellations.sortedBy { it.position ?: it.id }.forEach { constellation ->
                        Column(
                            modifier = Modifier.width(88.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            ShowcaseArtwork(
                                constellation.icon,
                                constellation.name ?: "Constellation",
                                "C${constellation.position ?: constellation.id}",
                                Modifier.size(54.dp)
                            )
                            Text(
                                constellation.name ?: "Constellation ${constellation.position ?: constellation.id}",
                                fontSize = 10.sp,
                                maxLines = 2
                            )
                            Text(
                                when (constellation.isActive) {
                                    true -> "Unlocked"
                                    false -> "Locked"
                                    null -> "Unknown"
                                },
                                fontSize = 10.sp,
                                color = if (constellation.isActive == true) MaterialTheme.colorScheme.primary
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
private fun WeaponShowcaseRow(weapon: com.irminsul.codex.domain.model.WeaponSnapshot, stale: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ShowcaseArtwork(weapon.icon, weapon.name ?: "Weapon", "⚔", Modifier.size(64.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(weapon.name ?: "Weapon #${weapon.id}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Lv. ${weapon.level ?: "—"} • Refinement ${weapon.refinementLevel ?: "—"} • ${weapon.rarity?.let { "★".repeat(it.coerceIn(1, 5)) } ?: "Rarity unavailable"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (stale) Text("Using older account data", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ArtifactShowcaseRow(artifacts: List<com.irminsul.codex.domain.model.ArtifactSnapshot>, stale: Boolean = false) {
    if (artifacts.isEmpty()) {
        ShowcaseUnavailable("No equipped artifact entries were returned.")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            artifacts.sortedBy { it.slot ?: Int.MAX_VALUE }.forEach { artifact ->
                Column(
                    modifier = Modifier.width(76.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ShowcaseArtwork(artifact.icon, artifact.name ?: "Artifact", "◇", Modifier.size(62.dp))
                    Text(artifact.slotName ?: artifact.name ?: "Artifact", fontSize = 10.sp, maxLines = 2)
                    Text("+${artifact.level ?: "—"}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        artifacts.sortedBy { it.slot ?: Int.MAX_VALUE }.forEach { artifact ->
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(artifact.name ?: artifact.slotName ?: "Artifact", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                artifact.mainProperty?.let { stat ->
                    Text(
                        "${stat.statName ?: "Main stat"}: ${stat.value ?: stat.final ?: stat.base ?: "—"}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                artifact.subProperties.forEach { stat ->
                    Text(
                        "${stat.statName ?: "Substat"}: ${stat.value ?: stat.final ?: stat.base ?: "—"}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (stale) Text("Using older account data", fontSize = 10.sp, color = MaterialTheme.colorScheme.error)
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
