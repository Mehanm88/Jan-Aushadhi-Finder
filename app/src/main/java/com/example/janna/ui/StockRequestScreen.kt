package com.example.janna.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.janna.data.MedicineRepository
import com.example.janna.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockRequestScreen() {
    var selectedStore by remember { mutableStateOf(MedicineRepository.stores[0].name) }
    var medicineName by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = MedicalNavy,
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(MedicalNavy, MedicalNavyLight)
                            )
                        )
                        .statusBarsPadding()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "INVENTORY REQUEST",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MedicalTeal,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Request Stock Availability Alerts",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = null,
                            tint = MedicalTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        containerColor = BackgroundClinical
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "CAN'T FIND YOUR MEDICINE?",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MedicalTeal,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Request stock availability alerts from your preferred Jan Aushadhi Kendra.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "TARGET KENDRA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MedicalNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedStore,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MedicalTeal,
                                unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            MedicineRepository.stores.forEach { store ->
                                DropdownMenuItem(
                                    text = { Text(store.name, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        selectedStore = store.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "MEDICINE NAME",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MedicalNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = medicineName,
                        onValueChange = { medicineName = it },
                        placeholder = { Text("e.g. Metformin 500mg", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MedicalTeal,
                            unfocusedBorderColor = TextTertiary.copy(alpha = 0.3f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { showSuccessDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = medicineName.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MedicalNavy,
                    disabledContainerColor = MedicalNavy.copy(alpha = 0.3f)
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("SUBMIT INVENTORY REQUEST", fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            }
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                icon = {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(32.dp))
                        }
                    }
                },
                title = { 
                    Text(
                        "REQUEST LOGGED", 
                        fontWeight = FontWeight.Black, 
                        color = MedicalNavy,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) 
                },
                text = { 
                    Text(
                        "Your notification request for '$medicineName' has been successfully registered at $selectedStore.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) 
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            showSuccessDialog = false
                            medicineName = ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalNavy),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("DONE", fontWeight = FontWeight.Black)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            )
        }
    }
}
