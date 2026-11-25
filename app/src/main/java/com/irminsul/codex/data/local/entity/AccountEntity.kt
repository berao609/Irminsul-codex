package com.irminsul.codex.data.local.entity

import androidx.room.Entity

@Entity(tableName = "accounts", primaryKeys = ["uid", "server"])
data class AccountEntity(
    val uid: String,
    val server: String,
    val createdAtEpochMillis: Long,
    val lastUpdatedAtEpochMillis: Long
)
