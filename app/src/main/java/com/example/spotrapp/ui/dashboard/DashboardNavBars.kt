package com.example.spotrapp.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.ui.theme.SpotrGray
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrWhite

@Composable
fun DashboardTopBar(onSearchClick: () -> Unit, onHelpClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Spotr",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = SpotrPrimary
        )

        Row {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = SpotrPrimary
                )
            }
            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = "Show tutorial",
                    tint = SpotrPrimary
                )
            }
        }
    }
}

@Composable
fun DashboardBottomNav(
    selectedItem: String?,
    onHomeClick: () -> Unit,
    onScanClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onScanButtonPositioned: (Rect) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // home button
        NavItem(
            icon = Icons.Filled.Home,
            label = "Home",
            tint = if (selectedItem == "home") {
                SpotrPrimary
            } else {
                SpotrGray
            },
            onClick = onHomeClick
        )

        // cam button
        Surface(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                onScanButtonPositioned(coordinates.boundsInRoot())
            },
            shape = RoundedCornerShape(12.dp),
            color = SpotrPrimary,
            onClick = onScanClick
        ) {
            Icon(
                imageVector = Icons.Outlined.CameraAlt,
                contentDescription = "Scan",
                tint = SpotrWhite,
                modifier = Modifier.padding(12.dp)
            )
        }

        // history button
        NavItem(
            icon = Icons.Outlined.History,
            label = "History",
            tint = if (selectedItem == "history") {
                SpotrPrimary
            } else {
                SpotrGray
            },
            onClick = onHistoryClick
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint
            )

            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}