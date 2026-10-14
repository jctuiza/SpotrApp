package com.example.spotrapp.ui.common

// for the tutorial
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.ui.theme.SpotrSecondary
import kotlin.math.min

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
    highlightBounds: Rect? = null,
    highlightCorrectionX: Dp = 0.dp,
    highlightCorrectionY: Dp = 0.dp
) {
    val density = LocalDensity.current
    var overlayPositionInRoot by remember { mutableStateOf(Offset.Zero) }
    val fallbackRadius = with(density) { 10.dp.toPx() }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                overlayPositionInRoot = coordinates.positionInRoot()
            }
            .clickable(onClick = onDismiss)
    ) {
        val overlayHeightPx = with(density) { maxHeight.toPx() }
        val bounds = highlightBounds
        val localHighlightTop = bounds?.top?.minus(overlayPositionInRoot.y) ?: 0f
        val highlightCenterY = bounds?.let { localHighlightTop + it.height / 2f }
        val messageAtTop = highlightCenterY != null && highlightCenterY > overlayHeightPx / 2f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                },
            onDraw = {
                drawRect(Color.Black.copy(alpha = 0.72f))
                if (bounds != null) {
                    val left = bounds.left - overlayPositionInRoot.x + highlightCorrectionX.toPx()
                    val top = bounds.top - overlayPositionInRoot.y + highlightCorrectionY.toPx()
                    val width = bounds.width
                    val height = bounds.height
                    val radius = if (highlightShape == CircleShape) {
                        min(width, height) / 2f
                    } else {
                        fallbackRadius
                    }
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        cornerRadius = CornerRadius(radius, radius),
                        blendMode = BlendMode.Clear
                    )
                }
            }
        )

        if (bounds != null) {
            with(density) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .offset(
                            x = (bounds.left - overlayPositionInRoot.x + highlightCorrectionX.toPx()).toDp(),
                            y = (bounds.top - overlayPositionInRoot.y + highlightCorrectionY.toPx()).toDp()
                        )
                        .size(
                            width = bounds.width.toDp(),
                            height = bounds.height.toDp()
                        )
                        .border(2.dp, SpotrSecondary, highlightShape)
                )
            }
        } else {
            androidx.compose.foundation.layout.Box(
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
                .align(
                    when {
                        bounds == null -> Alignment.Center
                        messageAtTop -> Alignment.TopCenter
                        else -> Alignment.BottomCenter
                    }
                )
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .background(Color(0xF2102A43), RoundedCornerShape(16.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp)
        )
    }
}
