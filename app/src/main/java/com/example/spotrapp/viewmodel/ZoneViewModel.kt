package com.example.spotrapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotrapp.data.local.ZoneEntity
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.data.repository.ZoneRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ZoneViewModel @Inject constructor(
    private val repository: ZoneRepository
) : ViewModel() {

    private val _zoneState = MutableStateFlow<Resource<List<ZoneEntity>>>(Resource.Loading())
    val zoneState: StateFlow<Resource<List<ZoneEntity>>> = _zoneState

    private val _actionState = MutableStateFlow<Resource<ZoneEntity>?>(null)
    val actionState: StateFlow<Resource<ZoneEntity>?> = _actionState

    private val _deleteItemCount = MutableStateFlow<Int?>(null)
    val deleteItemCount: StateFlow<Int?> = _deleteItemCount

    init {
        viewModelScope.launch {
            repository.zones.collect { resource -> _zoneState.value = resource }
        }
    }

    fun addZone(name: String) {
        viewModelScope.launch {
            val trimmedName = name.trim()
            if (trimmedName.isEmpty()) {
                _actionState.value = Resource.Error("Zone name cannot be empty.")
                return@launch
            }

            if (repository.isDuplicate(trimmedName)) {
                _actionState.value = Resource.Error(""$trimmedName" already exists.")
                return@launch
            }

            repository.addZone(ZoneEntity(name = trimmedName)).collect {
                _actionState.value = it
            }
        }
    }

    fun renameZone(zone: ZoneEntity, newName: String) {
        viewModelScope.launch {
            val trimmedName = newName.trim()
            if (trimmedName.isEmpty()) {
                _actionState.value = Resource.Error("Zone name cannot be empty.")
                return@launch
            }
            if (repository.isDuplicate(trimmedName, zone.id)) {
                _actionState.value = Resource.Error(""$trimmedName" already exists.")
                return@launch
            }

            repository.updateZone(zone.copy(name = trimmedName)).collect {
                _actionState.value = it
            }
        }
    }

    fun prepareDeleteZone(zone: ZoneEntity) {
        viewModelScope.launch {
            _deleteItemCount.value = repository.countItemsInZone(zone.id)
        }
    }

    fun deleteZone(zone: ZoneEntity) {
        viewModelScope.launch {
            repository.deleteZone(zone).collect { resource ->
                when (resource) {
                    is Resource.Success -> _actionState.value = Resource.Success(zone)
                    is Resource.Error -> _actionState.value = Resource.Error(resource.message)
                    is Resource.Loading -> _actionState.value = Resource.Loading()
                }
                if (resource is Resource.Success) _deleteItemCount.value = null
            }
        }
    }

    fun clearDeleteItemCount() { _deleteItemCount.value = null }
    fun clearActionState() { _actionState.value = null }
}
