package com.irminsul.codex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.irminsul.codex.data.local.entity.AccountEntity
import com.irminsul.codex.data.local.entity.AccountSnapshotEntity
import com.irminsul.codex.data.local.entity.AccountSnapshotJsonChunkEntity

@Dao
interface AccountSnapshotDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccountIfMissing(account: AccountEntity)

    @Query("""
        UPDATE accounts
        SET lastUpdatedAtEpochMillis = MAX(lastUpdatedAtEpochMillis, :timestamp)
        WHERE uid = :uid AND server = :server
    """)
    suspend fun updateLastUpdatedAt(uid: String, server: String, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSnapshot(snapshot: AccountSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertJsonChunks(chunks: List<AccountSnapshotJsonChunkEntity>)

    @Query("""
        SELECT content FROM account_snapshot_json_chunks
        WHERE snapshotId = :snapshotId
        ORDER BY chunkIndex ASC
    """)
    suspend fun getJsonChunks(snapshotId: String): List<String>

    @Query("""
        SELECT * FROM account_snapshots
        WHERE uid = :uid
        ORDER BY exportedAtEpochMillis DESC, snapshotId DESC
        LIMIT 1
    """)
    suspend fun getLatestSnapshot(uid: String): AccountSnapshotEntity?

    @Query("""
        SELECT * FROM account_snapshots
        WHERE uid = :uid AND server = :server
        ORDER BY exportedAtEpochMillis DESC, snapshotId DESC
    """)
    suspend fun getSnapshotHistory(uid: String, server: String): List<AccountSnapshotEntity>

    @Query("SELECT * FROM accounts WHERE uid = :uid AND server = :server LIMIT 1")
    suspend fun getAccount(uid: String, server: String): AccountEntity?
}
