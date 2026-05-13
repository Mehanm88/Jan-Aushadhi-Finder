package com.example.janna.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.janna.data.ProfileEntity
import com.example.janna.data.ReminderEntity
import com.example.janna.ui.components.DosageIcon
import com.example.janna.ui.theme.*
import com.example.janna.util.AdherenceUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(viewModel: ReminderViewModel) {
    val context = LocalContext.current
    val profiles by viewModel.profiles.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    
    var showAddReminderSheet by remember { mutableStateOf(false) }
    var showAddProfileDialog by remember { mutableStateOf(false) }
    var selectedReminderForStock by remember { mutableStateOf<ReminderEntity?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Notifications enabled!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FAMILY HEALTH PROFILES",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = MedicalTeal,
                            letterSpacing = 1.5.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.testNotification() }) {
                                Icon(
                                    Icons.Default.NotificationsActive,
                                    contentDescription = "Test Notification",
                                    tint = MedicalTeal
                                )
                            }
                            Icon(
                                Icons.Default.GroupAdd,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.4f),
                                modifier = Modifier.clickable { showAddProfileDialog = true }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(end = 20.dp)
                    ) {
                        items(profiles) { profile ->
                            ProfileChip(
                                profile = profile,
                                isSelected = selectedProfileId == profile.id,
                                onClick = { viewModel.selectProfile(profile.id) }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddReminderSheet = true },
                icon = { Icon(Icons.Default.AlarmAdd, contentDescription = null) },
                text = { Text("SET REFILL ALARM", fontWeight = FontWeight.Black, letterSpacing = 1.sp) },
                containerColor = MedicalTeal,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp)
            )
        },
        containerColor = BackgroundClinical
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                ClinicalSummaryCard(reminders.size)
            }

            item {
                Text(
                    text = "ACTIVE PRESCRIPTIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }

            items(reminders, key = { it.id }) { reminder ->
                ClinicalReminderItem(
                    reminder = reminder,
                    onToggle = { isActive -> viewModel.toggleReminder(reminder, isActive) },
                    onDelete = { viewModel.deleteReminder(reminder) },
                    onRefreshStock = { selectedReminderForStock = reminder }
                )
            }
            
            if (reminders.isEmpty()) {
                item {
                    EmptyRemindersView()
                }
            }
        }
    }

    if (showAddReminderSheet) {
        AddReminderSheet(
            profileId = selectedProfileId,
            onDismiss = { showAddReminderSheet = false },
            onAdd = { reminder -> 
                viewModel.addReminder(reminder)
                showAddReminderSheet = false
            }
        )
    }

    if (showAddProfileDialog) {
        AddProfileDialog(
            onDismiss = { showAddProfileDialog = false },
            onAdd = { name, relation, emoji ->
                viewModel.addProfile(name, relation, emoji)
                showAddProfileDialog = false
            }
        )
    }

    if (selectedReminderForStock != null) {
        UpdateStockDialog(
            reminder = selectedReminderForStock!!,
            onDismiss = { selectedReminderForStock = null },
            onUpdate = { newStock ->
                viewModel.addReminder(selectedReminderForStock!!.copy(currentStock = newStock))
                selectedReminderForStock = null
            }
        )
    }
}

