package com.example.spotrapp.ui.putaway

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.spotrapp.ui.theme.SpotrPrimary
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
    val cameraCapture = rememberCameraCapture(onPhotoCaptured)

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            copyGalleryImageToAppStorage(context, uri)?.let(onPhotoCaptured)
        }
        showChooser = false
    }

    if (showChooser) {
        AlertDialog(
            onDismissRequest = onBackClick,
            title = { Text("Add an item photo") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Take a new photo or choose an existing image.")
                    Button(
                        onClick = { cameraCapture.launch() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Spacer(Modifier.height(0.dp))
                        Text("Take Photo", modifier = Modifier.padding(start = 8.dp), color = SpotrWhite)
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary)
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null)
                        Text("Choose from Gallery", modifier = Modifier.padding(start = 8.dp), color = SpotrWhite)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onBackClick) {
                    Text("Cancel", color = SpotrPrimary)
                }
            }
        )
    }
}

private fun copyGalleryImageToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val photoDir = java.io.File(context.getExternalFilesDir(null), "item_photos")
            .apply { mkdirs() }
        val file = java.io.File(photoDir, "GALLERY_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file.absolutePath
    } catch (_: Exception) {
        null
    }
}
