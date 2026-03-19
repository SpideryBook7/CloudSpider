package com.spiderybook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.spiderybook.data.local.dao.MainDao
import com.spiderybook.data.local.entity.FavoriteEntity
import com.spiderybook.data.local.entity.HistoryEntity

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [FavoriteEntity::class, HistoryEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mainDao(): MainDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE history ADD COLUMN duration INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE history ADD COLUMN showTitle TEXT NOT NULL DEFAULT ''")
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE favorites ADD COLUMN status TEXT NOT NULL DEFAULT 'Want to Watch'")
                database.execSQL("ALTER TABLE favorites ADD COLUMN watchedEpisodes INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE favorites ADD COLUMN totalEpisodes INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
