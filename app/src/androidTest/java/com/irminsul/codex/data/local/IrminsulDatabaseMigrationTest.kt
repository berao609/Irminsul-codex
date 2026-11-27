package com.irminsul.codex.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IrminsulDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        IrminsulDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

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

        helper.createDatabase(databaseName, 1).apply {
            execSQL(
                "INSERT INTO accounts(uid, server, createdAtEpochMillis, lastUpdatedAtEpochMillis) VALUES(?, ?, ?, ?)",
                arrayOf<Any>("800000001", "os_asia", 1000L, 2000L)
            )
            insertLegacySnapshot("snapshot-small", smallJson, 3000L)
            insertLegacySnapshot("snapshot-large", largeJson, 4000L)
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            databaseName,
            2,
            true,
            IrminsulDatabase.MIGRATION_1_2
        )

        try {
            val snapshots = migrated.query(
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

                    val chunksCursor = migrated.query(
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

            val accountCursor = migrated.query(
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

    private fun androidx.sqlite.db.SupportSQLiteDatabase.insertLegacySnapshot(
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
