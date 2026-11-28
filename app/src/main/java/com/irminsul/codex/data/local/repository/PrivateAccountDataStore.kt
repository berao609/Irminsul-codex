package com.irminsul.codex.data.local.repository

import com.irminsul.codex.api.AccountProfile
import com.irminsul.codex.api.DailyNote
import java.time.Instant

data class CachedPrivateData<T>(
    val value: T,
    val retrievedAt: Instant
)

data class PrivateAccountDataCache(
    val uid: String,
    val dailyNote: CachedPrivateData<DailyNote>?,
    val accountProfile: CachedPrivateData<AccountProfile>?,
    val dailyNoteError: String? = null,
    val accountProfileError: String? = null
)

/**
 * Stores app-private HoYoLAB presentation data separately from portable account JSON.
 * Authentication cookies are not accepted or persisted by this contract.
 */
interface PrivateAccountDataStore {
    suspend fun get(uid: String): PrivateAccountDataCache?
    suspend fun saveDailyNote(uid: String, value: DailyNote, retrievedAt: Instant)
    suspend fun saveAccountProfile(uid: String, value: AccountProfile, retrievedAt: Instant)
}
