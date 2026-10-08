package com.example.spotrapp.ui.retrieval

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.net.Uri
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.spotrapp.data.local.ItemEntity
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.ui.common.ConfirmationPopup
import com.example.spotrapp.ui.putaway.rememberCameraCapture
import com.example.spotrapp.ui.common.OtherZoneButton
import com.example.spotrapp.ui.common.ScreenHeader
import com.example.spotrapp.ui.common.StatusPopup
import com.example.spotrapp.ui.common.ZoneButton
import com.example.spotrapp.ui.common.splitZones
import com.example.spotrapp.ui.dashboard.AddZoneDialog
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrWhite
import com.example.spotrapp.viewmodel.ItemViewModel
import com.example.spotrapp.viewmodel.ZoneViewModel
import java.io.File
import java.util.Locale

@Composable
fun EditItemScreen(
    item: ItemEntity,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    itemViewModel: ItemViewModel = hiltViewModel(),
    zoneViewModel: ZoneViewModel = hiltViewModel()
) {
    val itemState by itemViewModel.itemState.collectAsStateWithLifecycle()
    val zoneState by zoneViewModel.zoneState.collectAsStateWithLifecycle()
    val actionState by itemViewModel.actionState.collectAsStateWithLifecycle()
    val zoneActionState by zoneViewModel.actionState.collectAsStateWithLifecycle()

// dialog states
    var showConfirmEditPopup by remember { mutableStateOf(false) }
    var showSavedPopup by remember { mutableStateOf(false) }
    var showConfirmCancelPopup by remember { mutableStateOf(false) }
    var showAddZoneDialog by remember { mutableStateOf(false) }

// Used to know that the current zone action came from this screen.
    var addingZoneFromItemScreen by remember { mutableStateOf(false) }

// Static text field state
    var itemName by remember { mutableStateOf(item.name) }
    var editedImagePath by remember { mutableStateOf(item.imagePath) }

    val context = LocalContext.current
    val cameraCapture = rememberCameraCapture { path -> editedImagePath = path }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            copyGalleryImageForEdit(context, uri)?.let { editedImagePath = it }
        }
    }

// Keep the zone name in the UI because ZoneButton and OtherZoneButton work with zone names
    val zones = (zoneState as? Resource.Success)?.data ?: emptyList()

    val initialZoneName =
        zones.find { it.id == item.zoneId }?.name ?: ""

    var selectedZone by remember(initialZoneName) {
        mutableStateOf(initialZoneName)
    }

// error state
    var nameError by remember { mutableStateOf<String?>(null) }
    var zoneError by remember { mutableStateOf<String?>(null) }

