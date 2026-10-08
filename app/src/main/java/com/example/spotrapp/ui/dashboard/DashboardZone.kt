package com.example.spotrapp.ui.dashboard

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.data.local.ZoneEntity
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrWhite

@Composable
fun ZoneFilterRow(
    zones: List<ZoneEntity>,
    selectedZone: String,
    onZoneSelected: (String) -> Unit,
    onZoneLongPress: (ZoneEntity) -> Unit
) {
    if (zones.isEmpty()) {
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // "All" is always available once at least one zone exists.
        Surface(
            shape = RoundedCornerShape(75),
            color = if (selectedZone == "All") {
                SpotrPrimary
            } else {
                SpotrSecondary
            },
            modifier = Modifier.combinedClickable(
                onClick = {
                    onZoneSelected("All")
                }
            )
        ) {
            Text(
                text = "All",
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                color = if (selectedZone == "All") {
                    SpotrWhite
                } else {
                    SpotrPrimary
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        // Display every zone from the database.
        zones.forEach { zone ->

            val isSelected = zone.name == selectedZone

            Surface(
                shape = RoundedCornerShape(75.dp),
                color = if (isSelected) {
                    SpotrPrimary
                } else {
                    SpotrSecondary
                },
                modifier = Modifier.combinedClickable(
                    onClick = {
                        onZoneSelected(zone.name)
                    },
                    onLongClick = {
                        onZoneLongPress(zone)
                    }
                )
            ) {
                Text(
                    text = zone.name,
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    color = if (isSelected) {
                        SpotrWhite
                    } else {
                        SpotrPrimary
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // "+" opens the Add Zone dialog.
        Surface(
            shape = RoundedCornerShape(75.dp),
            color = SpotrSecondary,
            modifier = Modifier.combinedClickable(
                onClick = {
                    onZoneSelected("+")
                }
            )
        ) {
            Text(
                text = "+",
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                ),
                color = SpotrPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}