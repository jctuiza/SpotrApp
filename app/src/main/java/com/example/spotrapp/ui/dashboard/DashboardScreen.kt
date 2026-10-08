package com.example.spotrapp.ui.dashboard

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotrapp.data.local.ItemEntity
import com.example.spotrapp.data.local.ZoneEntity
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.ui.common.TutorialOverlay
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrWhite
import com.example.spotrapp.viewmodel.ItemViewModel
import com.example.spotrapp.viewmodel.ZoneViewModel

@Composable
fun DashboardScreen(
    onItemClick: (ItemEntity) -> Unit,
    showTutorial: Boolean,
    onShowTutorial: () -> Unit,
    onDismissTutorial: () -> Unit,
    onSearchClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onScanClick: () -> Unit,
    itemViewModel: ItemViewModel = hiltViewModel(),
    zoneViewModel: ZoneViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    var selectedZone by remember { mutableStateOf("All") }

    var showAddZoneDialog by remember { mutableStateOf(false) }

    var showDeleteZoneDialog by remember { mutableStateOf(false) }
    var zoneToDelete by remember { mutableStateOf<ZoneEntity?>(null) }

    var showItemDetailsSheet by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ItemEntity?>(null) }

    var scanButtonBounds by remember { mutableStateOf<Rect?>(null) }

    var selectedItemIds by remember { mutableStateOf(setOf<Int>()) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showRenameZoneDialog by remember { mutableStateOf(false) }

    val itemState by itemViewModel.itemState.collectAsStateWithLifecycle()
    val zoneState by zoneViewModel.zoneState.collectAsStateWithLifecycle()
    val zoneActionState by zoneViewModel.actionState.collectAsStateWithLifecycle()
    val deleteItemCount by zoneViewModel.deleteItemCount.collectAsStateWithLifecycle()

    val items =
        if (itemState is Resource.Success) {
            (itemState as Resource.Success<List<ItemEntity>>).data
        } else {
            emptyList()
        }

    val zones =
        if (zoneState is Resource.Success) {
            (zoneState as Resource.Success<List<ZoneEntity>>).data
        } else {
            emptyList()
        }

    
    LaunchedEffect(zones) {
        if (
            selectedZone != "All" &&
            zones.none { it.name == selectedZone }
        ) {
            selectedZone = "All"
        }
    }

    
    LaunchedEffect(zoneActionState) {
        when (val action = zoneActionState) {

            is Resource.Error -> {

                Toast.makeText(
                    context,
                    action.message,
                    Toast.LENGTH_SHORT
                ).show()

                zoneViewModel.clearActionState()
            }

            else -> Unit
        }
    }

    
    LaunchedEffect(deleteItemCount) {

        if (deleteItemCount != null) {
            showDeleteZoneDialog = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Scaffold(
            topBar = {
                DashboardTopBar(
                    onSearchClick = onSearchClick,
                    onHelpClick = onShowTutorial
                )
            },

            bottomBar = {
                DashboardBottomNav(
                    selectedItem = "home",
                    onHomeClick = {},
                    onScanClick = onScanClick,
                    onHistoryClick = onHistoryClick,
                    onScanButtonPositioned = { bounds ->
                        scanButtonBounds = bounds
                    }
                )
            }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {

                    
                    if (zones.isNotEmpty()) {

                        ZoneFilterRow(
                            zones = zones,
                            selectedZone = selectedZone,

                            onZoneSelected = { zone ->

                                if (zone == "+") {

                                    showAddZoneDialog = true

                                } else {

                                    selectedZone = zone
                                }
                            },

                            onZoneLongPress = { zone ->

                                zoneToDelete = zone

                                zoneViewModel.prepareDeleteZone(
                                    zone
                                )
                            }
                        )
                    }

                    if (selectedItemIds.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showMoveDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Move " + selectedItemIds.size + " selected", color = SpotrWhite)
                            }
                            Button(
                                onClick = { selectedItemIds = emptySet() },
                                colors = ButtonDefaults.buttonColors(containerColor = SpotrSecondary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel", color = SpotrPrimary)
                            }
                        }
                    }

                    
                    val filteredItems =
                        if (selectedZone == "All") {

                            items

                        } else {

                            val selectedZoneEntity =
                                zones.find {
                                    it.name == selectedZone
                                }

                            if (selectedZoneEntity != null) {

                                items.filter {
                                    it.zoneId == selectedZoneEntity.id
                                }

                            } else {

                                emptyList()
                            }
                        }

                    
                    if (itemState is Resource.Loading) {

                        Box(
                            modifier = Modifier.weight(1f)
                        )

                        
                    } else if (zones.isEmpty()) {

                        DashboardEmptyState(
                            modifier = Modifier.weight(1f),
                            emptyState = DashboardEmptyStateType.NO_ZONES,
                            onAddZoneClick = {
                                showAddZoneDialog = true
                            }
                        )

                        
                    } else if (filteredItems.isEmpty()) {

                        DashboardEmptyState(
                            modifier = Modifier.weight(1f),
                            emptyState = DashboardEmptyStateType.NO_ITEMS
                        )

                        
                    } else {

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement =
                                Arrangement.spacedBy(12.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {

                            items(filteredItems) { item ->

                                ItemCard(
                                    item = item,
                                    selected = selectedItemIds.contains(item.id),
                                    selectionMode = selectedItemIds.isNotEmpty(),
                                    onClick = {
                                        if (selectedItemIds.isNotEmpty()) {
                                            selectedItemIds = if (selectedItemIds.contains(item.id)) selectedItemIds - item.id else selectedItemIds + item.id
                                        } else {
                                            onItemClick(item)
                                        }
                                    },
                                    onLongClick = {
                                        selectedItemIds = selectedItemIds + item.id
                                    },
                                    onMenuClick = {
                                        selectedItem = item
                                        showItemDetailsSheet = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        
        if (showAddZoneDialog) {

            AddZoneDialog(
                onDismiss = {
                    showAddZoneDialog = false
                },

                onConfirm = { zoneName ->

                    val trimmedName = zoneName.trim()

                    if (trimmedName.isNotEmpty()) {

                        zoneViewModel.addZone(
                            trimmedName
                        )

                        showAddZoneDialog = false

                    } else {

                        Toast.makeText(
                            context,
                            "Zone name cannot be blank.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }

        
        if (
            showDeleteZoneDialog &&
            zoneToDelete != null
        ) {

            val itemCount =
                deleteItemCount ?: 0

            val zone =
                zoneToDelete!!

            AlertDialog(
                onDismissRequest = {

                    showDeleteZoneDialog = false
                    zoneToDelete = null

                    zoneViewModel.clearDeleteItemCount()
                },

                title = {

                    Text(
                        text = "Delete \"${zone.name}\"?"
                    )
                },

                text = {

                    if (itemCount > 0) {

                        Text(
                            text =
                                if (itemCount == 1) {

                                    "This zone contains 1 item. " +
                                            "Deleting this zone will move " +
                                            "the item to Unassigned."

                                } else {

                                    "This zone contains $itemCount items. " +
                                            "Deleting this zone will move " +
                                            "these items to Unassigned."
                                }
                        )

                    } else {

                        Text(
                            text =
                                "This zone is empty. " +
                                        "Are you sure you want to delete it?"
                        )
                    }
                },

                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                showDeleteZoneDialog = false
                                zoneViewModel.clearDeleteItemCount()
                                showRenameZoneDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotrSecondary)
                        ) {
                            Text("Rename", color = SpotrPrimary)
                        }
                        Button(
                            onClick = {
                                zoneViewModel.deleteZone(zone)
                                if (selectedZone == zone.name) selectedZone = "All"
                                showDeleteZoneDialog = false
                                zoneToDelete = null
                                zoneViewModel.clearDeleteItemCount()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary)
                        ) {
                            Text("Delete", color = SpotrWhite)
                        }
                    }
                },

                dismissButton = {

                    Button(
                        onClick = {

                            showDeleteZoneDialog = false
                            zoneToDelete = null

                            zoneViewModel.clearDeleteItemCount()
                        },

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = SpotrSecondary
                            )
                    ) {

                        Text(
                            text = "Cancel",
                            color = SpotrPrimary
                        )
                    }
                }
            )
        }

        if (showRenameZoneDialog && zoneToDelete != null) {
            RenameZoneDialog(
                zone = zoneToDelete!!,
                onDismiss = {
                    showRenameZoneDialog = false
                    zoneToDelete = null
                },
                onConfirm = { newName ->
                    zoneViewModel.renameZone(zoneToDelete!!, newName)
                    showRenameZoneDialog = false
                    zoneToDelete = null
                }
            )
        }

        if (showMoveDialog && selectedItemIds.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { showMoveDialog = false },
                title = { Text("Move selected items") },
                text = {
                    Column {
                        zones.forEach { zone ->
                            Button(
                                onClick = {
                                    itemViewModel.moveItemsToZone(selectedItemIds.toList(), zone.id)
                                    selectedItemIds = emptySet()
                                    showMoveDialog = false
                                    Toast.makeText(context, "Items moved to " + zone.name + ".", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SpotrSecondary)
                            ) {
                                Text(zone.name, color = SpotrPrimary)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showMoveDialog = false }) { Text("Cancel") }
                }
            )
        }

        
        if (
            showItemDetailsSheet &&
            selectedItem != null
        ) {

            ItemDetailsBottomSheet(
                item = selectedItem!!,

                onDismiss = {

                    showItemDetailsSheet = false
                    selectedItem = null
                }
            )
        }

        
        if (
            showTutorial &&
            scanButtonBounds != null
        ) {

            TutorialOverlay(
                message =
                    "This is where you scan items. Tap anywhere to continue.",

                onDismiss =
                    onDismissTutorial,

                highlightBounds =
                    scanButtonBounds
            )
        }
    }
}