package com.irminsul.codex.presentation.viewmodel

import androidx.lifecycle.ViewModelStore
import com.irminsul.codex.data.mapper.AccountExportMapper
import com.irminsul.codex.domain.model.AccountExport
import com.irminsul.codex.domain.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class AccountViewModelTest {
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
            val viewModel = AccountViewModel(repository)
            store.put("account", viewModel)

            viewModel.exportAccount("800000001", "test-cookie")

            val state = viewModel.uiState.value
            assertFalse(state.isExporting)
            assertNull(state.progress)
            assertNull(state.errorMessage)
            assertEquals("800000001", state.snapshot?.identity?.uid)
            assertEquals(rawJson, state.pendingExport?.rawJson)

            viewModel.acknowledgeExport()
            assertNull(viewModel.uiState.value.pendingExport)
            assertNotNull(viewModel.uiState.value.snapshot)
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
            val viewModel = AccountViewModel(repository)
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
            val viewModel = AccountViewModel(repository)
            store.put("account", viewModel)

            viewModel.exportAccount("", "")

            assertFalse(repositoryCalled)
            assertEquals("Sign in with HoYoLAB first.", viewModel.uiState.value.errorMessage)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
