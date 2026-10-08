package com.example.spotrapp.ui.onboarding

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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotrapp.ui.theme.SpotrGray
import com.example.spotrapp.ui.theme.SpotrPrimary
import com.example.spotrapp.ui.theme.SpotrSecondary

data class OnboardingFeature(
    val icon: ImageVector,
    val title: String,
    val description: String
)

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val features: List<OnboardingFeature> = emptyList(),
    val buttonLabel: String
)

private val onboardingPages = listOf(
    OnboardingPage(
        title = "Welcome to Spotr!",
        subtitle = "Know where everything belongs.",
        buttonLabel = "Get Started"
    ),
    OnboardingPage(
        title = "Zones, not pins",
        subtitle = "Other trackers ask for exact spots. Spotr only needs the general area.",
        features = listOf(
            OnboardingFeature(Icons.Outlined.LocationOn, "Hardware Trackers", "Exact spots, hardware per item"),
            OnboardingFeature(Icons.Outlined.Map, "Spotr Zones", "General area, no hardware")
        ),
        buttonLabel = "Next"
    ),
    OnboardingPage(
        title = "See it, don't just read it",
        subtitle = "Add a quick photo to any zone so you recognize it at a glance",
        features = listOf(
            OnboardingFeature(Icons.Outlined.Lock, "Photos stay on your device", "Nothing is uploaded, since Spotr works fully offline.")
        ),
        buttonLabel = "Start Tutorial"
    )
)

@Composable
fun OnboardingScreen(
    onBoardingFinished: () -> Unit,
    onStartTutorial: () -> Unit = onBoardingFinished
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val page = onboardingPages[currentPage]

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (currentPage > 0) {
                IconButton(onClick = { currentPage-- }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = SpotrPrimary
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            TextButton(onClick = onBoardingFinished) {
                Text("Skip", color = SpotrPrimary)
            }
        }

        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingSlide(page = page)
        }

        Button(
            onClick = {
                if (currentPage < onboardingPages.lastIndex) {
                    currentPage++
                } else {
                    onStartTutorial()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(SpotrPrimary)
        ) {
            Text(page.buttonLabel)
        }

        PageIndicator(
            pageCount = onboardingPages.size,
            currentPage = currentPage,
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.Center
        )
    }
}

@Composable
fun OnboardingSlide(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = page.title,
            color = SpotrPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = page.subtitle,
            color = SpotrGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        if (page.features.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                page.features.forEach { feature -> OnboardingFeatureRow(feature) }
            }
        }
    }
}

@Composable
fun OnboardingFeatureRow(feature: OnboardingFeature) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(SpotrSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = feature.icon, contentDescription = null, tint = SpotrPrimary)
        }
        Column {
            Text(feature.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(feature.description, color = SpotrGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp)
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(width = 24.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(if (index == currentPage) SpotrPrimary else SpotrGray.copy(alpha = 0.4f))
            )
        }
    }
}
