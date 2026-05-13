package com.example.janna

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.janna.data.AppDatabase
import com.example.janna.data.MedicineRepository
import com.example.janna.ui.*
import com.example.janna.ui.theme.*
import com.example.janna.util.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Login : Screen("login", "Login", Icons.Default.Search)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Locator : Screen("locator", "Stores", Icons.Default.LocationOn)
    object Stock : Screen("stock", "Stock", Icons.AutoMirrored.Filled.Send)
    object Reminders : Screen("reminders", "Refill", Icons.Default.DateRange)
    object Schemes : Screen("schemes", "Schemes", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
    
    private val database by lazy { AppDatabase.getDatabase(this, lifecycleScope) }
    private val repository by lazy { MedicineRepository(database.medicineDao()) }
    private val preferenceManager by lazy { PreferenceManager(this) }
    
    private val medicineViewModel: MedicineViewModel by viewModels { ViewModelFactory(repository, preferenceManager) }
    private val savingsViewModel: SavingsViewModel by viewModels { ViewModelFactory(repository) }
    
    private val alarmScheduler by lazy { com.example.janna.util.AlarmScheduler(this) }
    private val reminderViewModel: ReminderViewModel by viewModels { 
        ViewModelFactory(repository, preferenceManager, database.reminderDao(), alarmScheduler) 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Global Error Handling (Simulated with Timber)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught Exception in thread ${thread.name}")
            // In a real app, you might show a "Something went wrong" screen
        }

        setContent {
            JannaTheme {
                val auth = remember { FirebaseAuth.getInstance() }
                var currentUser by remember { mutableStateOf(auth.currentUser) }
                
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.POST_NOTIFICATIONS,
                            Manifest.permission.CAMERA
                        )
                    )
                }

                var showSplash by remember { mutableStateOf(true) }
                var startScreen by remember { mutableStateOf(if (currentUser != null) Screen.Search.route else Screen.Login.route) }

                if (showSplash) {
                    SplashScreen(onSplashFinished = { showSplash = false })
                } else if (currentUser == null) {
                    LoginScreen(onLoginSuccess = { 
                        startScreen = Screen.Search.route
                        currentUser = auth.currentUser
                    })
                } else {
                    MainScreen(
                        medicineViewModel = medicineViewModel, 
                        savingsViewModel = savingsViewModel, 
                        reminderViewModel = reminderViewModel,
                        startDestination = startScreen,
                        onLogout = {
                            auth.signOut()
                            currentUser = null
                        },
                        onSendFeedback = { sendFeedbackEmail() }
                    )
                }
            }
        }
    }

    private fun sendFeedbackEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@janna-helper.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Jan Aushadhi Helper - User Feedback")
        }
        try {
            startActivity(Intent.createChooser(intent, "Send feedback..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    medicineViewModel: MedicineViewModel, 
    savingsViewModel: SavingsViewModel,
    reminderViewModel: ReminderViewModel,
    startDestination: String,
    onLogout: () -> Unit,
    onSendFeedback: () -> Unit
) {
    val navController = rememberNavController()
    val bottomBarItems = listOf(
        Screen.Search,
        Screen.Locator,
        Screen.Stock,
        Screen.Reminders,
        Screen.Schemes
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showTopAndBottomBar = bottomBarItems.any { it.route == currentDestination?.route }

    Scaffold(
        topBar = {
            if (showTopAndBottomBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Jan Aushadhi Helper",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Professional Medical Assistant",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onSendFeedback) {
                            Icon(Icons.Default.Email, contentDescription = "Feedback")
                        }
                        IconButton(onClick = {
                            onLogout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(navController.graph.id) {
                                    inclusive = true
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MedicalNavy,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        bottomBar = {
            if (showTopAndBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(80.dp)
                ) {
                    bottomBarItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    screen.icon, 
                                    contentDescription = null,
                                    tint = if (selected) MedicalTeal else TextSecondary
                                ) 
                            },
                            label = { 
                                Text(
                                    screen.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MedicalTeal else TextSecondary
                                ) 
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MedicalTealLight
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Login.route) {
                LoginScreen(onLoginSuccess = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                })
            }
            composable(Screen.Search.route) { 
                MedicineSearchScreen(medicineViewModel, savingsViewModel) 
            }
            composable(Screen.Locator.route) { StoreLocatorScreen() }
            composable(Screen.Stock.route) { StockRequestScreen() }
            composable(Screen.Reminders.route) { RemindersScreen(reminderViewModel) }
            composable(Screen.Schemes.route) { GovtSchemesScreen() }
        }
    }
}
