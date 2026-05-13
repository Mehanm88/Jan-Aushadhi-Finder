package com.example.janna.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.janna.R
import com.example.janna.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MedicalNavy, Color(0xFF0F172A))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                color = Color.White.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(2.dp, MedicalTeal.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Using a generic medical icon if resource doesn't exist, 
                    // but in a real app we'd use the generated logo
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MedicalTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "JAN-AUSHADHI",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                modifier = Modifier.animateContentSize()
            )
            
            Text(
                text = "PROFESSIONAL MEDICAL ASSISTANT",
                style = MaterialTheme.typography.labelMedium,
                color = MedicalTeal,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            CircularProgressIndicator(
                color = MedicalTeal,
                strokeWidth = 2.dp,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text = "Clinical-Grade Healthcare",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.3f),
            letterSpacing = 1.sp
        )
    }
}
