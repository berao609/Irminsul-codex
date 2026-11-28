package com.irminsul.codex.data.repository

import com.irminsul.codex.api.AccountProfile
import com.irminsul.codex.api.DailyNote
import com.irminsul.codex.api.HoYoLabClient
import com.irminsul.codex.data.local.repository.PrivateAccountDataStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Instant

data class PrivateDataChannelFetch<T>(
    val result: Result<T>,
    val retrievedAt: Instant,
    val persistenceError: String? = null
)

data class PrivateAccountDataRefresh(
    val dailyNote: PrivateDataChannelFetch<DailyNote>,
    val accountProfile: PrivateDataChannelFetch<AccountProfile>
)

/**
 * Coordinates independent private HoYoLAB presentation-data requests.
 *
 * Both requests share the caller's authenticated session but have separate results.
 * A failure in one request does not discard a successful sibling result.
 */
class HoYoLabPrivateDataCoordinator(
    private val fetchDailyNote: suspend (String, String) -> Result<DailyNote> = { uid, cookie ->
        HoYoLabClient.fetchDailyNote(uid, cookie)
    },
    private val fetchAccountProfile: suspend (String, String) -> Result<AccountProfile> = { uid, cookie ->
        HoYoLabClient.fetchAccountProfile(uid, cookie)
    },
    private val clock: () -> Instant = Instant::now,
    private val privateDataStore: PrivateAccountDataStore? = null
) {
    suspend fun restore(uid: String) = privateDataStore?.get(uid)

    suspend fun refresh(uid: String, cookie: String): PrivateAccountDataRefresh = coroutineScope {
        val dailyNoteDeferred = async { fetchSafely { fetchDailyNote(uid, cookie) } }
        val profileDeferred = async { fetchSafely { fetchAccountProfile(uid, cookie) } }
        val dailyNote = persistDailyNote(uid, dailyNoteDeferred.await())
        val accountProfile = persistAccountProfile(uid, profileDeferred.await())
        PrivateAccountDataRefresh(dailyNote = dailyNote, accountProfile = accountProfile)
    }

    private suspend fun persistDailyNote(
        uid: String,
        fetched: PrivateDataChannelFetch<DailyNote>
    ): PrivateDataChannelFetch<DailyNote> {
        val value = fetched.result.getOrNull() ?: return fetched
        val store = privateDataStore ?: return fetched
        return try {
            store.saveDailyNote(uid, value, fetched.retrievedAt)
            fetched
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            fetched.copy(persistenceError = error.message ?: "Private Daily Note cache could not be saved.")
        }
    }

    private suspend fun persistAccountProfile(
        uid: String,
        fetched: PrivateDataChannelFetch<AccountProfile>
    ): PrivateDataChannelFetch<AccountProfile> {
        val value = fetched.result.getOrNull() ?: return fetched
        val store = privateDataStore ?: return fetched
        return try {
            store.saveAccountProfile(uid, value, fetched.retrievedAt)
            fetched
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            fetched.copy(persistenceError = error.message ?: "Private profile cache could not be saved.")
        }
    }

    private suspend fun <T> fetchSafely(
        fetch: suspend () -> Result<T>
    ): PrivateDataChannelFetch<T> {
        val result = try {
            fetch()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            Result.failure(error)
        }
        result.exceptionOrNull()?.let { error ->
            if (error is CancellationException) throw error
        }
        return PrivateDataChannelFetch(result, clock())
    }
}
