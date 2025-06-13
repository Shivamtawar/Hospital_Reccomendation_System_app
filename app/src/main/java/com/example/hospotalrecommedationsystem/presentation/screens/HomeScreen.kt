package com.example.hospotalrecommedationsystem.presentation.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hospotalrecommedationsystem.presentation.viewmodel.HospitalViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToResults: (Double, Double, String) -> Unit,
    viewModel: HospitalViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var disease by remember { mutableStateOf("") }
    var locationStatus by remember { mutableStateOf(LocationStatus.Requesting) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var hasCheckedPermission by remember { mutableStateOf(false) }

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

    // Function to check if location permissions are granted
    fun hasLocationPermission(): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocationGranted || coarseLocationGranted
    }

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

    // Check permission status on first load
    LaunchedEffect(hasCheckedPermission) {
        if (!hasCheckedPermission) {
            delay(500) // Small delay for better UX

            if (hasLocationPermission()) {
                // Permission already granted, directly request location
                locationStatus = LocationStatus.Loading
                viewModel.requestLocation { latitude, longitude ->
                    locationStatus = if (latitude != null && longitude != null) {
                        LocationStatus.Success
                    } else {
                        LocationStatus.Failed
                    }
                }
            } else {
                // Permission not granted, show dialog
                showPermissionDialog = true
            }
            hasCheckedPermission = true
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
                .padding(24.dp)
                .alpha(animatedVisibility.value),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App Header with Icon
            AnimatedHeader()

            Spacer(modifier = Modifier.height(48.dp))

            // Location Status Card
            LocationStatusCard(
                locationStatus = locationStatus,
                onRequestPermission = {
                    if (!hasLocationPermission()) {
                        showPermissionDialog = true
                    }
                },
                modifier = Modifier.scale(cardScale.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Disease Input Card
            DiseaseInputCard(
                disease = disease,
                onDiseaseChange = { disease = it },
                modifier = Modifier.scale(cardScale.value)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Find Hospitals Button
            AnimatedFindButton(
                enabled = disease.isNotBlank() && locationStatus == LocationStatus.Success,
                onClick = {
                    if (disease.isNotBlank() && viewModel.latitude.value != null && viewModel.longitude.value != null) {
                        val encodedDisease = URLEncoder.encode(disease, "UTF-8")
                        onNavigateToResults(viewModel.latitude.value!!, viewModel.longitude.value!!, encodedDisease)
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Health Tips Footer
            HealthTipsFooter()
        }

        // Permission Dialog - only show if permission is not granted
        if (showPermissionDialog && !hasLocationPermission()) {
            LocationPermissionDialog(
                onGrantPermission = {
                    showPermissionDialog = false
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onDismiss = {
                    showPermissionDialog = false
                    locationStatus = LocationStatus.Denied
                }
            )
        }
    }
}

@Composable
private fun AnimatedHeader() {
    val iconScale = remember { Animatable(0f) }
    val iconRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            iconScale.animateTo(1f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy))
        }
        launch {
            delay(500)
            iconRotation.animateTo(360f, animationSpec = tween(1000, easing = EaseInOutCubic))
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics {
            contentDescription = "Hospital Recommendation App Header"
        }
    ) {
        Icon(
            imageVector = Icons.Default.LocalHospital,
            contentDescription = "Hospital Icon",
            modifier = Modifier
                .size(64.dp)
                .scale(iconScale.value)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                    )
                )
                .padding(12.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "HealthFinder",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            ),
            color = Color(0xFF2E7D32),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Find the right care, right away",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

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
                .padding(20.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiseaseInputCard(
    disease: String,
    onDiseaseChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "What do you need help with?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF333333)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = disease,
                onValueChange = onDiseaseChange,
                label = { Text("Enter symptoms or specialty") },
                placeholder = { Text("e.g., Chest pain, Cardiology, Fever") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Enter disease or symptoms text field"
                    },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    focusedLabelColor = Color(0xFF2196F3)
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = Color(0xFF666666)
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "💡 Be specific for better recommendations",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888)
            )
        }
    }
}

@Composable
private fun AnimatedFindButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val buttonColor = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFFBDBDBD), Color(0xFF9E9E9E))
        )
    }

    LaunchedEffect(enabled) {
        if (enabled) {
            scale.animateTo(1.05f, animationSpec = tween(200))
            scale.animateTo(1f, animationSpec = tween(200))
        }
    }

    Button(
        onClick = {
            onClick()
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale.value)
            .semantics {
                contentDescription = if (enabled) "Find hospitals near you" else "Complete location and symptoms to find hospitals"
            },
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(buttonColor, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Find Hospitals Nearby",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color.White
                )
            }
        }
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

enum class LocationStatus(val title: String, val message: String) {
    Requesting("Getting Ready", "Preparing location services..."),
    Loading("Finding You", "Acquiring your location..."),
    Success("Location Found", "Ready to find nearby hospitals"),
    Failed("Location Error", "Could not determine your location"),
    Denied("Permission Needed", "Location access is required to find hospitals")
}