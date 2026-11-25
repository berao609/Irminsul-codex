package com.irminsul.codex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.irminsul.codex.data.local.dao.AccountSnapshotDao
import com.irminsul.codex.data.local.entity.AccountEntity
import com.irminsul.codex.data.local.entity.AccountSnapshotEntity
import com.irminsul.codex.data.local.entity.AccountSnapshotJsonChunkEntity

@Database(
    entities = [
        AccountEntity::class,
        AccountSnapshotEntity::class,
        AccountSnapshotJsonChunkEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class IrminsulDatabase : RoomDatabase() {
    abstract fun accountSnapshotDao(): AccountSnapshotDao

    companion object {
        private const val JSON_CHUNK_SIZE = SnapshotJsonChunks.CHUNK_SIZE

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS account_snapshot_json_chunks (
                        snapshotId TEXT NOT NULL,
                        chunkIndex INTEGER NOT NULL,
                        content TEXT NOT NULL,
                        PRIMARY KEY(snapshotId, chunkIndex),
                        FOREIGN KEY(snapshotId) REFERENCES account_snapshots(snapshotId)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_account_snapshot_json_chunks_snapshotId " +
                        "ON account_snapshot_json_chunks(snapshotId)"
                )

                // Read only IDs and lengths through CursorWindow. The large JSON stays
                // inside SQLite and is split with SQL substr(), never materialized here.
                val snapshots = mutableListOf<Pair<String, Long>>()
                database.query(
                    "SELECT snapshotId, length(rawJson) FROM account_snapshots WHERE length(rawJson) > 0"
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        snapshots += cursor.getString(0) to cursor.getLong(1)
                    }
                }

                snapshots.forEach { (snapshotId, jsonLength) ->
                    var chunkIndex = 0
                    var start = 1L
                    while (start <= jsonLength) {
                        val count = minOf(JSON_CHUNK_SIZE.toLong(), jsonLength - start + 1L)
                        database.execSQL(
                            """
                            INSERT INTO account_snapshot_json_chunks(snapshotId, chunkIndex, content)
                            SELECT snapshotId, ?, substr(rawJson, ?, ?)
                            FROM account_snapshots
                            WHERE snapshotId = ?
                            """.trimIndent(),
                            arrayOf(chunkIndex, start, count, snapshotId)
                        )
                        start += count
                        chunkIndex++
                    }
                    database.execSQL(
                        "UPDATE account_snapshots SET rawJson = '' WHERE snapshotId = ?",
                        arrayOf(snapshotId)
                    )
                }
            }
        }

        @Volatile private var instance: IrminsulDatabase? = null

        fun getInstance(context: Context): IrminsulDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IrminsulDatabase::class.java,
                    "irminsul_codex.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instance = it }
            }
    }
}
