package com.example.spotrapp.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.ui.theme.SpotrGray
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrTextBlack
import com.example.spotrapp.ui.theme.SpotrWhite

@Composable
fun VoiceSearchScreen(
    onBackClick: () -> Unit,
    onResult: (String) -> Unit
) {
    val voiceCapture = rememberVoiceCapture(onResult = onResult)

    Scaffold { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SpotrWhite)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 12.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Back button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    onClick = onBackClick
                ) {
                    Icon(
                        imageVector =
                            Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SpotrTextBlack,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Search by Voice",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SpotrTextBlack
                )

                Spacer(
                    modifier = Modifier.height(12.dp)
                )

                Text(
                    text = "Tap the microphone and say the name of an item.",
                    fontSize = 14.sp,
                    color = SpotrGray
                )

                Spacer(
                    modifier = Modifier.height(40.dp)
                )

                Surface(
                    shape = CircleShape,
                    color = SpotrPrimary,
                    // was: { itemNotFound = true } — now actually launches
                    // the system speech recognizer
                    onClick = { voiceCapture.launch() }
                ) {

                    Box(
                        modifier = Modifier.size(120.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "voice search",
                            tint = SpotrWhite,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Tap to start",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SpotrGray
                )
            }
        }
    }
}