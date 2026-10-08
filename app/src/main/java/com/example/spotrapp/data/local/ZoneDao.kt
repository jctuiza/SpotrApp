package com.example.spotrapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ZoneDao {

    @Insert
    suspend fun insert(zone: ZoneEntity): Long

    @Update
    suspend fun update(entity: ZoneEntity): Int

    @Delete
    suspend fun delete(zone: ZoneEntity): Int

    @Query("SELECT * FROM zones ORDER BY id ASC")
    fun getAllZones(): Flow<List<ZoneEntity>>

    // Case-insensitive duplicate check.
    @Query("SELECT * FROM zones WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(name: String): ZoneEntity?

    // Used when an item needs the zone name from its zoneId.
    @Query("SELECT * FROM zones WHERE id = :zoneId LIMIT 1")
    suspend fun findById(zoneId: Int): ZoneEntity?
}
