package com.example.spotrapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity): Int

    @Delete
    suspend fun delete(item: ItemEntity): Int

    @Query("SELECT * FROM items ORDER BY id DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query(
        "SELECT * FROM items " +
                "WHERE LOWER(name) = LOWER(:name) AND zoneId = :zoneId " +
                "LIMIT 1"
    )
    suspend fun findByNameAndZone(
        name: String,
        zoneId: Int
    ): ItemEntity?

    @Query("SELECT COUNT(*) FROM items WHERE zoneId = :zoneId")
    suspend fun countItemsInZone(zoneId: Int): Int

    // Used for bulk moving selected items to another zone.
    @Query("UPDATE items SET zoneId = :zoneId WHERE id IN (:itemIds)")
    suspend fun moveItemsToZone(itemIds: List<Int>, zoneId: Int): Int
}
