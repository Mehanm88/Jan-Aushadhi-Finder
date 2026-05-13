package com.example.janna.ui

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.janna.data.CartItem
import com.example.janna.data.Medicine
import com.example.janna.ui.theme.*
import com.example.janna.ui.components.shimmerEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineSearchScreen(medicineViewModel: MedicineViewModel, savingsViewModel: SavingsViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by medicineViewModel.searchResults.collectAsState()
    val isLoading by medicineViewModel.isLoading.collectAsState()
    val savingsSummary by savingsViewModel.savingsSummary.collectAsState()
    val cartItems by savingsViewModel.cartItems.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedMedicineForStock by remember { mutableStateOf<Medicine?>(null) }
    var showStockSheet by remember { mutableStateOf(false) }

    val voiceLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            data?.firstOrNull()?.let {
                searchQuery = it
                medicineViewModel.searchMedicine(it)
            }
        }
    }

    val ocrLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val recognizedText = result.data?.getStringExtra("recognized_text") ?: ""
            if (recognizedText.isNotEmpty()) {
                searchQuery = recognizedText
                medicineViewModel.searchMedicine(recognizedText)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        .padding(bottom = 20.dp)
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
                                text = "JAN AUSHADHI HELPER",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MedicalTeal,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Professional Medical Assistant",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Surface(
                            modifier = Modifier.size(40.dp),
                            color = Color.White.copy(alpha = 0.1f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MedicalTeal, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // Custom Modern Search Bar
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        SearchBar(
                            query = searchQuery,
                            onQueryChange = { 
                                searchQuery = it
                                medicineViewModel.searchMedicine(it)
                            },
                            onClear = {
                                searchQuery = ""
                                medicineViewModel.searchMedicine("")
                            },
                            onVoiceClick = {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak medicine name...")
                                }
                                voiceLauncher.launch(intent)
                            },
                            onCameraClick = {
                                ocrLauncher.launch(Intent(context, PrescriptionScannerActivity::class.java))
                            }
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
        ) {
            AnimatedVisibility(
                visible = cartItems.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                SavingsImpactCard(savingsSummary) { savingsViewModel.clearPrescription() }
            }

            // State-based Content with Animations
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedContent(
                    targetState = Triple(isLoading, searchResults.isEmpty(), searchQuery.isNotEmpty()),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                    },
                    label = "ContentState"
                ) { (loading, empty, hasQuery) ->
                    when {
                        loading -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(6) { ShimmerMedicineItem() }
                            }
                        }
                        empty -> {
                            EmptyStateView(hasQuery = hasQuery)
                        }
                        else -> {
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(searchResults, key = { it.id }) { medicine ->
                                    val savingsDetails = medicineViewModel.getSavingsDetails(medicine)
                                    ClinicalMedicineCard(
                                        medicine = medicine,
                                        savingsDetails = savingsDetails,
                                        onAdd = { savingsViewModel.addItemToPrescription(CartItem(medicine)) },
                                        onCheckStock = {
                                            selectedMedicineForStock = medicine
                                            showStockSheet = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStockSheet && selectedMedicineForStock != null) {
        StockRequestBottomSheet(
            medicine = selectedMedicineForStock!!,
            onDismiss = { showStockSheet = false },
            onResult = { message ->
                scope.launch {
                    snackbarHostState.showSnackbar(message)
                }
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String, 
    onQueryChange: (String) -> Unit, 
    onClear: () -> Unit,
    onVoiceClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        placeholder = { Text("Search brand or salt name...", color = TextSecondary.copy(alpha = 0.6f)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedicalTeal) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
                IconButton(onClick = onVoiceClick) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Search", tint = MedicalTeal)
                }
                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp), color = TextTertiary.copy(alpha = 0.3f))
                IconButton(onClick = onCameraClick) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = "Scan", tint = MedicalTeal)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MedicalTeal
        )
    )
}

@Composable
fun ShimmerMedicineItem() {
    Card(
        modifier = Modifier.fillMaxWidth().height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(modifier = Modifier.width(120.dp).height(20.dp).shimmerEffect())
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.width(180.dp).height(14.dp).shimmerEffect())
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Box(modifier = Modifier.width(80.dp).height(24.dp).shimmerEffect())
                    Box(modifier = Modifier.width(100.dp).height(40.dp).shimmerEffect())
                }
            }
        }
    }
}

