package com.example.foodtruck.ui

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.foodtruck.ui.theme.IndigoPrimary
import com.example.foodtruck.ui.theme.IndigoLight
import com.example.foodtruck.ui.theme.IndigoDark
import com.example.foodtruck.ui.theme.IndigoSurface

@Composable
fun FoodTruckSignupScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val firebaseUser = auth.currentUser
    val isGoogleUser = firebaseUser != null

    var organization by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(firebaseUser) {
        if (firebaseUser != null) {
            name = firebaseUser.displayName ?: ""
            email = firebaseUser.email ?: ""
        }
    }
    var industry by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Select Location") }
    var latLng by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Register Your Food Truck",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                },
                backgroundColor = Color(0xFF5960AB),
                elevation = 4.dp,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        backgroundColor = Color(0xFF969BD0)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(IndigoSurface)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Food Truck Details",
                        fontSize = 22.sp,
                        color = IndigoDark,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    CustomTextField("Organization", organization) { organization = it }
                    Spacer(modifier = Modifier.height(12.dp))

                    CustomTextField("Owner Name", name, enabled = !isGoogleUser) { name = it }
                    Spacer(modifier = Modifier.height(12.dp))

                    CustomTextField(
                        "Work Email",
                        email,
                        keyboardType = KeyboardType.Email,
                        enabled = !isGoogleUser
                    ) { email = it }
                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isGoogleUser) {
                        CustomTextField(
                            "Password",
                            password,
                            keyboardType = KeyboardType.Password,
                            isPassword = true
                        ) { password = it }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    CustomTextField("Industry", industry) { industry = it }
                    Spacer(modifier = Modifier.height(12.dp))

                    CustomTextField("City", city) { city = it }
                    Spacer(modifier = Modifier.height(20.dp))

                    // Location picker button
                    Button(
                        onClick = { showMapPicker = true },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (latLng != null) IndigoPrimary else IndigoLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (latLng != null) "✓ Location Selected" else "Select Location on Map",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Show selected coordinates below button
                    if (latLng != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Lat: ${"%.4f".format(latLng!!.first)}, Lng: ${"%.4f".format(latLng!!.second)}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = IndigoPrimary)                        } else {
                            Button(
                                onClick = {
                                    if (organization.isEmpty() || name.isEmpty() || email.isEmpty() ||
                                        (!isGoogleUser && password.isEmpty()) || industry.isEmpty() || city.isEmpty() ||
                                        latLng == null) {
                                        Toast.makeText(context, "Please fill all fields and select location!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    CoroutineScope(Dispatchers.IO).launch {
                                        try {
                                            val ownerId = if (isGoogleUser) {
                                                firebaseUser!!.uid
                                            } else {
                                                val result = auth.createUserWithEmailAndPassword(email, password).await()
                                                result.user?.uid ?: ""
                                            }

                                            registerFoodTruck(
                                                db, ownerId, organization, name, email, industry, city,
                                                location, latLng!!.first, latLng!!.second
                                            ) {
                                                isLoading = false
                                                showDialog = true
                                            }
                                        } catch (e: Exception) {
                                            Log.e("Firestore", "Error saving food truck data", e)
                                            isLoading = false
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = IndigoPrimary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .shadow(
                                        elevation = 4.dp,
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Submit",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Map Picker Dialog
    if (showMapPicker) {
        MapPickerDialog(
            onLocationSelected = { selectedLatLng, address ->
                latLng = selectedLatLng.latitude to selectedLatLng.longitude
                location = address
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Registration Successful") },
            text = { Text("Your food truck has been registered successfully!") },
            backgroundColor = Color.White,
            contentColor = Color(0xFF212121),
            buttons = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            showDialog = false
                            navController.navigate("home")
                        }
                    ) {
                        Text("Continue", color = IndigoPrimary)
                    }
                }
            }
        )
    }
}

// ─── Map Picker Dialog ───────────────────────────────────────────────────────

@Composable
fun MapPickerDialog(
    onLocationSelected: (LatLng, String) -> Unit,
    onDismiss: () -> Unit
) {
    // Default center: Lahore, Pakistan
    var selectedLatLng by remember { mutableStateOf(LatLng(31.5204, 74.3587)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLatLng, 12f)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            backgroundColor = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(IndigoPrimary)
                        .padding(12.dp)
                ) {
                    Text(
                        "Tap on map to select location",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                // Map
                Box(modifier = Modifier.weight(1f)) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraPositionState,
                        onMapClick = { latLng ->
                            selectedLatLng = latLng
                        }
                    ) {
                        Marker(
                            state = MarkerState(position = selectedLatLng),
                            title = "Food Truck Location"
                        )
                    }
                }

                // Confirm button
                Button(
                    onClick = {
                        val address = "Lat: ${"%.4f".format(selectedLatLng.latitude)}, Lng: ${"%.4f".format(selectedLatLng.longitude)}"
                        onLocationSelected(selectedLatLng, address)
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = IndigoPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Location", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ─── Helper Composable ───────────────────────────────────────────────────────

@Composable
fun CustomTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (String) -> Unit
) {
    var passwordVisibility by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        visualTransformation = if (isPassword && !passwordVisibility)
            PasswordVisualTransformation()
        else VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisibility)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = {
                    passwordVisibility = !passwordVisibility
                }) {
                    Icon(
                        imageVector = image,
                        contentDescription = if (passwordVisibility) "Hide password" else "Show password",
                        tint = IndigoPrimary
                    )
                }
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = IndigoPrimary,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = IndigoPrimary,
            cursorColor = IndigoPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    )
}

// ─── Firebase Helper ─────────────────────────────────────────────────────────

suspend fun registerFoodTruck(
    db: FirebaseFirestore,
    ownerId: String,
    organization: String,
    name: String,
    email: String,
    industry: String,
    city: String,
    location: String,
    latitude: Double,
    longitude: Double,
    onComplete: () -> Unit
) {
    val foodTruckData = mapOf(
        "ownerId" to ownerId,
        "organization" to organization.trim(),
        "name" to name.trim(),
        "email" to email.trim(),
        "industry" to industry.trim(),
        "city" to city.trim(),
        "location" to location.trim(),
        "latitude" to latitude,
        "longitude" to longitude,
        "createdAt" to System.currentTimeMillis()
    )

    db.collection("food_truck_owners").document(ownerId).set(foodTruckData).await()
    onComplete()
}