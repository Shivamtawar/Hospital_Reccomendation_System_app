package com.example.hospotalrecommedationsystem.presentation.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

// Move the updateUserField function outside of the ProfileScreen composable
fun updateUserField(context: android.content.Context, fieldName: String, value: String) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    if (userId != null) {
        FirebaseDatabase.getInstance().getReference("Users")
            .child(userId)
            .child(fieldName)
            .setValue(value)
            .addOnSuccessListener {
                Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }
}

// Define colors to match with SearchScreen
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF8F9FA),
        Color(0xFFF0F2F5)
    )
)

val CardGradientStart = Color(0xFFFFFFFF)
val CardGradientEnd = Color(0xFFF5F7FA)
val BlueTurquoiseGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4F6CF7),
        Color(0xFF4FCDF7)
    )
)
val SkyAquaGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4FADF7),
        Color(0xFF4FE1F7)
    )
)
val SoftPeachGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFFF7A5A),
        Color(0xFFFF9D7A)
    )
)
val AlmostBlack = Color(0xFF202124)
val DarkGray = Color(0xFF5F6368)
val MediumGray = Color(0xFFDADCE0)
val SoftBlue = Color(0xFF4285F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var imageString by remember { mutableStateOf("") }
    val context = LocalContext.current
    val density = LocalDensity.current
    val snackbarHostState = remember { SnackbarHostState() }

    // For edit dialogs
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showEmailDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }

    // New field values
    var newUsername by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var newPhone by remember { mutableStateOf("") }

    // For image picking
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            val encodedImage = encodeBitmapToBase64(bitmap)
            updateUserField(context, "imageUrl", encodedImage)
        }
    }

    // Load user data
    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseDatabase.getInstance().getReference("Users")
                .child(userId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        username = snapshot.child("username").getValue(String::class.java) ?: ""
                        email = snapshot.child("email").getValue(String::class.java) ?: ""
                        phone = snapshot.child("phoneNumber").getValue(String::class.java) ?: ""
                        imageString = snapshot.child("imageUrl").getValue(String::class.java) ?: ""

                        // Initialize edit fields with current values
                        newUsername = username
                        newEmail = email
                        newPhone = phone

                        isLoading = false
                    }

                    override fun onCancelled(error: DatabaseError) {
                        isLoading = false
                        Toast.makeText(context, "Error loading profile", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            // Handle case where user is not logged in
            error = "User not logged in"
            isLoading = false
        }
    }

    // Logout function
    fun logout() {
        FirebaseAuth.getInstance().signOut()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        navController.navigate("login") {
            popUpTo("login") { inclusive = true }
        }
    }

    // Username edit dialog
    if (showUsernameDialog) {
        AlertDialog(
            onDismissRequest = { showUsernameDialog = false },
            title = { Text("Edit Username") },
            text = {
                OutlinedTextField(
                    value = newUsername,
                    onValueChange = { newUsername = it },
                    label = { Text("Username") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftBlue,
                        unfocusedBorderColor = MediumGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateUserField(context, "username", newUsername)
                        showUsernameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftBlue
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUsernameDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Email edit dialog
    if (showEmailDialog) {
        AlertDialog(
            onDismissRequest = { showEmailDialog = false },
            title = { Text("Edit Email") },
            text = {
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftBlue,
                        unfocusedBorderColor = MediumGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateUserField(context, "email", newEmail)
                        showEmailDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftBlue
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEmailDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Phone edit dialog
    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = { Text("Edit Phone") },
            text = {
                OutlinedTextField(
                    value = newPhone,
                    onValueChange = { newPhone = it },
                    label = { Text("Phone") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftBlue,
                        unfocusedBorderColor = MediumGray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        updateUserField(context, "phoneNumber", newPhone)
                        showPhoneDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SoftBlue
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPhoneDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "My Profile",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        // Loading state with more visible indicator
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(60.dp),
                                    color = SoftBlue,
                                    strokeWidth = 5.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    "Loading profile...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = DarkGray
                                )
                            }
                        }
                    }
                    error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Error,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Error: $error",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { navController.navigate("login") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SoftBlue
                                    )
                                ) {
                                    Text("Go to Login")
                                }
                            }
                        }
                    }
                    else -> {
                        // Content is loaded - show the profile
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Profile Image Card
                            Card(
                                modifier = Modifier
                                    .size(200.dp)
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        spotColor = SoftBlue.copy(alpha = 0.1f),
                                        ambientColor = SoftBlue.copy(alpha = 0.1f)
                                    ),
                                shape = CircleShape,
                                colors = CardDefaults.cardColors(
                                    containerColor = CardGradientEnd
                                )
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    if (imageString.isNotEmpty()) {
                                        val bitmap = decodeBase64ToImage(imageString)
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        // Default profile icon with gradient background
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    brush = SkyAquaGradient,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Person,
                                                contentDescription = "Profile",
                                                tint = Color.White,
                                                modifier = Modifier.size(100.dp)
                                            )
                                        }
                                    }

                                    // Image edit button
                                    FloatingActionButton(
                                        onClick = { imagePicker.launch("image/*") },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp),
                                        containerColor = SoftBlue,
                                        shape = CircleShape
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Profile Picture",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Display name with larger font
                            Text(
                                text = username,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = AlmostBlack,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = email,
                                style = MaterialTheme.typography.bodyLarge,
                                color = DarkGray,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // User Information Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        spotColor = SoftBlue.copy(alpha = 0.1f),
                                        ambientColor = SoftBlue.copy(alpha = 0.1f)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = CardGradientStart
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "Personal Information",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = AlmostBlack
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Username Row
                                    ProfileInfoRow(
                                        icon = Icons.Outlined.Person,
                                        label = "Username",
                                        value = username,
                                        onEditClick = {
                                            newUsername = username
                                            showUsernameDialog = true
                                        }
                                    )

                                    Divider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = MediumGray.copy(alpha = 0.3f)
                                    )

                                    // Email Row
                                    ProfileInfoRow(
                                        icon = Icons.Outlined.Email,
                                        label = "Email",
                                        value = email,
                                        onEditClick = {
                                            newEmail = email
                                            showEmailDialog = true
                                        }
                                    )

                                    Divider(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        color = MediumGray.copy(alpha = 0.3f)
                                    )

                                    // Phone Row
                                    ProfileInfoRow(
                                        icon = Icons.Outlined.Phone,
                                        label = "Phone",
                                        value = phone,
                                        onEditClick = {
                                            newPhone = phone
                                            showPhoneDialog = true
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Security Card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = RoundedCornerShape(16.dp),
                                        spotColor = SoftBlue.copy(alpha = 0.1f),
                                        ambientColor = SoftBlue.copy(alpha = 0.1f)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = CardGradientEnd
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Logout Button with gradient background
                                    Button(
                                        onClick = { logout() },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(vertical = 16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(
                                            defaultElevation = 0.dp
                                        )
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    brush = SoftPeachGradient,
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ExitToApp,
                                                    contentDescription = "Logout",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    "Logout",
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = BlueTurquoiseGradient,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = AlmostBlack
                )
            }
        }

        // Edit button
        IconButton(
            onClick = onEditClick,
            modifier = Modifier
                .size(40.dp)
                .shadow(4.dp, CircleShape)
                .background(Color.White, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit $label",
                tint = SoftBlue
            )
        }
    }
}

fun decodeBase64ToImage(base64Str: String): Bitmap {
    val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}

fun encodeBitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}