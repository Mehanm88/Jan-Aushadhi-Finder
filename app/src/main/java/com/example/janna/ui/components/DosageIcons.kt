package com.example.janna.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DosageIcon(dosageTime: String, modifier: Modifier = Modifier) {
    val (icon, color) = when (dosageTime) {
        "Morning" -> Icons.Default.Brightness5 to Color(0xFFFFB300) // Sun
        "Noon" -> Icons.Default.Brightness7 to Color(0xFFFF8F00) // High Sun
        "Night" -> Icons.Default.NightsStay to Color(0xFF303F9F) // Moon
        else -> Icons.Default.Brightness5 to Color.Gray
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = androidx.compose.foundation.shape.CircleShape,
        modifier = modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = dosageTime,
            tint = color,
            modifier = Modifier.padding(6.dp).size(20.dp)
        )
    }
}
