package com.example.spotrapp.ui.putaway

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.ui.common.TutorialOverlay
import com.example.spotrapp.ui.theme.SpotrGray
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrWhite

@Composable
fun CameraScanScreen(
    onBackClick: () -> Unit,
    onPhotoCaptured: (String) -> Unit,
    showTutorial: Boolean = false,
    onDismissTutorial: () -> Unit = {}
) {

    var shutterButtonBounds by remember { mutableStateOf<Rect?>(null) }
    val context = LocalContext.current

    val cameraCapture = rememberCameraCapture(onPhotoCaptured = onPhotoCaptured)

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val localPath = copyGalleryImageToAppStorage(context, uri)
            if (localPath != null) onPhotoCaptured(localPath)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SpotrWhite,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = "Position the item within the frame and ensure " +
                            "it is well-lit before taking a photo.",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = SpotrGray
                )

                Spacer(modifier = Modifier.height(15.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SpotrWhite,
                    onClick = { galleryLauncher.launch("image/*") }
                ) {
                    Text(
                        text = "Choose from Gallery",
                        color = SpotrPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {

                    Icon(
                        imageVector = Icons.Filled.CenterFocusStrong,
                        contentDescription = "Scan area",
                        tint = SpotrWhite,
                        modifier = Modifier.size(260.dp)
                    )

                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp)
                            .onGloballyPositioned { coordinates ->
                                shutterButtonBounds = coordinates.boundsInRoot()
                            },
                        shape = CircleShape,
                        color = SpotrPrimary,
                        onClick = { cameraCapture.launch() }
                    ) {
                        Box(
                            modifier = Modifier.size(90.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Scan",
                                tint = SpotrWhite,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showTutorial && shutterButtonBounds != null) {
            TutorialOverlay(
                message = "This is where you capture or choose an item photo. Tap anywhere to continue.",
                onDismiss = onDismissTutorial,
                highlightBounds = shutterButtonBounds,
                highlightShape = CircleShape
            )
        }
    }
}

private fun copyGalleryImageToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val photoDir = java.io.File(
            context.getExternalFilesDir(null),
            "item_photos"
        ).apply { mkdirs() }

        val file = java.io.File(
            photoDir,
            "GALLERY_${System.currentTimeMillis()}.jpg"
        )

        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        } ?: return null

        file.absolutePath
    } catch (_: Exception) {
        null
    }
}
