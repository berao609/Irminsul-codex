package com.irminsul.codex.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.irminsul.codex.domain.analysis.AccountIntelligenceEngine
import com.irminsul.codex.domain.model.AccountExport
import com.irminsul.codex.domain.model.AccountIntelligenceOverview
import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.model.CharacterDevelopmentGoal
import com.irminsul.codex.domain.repository.AccountRepository
import com.irminsul.codex.domain.repository.AccountSnapshotStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountExportUiState(
    val isExporting: Boolean = false,
    val progress: String? = null,
    val errorMessage: String? = null,
    val snapshot: AccountSnapshot? = null,
    val pendingExport: AccountExport? = null,
    val isAnalyzingIntelligence: Boolean = false,
    val isRestoringSnapshot: Boolean = false,
    val intelligenceOverview: AccountIntelligenceOverview? = null,
    val intelligenceErrorMessage: String? = null,
)

/**
 * Owns account-export work, local snapshot persistence, and deterministic account
 * intelligence state observed by the UI. Android document saving remains in Compose.
 */
class AccountViewModel(
    private val repository: AccountRepository,
    private val snapshotStore: AccountSnapshotStore,
    private val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountExportUiState())
    val uiState: StateFlow<AccountExportUiState> = _uiState.asStateFlow()

    /**
     * Restores the latest locally saved snapshot for this UID and rebuilds its
     * deterministic overview without contacting HoYoLAB.
     */
    fun restoreLatestSnapshot(uid: String) {
        val normalizedUid = uid.trim()
        if (normalizedUid.isBlank() || _uiState.value.isRestoringSnapshot || _uiState.value.isExporting) return
        if (_uiState.value.snapshot?.identity?.uid == normalizedUid &&
            _uiState.value.intelligenceOverview != null
        ) return

        viewModelScope.launch {
            _uiState.update { state ->
                if (state.snapshot?.identity?.uid != normalizedUid) {
                    state.copy(
                        snapshot = null,
                        intelligenceOverview = null,
                        intelligenceErrorMessage = null,
                        pendingExport = null,
                        isRestoringSnapshot = true
                    )
                } else {
                    state.copy(isRestoringSnapshot = true)
                }
            }
            try {
                val cached = snapshotStore.getLatest(normalizedUid)
                if (cached != null && cached.snapshot.identity.uid == normalizedUid) {
                    _uiState.update {
                        it.copy(
                            snapshot = cached.snapshot,
                            pendingExport = null,
                            errorMessage = null,
                            intelligenceErrorMessage = null
                        )
                    }
                    analyzeSnapshot(cached.snapshot)
                } else {
                    if (cached != null) {
                        throw IllegalStateException("Saved snapshot UID does not match the signed-in account.")
                    }
                    _uiState.update {
                        it.copy(
                            snapshot = null,
                            intelligenceOverview = null,
                            intelligenceErrorMessage = null
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        intelligenceErrorMessage = "Could not restore the saved account analysis: ${error.message ?: "local storage error"}"
                    )
                }
            } finally {
                _uiState.update { it.copy(isRestoringSnapshot = false) }
            }
        }
    }

    fun exportAccount(uid: String, cookie: String) {
        retrieveAccount(uid, cookie, prepareJsonExport = true)
    }

    /**
     * Retrieves and analyzes the account without opening the optional JSON-save flow.
     * Account intelligence is a first-class in-app action, independent of export.
     */
    fun analyzeAccount(uid: String, cookie: String) {
        retrieveAccount(uid, cookie, prepareJsonExport = false)
    }

    private fun retrieveAccount(uid: String, cookie: String, prepareJsonExport: Boolean) {
        if (uid.isBlank() || cookie.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in with HoYoLAB first.") }
            return
        }
        if (_uiState.value.isExporting || _uiState.value.isRestoringSnapshot) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    progress = if (prepareJsonExport) "Starting extraction..." else "Retrieving account for analysis...",
                    errorMessage = null,
                    pendingExport = null,
                    intelligenceErrorMessage = null,
                    isAnalyzingIntelligence = false,
                )
            }

            try {
                val result = repository.fetchAccountExport(uid.trim(), cookie.trim()) { message ->
                    _uiState.update { state -> state.copy(progress = message) }
                }
                result.fold(
                    onSuccess = { export ->
                        var persistenceError: String? = null
                        try {
                            snapshotStore.save(export)
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (error: Exception) {
                            persistenceError = "Account retrieved, but local saving failed: ${error.message ?: "unknown storage error"}"
                        }
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                progress = null,
                                errorMessage = persistenceError,
                                snapshot = export.snapshot,
                                pendingExport = if (prepareJsonExport) export else null
                            )
                        }
                        analyzeSnapshot(export.snapshot)
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                progress = null,
                                errorMessage = error.message ?: "Account retrieval failed.",
                            )
                        }
                    }
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        progress = null,
                        errorMessage = error.message ?: "Account retrieval failed.",
                    )
                }
            }
        }
    }

    private suspend fun analyzeSnapshot(snapshot: AccountSnapshot) {
        _uiState.update {
            it.copy(
                isAnalyzingIntelligence = true,
                intelligenceOverview = null,
                intelligenceErrorMessage = null
            )
        }
        try {
            val overview = withContext(cpuDispatcher) {
                AccountIntelligenceEngine.analyze(snapshot, CharacterDevelopmentGoal.BALANCED_DEVELOPMENT)
            }
            _uiState.update {
                it.copy(
                    isAnalyzingIntelligence = false,
                    intelligenceOverview = overview,
                    intelligenceErrorMessage = null
                )
            }
        } catch (cancelled: CancellationException) {
            _uiState.update { it.copy(isAnalyzingIntelligence = false) }
            throw cancelled
        } catch (error: Exception) {
            _uiState.update {
                it.copy(
                    isAnalyzingIntelligence = false,
                    intelligenceOverview = null,
                    intelligenceErrorMessage = error.message ?: "Account analysis failed."
                )
            }
        }
    }

    fun acknowledgeExport() {
        _uiState.update { it.copy(pendingExport = null) }
    }

    class Factory(
        private val repository: AccountRepository,
        private val snapshotStore: AccountSnapshotStore,
        private val cpuDispatcher: CoroutineDispatcher = Dispatchers.Default
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                return AccountViewModel(repository, snapshotStore, cpuDispatcher) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
