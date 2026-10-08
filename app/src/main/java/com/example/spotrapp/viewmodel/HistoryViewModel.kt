package com.example.spotrapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotrapp.data.local.HistoryAction
import com.example.spotrapp.data.local.HistoryEntity
import com.example.spotrapp.data.repository.HistoryRepository
import com.example.spotrapp.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    // historyscreen reads this
    val historyState: StateFlow<Resource<List<HistoryEntity>>> = repository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading())

    // for recent searches in search screen
    val recentSearches: StateFlow<List<String>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Call when the user submits a search (not when typing)
    fun logSearch(query: String) {
        val text = query.trim()
        if (text.isEmpty()) return
        viewModelScope.launch { repository.log(HistoryAction.SEARCHED, text) }
    }
}