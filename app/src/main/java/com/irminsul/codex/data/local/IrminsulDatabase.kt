package com.irminsul.codex.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.irminsul.codex.data.local.dao.AccountSnapshotDao
import com.irminsul.codex.data.local.entity.AccountEntity
import com.irminsul.codex.data.local.entity.AccountSnapshotEntity

@Database(
    entities = [AccountEntity::class, AccountSnapshotEntity::class],
    version = 1,
    exportSchema = true
)
abstract class IrminsulDatabase : RoomDatabase() {
    abstract fun accountSnapshotDao(): AccountSnapshotDao

    companion object {
        @Volatile private var instance: IrminsulDatabase? = null

        fun getInstance(context: Context): IrminsulDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    IrminsulDatabase::class.java,
                    "irminsul_codex.db"
                ).build().also { instance = it }
            }
    }
}
