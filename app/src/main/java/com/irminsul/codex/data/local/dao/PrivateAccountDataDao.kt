package com.irminsul.codex.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.irminsul.codex.data.local.entity.PrivateAccountDataEntity

@Dao
interface PrivateAccountDataDao {
    @Query("SELECT * FROM private_account_data WHERE uid = :uid LIMIT 1")
    suspend fun get(uid: String): PrivateAccountDataEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(entity: PrivateAccountDataEntity)

    @Query(
        """
        UPDATE private_account_data
        SET dailyNoteJson = :json, dailyNoteRetrievedAt = :retrievedAt
        WHERE uid = :uid
        """
    )
    suspend fun updateDailyNote(uid: String, json: String, retrievedAt: String)

    @Query(
        """
        UPDATE private_account_data
        SET accountProfileJson = :json, accountProfileRetrievedAt = :retrievedAt
        WHERE uid = :uid
        """
    )
    suspend fun updateAccountProfile(uid: String, json: String, retrievedAt: String)
}
