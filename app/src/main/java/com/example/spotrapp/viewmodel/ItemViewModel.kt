package com.example.spotrapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotrapp.data.local.ItemEntity
import com.example.spotrapp.data.repository.ItemRepository
import com.example.spotrapp.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// handle state for items
@HiltViewModel
class ItemViewModel @Inject constructor(
    private val repository: ItemRepository
) : ViewModel() {

    private val _itemState =
        MutableStateFlow<Resource<List<ItemEntity>>>(Resource.Loading())

    val itemState: StateFlow<Resource<List<ItemEntity>>> = _itemState

    // most recent action (delete, add etc)
    private val _actionState =
        MutableStateFlow<Resource<Unit>?>(null)

    val actionState: StateFlow<Resource<Unit>?> = _actionState

    init {
        viewModelScope.launch {
            repository.items.collect { resource ->
                _itemState.value = resource
            }
        }
    }

    fun addItem(item: ItemEntity) {
        viewModelScope.launch {

            /*
             * New items must have a zone.
             *
             * An item can only have a null zoneId after its assigned
             * zone has been deleted. Users should select a valid zone
             * when adding a new item.
             */
            val zoneId = item.zoneId

            if (zoneId == null) {
                _actionState.value = Resource.Error(
                    "Please select a zone before adding the item."
                )
                return@launch
            }

            val isDuplicate = repository.isDuplicate(
                item.name,
                zoneId
            )

            if (isDuplicate) {
                _actionState.value = Resource.Error(
                    "\"${item.name}\" already exists in this zone."
                )
            } else {
                repository.addItem(item).collect { resource ->
                    _actionState.value = resource
                }
            }
        }
    }

    fun updateItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.updateItem(item).collect { resource ->
                _actionState.value = resource
            }
        }
    }

    fun deleteItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item).collect { resource ->
                _actionState.value = resource
            }
        }
    }

    // logs a retrieve event, item stays in the list
    fun retrieveItem(item: ItemEntity) {
        viewModelScope.launch {
            repository.retrieveItem(item).collect { resource ->
                _actionState.value = resource
            }
        }
    }

    fun clearActionState() {
        _actionState.value = null
    }
}