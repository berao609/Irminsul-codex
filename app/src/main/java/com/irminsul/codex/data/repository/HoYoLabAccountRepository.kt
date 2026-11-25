package com.irminsul.codex.data.repository

import com.irminsul.codex.api.HoYoLabClient
import com.irminsul.codex.data.mapper.AccountExportMapper
import com.irminsul.codex.domain.model.AccountExport
import com.irminsul.codex.domain.model.AccountSnapshot
import kotlinx.coroutines.CancellationException
import com.irminsul.codex.domain.repository.AccountRepository
import org.json.JSONObject

/**
 * Adapts the existing HoYoLAB JSON export workflow to the domain repository contract.
 *
 * Export retrieval and mapping are injectable so the adapter can be tested without
 * network access or real account credentials.
 */
class HoYoLabAccountRepository(
    private val exportJson: suspend (
        uid: String,
        cookie: String,
        onProgress: (String) -> Unit
    ) -> Result<String> = { uid, cookie, onProgress ->
        HoYoLabClient.exportFullAccountJson(uid, cookie, onProgress)
    },
    private val mapExport: (String) -> AccountSnapshot = { json ->
        AccountExportMapper.map(JSONObject(json))
    }
) : AccountRepository {

    override suspend fun fetchAccountExport(
        uid: String,
        cookie: String,
        onProgress: (String) -> Unit
    ): Result<AccountExport> {
        val exportResult = exportJson(uid, cookie, onProgress)
        return exportResult.fold(
            onSuccess = { json ->
                try {
                    Result.success(AccountExport(snapshot = mapExport(json), rawJson = json))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: Exception) {
                    Result.failure(error)
                }
            },
            onFailure = { error -> Result.failure(error) }
        )
    }
}
