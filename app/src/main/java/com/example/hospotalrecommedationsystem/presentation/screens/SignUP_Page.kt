package com.example.hospitalrecommendationsystem.presentation.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hospotalrecommedationsystem.presentation.viewmodel.SignInStates
import com.example.hospotalrecommedationsystem.presentation.viewmodel.SignInViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController
) {
    val viewmodel: SignInViewModel = hiltViewModel()
    val uistate = viewmodel.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }
    var isPhoneValid by remember { mutableStateOf(true) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    // Medical color scheme
    val medicalBlue = Color(0xFF2196F3)
    val medicalGreen = Color(0xFF4CAF50)
    val medicalTeal = Color(0xFF009688)
    val lightBlue = Color(0xFFE3F2FD)
    val lightGreen = Color(0xFFE8F5E8)

    // Animation states
    var headerVisible by remember { mutableStateOf(false) }
    var profileVisible by remember { mutableStateOf(false) }
    var formVisible by remember { mutableStateOf(false) }
    var buttonVisible by remember { mutableStateOf(false) }

    // Button press animation
    var buttonPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (buttonPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    // Animate items sequentially
    LaunchedEffect(Unit) {
        headerVisible = true
        delay(200)
        profileVisible = true
        delay(300)
        formVisible = true
        delay(200)
        buttonVisible = true
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                bitmap = BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    LaunchedEffect(key1 = uistate) {
        when (uistate.value) {
            is SignInStates.Success -> {
                navController.navigate("bottom")
            }
            is SignInStates.Error -> {
                Toast.makeText(context, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Medical themed gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lightBlue,
                            Color.White,
                            lightGreen.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        // Medical cross pattern overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            medicalBlue.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        radius = 800f
                    )
                )
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 40.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Medical header with animated intro
            AnimatedVisibility(
                visible = headerVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Medical logo/icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(medicalBlue, medicalTeal)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = "Hospital",
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Join HealthCare+",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = medicalBlue,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Find the best hospitals and healthcare services near you",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // Profile Photo with medical theme
            AnimatedVisibility(
                visible = profileVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 60 })
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 20.dp)
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    lightBlue.copy(alpha = 0.8f),
                                    medicalBlue.copy(alpha = 0.2f)
                                )
                            )
                        )
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    medicalBlue.copy(alpha = 0.8f),
                                    medicalGreen.copy(alpha = 0.6f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Profile Photo",
                                modifier = Modifier.size(36.dp),
                                tint = medicalBlue
                            )
                            Text(
                                text = "Add Photo",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = medicalBlue,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Medical form with enhanced styling
            AnimatedVisibility(
                visible = formVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 80 })
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Medical form header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(medicalBlue.copy(alpha = 0.2f), medicalGreen.copy(alpha = 0.1f))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = medicalBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Patient Information",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = medicalBlue
                            )
                        }

                        // Medical form fields
                        MedicalInputField(
                            value = username,
                            onValueChange = { username = it },
                            label = "Full Name",
                            icon = Icons.Default.Person,
                            keyboardType = KeyboardType.Text,
                            isError = false,
                            medicalBlue = medicalBlue
                        )

                        MedicalInputField(
                            value = email,
                            onValueChange = {
                                email = it
                                isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isEmpty()
                            },
                            label = "Email Address",
                            icon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email,
                            isError = !isEmailValid,
                            medicalBlue = medicalBlue
                        )

                        AnimatedVisibility(
                            visible = !isEmailValid && email.isNotEmpty(),
                            enter = fadeIn()
                        ) {
                            MedicalErrorText("Please enter a valid email address")
                        }

                        MedicalInputField(
                            value = phoneNumber,
                            onValueChange = {
                                if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNumber = it
                                isPhoneValid = phoneNumber.length == 10 || phoneNumber.isEmpty()
                            },
                            label = "Mobile Number",
                            icon = Icons.Default.Phone,
                            keyboardType = KeyboardType.Phone,
                            isError = !isPhoneValid,
                            medicalBlue = medicalBlue
                        )

                        AnimatedVisibility(
                            visible = !isPhoneValid && phoneNumber.isNotEmpty(),
                            enter = fadeIn()
                        ) {
                            MedicalErrorText("Please enter a valid 10-digit mobile number")
                        }

                        // Medical themed password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                isPasswordValid = it.length >= 8 || it.isEmpty()
                            },
                            label = { Text("Create Password", color = medicalBlue.copy(alpha = 0.7f)) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(medicalBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = "Password",
                                        tint = medicalBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = if (showPassword) "Hide Password" else "Show Password",
                                        tint = medicalBlue.copy(alpha = 0.7f)
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = !isPasswordValid,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = medicalBlue,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                errorBorderColor = Color.Red,
                                focusedContainerColor = lightBlue.copy(alpha = 0.1f),
                                unfocusedContainerColor = Color.Gray.copy(alpha = 0.05f)
                            )
                        )

                        AnimatedVisibility(
                            visible = !isPasswordValid && password.isNotEmpty(),
                            enter = fadeIn()
                        ) {
                            MedicalErrorText("Password must be at least 8 characters long")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Medical themed sign up button
            AnimatedVisibility(
                visible = buttonVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                if (uistate.value == SignInStates.Loading) {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp,
                                    color = medicalBlue
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Creating your account...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = medicalBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .scale(buttonScale)
                            .clickable {
                                buttonPressed = true
                                if (bitmap == null) {
                                    Toast.makeText(context, "Please add your profile picture", Toast.LENGTH_SHORT).show()
                                } else if (username.isNotEmpty() && email.isNotEmpty() &&
                                    password.isNotEmpty() && phoneNumber.isNotEmpty() &&
                                    isEmailValid && isPasswordValid && isPhoneValid
                                ) {
                                    bitmap?.let {
                                        viewmodel.SignIN(
                                            navController,
                                            email,
                                            password,
                                            username,
                                            phoneNumber,
                                            it,
                                        )
                                    }
                                } else {
                                    Toast.makeText(context, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                                }
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            medicalBlue,
                                            medicalTeal,
                                            medicalGreen
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AppRegistration,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Join HealthCare+",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Reset button press animation
                    LaunchedEffect(buttonPressed) {
                        if (buttonPressed) {
                            delay(150)
                            buttonPressed = false
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Medical themed login redirect
            AnimatedVisibility(
                visible = buttonVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
            ) {
                Card(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            tint = medicalBlue.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Already have an account? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        TextButton(
                            onClick = { navController.navigate("login") },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Sign In",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = medicalBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    isError: Boolean,
    medicalBlue: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = medicalBlue.copy(alpha = 0.7f)) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(medicalBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = medicalBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        },
        isError = isError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = medicalBlue,
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
            errorBorderColor = Color.Red,
            focusedContainerColor = Color(0xFFE3F2FD).copy(alpha = 0.1f),
            unfocusedContainerColor = Color.Gray.copy(alpha = 0.05f)
        )
    )
}

@Composable
private fun MedicalErrorText(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = Color.Red,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.Red,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

fun convertUriToBitmap(context: Context, uri: Uri): Bitmap? {
    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BitmapFactory.decodeStream(inputStream)
    }
}