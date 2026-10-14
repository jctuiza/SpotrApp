package com.example.spotrapp.data.local

// dao and queries for item entities
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Insert
    suspend fun insert(item: ItemEntity): Long

    @Update
    suspend fun update(item: ItemEntity): Int

    @Delete
    suspend fun delete(item: ItemEntity): Int

    @Query("SELECT * FROM items WHERE isRetrieved = 0 ORDER BY id DESC")
    fun getAllItems(): Flow<List<ItemEntity>>

    @Query("UPDATE items SET isRetrieved = 1 WHERE id = :itemId AND isRetrieved = 0")
    suspend fun markRetrieved(itemId: Int): Int

    @Query("UPDATE items SET notes = :notes WHERE id = :itemId AND isRetrieved = 0")
    suspend fun updateNotes(itemId: Int, notes: String?): Int

    @Query(
        "SELECT * FROM items " +
                "WHERE LOWER(name) = LOWER(:name) AND zoneId = :zoneId AND isRetrieved = 0 " +
                "LIMIT 1"
    )
    suspend fun findByNameAndZone(
        name: String,
        zoneId: Int
    ): ItemEntity?

    @Query("SELECT COUNT(*) FROM items WHERE zoneId = :zoneId AND isRetrieved = 0")
    suspend fun countItemsInZone(zoneId: Int): Int

    @Query("UPDATE items SET zoneId = :zoneId WHERE id IN (:itemIds) AND isRetrieved = 0")
    suspend fun moveItemsToZone(itemIds: List<Int>, zoneId: Int): Int

    @Query("SELECT * FROM items WHERE id IN (:itemIds) AND isRetrieved = 0")
    suspend fun getActiveItemsByIds(itemIds: List<Int>): List<ItemEntity>

    @Query("DELETE FROM items WHERE id IN (:itemIds) AND isRetrieved = 0")
    suspend fun deleteItemsByIds(itemIds: List<Int>): Int

    @Transaction
    suspend fun deleteActiveItems(itemIds: List<Int>): List<ItemEntity> {
        if (itemIds.isEmpty()) return emptyList()
        val activeItems = getActiveItemsByIds(itemIds)
        deleteItemsByIds(activeItems.map { it.id })
        return activeItems
    }

    @Transaction
    suspend fun retrieveActiveItems(itemIds: List<Int>): List<ItemEntity> {
        if (itemIds.isEmpty()) return emptyList()
        val activeItems = getActiveItemsByIds(itemIds)
        if (activeItems.isNotEmpty()) {
            markItemsRetrieved(activeItems.map { it.id })
        }
        return activeItems
    }

    @Query("UPDATE items SET isRetrieved = 1 WHERE id IN (:itemIds) AND isRetrieved = 0")
    suspend fun markItemsRetrieved(itemIds: List<Int>): Int
}
