package com.irminsul.codex.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "account_snapshot_json_chunks",
    primaryKeys = ["snapshotId", "chunkIndex"],
    foreignKeys = [
        ForeignKey(
            entity = AccountSnapshotEntity::class,
            parentColumns = ["snapshotId"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["snapshotId"])]
)
data class AccountSnapshotJsonChunkEntity(
    val snapshotId: String,
    val chunkIndex: Int,
    val content: String
)