// voice input states
    var isListening by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    fun startSpeechRecognition() {
        if (speechRecognizer == null) {
            voiceError =
                "Voice recognition is not available on this device."
            return
        }

        voiceError = null

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )
        }

        speechRecognizer.startListening(intent)
    }

    val microphonePermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                startSpeechRecognition()
            } else {
                voiceError =
                    "Microphone permission is required for voice input."
            }
        }

    DisposableEffect(speechRecognizer) {

        if (speechRecognizer != null) {

            speechRecognizer.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(
                        params: Bundle?
                    ) {
                        isListening = true
                        voiceError = null
                    }

                    override fun onBeginningOfSpeech() {
                        isListening = true
                    }

                    override fun onRmsChanged(rmsdB: Float) { }

                    override fun onBufferReceived(buffer: ByteArray?) { }

                    override fun onEndOfSpeech() {
                        isListening = false
                    }

                    override fun onError(
                        error: Int
                    ) {
                        isListening = false

                        voiceError = when (error) {

                            SpeechRecognizer.ERROR_AUDIO ->
                                "There was a problem with the microphone."

                            SpeechRecognizer.ERROR_CLIENT ->
                                "Voice input was interrupted."

                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                                "Microphone permission is required."

                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                                "Voice recognition is unavailable offline."

                            SpeechRecognizer.ERROR_NO_MATCH ->
                                "I couldn't understand what you said."

                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
                                "Voice recognition is currently busy."

                            SpeechRecognizer.ERROR_SERVER ->
                                "Speech recognition service is unavailable."

                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                                "No speech was detected."

                            else ->
                                "Voice input failed. Please try again."
                        }
                    }

                    override fun onResults(
                        results: Bundle?
                    ) {
                        isListening = false

                        val matches =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                        val recognizedText =
                            matches?.firstOrNull()

                        if (!recognizedText.isNullOrBlank()) {

                            itemName =
                                recognizedText.trim()

                            nameError = null
                            voiceError = null
                        }
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {
                        // Final results are used for the item name.
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) { }
                }
            )
        }

        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val items =
        (itemState as? Resource.Success)?.data ?: emptyList()

    val zoneChoices = splitZones(
        zones = zones,
        items = items,
        currentZone = initialZoneName
    )

    LaunchedEffect(actionState) {

        when (val action = actionState) {

            is Resource.Success -> {
                showSavedPopup = true
                itemViewModel.clearActionState()
            }

            is Resource.Error -> {
                nameError = action.message
                itemViewModel.clearActionState()
            }

            else -> Unit
        }
    }

    LaunchedEffect(zoneActionState) {

        if (!addingZoneFromItemScreen) {
            return@LaunchedEffect
        }

        when (val action = zoneActionState) {

            is Resource.Success -> {

                selectedZone = action.data.name

                zoneError = null

                showAddZoneDialog = false

                addingZoneFromItemScreen = false

                zoneViewModel.clearActionState()
            }

            is Resource.Error -> {

                zoneError = action.message

                addingZoneFromItemScreen = false

                zoneViewModel.clearActionState()
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

            // Screen header
            ScreenHeader(
                title = "Edit Item",
                onBack = {
                    // same logic just like in add item screen
                    showConfirmCancelPopup = true
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // Item image
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

                    if (editedImagePath != null) {

                        AsyncImage(
                            model = File(editedImagePath!!),
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { cameraCapture.launch() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary)
                    ) {
                        Text("Camera", color = SpotrWhite)
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotrSecondary)
                    ) {
                        Text("Gallery", color = SpotrPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Item name
                Column {

                    Text(
                        text = "Item Name",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    TextField(
                        value = itemName,

                        onValueChange = {
                            itemName = it

                            if (nameError != null) {
                                nameError = null
                            }

                            if (voiceError != null) {
                                voiceError = null
                            }
                        },

                        modifier = Modifier.fillMaxWidth(),

                        singleLine = true,

                        isError = nameError != null,

                        supportingText = {

                            if (nameError != null) {

                                Text(
                                    text = nameError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )

                            } else if (voiceError != null) {

                                Text(
                                    text = voiceError!!,
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )

                            } else if (isListening) {

                                Text(
                                    text = "Listening...",
                                    color = SpotrPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        },

                        trailingIcon = {

                            IconButton(
                                onClick = {

                                    if (isListening) {

                                        speechRecognizer?.stopListening()
                                        isListening = false

                                    } else {

                                        val permissionGranted =
                                            ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED

                                        if (permissionGranted) {

                                            startSpeechRecognition()

                                        } else {

                                            microphonePermissionLauncher.launch(
                                                Manifest.permission.RECORD_AUDIO
                                            )
                                        }
                                    }
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Filled.Mic,

                                    contentDescription =
                                        if (isListening) {
                                            "Stop voice input"
                                        } else {
                                            "Voice input"
                                        },

                                    tint =
                                        if (isListening) {
                                            Color.Red
                                        } else {
                                            SpotrPrimary
                                        },

                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },

                        shape = RoundedCornerShape(6.dp),

                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = SpotrPrimary,
                            unfocusedIndicatorColor = Color(0xFFCCCCCC),
                            cursorColor = SpotrPrimary,
                            errorIndicatorColor = Color.Red,
                            errorContainerColor = Color.White
                        )
                    )
                }

                // Zones
                Column {

                    if (zones.isEmpty()) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "No zones available",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SpotrPrimary
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Create a zone to assign this item.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )

                            Spacer(
                                modifier = Modifier.height(12.dp)
                            )

                            Button(
                                onClick = {
                                    showAddZoneDialog = true
                                    zoneError = null
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SpotrPrimary
                                )
                            ) {

                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = null,
                                    tint = SpotrWhite
                                )

                                Spacer(
                                    modifier = Modifier.size(6.dp)
                                )

                                Text(
                                    text = "Add Zone",
                                    color = SpotrWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                    } else {

                        // Suggested zones
                        Column {

                            Text(
                                text = "Suggested Zones",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            Spacer(
                                modifier = Modifier.height(8.dp)
                            )

                            zoneChoices.suggested.forEach { zone ->

                                ZoneButton(
                                    zoneName = zone,
                                    isSelected =
                                        selectedZone == zone,

                                    onClick = {

                                        selectedZone = zone

                                        if (zoneError != null) {
                                            zoneError = null
                                        }
                                    }
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )
                            }
                        }

                        // Other zones
                        if (zoneChoices.other.isNotEmpty()) {

                            Column {

                                Text(
                                    text = "Other Zones",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.spacedBy(10.dp)
                                ) {

                                    zoneChoices.other.forEach { zone ->

                                        OtherZoneButton(
                                            zoneName = zone,

                                            isSelected =
                                                selectedZone == zone,

                                            onClick = {

                                                selectedZone = zone

                                                if (zoneError != null) {
                                                    zoneError = null
                                                }
                                            },

                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Button(
                            onClick = {
                                showAddZoneDialog = true
                                zoneError = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SpotrSecondary
                            )
                        ) {

                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = SpotrPrimary
                            )

                            Spacer(
                                modifier = Modifier.size(6.dp)
                            )

                            Text(
                                text = "Add Zone",
                                color = SpotrPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (zoneError != null) {

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = zoneError!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                // Save button
                Button(
                    onClick = {

                        val trimmedName =
                            itemName.trim()

                        nameError =
                            if (trimmedName.isBlank()) {
                                "Please enter an item name."
                            } else {
                                null
                            }

                        zoneError =
                            if (selectedZone.isBlank()) {
                                "Please select a zone."
                            } else {
                                null
                            }

                        if (
                            nameError == null &&
                            zoneError == null
                        ) {
                            showConfirmEditPopup = true
                        }
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
                        text = "Save",
                        color = SpotrWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        // Add Zone popup
        if (showAddZoneDialog) {

            AddZoneDialog(
                onDismiss = {
                    showAddZoneDialog = false
                    addingZoneFromItemScreen = false
                },
                onConfirm = { zoneName ->

                    addingZoneFromItemScreen = true
                    zoneViewModel.addZone(zoneName)
                }
            )
        }

        // confirm dialog
        if (showConfirmEditPopup) {

            ConfirmationPopup(
                message = "Are you sure you want to edit?",
                confirmLabel = "Yes",
                cancelLabel = "Cancel",

                onConfirm = {

                    showConfirmEditPopup = false

                    val selectedZoneEntity =
                        zones.find {
                            it.name == selectedZone
                        }

                    if (selectedZoneEntity == null) {

                        zoneError =
                            "Selected zone could not be found."

                    } else {

                        itemViewModel.updateItem(
                            item.copy(
                                name = itemName.trim(),
                                type = itemName.trim(),
                                zoneId = selectedZoneEntity.id,
                                imagePath = editedImagePath
                            )
                        )
                    }
                },

                onCancel = {
                    showConfirmEditPopup = false
                }
            )
        }

        // saved dialog
        if (showSavedPopup) {

            StatusPopup(
                message = "Item Edited Successfully!",

                onDismiss = {

                    showSavedPopup = false
                    onSaved()
                }
            )
        }

        // cancel dialog
        if (showConfirmCancelPopup) {

            ConfirmationPopup(
                message = "Are you sure you want to cancel?",
                confirmLabel = "Yes",
                cancelLabel = "No",

                onConfirm = {

                    showConfirmCancelPopup = false
                    onBack()
                },

                onCancel = {
                    showConfirmCancelPopup = false
                }
            )
        }
    }
}


private fun copyGalleryImageForEdit(context: android.content.Context, uri: Uri): String? {
    return try {
        val dir = java.io.File(context.getExternalFilesDir(null), "item_photos").apply { mkdirs() }
        val file = java.io.File(dir, "EDIT_GALLERY_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file.absolutePath
    } catch (_: Exception) {
        null
    }
}