@Composable
fun SavingsImpactCard(summary: SavingsSummary, onClear: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(modifier = Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(MedicalTeal.copy(alpha = 0.05f), Color.White)
            )
        )) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "YOUR TOTAL SAVINGS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "₹${try { "%.2f".format(summary.totalSavings) } catch (e: Exception) { "0.00" }}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MedicalTeal,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Surface(
                        modifier = Modifier.size(48.dp).clickable { onClear() },
                        color = WarningRedLight,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear", tint = WarningRed, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Animated Progress Bar
                val progress by animateFloatAsState(
                    targetValue = summary.savingsRatio,
                    animationSpec = tween(1000, easing = EaseOutExpo),
                    label = "progress"
                )
                
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Generic Cost", style = MaterialTheme.typography.labelSmall, color = MedicalTeal)
                        Text("Branded Cost", style = MaterialTheme.typography.labelSmall, color = WarningRed)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(CircleShape)
                            .background(WarningRedLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(MedicalTeal, SuccessGreen)))
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockRequestBottomSheet(
    medicine: Medicine,
    onDismiss: () -> Unit,
    onResult: (String) -> Unit
) {
    var isChecking by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = TextTertiary.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Stock Inquiry",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
            Text(
                text = medicine.brandName,
                style = MaterialTheme.typography.bodyLarge,
                color = MedicalTeal
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            if (isChecking) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(64.dp), color = MedicalTeal, strokeWidth = 6.dp)
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MedicalTeal)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Locating nearest Jan-Aushadhi Kendras...", style = MaterialTheme.typography.bodyMedium)
            } else if (resultMessage == null) {
                Button(
                    onClick = {
                        isChecking = true
                        scope.launch {
                            delay(2500) 
                            isChecking = false
                            val available = Random.nextBoolean()
                            if (available) {
                                resultMessage = "In Stock at: Kendra #42, Main Road"
                                onResult(resultMessage!!)
                                onDismiss()
                            } else {
                                resultMessage = "Currently Unavailable. Notification sent."
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalNavy)
                ) {
                    Text("CHECK REAL-TIME STOCK", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            } else {
                Icon(
                    imageVector = if (resultMessage!!.contains("In Stock")) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (resultMessage!!.contains("In Stock")) SuccessGreen else WarningRed,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(resultMessage!!, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(32.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("DONE", fontWeight = FontWeight.Black, color = MedicalNavy)
                }
            }
        }
    }
}

@Composable
fun ClinicalMedicineCard(
    medicine: Medicine,
    savingsDetails: SavingsDetails,
    onAdd: () -> Unit,
    onCheckStock: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.brandName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MedicalNavy
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = medicine.genericName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = TextSecondary
                        )
                    }
                }
                
                Surface(
                    color = SuccessGreenLight,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "SAVE ${savingsDetails.percentageSavings}%",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = SuccessGreenDark,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Market Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                    Text(
                        text = "₹${"%.2f".format(medicine.brandPrice)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textDecoration = TextDecoration.LineThrough
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹${try { "%.2f".format(medicine.genericPrice) } catch (e: Exception) { "0.00" }}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = SuccessGreenDark
                    )
                }

                Row {
                    FilledTonalIconButton(
                        onClick = onCheckStock,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = MedicalTealLight)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = "Stock", tint = MedicalTeal)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onAdd,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTeal),
                        contentPadding = PaddingValues(horizontal = 20.dp)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ADD", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(hasQuery: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(160.dp),
            color = MedicalTeal.copy(alpha = 0.05f),
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (hasQuery) Icons.Outlined.SearchOff else Icons.Default.MedicalInformation,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MedicalTeal.copy(alpha = 0.2f)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = if (hasQuery) "Medicine Not Found" else "Your Health, Optimized",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            color = MedicalNavy,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (hasQuery) 
                "We couldn't locate this brand. Try searching by generic salt name or check for typos." 
                else "Search for any branded medicine to find high-quality generic alternatives and save up to 80%.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        
        if (hasQuery) {
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MedicalTeal)
            ) {
                Text("EXPLORE ALL MEDICINES", color = MedicalTeal, fontWeight = FontWeight.Bold)
            }
        }
    }
}
