package com.example.hospotalrecommedationsystem.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hospotalrecommedationsystem.presentation.viewmodel.HospitalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCareScreen(
    onNavigateToResults: (Double, Double, String) -> Unit,
    viewModel: HospitalViewModel = hiltViewModel()
) {
    var locationStatus by remember { mutableStateOf(LocationStatus.Requesting) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasRequestedPermission by remember { mutableStateOf(false) }
    var selectedDisease by remember { mutableStateOf<String?>(null) }

    // Animation states
    val animatedVisibility = remember { Animatable(0f) }
    val cardScale = remember { Animatable(0.8f) }

    // Health-focused color scheme
    val healthyGreen = Color(0xFF4CAF50)
    val medicalBlue = Color(0xFF2196F3)
    val softTeal = Color(0xFF009688)
    val lightBackground = Color(0xFFF8FFFE)

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            lightBackground,
            Color(0xFFE8F5E8),
            Color(0xFFE3F2FD)
        )
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted || coarseLocationGranted) {
            locationStatus = LocationStatus.Loading
            viewModel.requestLocation { latitude, longitude ->
                locationStatus = if (latitude != null && longitude != null) {
                    LocationStatus.Success
                } else {
                    LocationStatus.Failed
                }
            }
        } else {
            locationStatus = LocationStatus.Denied
        }
    }

    LaunchedEffect(Unit) {
        animatedVisibility.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCubic))
        delay(200)
        cardScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    // Auto-request permission on first load
    LaunchedEffect(hasRequestedPermission) {
        if (!hasRequestedPermission) {
            delay(500)
            showPermissionDialog = true
        }
    }

    // Auto-navigate when disease is selected and location is available
    LaunchedEffect(selectedDisease, locationStatus) {
        if (selectedDisease != null && locationStatus == LocationStatus.Success) {
            val latitude = viewModel.latitude.value
            val longitude = viewModel.longitude.value
            if (latitude != null && longitude != null) {
                val encodedDisease = URLEncoder.encode(selectedDisease!!, "UTF-8")
                onNavigateToResults(latitude, longitude, encodedDisease)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp,24.dp,24.dp,120.dp)
                .alpha(animatedVisibility.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // App Header
            QuickCareHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // Location Status Card
            LocationStatusCard(
                locationStatus = locationStatus,
                onRequestPermission = {
                        showPermissionDialog = true

                },
                modifier = Modifier.scale(cardScale.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Disease Grid
            DiseaseSelectionGrid(
                enabled = locationStatus == LocationStatus.Success,
                onDiseaseSelected = { disease ->
                    selectedDisease = disease
                },
                modifier = Modifier.scale(cardScale.value)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Health Tips Footer
            HealthTipsFooter()
        }

        // Permission Dialog
        if (showPermissionDialog) {
            LocationPermissionDialog(
                onGrantPermission = {
                    hasRequestedPermission = true
                    showPermissionDialog = false
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onDismiss = {
                    hasRequestedPermission = true
                    showPermissionDialog = false
                    locationStatus = LocationStatus.Denied
                }
            )
        }
    }
}

@Composable
private fun QuickCareHeader() {
    val iconScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics {
            contentDescription = "Quick Care Selection Header"
        }
    ) {
        Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = "Quick Care Icon",
            modifier = Modifier
                .size(56.dp)
                .scale(iconScale.value)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF2196F3), Color(0xFF1976D2))
                    )
                )
                .padding(12.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Quick Care",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = Color(0xFF1976D2),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Select your condition for instant care",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DiseaseSelectionGrid(
    enabled: Boolean,
    onDiseaseSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val diseases = listOf(
        DiseaseCategory("Emergency", "Chest Pain", Icons.Default.Emergency, Color(0xFFD32F2F)),
        DiseaseCategory("Heart", "Cardiology", Icons.Default.Favorite, Color(0xFFE91E63)),
        DiseaseCategory("Brain", "Neurology", Icons.Default.Psychology, Color(0xFF9C27B0)),
        DiseaseCategory("Bones", "Orthopedics", Icons.Default.Accessible, Color(0xFF3F51B5)),
        DiseaseCategory("Skin", "Dermatology", Icons.Default.Face, Color(0xFF2196F3)),
        DiseaseCategory("Eyes", "Ophthalmology", Icons.Default.RemoveRedEye, Color(0xFF00BCD4)),
        DiseaseCategory("Child", "Pediatrics", Icons.Default.ChildCare, Color(0xFF4CAF50)),
        DiseaseCategory("Women", "Gynecology", Icons.Default.PregnantWoman, Color(0xFF8BC34A)),
        DiseaseCategory("General", "General Medicine", Icons.Default.MedicalServices, Color(0xFFFF9800)),
        DiseaseCategory("Mental", "Psychiatry", Icons.Default.Mood, Color(0xFFFF5722)),
        DiseaseCategory("Dental", "Dentistry", Icons.Default.Coronavirus, Color(0xFF795548)),
        DiseaseCategory("Surgery", "General Surgery", Icons.Default.LocalHospital, Color(0xFF607D8B))
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Choose Your Care Type",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF333333)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!enabled) {
                Text(
                    text = "📍 Enable location to select care options",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(diseases) { disease ->
                    DiseaseCard(
                        disease = disease,
                        enabled = enabled,
                        onClick = { onDiseaseSelected(disease.specialty) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DiseaseCard(
    disease: DiseaseCategory,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val alpha = if (enabled) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .alpha(alpha)
            .clickable(enabled = enabled) {
                onClick()
            }
            .semantics {
                contentDescription = "${disease.name} - ${disease.specialty}"
                role = Role.Button
            },
        shape = RoundedCornerShape(16.dp),

        elevation = CardDefaults.cardElevation(if (enabled) 4.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(disease.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = disease.icon,
                    contentDescription = null,
                    tint = disease.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = disease.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )

            Text(
                text = disease.specialty,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center
            )
        }
    }

    LaunchedEffect(enabled) {
        if (enabled) {
            scale.animateTo(1.05f, animationSpec = tween(100))
            scale.animateTo(1f, animationSpec = tween(100))
        }
    }
}

// Reuse the same components from HomeScreen
@Composable
private fun LocationStatusCard(
    locationStatus: LocationStatus,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBackgroundColor = when (locationStatus) {
        LocationStatus.Success -> Color(0xFFE8F5E8)
        LocationStatus.Failed, LocationStatus.Denied -> Color(0xFFFFEBEE)
        else -> Color(0xFFF3E5F5)
    }

    val iconColor = when (locationStatus) {
        LocationStatus.Success -> Color(0xFF4CAF50)
        LocationStatus.Failed, LocationStatus.Denied -> Color(0xFFD32F2F)
        else -> Color(0xFF9C27B0)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Location Status: ${locationStatus.message}"
                role = Role.Button
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedLocationIcon(locationStatus, iconColor)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = locationStatus.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF333333)
                )
                Text(
                    text = locationStatus.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }

            if (locationStatus == LocationStatus.Denied) {
                FilledTonalButton(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("Enable", color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun AnimatedLocationIcon(status: LocationStatus, color: Color) {
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(status) {
        if (status == LocationStatus.Loading || status == LocationStatus.Requesting) {
            rotation.animateTo(
                360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing)
                )
            )
        }
    }

    val icon = when (status) {
        LocationStatus.Success -> Icons.Default.LocationOn
        LocationStatus.Failed, LocationStatus.Denied -> Icons.Default.LocationOff
        else -> Icons.Default.MyLocation
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun HealthTipsFooter() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0F7FF)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = Color(0xFF2196F3),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "In emergency situations, call 108 immediately",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1976D2)
            )
        }
    }
}

@Composable
private fun LocationPermissionDialog(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Location Access Required")
            }
        },
        text = {
            Text(
                "To find hospitals near you, we need access to your location. Your privacy is important to us - location data is only used to show nearby medical facilities.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onGrantPermission,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Grant Permission", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}



data class DiseaseCategory(
    val name: String,
    val specialty: String,
    val icon: ImageVector,
    val color: Color
)

