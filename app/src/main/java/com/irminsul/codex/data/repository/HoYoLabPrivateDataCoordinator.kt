package com.irminsul.codex.data.repository

import com.irminsul.codex.api.AccountProfile
import com.irminsul.codex.api.DailyNote
import com.irminsul.codex.api.HoYoLabClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.time.Instant

data class PrivateDataChannelFetch<T>(
    val result: Result<T>,
    val retrievedAt: Instant
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
    private val clock: () -> Instant = Instant::now
) {
    suspend fun refresh(uid: String, cookie: String): PrivateAccountDataRefresh = coroutineScope {
        val dailyNote = async { fetchSafely { fetchDailyNote(uid, cookie) } }
        val accountProfile = async { fetchSafely { fetchAccountProfile(uid, cookie) } }
        PrivateAccountDataRefresh(
            dailyNote = dailyNote.await(),
            accountProfile = accountProfile.await()
        )
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
