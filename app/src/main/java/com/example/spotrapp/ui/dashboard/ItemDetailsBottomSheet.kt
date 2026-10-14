package com.example.spotrapp.ui.dashboard

// for clicking the 3 dot below the item card
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.spotrapp.data.local.ItemEntity
import com.example.spotrapp.ui.theme.SpotrSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class PhotoMetadata(
    val fileName: String,
    val sizeLabel: String,
    val photoDate: String,
    val dimensions: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsBottomSheet(
    item: ItemEntity,
    onDismiss: () -> Unit
) {
    val photoMetadata = produceState<PhotoMetadata?>(initialValue = null, key1 = item.imagePath) {
        value = withContext(Dispatchers.IO) {
            val photoFile = item.imagePath?.let(::File)
            if (photoFile == null || !photoFile.isFile) {
                null
            } else {
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(photoFile.absolutePath, options)
                val bytes = photoFile.length()
                val sizeLabel = if (bytes < 1024L * 1024L) {
                    String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0)
                } else {
                    String.format(Locale.getDefault(), "%.2f MB", bytes / (1024.0 * 1024.0))
                }
                PhotoMetadata(
                    fileName = photoFile.name,
                    sizeLabel = sizeLabel,
                    photoDate = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
                        .format(Date(photoFile.lastModified())),
                    dimensions = if (options.outWidth > 0 && options.outHeight > 0) {
                        "${options.outWidth} × ${options.outHeight} px"
                    } else {
                        "Unavailable"
                    }
                )
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        containerColor = Color.Transparent,
        dragHandle = null,
        shape = RoundedCornerShape(0.dp),
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.4f)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                shape = RoundedCornerShape(
                    topStart = 32.dp,
                    topEnd = 32.dp
                ),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 100.dp,
                            start = 32.dp,
                            end = 32.dp,
                            bottom = 32.dp
                        )
                        .verticalScroll(rememberScrollState())
                ) {
                    DetailSection(
                        label = "Item Name",
                        value = item.name
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    DetailSection(
                        label = "Type",
                        value = item.type
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    DetailSection(
                        label = "Date Added",
                        value = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                            .format(Date(item.dateAdded))
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Photo Information",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val metadata = photoMetadata.value
                    if (metadata == null) {
                        DetailSection(
                            label = "Photo",
                            value = if (item.imagePath.isNullOrBlank()) "No photo attached" else "Photo unavailable"
                        )
                    } else {
                        DetailSection(label = "Filename", value = metadata.fileName)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailSection(label = "File Size", value = metadata.sizeLabel)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailSection(label = "Photo Date", value = metadata.photoDate)
                        Spacer(modifier = Modifier.height(12.dp))
                        DetailSection(label = "Image Dimensions", value = metadata.dimensions)
                    }
                }
            }

            // Close Button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(top = 96.dp, start = 16.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Floating Image Card
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 0.dp)
                    .size(160.dp),
                shape = RoundedCornerShape(24.dp),
                color = SpotrSecondary,
                shadowElevation = 8.dp
            ) {
                if (item.imagePath != null) {
                    AsyncImage(
                        model = File(item.imagePath),
                        contentDescription = item.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}