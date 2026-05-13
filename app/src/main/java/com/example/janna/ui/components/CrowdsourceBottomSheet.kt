package com.example.janna.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.janna.data.Store
import com.example.janna.ui.theme.ClinicalBlue
import com.example.janna.ui.theme.SuccessGreen
import com.example.janna.ui.theme.WarningRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrowdsourceBottomSheet(
    store: Store,
    onDismiss: () -> Unit,
    onReport: (Boolean, Int) -> Unit
) {
    var selectedStockStatus by remember { mutableStateOf<Boolean?>(null) }
    var rating by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Help the Community",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Is medicine available at ${store.name}?",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatusButton(
                    text = "IN STOCK",
                    icon = SuccessGreen,
                    isSelected = selectedStockStatus == true,
                    onClick = { selectedStockStatus = true },
                    modifier = Modifier.weight(1f)
                )
                StatusButton(
                    text = "OUT OF STOCK",
                    icon = WarningRed,
                    isSelected = selectedStockStatus == false,
                    onClick = { selectedStockStatus = false },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Rate Kendra Service", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < rating) Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = null,
                        tint = if (index < rating) Color(0xFFFFB300) else Color.LightGray,
                        modifier = Modifier.size(40.dp).clickable { rating = index + 1 }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (selectedStockStatus != null && rating > 0) {
                        onReport(selectedStockStatus!!, rating)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedStockStatus != null && rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = ClinicalBlue)
            ) {
                Text("SUBMIT REPORT", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusButton(
    text: String,
    icon: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) icon.copy(alpha = 0.1f) else Color.Transparent,
            contentColor = if (isSelected) icon else Color.Gray
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) icon else Color.LightGray
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(8.dp).background(icon, CircleShape))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
