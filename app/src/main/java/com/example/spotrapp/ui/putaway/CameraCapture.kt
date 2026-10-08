package com.example.spotrapp.ui.putaway

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
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
        }

        // if denied: nothing happens for now
        // caller can use hasPermission if needed
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

                permissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    )
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