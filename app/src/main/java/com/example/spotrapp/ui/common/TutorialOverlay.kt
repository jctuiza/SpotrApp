package com.example.spotrapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.ui.theme.SpotrSecondary
import kotlin.math.roundToInt

// reusable per tutorial
@Composable
fun TutorialOverlay(
    message: String,
    onDismiss: () -> Unit,
    highlightAlignment: Alignment = Alignment.Center,
    highlightWidth: Dp = 64.dp,
    highlightHeight: Dp = 56.dp,
    highlightBottomPadding: Dp = 0.dp,
    highlightOffsetX: Dp = 0.dp,
    highlightOffsetY: Dp = 0.dp,
    highlightShape: Shape = RoundedCornerShape(8.dp),
    highlightBounds: Rect? = null
) {
    val density = LocalDensity.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss)
    ) {
        if (highlightBounds != null) {
            with(density) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                highlightBounds.left.roundToInt(),
                                highlightBounds.top.roundToInt()
                            )
                        }
                        .size(
                            width = highlightBounds.width.toDp(),
                            height = highlightBounds.height.toDp()
                        )
                        .border(2.dp, SpotrSecondary, highlightShape)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .align(highlightAlignment)
                    .offset(x = highlightOffsetX, y = highlightOffsetY)
                    .padding(bottom = highlightBottomPadding)
                    .size(highlightWidth, highlightHeight)
                    .border(2.dp, SpotrSecondary, highlightShape)
            )
        }

        Text(
            text = message,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        )
    }
}