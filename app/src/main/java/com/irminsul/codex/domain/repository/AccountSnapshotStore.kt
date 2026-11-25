package com.irminsul.codex.domain.repository

import com.irminsul.codex.domain.model.AccountExport

/**
 * Local persistence boundary for immutable account export snapshots.
 * Authentication credentials are deliberately excluded from this contract.
 */
interface AccountSnapshotStore {
    suspend fun save(export: AccountExport)
    suspend fun getHistory(uid: String, server: String): List<AccountExport>
}
