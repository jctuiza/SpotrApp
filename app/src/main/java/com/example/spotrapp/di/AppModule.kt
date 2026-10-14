package com.example.spotrapp.di

/**
 * Configures Hilt dependency injection for the Room database, DAOs, and repositories used across the app.
 */

import android.content.Context
import androidx.room.Room
import com.example.spotrapp.data.local.AppDatabase
import com.example.spotrapp.data.local.HistoryDao
import com.example.spotrapp.data.local.ItemDao
import com.example.spotrapp.data.local.ZoneDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "spotr_database")
            // wipes local data when the schema version changes
            .addMigrations(AppDatabase.MIGRATION_7_8)
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideItemDao(database: AppDatabase): ItemDao = database.itemDao()

    @Provides
    fun provideZoneDao(database: AppDatabase): ZoneDao = database.zoneDao()

    @Provides
    fun provideHistoryDao(database: AppDatabase): HistoryDao = database.historyDao()

}