@Composable
fun ProfileChip(profile: ProfileEntity, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            color = if (isSelected) MedicalTeal else Color.White.copy(alpha = 0.1f),
            shape = CircleShape,
            border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null,
            shadowElevation = if (isSelected) 12.dp else 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(profile.avatarEmoji, fontSize = 28.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = profile.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ClinicalReminderItem(
    reminder: ReminderEntity,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onRefreshStock: () -> Unit
) {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = sdf.format(Date(reminder.reminderTime))
    val daysLeft = AdherenceUtils.calculateDaysRemaining(reminder)
    val statusColor = AdherenceUtils.getStockStatusColor(daysLeft)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MedicalTealLight,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        DosageIcon(dosageTime = reminder.dosageTime)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.medicineName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MedicalNavy
                    )
                    Text(
                        text = "Daily at $timeStr • ${reminder.dosageTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = WarningRed.copy(alpha = 0.6f))
                }
                Switch(
                    checked = reminder.isActive,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MedicalTeal
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = TextTertiary.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = statusColor,
                            shape = CircleShape
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (daysLeft <= 0) "CRITICAL: OUT OF STOCK" else "$daysLeft DAYS OF STOCK LEFT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                    if (daysLeft > 0) {
                        val refillDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(AdherenceUtils.predictRefillDate(reminder)))
                        Text(
                            text = "Predicted Refill: $refillDate",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                
                Button(
                    onClick = onRefreshStock,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalNavy)
                ) {
                    Text("REFRESH STOCK", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderSheet(profileId: Int, onDismiss: () -> Unit, onAdd: (ReminderEntity) -> Unit) {
    var medicineName by remember { mutableStateOf("") }
    var dosageTime by remember { mutableStateOf("Morning") }
    var currentStock by remember { mutableStateOf("30") }
    var dosagePerDay by remember { mutableStateOf("1") }
    
    val timePickerState = rememberTimePickerState(initialHour = 8, initialMinute = 0)
    var showTimePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextTertiary.copy(alpha = 0.3f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                "NEW REFILL ALARM",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MedicalNavy
            )
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = medicineName,
                onValueChange = { medicineName = it },
                label = { Text("Medicine Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalTeal)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = currentStock,
                    onValueChange = { currentStock = it },
                    label = { Text("Current Stock (Pills)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalTeal)
                )
                OutlinedTextField(
                    value = dosagePerDay,
                    onValueChange = { dosagePerDay = it },
                    label = { Text("Pills per Day") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MedicalTeal)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Dosage Time", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextSecondary)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Morning", "Afternoon", "Night").forEach { time ->
                    FilterChip(
                        selected = dosageTime == time,
                        onClick = { dosageTime = time },
                        label = { Text(time) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MedicalTeal,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true },
                color = MedicalTealLight,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MedicalTeal)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Notification Time: ${String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)}",
                        fontWeight = FontWeight.Bold,
                        color = MedicalTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    
                    // If time is in the past, set it for tomorrow
                    if (calendar.timeInMillis <= System.currentTimeMillis()) {
                        calendar.add(Calendar.DAY_OF_YEAR, 1)
                    }

                    onAdd(
                        ReminderEntity(
                            medicineName = medicineName,
                            reminderTime = calendar.timeInMillis,
                            isActive = true,
                            profileId = profileId,
                            dosageTime = dosageTime,
                            currentStock = currentStock.toIntOrNull() ?: 30,
                            dosagePerDay = dosagePerDay.toIntOrNull() ?: 1
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalNavy),
                enabled = medicineName.isNotBlank()
            ) {
                Text("ACTIVATE ALARM", fontWeight = FontWeight.Black)
            }
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("OK") }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
fun AddProfileDialog(onDismiss: () -> Unit, onAdd: (String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var relation by remember { mutableStateOf("Family") }
    var emoji by remember { mutableStateOf("👤") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Family Member", fontWeight = FontWeight.Black) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("👤", "👨", "👩", "👴", "👵", "👶").forEach { e ->
                        Surface(
                            modifier = Modifier.size(40.dp).clickable { emoji = e },
                            color = if (emoji == e) MedicalTealLight else Color.Transparent,
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) { Text(e) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onAdd(name, relation, emoji) }) {
                Text("ADD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        },
        containerColor = Color.White
    )
}

@Composable
fun UpdateStockDialog(reminder: ReminderEntity, onDismiss: () -> Unit, onUpdate: (Int) -> Unit) {
    var stock by remember { mutableStateOf(reminder.currentStock.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Stock: ${reminder.medicineName}", fontWeight = FontWeight.Black) },
        text = {
            Column {
                Text("How many pills do you have now?", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onUpdate(stock.toIntOrNull() ?: reminder.currentStock) }) {
                Text("UPDATE")
            }
        },
        containerColor = Color.White
    )
}

@Composable
fun EmptyRemindersView() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            color = MedicalTeal.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MedicalTeal.copy(alpha = 0.2f)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Clean Slate",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black,
            color = MedicalNavy
        )
        Text(
            text = "No prescriptions active for this profile.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun ClinicalSummaryCard(count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MedicalNavy),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(MedicalNavy, MedicalNavyLight)
            )
        )) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            tint = MedicalTeal,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(
                        text = if (count > 0) "ADHERENCE: OPTIMAL" else "HEALTH INSIGHTS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MedicalTeal,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (count > 0) "You have $count prescriptions being monitored." else "Set a refill alarm to track your stock.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
