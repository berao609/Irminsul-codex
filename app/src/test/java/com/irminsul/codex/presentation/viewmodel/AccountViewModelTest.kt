package com.irminsul.codex.presentation.viewmodel

import androidx.lifecycle.ViewModelStore
import com.irminsul.codex.data.mapper.AccountExportMapper
import com.irminsul.codex.domain.model.AccountExport
import com.irminsul.codex.domain.model.CharacterDevelopmentGoal
import com.irminsul.codex.domain.repository.AccountRepository
import com.irminsul.codex.domain.repository.AccountSnapshotStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
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
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore())
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
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore())
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
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore(export))
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
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore(export))
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
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore())
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
            val viewModel = AccountViewModel(repository, NoOpSnapshotStore())
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
            val viewModel = AccountViewModel(repository, snapshotStore)
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

}
