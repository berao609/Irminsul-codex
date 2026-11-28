package com.irminsul.codex.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.irminsul.codex.api.AccountProfile
import com.irminsul.codex.api.DailyNote
import com.irminsul.codex.data.local.repository.RoomPrivateAccountDataStore
import com.irminsul.codex.data.local.entity.PrivateAccountDataEntity
import org.json.JSONObject
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class RoomPrivateAccountDataStoreTest {
    private lateinit var database: IrminsulDatabase
    private lateinit var store: RoomPrivateAccountDataStore

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            IrminsulDatabase::class.java
        ).allowMainThreadQueries().build()
        store = RoomPrivateAccountDataStore(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun savesAndRestoresPrivateChannelsWithIndependentTimestamps() = runBlocking {
        val dailyAt = Instant.parse("2026-11-28T01:00:00Z")
        val profileAt = Instant.parse("2026-11-28T01:05:00Z")
        val note = DailyNote(
            currentResin = 0, maxResin = 200, resinRecoveryTime = null,
            finishedTaskNum = 4, totalTaskNum = 4, isExtraTaskRewardReceived = false,
            remainResinDiscountNum = 3, resinDiscountNumLimit = 3,
            currentHomeCoin = null, maxHomeCoin = null,
            currentExpeditionNum = 5, maxExpeditionNum = 5,
            transformerObtained = null, transformerRecoveryTimeSeconds = null,
            transformerReached = null
        )
        val profile = AccountProfile("Traveler", "800000001", "os_asia", "https://example.test/avatar.png")
        store.saveDailyNote("800000001", note, dailyAt)
        store.saveAccountProfile("800000001", profile, profileAt)
        val restored = store.get("800000001")!!
        assertEquals(note, restored.dailyNote?.value)
        assertEquals(dailyAt, restored.dailyNote?.retrievedAt)
        assertEquals(profile, restored.accountProfile?.value)
        assertEquals(profileAt, restored.accountProfile?.retrievedAt)
        assertNull(restored.dailyNoteError)
        assertNull(restored.accountProfileError)
    }

    @Test
    fun updatingOneChannelPreservesTheOtherChannel() = runBlocking {
        val timestamp = Instant.parse("2026-11-28T01:00:00Z")
        val profile = AccountProfile("Traveler", "800000001", "os_asia", null)
        val first = DailyNote(
            currentResin = 100, maxResin = 200, resinRecoveryTime = 36_000L,
            finishedTaskNum = null, totalTaskNum = null, isExtraTaskRewardReceived = null,
            remainResinDiscountNum = null, resinDiscountNumLimit = null,
            currentHomeCoin = null, maxHomeCoin = null, currentExpeditionNum = null,
            maxExpeditionNum = null, transformerObtained = null,
            transformerRecoveryTimeSeconds = null, transformerReached = null
        )
        store.saveAccountProfile("800000001", profile, timestamp)
        store.saveDailyNote("800000001", first, timestamp)
        store.saveDailyNote("800000001", first.copy(currentResin = 101), timestamp.plusSeconds(60))
        val restored = store.get("800000001")!!
        assertEquals(101, restored.dailyNote?.value?.currentResin)
        assertEquals(profile, restored.accountProfile?.value)
    }

    @Test
    fun malformedDailyNoteCacheDoesNotHideValidProfileCache() = runBlocking {
        val uid = "800000001"
        val timestamp = Instant.parse("2026-11-28T01:00:00Z").toString()
        val dao = database.privateAccountDataDao()
        dao.insertIfMissing(PrivateAccountDataEntity(uid = uid))
        dao.updateDailyNote(uid, "{malformed", timestamp)
        val profileJson = JSONObject()
            .put("nickname", "Traveler")
            .put("uid", uid)
            .put("serverName", "os_asia")
            .put("avatarUrl", JSONObject.NULL)
            .toString()
        dao.updateAccountProfile(uid, profileJson, timestamp)

        val restored = store.get(uid)!!
        assertNull(restored.dailyNote)
        assertNotNull(restored.dailyNoteError)
        assertEquals("Traveler", restored.accountProfile?.value?.nickname)
        assertNull(restored.accountProfileError)
    }

    @Test
    fun missingAccountReturnsNoPrivateCache() = runBlocking {
        assertNull(store.get("800000001"))
    }
}
