package com.example.spotrapp.data.repository

import com.example.spotrapp.data.local.ItemDao
import com.example.spotrapp.data.local.ZoneDao
import com.example.spotrapp.data.local.ZoneEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class ZoneRepository @Inject constructor(
    private val zoneDao: ZoneDao,
    private val itemDao: ItemDao
) {

    val zones: Flow<Resource<List<ZoneEntity>>> = zoneDao.getAllZones()
        .map<List<ZoneEntity>, Resource<List<ZoneEntity>>> {
            Resource.Success(it)
        }
        .onStart {
            emit(Resource.Loading())
        }
        .catch { e ->
            emit(Resource.Error(e.message ?: "Failed to load zones"))
        }

    fun addZone(zone: ZoneEntity): Flow<Resource<ZoneEntity>> = flow {
        emit(Resource.Loading())
        try {
            val newId = zoneDao.insert(zone)
            emit(Resource.Success(zone.copy(id = newId.toInt())))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to add zone"))
        }
    }

    fun updateZone(zone: ZoneEntity): Flow<Resource<ZoneEntity>> = flow {
        emit(Resource.Loading())
        try {
            zoneDao.update(zone)
            emit(Resource.Success(zone))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to rename zone"))
        }
    }

    fun deleteZone(zone: ZoneEntity): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            zoneDao.delete(zone)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to delete zone"))
        }
    }

    suspend fun countItemsInZone(zoneId: Int): Int =
        itemDao.countItemsInZone(zoneId)

    suspend fun isDuplicate(name: String, excludingZoneId: Int? = null): Boolean {
        val existing = zoneDao.findByName(name)
        return existing != null && existing.id != excludingZoneId
    }
}
