package com.irminsul.codex.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "private_account_data")
data class PrivateAccountDataEntity(
    @PrimaryKey val uid: String,
    val dailyNoteJson: String? = null,
    val dailyNoteRetrievedAt: String? = null,
    val accountProfileJson: String? = null,
    val accountProfileRetrievedAt: String? = null
)
