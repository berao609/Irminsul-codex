package com.irminsul.codex.data.local

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IrminsulDatabaseMigrationTest {
    @Test
    fun migratesVersionOneSnapshotsWithoutChangingTheirRawJsonOrMetadata() {
        val databaseName = "irminsul-migration-1-2-test"
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(databaseName)

        val smallJson = """{"export_format_version":1,"uid":"800000001","note":"legacy"}"""
        val largeJson = buildString {
            append("""{"export_format_version":1,"uid":"800000001","payload":"""")
            repeat(32 * 1024 * 3 + 17) { index ->
                append(if (index % 97 == 0) "😀" else "x")
            }
            append("""" }""".trim())
        }

        val legacyHelper = object : SQLiteOpenHelper(context, databaseName, null, 1) {
            override fun onCreate(database: SQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE accounts (
                        uid TEXT NOT NULL,
                        server TEXT NOT NULL,
                        createdAtEpochMillis INTEGER NOT NULL,
                        lastUpdatedAtEpochMillis INTEGER NOT NULL,
                        PRIMARY KEY(uid, server)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE account_snapshots (
                        snapshotId TEXT NOT NULL PRIMARY KEY,
                        uid TEXT NOT NULL,
                        server TEXT NOT NULL,
                        exportedAt TEXT NOT NULL,
                        exportedAtEpochMillis INTEGER NOT NULL,
                        exportFormatVersion INTEGER NOT NULL,
                        overallStatus TEXT NOT NULL,
                        origin TEXT NOT NULL,
                        rawJson TEXT NOT NULL,
                        FOREIGN KEY(uid, server) REFERENCES accounts(uid, server)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE INDEX index_account_snapshots_uid_server_exportedAtEpochMillis
                    ON account_snapshots(uid, server, exportedAtEpochMillis)
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE TABLE room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)"
                )
                database.execSQL(
                    "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                        "VALUES(42, '25361700f4d8b417e6c36322528b17f7')"
                )
            }

            override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
                error("Unexpected legacy database upgrade: $oldVersion to $newVersion")
            }
        }

        legacyHelper.writableDatabase.apply {
            execSQL(
                "INSERT INTO accounts(uid, server, createdAtEpochMillis, lastUpdatedAtEpochMillis) VALUES(?, ?, ?, ?)",
                arrayOf<Any>("800000001", "os_asia", 1000L, 2000L)
            )
            insertLegacySnapshot("snapshot-small", smallJson, 3000L)
            insertLegacySnapshot("snapshot-large", largeJson, 4000L)
            close()
        }
        legacyHelper.close()

        val migrated = Room.databaseBuilder(
            context,
            IrminsulDatabase::class.java,
            databaseName
        )
            .addMigrations(IrminsulDatabase.MIGRATION_1_2)
            .build()

        try {
            // Opening the production Room database executes MIGRATION_1_2 and
            // performs Room's own schema validation without MigrationTestHelper.
            val database = migrated.openHelper.writableDatabase
            val snapshots = database.query(
                """
                SELECT snapshotId, uid, server, exportedAt, exportedAtEpochMillis,
                       exportFormatVersion, overallStatus, origin, rawJson
                FROM account_snapshots
                ORDER BY snapshotId
                """.trimIndent()
            )
            snapshots.use { cursor ->
                assertEquals(2, cursor.count)
                while (cursor.moveToNext()) {
                    val snapshotId = cursor.getString(0)
                    assertEquals("800000001", cursor.getString(1))
                    assertEquals("os_asia", cursor.getString(2))
                    assertEquals("2026-11-25T08:00:00Z", cursor.getString(3))
                    assertEquals(1, cursor.getInt(5))
                    assertEquals("SUCCESS", cursor.getString(6))
                    assertEquals("hoyolab", cursor.getString(7))
                    assertEquals("", cursor.getString(8))

                    val chunksCursor = database.query(
                        "SELECT content FROM account_snapshot_json_chunks WHERE snapshotId = ? ORDER BY chunkIndex",
                        arrayOf(snapshotId)
                    )
                    val chunks = chunksCursor.use { chunkCursor ->
                        buildList {
                            while (chunkCursor.moveToNext()) {
                                add(chunkCursor.getString(0))
                            }
                        }
                    }
                    val expectedJson = when (snapshotId) {
                        "snapshot-small" -> smallJson
                        "snapshot-large" -> largeJson
                        else -> error("Unexpected migrated snapshot: $snapshotId")
                    }
                    assertEquals(expectedJson, chunks.joinToString(separator = ""))
                    if (snapshotId == "snapshot-small") {
                        assertEquals(1, chunks.size)
                    } else {
                        assertTrue(chunks.size > 1)
                    }
                }
            }

            val accountCursor = database.query(
                "SELECT createdAtEpochMillis, lastUpdatedAtEpochMillis FROM accounts WHERE uid = ? AND server = ?",
                arrayOf("800000001", "os_asia")
            )
            accountCursor.use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1000L, cursor.getLong(0))
                assertEquals(2000L, cursor.getLong(1))
            }
        } finally {
            migrated.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun SQLiteDatabase.insertLegacySnapshot(
        snapshotId: String,
        rawJson: String,
        timestamp: Long
    ) {
        execSQL(
            """
            INSERT INTO account_snapshots(
                snapshotId, uid, server, exportedAt, exportedAtEpochMillis,
                exportFormatVersion, overallStatus, origin, rawJson
            ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any>(
                snapshotId,
                "800000001",
                "os_asia",
                "2026-11-25T08:00:00Z",
                timestamp,
                1,
                "SUCCESS",
                "hoyolab",
                rawJson
            )
        )
    }
}
