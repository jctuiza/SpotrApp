package com.example.spotrapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary
import com.example.spotrapp.ui.theme.SpotrWhite

@Composable
fun ZoneButton(
    zoneName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) SpotrPrimary else SpotrSecondary

    val textColor =
        if (isSelected) SpotrWhite else SpotrPrimary

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                backgroundColor,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                vertical = 15.dp,
                horizontal = 20.dp
            )
    ) {

        // Selection indicator
        Box(
            modifier = Modifier.size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) {
                    Icons.Filled.CheckCircle
                } else {
                    Icons.Filled.RadioButtonUnchecked
                },
                contentDescription = if (isSelected) {
                    "Selected"
                } else {
                    "Not selected"
                },
                tint = if (isSelected) {
                    SpotrWhite
                } else {
                    SpotrPrimary
                },
                modifier = Modifier.size(20.dp)
            )
        }


        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = zoneName,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            )
    }
}

@Composable
fun OtherZoneButton(
    zoneName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                if (isSelected) SpotrPrimary else SpotrSecondary,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = zoneName,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) SpotrWhite else SpotrPrimary
        )
    }
}