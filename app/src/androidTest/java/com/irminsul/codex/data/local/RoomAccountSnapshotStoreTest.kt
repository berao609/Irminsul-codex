package com.irminsul.codex.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.irminsul.codex.data.local.repository.RoomAccountSnapshotStore
import com.irminsul.codex.data.mapper.AccountExportMapper
import com.irminsul.codex.domain.model.AccountExport
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class RoomAccountSnapshotStoreTest {
    private lateinit var database: IrminsulDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            IrminsulDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun savesMultipleImmutableSnapshotsAndPreservesExactRawJson() = runBlocking {
        val firstJson = exportJson("2026-11-25T08:00:00Z")
        val secondJson = exportJson("2026-11-25T09:00:00Z")
        val ids = sequenceOf("snapshot-1", "snapshot-2").iterator()
        val store = RoomAccountSnapshotStore(
            database = database,
            snapshotIdProvider = { ids.next() }
        )

        store.save(toExport(firstJson))
        store.save(toExport(secondJson))

        val history = store.getHistory("800000001", "os_asia")
        assertEquals(2, history.size)
        assertEquals(secondJson, history[0].rawJson)
        assertEquals(firstJson, history[1].rawJson)
        assertEquals("2026-11-25T09:00:00Z", history[0].snapshot.metadata.exportedAt.toString())

        val account = database.accountSnapshotDao().getAccount("800000001", "os_asia")
        assertNotNull(account)
        assertEquals(Instant.parse("2026-11-25T08:00:00Z").toEpochMilli(), account!!.createdAtEpochMillis)
        assertEquals(Instant.parse("2026-11-25T09:00:00Z").toEpochMilli(), account.lastUpdatedAtEpochMillis)
    }

    @Test
    fun keepsAccountsWithSameUidOnDifferentServersSeparate() = runBlocking {
        val ids = sequenceOf("asia", "america").iterator()
        val store = RoomAccountSnapshotStore(
            database = database,
            snapshotIdProvider = { ids.next() }
        )
        store.save(toExport(exportJson("2026-11-25T08:00:00Z", "os_asia")))
        store.save(toExport(exportJson("2026-11-25T08:30:00Z", "os_usa")))
        assertEquals(1, store.getHistory("800000001", "os_asia").size)
        assertEquals(1, store.getHistory("800000001", "os_usa").size)
    }

    private fun toExport(json: String) =
        AccountExport(AccountExportMapper.map(JSONObject(json)), json)

    private fun exportJson(timestamp: String, server: String = "os_asia") = """
        {
          "export_format_version": 1,
          "exported_at": "$timestamp",
          "uid": "800000001",
          "server": "$server",
          "export_status": {
            "overall_status": "complete",
            "sections": {}
          }
        }
    """.trimIndent()
}
