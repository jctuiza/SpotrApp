package com.example.spotrapp.ui.retrieval

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.spotrapp.data.local.ItemEntity
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.ui.common.ConfirmationPopup
import com.example.spotrapp.ui.common.ScreenHeader
import com.example.spotrapp.ui.common.StatusPopup
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.viewmodel.ItemViewModel
import com.example.spotrapp.viewmodel.ZoneViewModel
import java.io.File

private enum class PendingAction { NONE, RETRIEVE, DELETE }

@Composable
fun ItemDetailsScreen(
    item: ItemEntity,
    onBack: () -> Unit,
    onEditClick: (ItemEntity) -> Unit,
    onRetrieved: () -> Unit,
    onDeleted: () -> Unit,
    itemViewModel: ItemViewModel = hiltViewModel(),
    zoneViewModel: ZoneViewModel = hiltViewModel()
) {
    val actionState by itemViewModel.actionState.collectAsStateWithLifecycle()
    val zoneState by zoneViewModel.zoneState.collectAsStateWithLifecycle()

    var showRetrievedPopup by remember { mutableStateOf(false) }
    var showConfirmDeletePopup by remember { mutableStateOf(false) }
    var showDeletedPopup by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf(PendingAction.NONE) }
    
    val zoneName = when (val state = zoneState) {
        is Resource.Success -> {
            state.data.find { zone ->
                zone.id == item.zoneId
            }?.name ?: "Unassigned"
        }

        else -> "Unassigned"
    }

    LaunchedEffect(actionState) {
        when (actionState) {
            is Resource.Success -> {
                when (pendingAction) {
                    PendingAction.RETRIEVE -> showRetrievedPopup = true
                    PendingAction.DELETE -> showDeletedPopup = true
                    PendingAction.NONE -> Unit
                }
                pendingAction = PendingAction.NONE
                itemViewModel.clearActionState()
            }

            is Resource.Error -> {
                pendingAction = PendingAction.NONE
                itemViewModel.clearActionState()
            }

            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp
                )
        ) {

            ScreenHeader(
                title = item.name,
                onBack = onBack
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .background(
                            color = SpotrSecondary,
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (item.imagePath != null) {
                        // Member 3's swap: was painterResource(item.imageRes)
                        AsyncImage(
                            model = File(item.imagePath),
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column {

                    DetailField(
                        label = "Item Name",
                        value = item.name
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    DetailField(
                        label = "Location",
                        value = zoneName
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    DetailField(
                        label = "Date Added",
                        value = item.dateAdded.toString()
                    )

                    Spacer(modifier = Modifier.height(15.dp))

                    DetailField(
                        label = "Notes",
                        value = item.notes?.ifBlank { "-" } ?: "-"
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    // logs the retrieved event immediately.
                    Button(
                        onClick = {
                            pendingAction = PendingAction.RETRIEVE
                            itemViewModel.retrieveItem(item)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotrPrimary
                        )
                    ) {
                        Text(
                            text = "Retrieve",
                            color = Color.White
                        )
                    }

                    // Edit and Delete buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Button(
                            onClick = {
                                onEditClick(item)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotrPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = Color.White
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Edit",
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                showConfirmDeletePopup = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB0BEC5)
                            )
                        ) {
                            Text(
                                text = "Delete",
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }

        if (showRetrievedPopup) {
            StatusPopup(
                message = "Item Retrieved",
                icon = Icons.Filled.Check,
                onDismiss = {
                    showRetrievedPopup = false
                    onRetrieved()
                }
            )
        }

        if (showConfirmDeletePopup) {
            ConfirmationPopup(
                message = "Are you sure you want to delete this item?",
                confirmLabel = "Yes",
                cancelLabel = "Cancel",
                onConfirm = {
                    showConfirmDeletePopup = false
                    pendingAction = PendingAction.DELETE
                    itemViewModel.deleteItem(item)
                },
                onCancel = {
                    showConfirmDeletePopup = false
                }
            )
        }

        if (showDeletedPopup) {
            StatusPopup(
                message = "Item successfully deleted",
                onDismiss = {
                    showDeletedPopup = false
                    onDeleted()
                }
            )
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value
        )
    }
}