package com.example.spotrapp.data.repository

import com.example.spotrapp.data.local.HistoryAction
import com.example.spotrapp.data.local.HistoryDao
import com.example.spotrapp.data.local.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val historyDao: HistoryDao
) {

    val history: Flow<Resource<List<HistoryEntity>>> = historyDao.getAll()
        .map<List<HistoryEntity>, Resource<List<HistoryEntity>>> { Resource.Success(it) }
        .onStart { emit(Resource.Loading()) }
        .catch { e -> emit(Resource.Error(e.message ?: "Failed to load history")) }

    val recentSearches: Flow<List<String>> = historyDao.getRecentSearches(5)

    // item repository calls this so screens never have to remember to any actions (add, delete etc).
    suspend fun log(action: HistoryAction, title: String, itemId: Int? = null) {
        historyDao.insert(HistoryEntity(action = action, itemId = itemId, title = title))
    }
}