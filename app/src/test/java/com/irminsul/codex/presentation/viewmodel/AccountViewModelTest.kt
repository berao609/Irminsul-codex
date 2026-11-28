package com.irminsul.codex.presentation.viewmodel

import androidx.lifecycle.ViewModelStore
import com.irminsul.codex.data.mapper.AccountExportMapper
import com.irminsul.codex.domain.model.AccountExport
import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.analysis.AccountIntelligenceEngine
import com.irminsul.codex.domain.model.CharacterDevelopmentGoal
import com.irminsul.codex.domain.repository.AccountRepository
import com.irminsul.codex.domain.repository.AccountSnapshotStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AccountViewModelTest {
    private class NoOpSnapshotStore(private val latest: AccountExport? = null) : AccountSnapshotStore {
        override suspend fun save(export: AccountExport) = Unit
        override suspend fun getLatest(uid: String): AccountExport? = latest
        override suspend fun getHistory(uid: String, server: String): List<AccountExport> = emptyList()
    }

    private val rawJson = """
        {
          "export_format_version": 1,
          "exported_at": "2026-11-25T08:00:00Z",
          "uid": "800000001",
          "server": "os_asia",
          "export_status": {
            "overall_status": "complete",
            "sections": {}
          }
        }
    """.trimIndent()

    @Test
    fun publishesNormalizedSnapshotAndOriginalJsonAfterSuccess() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        try {
            val export = AccountExport(
                snapshot = AccountExportMapper.map(JSONObject(rawJson)),
                rawJson = rawJson
            )
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(
                    uid: String,
                    cookie: String,
                    onProgress: (String) -> Unit
                ): Result<AccountExport> {
                    assertEquals("800000001", uid)
                    assertEquals("test-cookie", cookie)
                    onProgress("Fetching account data")
                    return Result.success(export)
                }
            }
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore(), UnconfinedTestDispatcher())
            store.put("account", viewModel)

            viewModel.exportAccount("800000001", "test-cookie")

            val state = viewModel.uiState.value
            assertFalse(state.isExporting)
            assertNull(state.progress)
            assertNull(state.errorMessage)
            assertEquals("800000001", state.snapshot?.identity?.uid)
            assertEquals(rawJson, state.pendingExport?.rawJson)
            assertFalse(state.isAnalyzingIntelligence)
            assertNotNull(state.intelligenceOverview)
            assertEquals(
                CharacterDevelopmentGoal.BALANCED_DEVELOPMENT,
                state.intelligenceOverview?.selectedGoal
            )
            assertTrue(state.intelligenceOverview?.hasUnverifiedSources == true)

            viewModel.acknowledgeExport()
            assertNull(viewModel.uiState.value.pendingExport)
            assertNotNull(viewModel.uiState.value.snapshot)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun analyzeAccountPublishesIntelligenceWithoutPreparingJsonExport() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        try {
            val export = AccountExport(
                snapshot = AccountExportMapper.map(JSONObject(rawJson)),
                rawJson = rawJson
            )
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(
                    uid: String,
                    cookie: String,
                    onProgress: (String) -> Unit
                ): Result<AccountExport> = Result.success(export)
            }
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore(), UnconfinedTestDispatcher())
            store.put("account", viewModel)

            viewModel.analyzeAccount("800000001", "test-cookie")

            val state = viewModel.uiState.value
            assertFalse(state.isExporting)
            assertNotNull(state.snapshot)
            assertNotNull(state.intelligenceOverview)
            assertNull("Analysis must not trigger the optional JSON-save flow.", state.pendingExport)
            assertNull(state.intelligenceErrorMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun restoresLatestSnapshotWithoutCallingNetworkRepository() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        try {
            val export = AccountExport(
                snapshot = AccountExportMapper.map(JSONObject(rawJson)),
                rawJson = rawJson
            )
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(
                    uid: String,
                    cookie: String,
                    onProgress: (String) -> Unit
                ): Result<AccountExport> {
                    fail("Restoring a cached snapshot must not call the network repository.")
                    return Result.failure(IllegalStateException("unexpected network call"))
                }
            }
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore(export), UnconfinedTestDispatcher())
            store.put("account", viewModel)

            viewModel.restoreLatestSnapshot("800000001")

            val state = viewModel.uiState.value
            assertFalse(state.isRestoringSnapshot)
            assertFalse(state.isExporting)
            assertEquals("800000001", state.snapshot?.identity?.uid)
            assertNotNull(state.intelligenceOverview)
            assertNull(state.pendingExport)
            assertNull(state.intelligenceErrorMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedRefreshKeepsPreviouslyRestoredSnapshotAndOverview() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        try {
            val export = AccountExport(
                snapshot = AccountExportMapper.map(JSONObject(rawJson)),
                rawJson = rawJson
            )
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(
                    uid: String,
                    cookie: String,
                    onProgress: (String) -> Unit
                ): Result<AccountExport> = Result.failure(IllegalStateException("network unavailable"))
            }
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore(export), UnconfinedTestDispatcher())
            store.put("account", viewModel)

            viewModel.restoreLatestSnapshot("800000001")
            val restoredSnapshot = viewModel.uiState.value.snapshot
            val restoredOverview = viewModel.uiState.value.intelligenceOverview
            assertNotNull(restoredSnapshot)
            assertNotNull(restoredOverview)

            viewModel.analyzeAccount("800000001", "test-cookie")

            assertSame(restoredSnapshot, viewModel.uiState.value.snapshot)
            assertSame(restoredOverview, viewModel.uiState.value.intelligenceOverview)
            assertEquals("network unavailable", viewModel.uiState.value.errorMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun exposesRepositoryFailureWithoutCreatingSnapshot() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        try {
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(
                    uid: String,
                    cookie: String,
                    onProgress: (String) -> Unit
                ): Result<AccountExport> = Result.failure(IllegalStateException("network unavailable"))
            }
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore(), UnconfinedTestDispatcher())
            store.put("account", viewModel)

            viewModel.exportAccount("800000001", "test-cookie")

            assertFalse(viewModel.uiState.value.isExporting)
            assertEquals("network unavailable", viewModel.uiState.value.errorMessage)
            assertNull(viewModel.uiState.value.snapshot)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun rejectsBlankCredentialsBeforeCallingRepository() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        try {
            var repositoryCalled = false
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(
                    uid: String,
                    cookie: String,
                    onProgress: (String) -> Unit
                ): Result<AccountExport> {
                    repositoryCalled = true
                    return Result.failure(IllegalStateException("should not be called"))
                }
            }
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore(), UnconfinedTestDispatcher())
            store.put("account", viewModel)

            viewModel.exportAccount("", "")

            assertFalse(repositoryCalled)
            assertEquals("Sign in with HoYoLAB first.", viewModel.uiState.value.errorMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
    @Test
    fun persistsSuccessfulExportWithoutBlockingJsonSaveWhenStorageFails() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        try {
            val export = AccountExport(AccountExportMapper.map(JSONObject(rawJson)), rawJson)
            var savedExport: AccountExport? = null
            val snapshotStore = object : AccountSnapshotStore {
                override suspend fun save(export: AccountExport) {
                    savedExport = export
                    throw IllegalStateException("database unavailable")
                }
                override suspend fun getLatest(uid: String): AccountExport? = null
                override suspend fun getHistory(uid: String, server: String): List<AccountExport> = emptyList()
            }
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(uid: String, cookie: String, onProgress: (String) -> Unit): Result<AccountExport> = Result.success(export)
            }
            val viewModel = AccountViewModel(repository, snapshotStore, UnconfinedTestDispatcher())
            store.put("account", viewModel)
            viewModel.exportAccount("800000001", "test-cookie")
            assertSame(export, savedExport)
            assertFalse(viewModel.uiState.value.isExporting)
            assertEquals("800000001", viewModel.uiState.value.snapshot?.identity?.uid)
            assertEquals(rawJson, viewModel.uiState.value.pendingExport?.rawJson)
            assertEquals("Account retrieved, but local saving failed: database unavailable", viewModel.uiState.value.errorMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun runsDeterministicAnalysisOnInjectedCpuDispatcher() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        val executor = Executors.newSingleThreadExecutor { runnable ->
            Thread(runnable, "account-analysis-test")
        }
        val dispatcher = executor.asCoroutineDispatcher()
        val analysisStarted = CountDownLatch(1)
        val callerThread = Thread.currentThread().name
        var analysisThread: String? = null
        try {
            val export = AccountExport(
                snapshot = AccountExportMapper.map(JSONObject(rawJson)),
                rawJson = rawJson
            )
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(
                    uid: String,
                    cookie: String,
                    onProgress: (String) -> Unit
                ): Result<AccountExport> = Result.success(export)
            }
            val viewModel = AccountViewModel(
                repository = repository,
                snapshotStore = NoOpSnapshotStore(),
                cpuDispatcher = dispatcher,
                analyzeIntelligence = { snapshot: AccountSnapshot, goal ->
                    analysisThread = Thread.currentThread().name
                    analysisStarted.countDown()
                    AccountIntelligenceEngine.analyze(snapshot, goal)
                }
            )
            store.put("account", viewModel)

            viewModel.analyzeAccount("800000001", "test-cookie")

            assertTrue("Analysis did not start on the injected dispatcher.", analysisStarted.await(5, TimeUnit.SECONDS))
            assertNotNull(analysisThread)
            assertNotEquals(callerThread, analysisThread)
        } finally {
            store.clear()
            dispatcher.close()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun sharedRefreshQueuesPortableRetrievalBehindInitialSnapshotRestore() = runBlocking {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val store = ViewModelStore()
        val restoreGate = CompletableDeferred<AccountExport?>()
        var repositoryCalls = 0
        try {
            val export = AccountExport(
                snapshot = AccountExportMapper.map(JSONObject(rawJson)),
                rawJson = rawJson
            )
            val snapshotStore = object : AccountSnapshotStore {
                override suspend fun save(export: AccountExport) = Unit
                override suspend fun getLatest(uid: String): AccountExport? = restoreGate.await()
                override suspend fun getHistory(uid: String, server: String): List<AccountExport> = emptyList()
            }
            val repository = object : AccountRepository {
                override suspend fun fetchAccountExport(
                    uid: String,
                    cookie: String,
                    onProgress: (String) -> Unit
                ): Result<AccountExport> {
                    repositoryCalls++
                    return Result.success(export)
                }
            }
            val viewModel = AccountViewModel(repository, snapshotStore, UnconfinedTestDispatcher())
            store.put("account", viewModel)

            viewModel.restoreLatestSnapshot("800000001")
            assertTrue(viewModel.uiState.value.isRestoringSnapshot)

            viewModel.analyzeAccount("800000001", "test-cookie")
            assertEquals("Portable retrieval must wait for cache restoration.", 0, repositoryCalls)

            restoreGate.complete(null)
            assertEquals(1, repositoryCalls)
            assertFalse(viewModel.uiState.value.isExporting)
            assertNotNull(viewModel.uiState.value.snapshot)
            assertNull("Shared refresh must not create an export document.", viewModel.uiState.value.pendingExport)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

}
