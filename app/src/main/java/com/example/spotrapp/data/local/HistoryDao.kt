package com.example.spotrapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert
    suspend fun insert(entity: HistoryEntity): Long

    @Update
    suspend fun update(entity: HistoryEntity): Int

    @Delete
    suspend fun delete(entity: HistoryEntity): Int


    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    fun getAll(): Flow<List<HistoryEntity>>

    @Query(
        "SELECT title FROM history WHERE `action` = 'SEARCHED' " +
                "GROUP BY title ORDER BY MAX(timestamp) DESC LIMIT :limit"
    )
    fun getRecentSearches(limit: Int): Flow<List<String>>
}