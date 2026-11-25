package com.irminsul.codex.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "account_snapshots",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["uid", "server"],
            childColumns = ["uid", "server"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["uid", "server", "exportedAtEpochMillis"])]
)
data class AccountSnapshotEntity(
    @PrimaryKey val snapshotId: String,
    val uid: String,
    val server: String,
    val exportedAt: String,
    val exportedAtEpochMillis: Long,
    val exportFormatVersion: Int,
    val overallStatus: String,
    val origin: String,
    val rawJson: String
)
