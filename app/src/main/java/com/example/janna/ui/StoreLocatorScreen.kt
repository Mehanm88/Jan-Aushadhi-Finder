package com.example.janna.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.janna.data.MedicineRepository
import com.example.janna.data.Store
import com.example.janna.ui.theme.*
import com.example.janna.ui.components.CrowdsourceBottomSheet
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@Composable
fun StoreLocatorScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val isDarkTheme = isSystemInDarkTheme()
    
    val defaultLocation = LatLng(12.9716, 77.5946) // Bengaluru
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    var stores by remember { mutableStateOf(MedicineRepository.stores) }
    var isMyLocationEnabled by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    var showRationale by remember { mutableStateOf(false) }
    var customMarkerIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    
    val isOnline = remember { isDeviceOnline(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedStoreForCrowdsource by remember { mutableStateOf<Store?>(null) }
    var showCrowdsourceSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isOnline) {
            snackbarHostState.showSnackbar(
                message = "No internet connection. Map may not load.",
                actionLabel = "RETRY",
                duration = SnackbarDuration.Indefinite
            )
        }
        try {
            customMarkerIcon = createCustomMarker(context)
        } catch (e: Exception) { e.printStackTrace() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isMyLocationEnabled = isGranted
        if (!isGranted) {
            val activity = context as? android.app.Activity
            if (activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showRationale = true
            }
        }
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text("Location Access Required", fontWeight = FontWeight.Black) },
            text = { Text("To find the nearest Jan Aushadhi Kendras, we need your location. Please enable it in app settings.") },
            confirmButton = {
                Button(
                    onClick = { 
                        showRationale = false
                        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MedicalNavy)
                ) {
                    Text("SETTINGS", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationale = false }) { Text("CANCEL", color = MedicalNavy) }
            }
        )
    }

    fun moveToUserLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    scope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f))
                        stores = MedicineRepository.generateMockStores(userLatLng)
                    }
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(Unit) {
        moveToUserLocation()
    }

    val mapProperties = remember(isMyLocationEnabled, isDarkTheme) {
        MapProperties(
            isMyLocationEnabled = isMyLocationEnabled,
            mapStyleOptions = if (isDarkTheme) MapStyleOptions(DARK_MAP_STYLE) else null
        )
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
                                text = "KENDRA LOCATOR",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MedicalTeal,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Find Nearest Jan Aushadhi Stores",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MedicalTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(myLocationButtonEnabled = false)
            ) {
                stores.forEach { store ->
                    MarkerInfoWindowContent(
                        state = MarkerState(position = LatLng(store.latitude, store.longitude)),
                        icon = customMarkerIcon ?: BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                        title = store.name
                    ) {
                        Card(
                            modifier = Modifier.padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) Color(0xFF1E1E1E) else Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = store.name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Black, color = if (isDarkTheme) Color.White else MedicalNavy)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (store.isOpen) "Open Now" else "Closed",
                                    color = if (store.isOpen) SuccessGreen else WarningRed,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(stores) { store -> 
                        ClinicalStoreCard(
                            store = store,
                            onCrowdsource = {
                                selectedStoreForCrowdsource = store
                                showCrowdsourceSheet = true
                            }
                        ) 
                    }
                }
            }
            
            FloatingActionButton(
                onClick = { moveToUserLocation() },
                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
                containerColor = Color.White,
                contentColor = MedicalTeal,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "My Location")
            }
        }
    }

    if (showCrowdsourceSheet && selectedStoreForCrowdsource != null) {
        CrowdsourceBottomSheet(
            store = selectedStoreForCrowdsource!!,
            onDismiss = { showCrowdsourceSheet = false },
            onReport = { isAvailable, rating ->
                showCrowdsourceSheet = false
                scope.launch {
                    snackbarHostState.showSnackbar("Thanks for your report! Your update helps the community.")
                }
            }
        )
    }
}

@Composable
fun ClinicalStoreCard(store: Store, onCrowdsource: () -> Unit) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.width(320.dp).height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = store.name, 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black, 
                        color = if (isDark) Color.White else MedicalNavy, 
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                        Text(text = " ${store.rating} (${store.reviewCount})", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "• ${store.distance} AWAY", style = MaterialTheme.typography.labelSmall, color = MedicalTeal, fontWeight = FontWeight.Black)
                    }
                }
                ClinicalStatusBadge(isOpen = store.isOpen)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    color = if (store.isStockAvailable) SuccessGreen else WarningRed,
                    shape = CircleShape
                ) {}
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (store.isStockAvailable) "STOCK RECENTLY VERIFIED" else "OUT OF STOCK REPORTED",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (store.isStockAvailable) SuccessGreen else WarningRed,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onCrowdsource,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("UPDATE STATUS", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Black, color = MedicalTeal)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier.size(36.dp).clickable { 
                            try {
                                val url = "https://api.whatsapp.com/send?phone=${store.phone}&text=Hi, is medicine available?"
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse(url)
                                context.startActivity(intent)
                            } catch (e: Exception) { Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show() }
                        },
                        color = Color(0xFF25D366).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color(0xFF25D366), modifier = Modifier.size(20.dp))
                        }
                    }
                    Surface(
                        modifier = Modifier.size(36.dp).clickable { 
                            val gmmIntentUri = Uri.parse("google.navigation:q=${store.latitude},${store.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        },
                        color = MedicalTeal.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Directions, contentDescription = "Directions", tint = MedicalTeal, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClinicalStatusBadge(isOpen: Boolean) {
    val color = if (isOpen) SuccessGreen else WarningRed
    val text = if (isOpen) "OPEN" else "CLOSED"
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(
            text = text, 
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), 
            style = MaterialTheme.typography.labelSmall, 
            fontWeight = FontWeight.Black, 
            color = color
        )
    }
}

private fun isDeviceOnline(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
    return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}

private fun createCustomMarker(context: Context): BitmapDescriptor {
    val size = 100
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#008080") // MedicalTeal
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
    paint.color = android.graphics.Color.WHITE
    val strokeWidth = 10f
    val padding = 25f
    canvas.drawRect(padding, size / 2f - strokeWidth / 2, size - padding, size / 2f + strokeWidth / 2, paint)
    canvas.drawRect(size / 2f - strokeWidth / 2, padding, size / 2f + strokeWidth / 2, size - padding, paint)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

const val DARK_MAP_STYLE = """
[
  { "elementType": "geometry", "stylers": [ { "color": "#242f3e" } ] },
  { "elementType": "labels.text.fill", "stylers": [ { "color": "#746855" } ] },
  { "elementType": "labels.text.stroke", "stylers": [ { "color": "#242f3e" } ] },
  { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [ { "color": "#d59563" } ] },
  { "featureType": "poi", "elementType": "labels.text.fill", "stylers": [ { "color": "#d59563" } ] },
  { "featureType": "road", "elementType": "geometry", "stylers": [ { "color": "#38414e" } ] },
  { "featureType": "road", "elementType": "geometry.stroke", "stylers": [ { "color": "#212a37" } ] },
  { "featureType": "road", "elementType": "labels.text.fill", "stylers": [ { "color": "#9ca5b3" } ] },
  { "featureType": "water", "elementType": "geometry", "stylers": [ { "color": "#17263c" } ] }
]
"""
