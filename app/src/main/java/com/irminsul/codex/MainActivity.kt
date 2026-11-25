package com.irminsul.codex

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.irminsul.codex.data.repository.HoYoLabAccountRepository
import com.irminsul.codex.data.local.IrminsulDatabase
import com.irminsul.codex.data.local.repository.RoomAccountSnapshotStore
import com.irminsul.codex.domain.model.CharacterDevelopmentGoal
import com.irminsul.codex.presentation.viewmodel.AccountViewModel
import com.irminsul.codex.ui.HoYoLabLoginDialog
import kotlinx.coroutines.launch

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

private enum class CodexTab(val label: String, val symbol: String) {
    HOME("Home", "⌂"),
    VISUALS("Visuals", "▥"),
    AI_CHAT("AI Chat", "✧"),
    INFO("Info", "ⓘ")
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
    var showAllRecommendations by remember { mutableStateOf(false) }

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
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                CodexTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.symbol, fontSize = 19.sp) },
                        label = { Text(tab.label, fontSize = 10.sp) },
                        alwaysShowLabel = true
                    )
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
                selectedTab = CodexTab.INFO
                showCredentials = true
            }) {
                Text("Account")
            }
        }

        if (showCredentials && (selectedTab == CodexTab.INFO || uid.isBlank() || cookie.isBlank())) {
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

        if (selectedTab == CodexTab.INFO && uid.isNotBlank() && cookie.isNotBlank()) {
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
                        enabled = !accountExportState.isExporting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (accountExportState.isExporting) "Extracting..." else "Save JSON")
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
                        text = "Choose the objective used to order existing evidence-backed recommendations. This does not change findings or their base priorities.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = accountExportState.selectedDevelopmentGoal == CharacterDevelopmentGoal.BALANCED_DEVELOPMENT,
                            onClick = { accountViewModel.selectDevelopmentGoal(CharacterDevelopmentGoal.BALANCED_DEVELOPMENT) },
                            label = { Text("Balanced") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = accountExportState.selectedDevelopmentGoal == CharacterDevelopmentGoal.BUILD_RELIABILITY,
                            onClick = { accountViewModel.selectDevelopmentGoal(CharacterDevelopmentGoal.BUILD_RELIABILITY) },
                            label = { Text("Build") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = accountExportState.selectedDevelopmentGoal == CharacterDevelopmentGoal.STAT_INTEGRITY,
                            onClick = { accountViewModel.selectDevelopmentGoal(CharacterDevelopmentGoal.STAT_INTEGRITY) },
                            label = { Text("Stat integrity") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = accountExportState.selectedDevelopmentGoal == CharacterDevelopmentGoal.DATA_CONFIDENCE,
                            onClick = { accountViewModel.selectDevelopmentGoal(CharacterDevelopmentGoal.DATA_CONFIDENCE) },
                            label = { Text("Data confidence") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = { accountViewModel.analyzeAccount(uid, cookie) },
                        enabled = !accountExportState.isExporting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            when {
                                accountExportState.isExporting -> "Retrieving account..."
                                accountExportState.snapshot == null -> "Analyze Account"
                                else -> "Refresh Analysis"
                            }
                        )
                    }

                    if (overview == null && !accountExportState.isAnalyzingIntelligence &&
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

        if (selectedTab == CodexTab.AI_CHAT) {
            Text("AI Assistant", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Your account-aware assistant", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Ask about character builds, account findings, and progression decisions. The chat interface is planned, but an AI provider has not been connected yet.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Text("Planned capabilities", fontWeight = FontWeight.SemiBold)
                    Text("• Explain deterministic account findings\n• Compare character builds\n• Help interpret account data\n• Discuss progression priorities", fontSize = 13.sp)
                    Text(
                        "AI will be opt-in through your own provider. HoYoLAB cookies will never be sent to an AI provider.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("AI chat is not active in this build.", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                }
            }
        }

        if (selectedTab == CodexTab.INFO) {
            Text("Info & settings", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
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
                        text = if (uid.isNotBlank()) HoYoLabClient.detectServer(uid) else "Connect your HoYoLAB account in Info",
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
    }
    }
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
    val artifactCount = character?.equipmentIntegrity?.artifactSourceEntryCount
        ?: (character?.artifacts as? DataValue.Available)?.value?.size
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
