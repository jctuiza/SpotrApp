package com.example.spotrapp.ui.putaway

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrWhite
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun rememberCameraCapture(
    onPhotoCaptured: (String) -> Unit
): CameraCaptureState {

    val context = LocalContext.current

    var pendingPhotoFile by remember {
        mutableStateOf<File?>(null)
    }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showPermissionDenied by remember { mutableStateOf(false) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->

        if (success) {
            pendingPhotoFile?.absolutePath?.let { path ->
                onPhotoCaptured(path)
            }
        }

        pendingPhotoFile = null
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->

        if (granted) {
            val (file, uri) = createPhotoUri(context)
            pendingPhotoFile = file
            takePictureLauncher.launch(uri)
        } else {
            showPermissionDenied = true
        }
    }

    val hasPermission =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    return CameraCaptureState(
        hasPermission = hasPermission,
        launch = {
            if (hasPermission) {
                val (file, uri) = createPhotoUri(context)
                pendingPhotoFile = file
                takePictureLauncher.launch(uri)
            } else {
                showPermissionRationale = true
            }
        }
    )

    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text("Allow camera access?", color = SpotrPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "SpotrApp only requests camera access when you choose Take Photo. The camera is used to capture an item photo that you choose to save with that item.",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionRationale = false
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Continue", color = SpotrPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("Not now", color = SpotrPrimary)
                }
            },
            containerColor = Color.White
        )
    }

    if (showPermissionDenied) {
        AlertDialog(
            onDismissRequest = { showPermissionDenied = false },
            title = { Text("Camera permission denied", color = SpotrPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Camera access was not granted. You can still choose a photo from your gallery, or allow camera access in your device settings.",
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                TextButton(onClick = { showPermissionDenied = false }) {
                    Text("OK", color = SpotrPrimary)
                }
            },
            containerColor = Color.White
        )
    }
}

data class CameraCaptureState(
    val hasPermission: Boolean,
    val launch: () -> Unit
)

private fun createPhotoUri(
    context: Context
): Pair<File, Uri> {

    val photoDir = File(
        context.getExternalFilesDir(null),
        "item_photos"
    ).apply {
        mkdirs()
    }

    val fileName =
        "ITEM_${
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.US
            ).format(Date())
        }.jpg"

    val file = File(
        photoDir,
        fileName
    )

    // Must match the authority declared in AndroidManifest.xml's
    // <provider> entry.
    val uri = FileProvider.getUriForFile(
        context,
        "com.example.spotrapp.fileprovider",
        file
    )

    return file to uri
}
@Composable
fun PhotoSourceDialog(
    title: String = "Change item photo",
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
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
                    onClick = onTakePhoto,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Text("Take Photo", modifier = Modifier.padding(start = 8.dp), color = SpotrWhite)
                }
                Button(
                    onClick = onChooseGallery,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SpotrPrimary)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                    Text("Choose from Gallery", modifier = Modifier.padding(start = 8.dp), color = SpotrWhite)
                }
                Button(
                    onClick = onDismiss,
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

fun copyGalleryImageToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val photoDir = File(context.getExternalFilesDir(null), "item_photos")
            .apply { mkdirs() }
        val file = File(photoDir, "GALLERY_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null
        file.absolutePath
    } catch (_: Exception) {
        null
    }
}
