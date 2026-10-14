package com.example.spotrapp.data.local

// database, entities, dao, converters, and migrations

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ItemEntity::class, ZoneEntity::class, HistoryEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE items ADD COLUMN isRetrieved INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }

    abstract fun itemDao(): ItemDao
    abstract fun zoneDao(): ZoneDao
    abstract fun historyDao(): HistoryDao
}