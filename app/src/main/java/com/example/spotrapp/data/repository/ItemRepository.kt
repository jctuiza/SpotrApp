package com.example.spotrapp.data.repository

import com.example.spotrapp.data.local.HistoryAction
import com.example.spotrapp.data.local.ItemDao
import com.example.spotrapp.data.local.ItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

// middleman ng itemdao - itemviewmodel
class ItemRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val historyRepository: HistoryRepository
) {

    val items: Flow<Resource<List<ItemEntity>>> = itemDao.getAllItems()
        .map<List<ItemEntity>, Resource<List<ItemEntity>>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading()) }
        .catch { e -> emit(Resource.Error(e.message ?: "Failed to load items")) }

    fun addItem(item: ItemEntity): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            val newId = itemDao.insert(item)
            historyRepository.log(HistoryAction.ADDED, item.name, newId.toInt())
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to add item"))
        }
    }

    fun updateItem(item: ItemEntity): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            itemDao.update(item)
            historyRepository.log(HistoryAction.EDITED, item.name, item.id)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to update item"))
        }
    }

    fun deleteItem(item: ItemEntity): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            itemDao.delete(item)
            historyRepository.log(HistoryAction.DELETED, item.name, item.id)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete item"))
        }
    }

    fun retrieveItem(item: ItemEntity): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            historyRepository.log(HistoryAction.RETRIEVED, item.name, item.id)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to log retrieval"))
        }
    }

    suspend fun isDuplicate(name: String, zoneId: Int): Boolean =
        itemDao.findByNameAndZone(name, zoneId) != null

    fun moveItemsToZone(itemIds: List<Int>, zoneId: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            if (itemIds.isEmpty()) {
                emit(Resource.Success(Unit))
            } else {
                itemDao.moveItemsToZone(itemIds, zoneId)
                emit(Resource.Success(Unit))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to move items"))
        }
    }
}
