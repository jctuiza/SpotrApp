package com.example.spotrapp.ui.putaway

// Displays the camera scanning interface used by the item put-away flow to capture item images.

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrWhite

@Composable
fun CameraScanScreen(
    onBackClick: () -> Unit,
    onPhotoCaptured: (String) -> Unit,
    showTutorial: Boolean = false,
    onDismissTutorial: () -> Unit = {}
) {
    val context = LocalContext.current
    var showChooser by remember { mutableStateOf(true) }
    val cameraCapture = rememberCameraCapture { photoPath ->
        showChooser = false
        onPhotoCaptured(photoPath)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        showChooser = false
        if (uri != null) {
            copyGalleryImageToAppStorage(context, uri)?.let(onPhotoCaptured)
        }
    }

    if (showChooser) {
        Dialog(onDismissRequest = onBackClick) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, SpotrSecondary, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add an item photo",
                        color = SpotrPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Take a new photo or choose an existing image.",
                        color = Color.DarkGray,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { cameraCapture.launch() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Text("Take Photo", modifier = Modifier.padding(start = 8.dp), color = SpotrWhite)
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Text("Choose from Gallery", modifier = Modifier.padding(start = 8.dp), color = SpotrWhite)
                    }
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotrSecondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = SpotrPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
