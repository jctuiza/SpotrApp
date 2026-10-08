package com.example.spotrapp.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.R
import com.example.spotrapp.ui.theme.SpotrGray
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrWhite

enum class DashboardEmptyStateType {
    NO_ZONES,
    NO_ITEMS
}

@Composable
fun DashboardEmptyState(
    modifier: Modifier = Modifier,
    emptyState: DashboardEmptyStateType = DashboardEmptyStateType.NO_ITEMS,
    onAddZoneClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Image(
            painter = painterResource(id = R.drawable.empty_box),
            contentDescription =
                if (emptyState == DashboardEmptyStateType.NO_ZONES) {
                    "No zones"
                } else {
                    "No items"
                },
            modifier = Modifier
                .size(240.dp)
                .padding(bottom = 16.dp)
        )

        if (emptyState == DashboardEmptyStateType.NO_ZONES) {

            Text(
                text = "No zones yet",
                color = SpotrPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "Create your first zone to start organizing your items.",
                color = SpotrGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = onAddZoneClick,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SpotrPrimary
                    )
            ) {

                Text(
                    text = "+ Add Zone",
                    color = SpotrWhite,
                    fontWeight = FontWeight.Bold
                )
            }

        } else {

            Text(
                text = "No items in this\nzone...",
                color = SpotrPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text =
                    "There are no logged items for the selected zone. " +
                            "Try choosing another one.",
                color = SpotrGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardEmptyStatePreview() {
    DashboardEmptyState(
        emptyState = DashboardEmptyStateType.NO_ITEMS
    )
}

@Preview(showBackground = true)
@Composable
fun DashboardNoZonesEmptyStatePreview() {
    DashboardEmptyState(
        emptyState = DashboardEmptyStateType.NO_ZONES,
        onAddZoneClick = {}
    )
}