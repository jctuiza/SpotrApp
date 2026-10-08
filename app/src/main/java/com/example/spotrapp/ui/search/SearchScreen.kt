package com.example.spotrapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotrapp.data.local.ItemEntity
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.ui.dashboard.DashboardBottomNav
import com.example.spotrapp.ui.theme.SpotrGray
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrTextBlack
import com.example.spotrapp.ui.theme.SpotrWhite
import com.example.spotrapp.viewmodel.HistoryViewModel
import com.example.spotrapp.viewmodel.ItemViewModel

@Composable
fun SearchScreen(
    onHomeClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onVoiceSearchClick: () -> Unit,
    onItemClick: (ItemEntity) -> Unit,
    incomingVoiceQuery: String? = null,
    onVoiceQueryConsumed: () -> Unit = {},
    itemViewModel: ItemViewModel = hiltViewModel(),
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val itemState by itemViewModel.itemState.collectAsStateWithLifecycle()
    val recentSearches by historyViewModel.recentSearches.collectAsStateWithLifecycle()
    val items = (itemState as? Resource.Success)?.data ?: emptyList()

    // search bar state
    var searchText by remember { mutableStateOf("") }
    var hasSearched by remember { mutableStateOf(false) }

    val searchResults = remember(searchText, items) {
        items.filter { it.name.contains(searchText, ignoreCase = true) }
    }

    fun runSearch(query: String) {
        searchText = query
        hasSearched = query.isNotBlank()
        if (query.isNotBlank()) {
            historyViewModel.logSearch(query)
        }
    }

    // Apply a voice result exactly once when it arrives.
    LaunchedEffect(incomingVoiceQuery) {
        if (!incomingVoiceQuery.isNullOrBlank()) {
            runSearch(incomingVoiceQuery)
            onVoiceQueryConsumed()
        }
    }

    Scaffold(
        bottomBar = {
            DashboardBottomNav(
                selectedItem = null,
                onHomeClick = onHomeClick,
                onScanClick = onScanClick,
                onHistoryClick = onHistoryClick
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
        ) {

            // search bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SpotrWhite)
                        .border(
                            width = 1.5.dp,
                            color = SpotrSecondary,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = SpotrGray
                    )

                    Spacer(
                        modifier = Modifier.width(10.dp)
                    )

                    BasicTextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            if (it.isEmpty()) {
                                hasSearched = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = SpotrTextBlack,
                            fontSize = 14.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                runSearch(searchText)
                            },
                        ),
                        decorationBox = { innerTextField ->

                            if (searchText.isEmpty()) {
                                Text(
                                    text = "Search an item...",
                                    color = SpotrGray,
                                    fontSize = 14.sp
                                )
                            }

                            innerTextField()
                        }
                    )

                    IconButton(
                        onClick = {

                            if (searchText.isEmpty()) {
                                onVoiceSearchClick()
                            } else {
                                searchText = ""
                                hasSearched = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (searchText.isEmpty()) {
                                Icons.Filled.Mic
                            } else {
                                Icons.Filled.Close
                            },
                            contentDescription = if (searchText.isEmpty()) {
                                "Voice search"
                            } else {
                                "Clear search"
                            },
                            tint = if (searchText.isEmpty()) {
                                SpotrPrimary
                            } else {
                                SpotrGray
                            }
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            if (hasSearched && searchResults.isEmpty()) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Icon(
                        imageVector = Icons.Filled.SearchOff,
                        contentDescription = "No results",
                        tint = SpotrGray,
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        text = "No results found!",
                        color = SpotrTextBlack,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "We couldn't find any matching results. " +
                                "Try searching again or change your filters.",
                        color = SpotrGray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }

            } else if (hasSearched) {

                Text(
                    text = "${searchResults.size} " +
                            if (searchResults.size == 1) "result found" else "results found",
                    color = SpotrTextBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(searchResults) { item ->
                        SearchResultRow(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }

            } else {

                Text(
                    text = "Recent Searches",
                    color = SpotrTextBlack,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(recentSearches) { query ->
                        RecentSearchesRow(
                            query = query,
                            onClick = { runSearch(query) }
                        )
                    }
                }
            }
        }
    }
}