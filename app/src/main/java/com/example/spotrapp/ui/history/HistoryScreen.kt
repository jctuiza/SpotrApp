package com.example.spotrapp.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotrapp.data.local.HistoryAction
import com.example.spotrapp.data.local.HistoryEntity
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.ui.dashboard.DashboardBottomNav
import com.example.spotrapp.ui.theme.SpotrGray
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrTextBlack
import com.example.spotrapp.ui.theme.SpotrWhite
import com.example.spotrapp.viewmodel.HistoryViewModel
import androidx.compose.ui.platform.LocalLocale

@Composable
fun HistoryScreen(
    onHomeClick: () -> Unit,
    onScanClick: () -> Unit,
    onItemClick: (Int) -> Unit,
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val historyState by historyViewModel.historyState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            DashboardBottomNav(
                selectedItem = "history",
                onHomeClick = onHomeClick,
                onScanClick = onScanClick,
                onHistoryClick = {}
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

            Text(
                text = "History",
                color = SpotrTextBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Recent Activity",
                color = SpotrTextBlack,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            when (val resource = historyState) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is Resource.Error -> {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Something went wrong: ${resource.message}")
                    }
                }

                is Resource.Success -> {
                    if (resource.data.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No activity yet.",
                                color = SpotrGray
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(resource.data) { entry ->
                                HistoryItemRow(
                                    entry = entry,
                                    onClick = {
                                        entry.itemId?.let { onItemClick(it) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// label + whether the row is tappable, per action
private fun HistoryAction.label(title: String): String = when (this) {
    HistoryAction.ADDED -> "Added $title"
    HistoryAction.EDITED -> "Edited $title"
    HistoryAction.DELETED -> "Deleted $title"
    HistoryAction.RETRIEVED -> "Retrieved $title"
    HistoryAction.SEARCHED -> "Searched \"$title\""
}

private fun HistoryAction.isTappable(): Boolean = when (this) {
    HistoryAction.DELETED, HistoryAction.SEARCHED -> false
    else -> true
}

@Composable
fun HistoryItemRow(
    entry: HistoryEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val tappable = entry.action.isTappable()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = SpotrWhite,
        onClick = { if (tappable) onClick() }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                shape = RoundedCornerShape(50),
                color = SpotrSecondary
            ) {
                Box(
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(
                modifier = Modifier.width(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = entry.action.label(entry.title),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = SpotrTextBlack
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = java.text.SimpleDateFormat(
                        "MMMM d, yyyy, h:mm a",
                        LocalLocale.current.platformLocale
                    ).format(java.util.Date(entry.timestamp)),
                    fontSize = 12.sp,
                    color = SpotrGray
                )
            }
        }
    }
}