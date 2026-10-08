package com.example.spotrapp.ui.putaway

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.spotrapp.data.local.ItemEntity
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.ui.common.ConfirmationPopup
import com.example.spotrapp.ui.common.OtherZoneButton
import com.example.spotrapp.ui.common.ScreenHeader
import com.example.spotrapp.ui.common.StatusPopup
import com.example.spotrapp.ui.common.TutorialOverlay
import com.example.spotrapp.ui.common.ZoneButton
import com.example.spotrapp.ui.common.splitZones
import com.example.spotrapp.ui.dashboard.AddZoneDialog
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrWhite
import com.example.spotrapp.viewmodel.ItemViewModel
import com.example.spotrapp.viewmodel.ZoneViewModel
import java.util.Locale

@Composable
fun AddItemScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    showTutorial: Boolean = false,
    onDismissTutorial: () -> Unit = {},
    onTutorialComplete: () -> Unit = {},
    photoPath: String? = null,
    itemViewModel: ItemViewModel = hiltViewModel(),
    zoneViewModel: ZoneViewModel = hiltViewModel()
) {
    val itemState by itemViewModel.itemState.collectAsStateWithLifecycle()
    val zoneState by zoneViewModel.zoneState.collectAsStateWithLifecycle()
    val actionState by itemViewModel.actionState.collectAsStateWithLifecycle()
    val zoneActionState by zoneViewModel.actionState.collectAsStateWithLifecycle()

    // popup states
    var showConfirmAddPopup by remember { mutableStateOf(false) }
    var showSavedPopup by remember { mutableStateOf(false) }
    var showConfirmCancelPopup by remember { mutableStateOf(false) }
    var showAddZoneDialog by remember { mutableStateOf(false) }

    // Used to know that the current zone action came from this screen.
    var addingZoneFromItemScreen by remember { mutableStateOf(false) }

    // Static text field state
    var itemName by remember { mutableStateOf("") }

    // zone select state
    var selectedZone by remember { mutableStateOf("") }

    // error handling states
    var nameError by remember { mutableStateOf<String?>(null) }
    var zoneError by remember { mutableStateOf<String?>(null) }

    // voice input states
    var isListening by remember { mutableStateOf(false) }
    var voiceError by remember { mutableStateOf<String?>(null) }

    // states for tutorial overlay
    var localTutorialStep by remember { mutableIntStateOf(0) }
    var showTutorialCompleteDialog by remember { mutableStateOf(false) }
    var nameFieldBounds by remember { mutableStateOf<Rect?>(null) }
    var zoneSectionBounds by remember { mutableStateOf<Rect?>(null) }
    var saveButtonBounds by remember { mutableStateOf<Rect?>(null) }

    val tutorialSteps = listOf(
        "Enter or speak an item's name here. Tap anywhere to continue." to nameFieldBounds,
        "Choose the zone where the item will be stored. Tap anywhere to continue." to zoneSectionBounds,
        "Save the item here when you're ready. Tap anywhere to finish the tutorial." to saveButtonBounds
    )

    val zones = (zoneState as? Resource.Success)?.data ?: emptyList()
    val items = (itemState as? Resource.Success)?.data ?: emptyList()

    val zoneChoices = splitZones(
        zones = zones,
        items = items
    )

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
                        params: android.os.Bundle?
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
                        results: android.os.Bundle?
                    ) {
                        isListening = false

                        val matches =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                        val recognizedText =
                            matches?.firstOrNull()

                        if (!recognizedText.isNullOrBlank()) {
                            itemName = recognizedText.trim()

                            nameError = null
                            voiceError = null
                        }
                    }

                    override fun onPartialResults(
                        partialResults: android.os.Bundle?
                    ) {
                        // Final results are used for the item name.
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: android.os.Bundle?
                    ) { }
                }
            )
        }

        onDispose {
            speechRecognizer?.destroy()
        }
    }

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

    /*
     * Handles zone creation when the user adds a zone directly
     * from the Add Item screen.
     *
     * The ZoneViewModel now returns the exact ZoneEntity that
     * was inserted, so there is no need to guess which zone
     * was created by looking for the newest ID.
     */
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

            ScreenHeader(
                title = "Item Details",
                onBack = {
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

                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = "Item Image",
                        tint = SpotrPrimary,
                        modifier = Modifier.size(180.dp)
                    )
                }

                // Item name
                Column(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        nameFieldBounds = coordinates.boundsInRoot()
                    }
                ) {

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

                        placeholder = {
                            Text(
                                text = "Enter item name"
                            )
                        },

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
                Column(
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        zoneSectionBounds = coordinates.boundsInRoot()
                    }
                ) {

                    if (zones.isEmpty()) {

                        /*
                         * Empty zone state.
                         *
                         * The user can now create their first zone
                         * without leaving the Add Item screen.
                         */
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "No zones yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = SpotrPrimary
                            )

                            Spacer(
                                modifier = Modifier.height(6.dp)
                            )

                            Text(
                                text = "Create a zone before adding this item.",
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
                                    modifier = Modifier.width(6.dp)
                                )

                                Text(
                                    text = "Add Zone",
                                    color = SpotrWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                    } else {

                        // Suggested Zones
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
                                    isSelected = selectedZone == zone,
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

                        // Other Zones
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

                        /*
                         * Allows the user to create another zone
                         * without leaving the Add Item screen.
                         */
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
                                modifier = Modifier.width(6.dp)
                            )

                            Text(
                                text = "Add Zone",
                                color = SpotrPrimary,
                                fontWeight = FontWeight.Bold
                            )
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
                            showConfirmAddPopup = true
                        }
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .onGloballyPositioned { coordinates ->
                            saveButtonBounds =
                                coordinates.boundsInRoot()
                        },

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
                },
                onConfirm = { zoneName ->

                    addingZoneFromItemScreen = true
                    zoneViewModel.addZone(zoneName)
                }
            )
        }

        // Confirm add popup
        if (showConfirmAddPopup) {

            ConfirmationPopup(
                message = "Are you sure you want to add this item?",
                confirmLabel = "Yes",
                cancelLabel = "Cancel",

                onConfirm = {

                    showConfirmAddPopup = false

                    val selectedZoneEntity =
                        zones.find {
                            it.name == selectedZone
                        }

                    if (selectedZoneEntity == null) {

                        zoneError =
                            "Selected zone could not be found."

                    } else {

                        itemViewModel.addItem(
                            ItemEntity(
                                name = itemName.trim(),
                                type = itemName.trim(),
                                zoneId = selectedZoneEntity.id,
                                imagePath = photoPath,
                                notes = null
                            )
                        )
                    }
                },

                onCancel = {
                    showConfirmAddPopup = false
                }
            )
        }

        // Saved popup
        if (showSavedPopup) {

            StatusPopup(
                message = "Item Added Successfully!",

                onDismiss = {

                    showSavedPopup = false
                    onSaved()
                }
            )
        }

        // Cancel popup
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

        if (
            showTutorial &&
            !showTutorialCompleteDialog &&
            localTutorialStep < tutorialSteps.size
        ) {

            val (stepMessage, stepBounds) =
                tutorialSteps[localTutorialStep]

            if (stepBounds != null) {

                TutorialOverlay(
                    message = stepMessage,
                    highlightBounds = stepBounds,
                    onDismiss = {
                        if (localTutorialStep < tutorialSteps.lastIndex) {
                            localTutorialStep++
                        } else {
                            showTutorialCompleteDialog = true
                        }
                    }
                )
            }
        }

        if (showTutorialCompleteDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = {},
                title = {
                    Text("Tutorial Complete")
                },
                text = {
                    Text("You're all set! You can now use Spotr to organize your items.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showTutorialCompleteDialog = false
                            onTutorialComplete()
                        }
                    ) {
                        Text("Go to Home")
                    }
                }
            )
        }
    }
}