package com.example.janna.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.janna.ui.theme.*

data class Scheme(
    val name: String,
    val description: String,
    val link: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Info
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GovtSchemesScreen() {
    val context = LocalContext.current
    val schemes = listOf(
        Scheme(
            "Ayushman Bharat (PM-JAY)",
            "World's largest health insurance scheme providing ₹5 Lakh cover per family per year for secondary and tertiary care hospitalization.",
            "https://nha.gov.in/PM-JAY"
        ),
        Scheme(
            "PM Bhartiya Janaushadhi Pariyojana",
            "Campaign launched by the Department of Pharmaceuticals to provide quality medicines at affordable prices through dedicated outlets.",
            "http://janaushadhi.gov.in/"
        ),
        Scheme(
            "National Health Mission",
            "Programs for reproductive-maternal-neonatal-child and adolescent health, and communicable and non-communicable diseases.",
            "https://nhm.gov.in/"
        )
    )

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
                                text = "GOVT. SCHEMES",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MedicalTeal,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Official Healthcare Portals",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                        Icon(
                            Icons.Default.Info,
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Surface(
                    color = MedicalTeal.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MedicalTeal.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MedicalTeal)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Access verified healthcare support portals. These links redirect to official government websites.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            items(schemes) { scheme ->
                SchemeCard(scheme) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scheme.link))
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun SchemeCard(scheme: Scheme, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MedicalTeal.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(scheme.icon, contentDescription = null, tint = MedicalTeal, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = scheme.name, 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black, 
                    color = MedicalNavy, 
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = scheme.description, 
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary, 
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedicalNavy)
            ) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("OPEN OFFICIAL PORTAL", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
            }
        }
    }
}
