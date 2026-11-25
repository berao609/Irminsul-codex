package com.irminsul.codex.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.irminsul.codex.domain.model.AccountExport
import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.repository.AccountRepository
import kotlinx.coroutines.CancellationException
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
    val saveImmediately: Boolean = false
)

/**
 * Owns account-export work and the normalized snapshot state observed by the UI.
 * Android document and clipboard actions remain in the Compose presentation layer.
 */
class AccountViewModel(
    private val repository: AccountRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountExportUiState())
    val uiState: StateFlow<AccountExportUiState> = _uiState.asStateFlow()

    fun exportAccount(uid: String, cookie: String, saveImmediately: Boolean) {
        if (uid.isBlank() || cookie.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in with HoYoLAB first.") }
            return
        }
        if (_uiState.value.isExporting) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    progress = "Starting extraction...",
                    errorMessage = null,
                    pendingExport = null,
                    saveImmediately = saveImmediately
                )
            }

            try {
                val result = repository.fetchAccountExport(uid.trim(), cookie.trim()) { message ->
                    _uiState.update { state -> state.copy(progress = message) }
                }
                result.fold(
                    onSuccess = { export ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                progress = null,
                                errorMessage = null,
                                snapshot = export.snapshot,
                                pendingExport = export
                            )
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isExporting = false,
                                progress = null,
                                errorMessage = error.message ?: "JSON extraction failed.",
                                saveImmediately = false
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
                        errorMessage = error.message ?: "JSON extraction failed.",
                        saveImmediately = false
                    )
                }
            }
        }
    }

    fun acknowledgeExport() {
        _uiState.update { it.copy(pendingExport = null, saveImmediately = false) }
    }

    class Factory(
        private val repository: AccountRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
                return AccountViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
