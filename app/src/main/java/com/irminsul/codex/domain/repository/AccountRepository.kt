package com.irminsul.codex.domain.repository

import com.irminsul.codex.domain.model.AccountSnapshot

/**
 * Domain boundary for retrieving a normalized account snapshot.
 *
 * The credential parameter is transitional: the existing application currently owns
 * credential collection and storage in the presentation layer. A dedicated credential
 * provider can replace it in a later architecture increment without changing consumers.
 */
interface AccountRepository {
    suspend fun fetchAccountSnapshot(
        uid: String,
        cookie: String,
        onProgress: (String) -> Unit = {}
    ): Result<AccountSnapshot>
}
