package com.example.foodtruck.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.foodtruck.ui.theme.*

@Composable
fun EditFoodTruckScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState()

    val ownerId = auth.currentUser?.uid ?: ""
    var restaurantName by remember { mutableStateOf("") }
    var restaurantImage by remember { mutableStateOf<String?>(null) }
    var ownerEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // For dialogs
    var showTermsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Fetch Existing Food Truck Data
    LaunchedEffect(ownerId) {
        if (ownerId.isNotEmpty()) {
            try {
                val doc = db.collection("food_truck_owners").document(ownerId).get().await()
                restaurantName = doc.getString("organization") ?: ""
                restaurantImage = doc.getString("restaurantImage")
                ownerEmail = auth.currentUser?.email ?: ""
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching data: $e")
                Toast.makeText(context, "Error loading data!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                backgroundColor = Color(0xFF5960AB),
                elevation = 8.dp,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F8F8))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF5960AB),
                                    Color(0xFF4A51A0)
                                )
                            )
                        )
                        .padding(top = 30.dp, bottom = 50.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Photo
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(3.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (restaurantImage != null && restaurantImage!!.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(restaurantImage),
                                    contentDescription = "Restaurant Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Restaurant,
                                    contentDescription = "Restaurant",
                                    tint = Color(0xFF5960AB),
                                    modifier = Modifier.size(70.dp)
                                )
                            }

                            // Camera Icon for changing photo
                            IconButton(
                                onClick = { navController.navigate("upload_image") },
                                modifier = Modifier
                                    .size(42.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF5960AB))
                                    .border(2.dp, Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Change Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Restaurant Name
                        Text(
                            text = restaurantName.ifEmpty { "Your Food Truck" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Owner Email
                        Text(
                            text = ownerEmail,
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Edit Restaurant Name Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-25).dp)
                        .padding(horizontal = 16.dp),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Business Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5960AB)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CustomTextField(
                            label = "Restaurant Name",
                            value = restaurantName,
                            onValueChange = { restaurantName = it },
                            leadingIcon = Icons.Filled.Store
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (isLoading) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF5960AB)
                            )
                        } else {
                            Button(
                                onClick = {
                                    if (restaurantName.isEmpty()) {
                                        Toast.makeText(context, "Please enter the name!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isLoading = true
                                    coroutineScope.launch {
                                        try {
                                            updateFoodTruckDetails(db, ownerId, restaurantName) {
                                                isLoading = false
                                                Toast.makeText(context, "Updated Successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            isLoading = false
                                            Log.e("Firestore", "Error updating data", e)
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5960AB)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .height(44.dp)
                            ) {
                                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Profile Options
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        ProfileOption(
                            icon = Icons.Outlined.History,
                            title = "Order History",
                            onClick = { navController.navigate("owner_orders") }
                        )

                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                        ProfileOption(
                            icon = Icons.Outlined.ReceiptLong,
                            title = "Terms and Policies",
                            onClick = { showTermsDialog = true }
                        )

                        Divider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                        ProfileOption(
                            icon = Icons.Outlined.HelpOutline,
                            title = "Help Center",
                            onClick = { showHelpDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Logout Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    elevation = 4.dp
                ) {
                    ProfileOption(
                        icon = Icons.Outlined.Logout,
                        title = "Logout",
                        onClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("login") {
                                popUpTo("main") { inclusive = true }
                            }
                        },
                        iconTint = Color(0xFFE57373)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Terms and Policies Dialog
            if (showTermsDialog) {
                Dialog(onDismissRequest = { showTermsDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color.White,
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "Terms and Policies",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5960AB)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "1. Service Agreement",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "By using our food truck management platform, you agree to abide by all applicable laws and regulations regarding food preparation, handling, and sales in your operating location.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "2. Commission and Fees",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Our platform charges a 10% commission on all completed orders. Payment processing fees may apply separately based on the payment method used by customers.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "3. Food Safety Requirements",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Food truck owners are responsible for maintaining all necessary licenses, permits, and health certifications required in their operating jurisdiction.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "4. Cancellation Policy",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Food truck owners may cancel orders only in exceptional circumstances. Repeated cancellations may result in account review or suspension.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { showTermsDialog = false },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5960AB)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Close", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Help Center Dialog
            if (showHelpDialog) {
                Dialog(onDismissRequest = { showHelpDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color.White,
                        elevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "Help Center",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5960AB)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Frequently Asked Questions",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "How do I update my menu?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Text(
                                "Navigate to the Menu tab and use the '+' button to add new items or edit existing ones by tapping on them.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "How do I process an order?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Text(
                                "New orders appear in your Orders tab. Tap on an order to view details and use the status buttons to update it as you prepare and complete the order.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "How do I update my location?",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Text(
                                "Go to the Location tab and either allow automatic GPS tracking or manually set your current location to help customers find you.",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                "Contact Support",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Email: trucktaste2030@gmail.com\nPhone: +966 55 469 1980\nAvailable: Mon-Fri, 8AM - 6PM",
                                fontSize = 14.sp,
                                color = Color(0xFF666666)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { showHelpDialog = false },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5960AB)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Close", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = Color(0xFF5960AB)
) {
    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Go to $title",
                tint = Color(0xFFAAAAAA),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color(0xFF666666),
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Enter $label", color = Color(0xFFAAAAAA)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = label,
                    tint = Color(0xFF5960AB)
                )
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF5960AB),
                unfocusedBorderColor = Color(0xFFDDDDDD),
                cursorColor = Color(0xFF5960AB),
                backgroundColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(8.dp))
        )
    }
}

suspend fun updateFoodTruckDetails(
    db: FirebaseFirestore,
    ownerId: String,
    restaurantName: String,
    onComplete: () -> Unit
) {
    val foodTruckData = mutableMapOf<String, Any>()
    foodTruckData["organization"] = restaurantName
    db.collection("food_truck_owners").document(ownerId)
        .update(foodTruckData)
        .addOnSuccessListener { onComplete() }
        .addOnFailureListener { e -> Log.e("Firestore", "Error updating data", e) }
}