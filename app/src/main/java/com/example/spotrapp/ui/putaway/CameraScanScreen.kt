package com.example.spotrapp.ui.putaway

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

    // state for tutorial bounds
    var shutterButtonBounds by remember { mutableStateOf<Rect?>(null) }

    val cameraCapture = rememberCameraCapture(onPhotoCaptured = onPhotoCaptured)

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            // Back button
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

                // Camera scanning area
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
                        // was onScanClick — now actually launches the camera
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
                message = "Tap the camera icon to capture a photo of your item",
                onDismiss = onDismissTutorial,
                highlightBounds = shutterButtonBounds,
                highlightShape = CircleShape
            )
        }
    }
}