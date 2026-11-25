package com.irminsul.codex.data.local.repository

import androidx.room.withTransaction
import com.irminsul.codex.data.local.IrminsulDatabase
import com.irminsul.codex.data.local.SnapshotJsonChunks
import com.irminsul.codex.data.local.entity.AccountEntity
import com.irminsul.codex.data.local.entity.AccountSnapshotEntity
import com.irminsul.codex.data.local.entity.AccountSnapshotJsonChunkEntity
import com.irminsul.codex.data.mapper.AccountExportMapper
import com.irminsul.codex.domain.model.AccountExport
import com.irminsul.codex.domain.model.AccountSnapshot
import com.irminsul.codex.domain.repository.AccountSnapshotStore
import org.json.JSONObject
import java.util.UUID

class RoomAccountSnapshotStore(
    private val database: IrminsulDatabase,
    private val snapshotIdProvider: () -> String = { UUID.randomUUID().toString() },
    private val mapExport: (String) -> AccountSnapshot = { json ->
        AccountExportMapper.map(JSONObject(json))
    }
) : AccountSnapshotStore {
    override suspend fun save(export: AccountExport) {
        val snapshot = export.snapshot
        val uid = snapshot.identity.uid
        val server = snapshot.identity.server
        val exportedAt = snapshot.metadata.exportedAt
        val timestamp = exportedAt.toEpochMilli()
        val snapshotId = snapshotIdProvider()
        val chunks = SnapshotJsonChunks.split(export.rawJson).mapIndexed { index, content ->
            AccountSnapshotJsonChunkEntity(
                snapshotId = snapshotId,
                chunkIndex = index,
                content = content
            )
        }

        database.withTransaction {
            val dao = database.accountSnapshotDao()
            dao.insertAccountIfMissing(
                AccountEntity(uid, server, timestamp, timestamp)
            )
            dao.updateLastUpdatedAt(uid, server, timestamp)
            dao.insertSnapshot(
                AccountSnapshotEntity(
                    snapshotId = snapshotId,
                    uid = uid,
                    server = server,
                    exportedAt = exportedAt.toString(),
                    exportedAtEpochMillis = timestamp,
                    exportFormatVersion = snapshot.metadata.exportFormatVersion,
                    overallStatus = snapshot.overallStatus.name,
                    origin = snapshot.metadata.origin,
                    // Large JSON is stored in bounded rows in account_snapshot_json_chunks.
                    rawJson = ""
                )
            )
            if (chunks.isNotEmpty()) {
                dao.insertJsonChunks(chunks)
            }
        }
    }

    override suspend fun getLatest(uid: String): AccountExport? =
        database.accountSnapshotDao().getLatestSnapshot(uid)?.let { record ->
            val rawJson = readRawJson(record.snapshotId, record.rawJson)
            AccountExport(mapExport(rawJson), rawJson)
        }

    override suspend fun getHistory(uid: String, server: String): List<AccountExport> =
        database.accountSnapshotDao().getSnapshotHistory(uid, server).map { record ->
            val rawJson = readRawJson(record.snapshotId, record.rawJson)
            AccountExport(mapExport(rawJson), rawJson)
        }

    private suspend fun readRawJson(snapshotId: String, legacyRawJson: String): String {
        val chunks = database.accountSnapshotDao().getJsonChunks(snapshotId)
        return if (chunks.isNotEmpty()) {
            SnapshotJsonChunks.join(chunks)
        } else {
            // Compatibility fallback for a database created before the chunk migration.
            legacyRawJson
        }
    }
}
