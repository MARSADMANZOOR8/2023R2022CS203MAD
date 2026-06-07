package com.example.foodtruck.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

val PrimaryColor = Color(0xFF5960AB)
val SecondaryColor = Color(0xFF969BD0)
val Background = Color(0xFFF5F9FF)
val SurfaceColor = Color.White
val AccentColor = Color(0xFFFFAC41)
val TextPrimaryColor = Color(0xFF2D3436)
val TextSecondaryColor = Color(0xFF636E72)

@Composable
fun AccountScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val scrollState = rememberScrollState()

    var userName by remember { mutableStateOf("Fetching...") }

    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.uid?.let { uid ->
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                userName = doc.getString("name") ?: "User"
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching user data", e)
                userName = "User"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PrimaryColor.copy(alpha = 0.05f), Background),
                    startY = 0f,
                    endY = 500f
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "My Profile",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    backgroundColor = PrimaryColor,
                    elevation = 0.dp,
                    actions = {
                        IconButton(onClick = { /* TODO: Open Settings */ }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            backgroundColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
            ) {
                // Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 4.dp,
                    backgroundColor = SurfaceColor
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Profile image with gradient background
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryColor, SecondaryColor)
                                    )
                                )
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(60.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = userName,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryColor
                        )

                        Text(
                            text = "Food Explorer",
                            fontSize = 16.sp,
                            color = TextSecondaryColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Account Section
                SectionHeader("My Account")

                AccountOptionItem(
                    title = "My Orders",
                    subtitle = "View your order history",
                    icon = Icons.Rounded.Receipt,
                    onClick = {
                        if (navController.currentDestination?.route != "orders") {
                            navController.navigate("orders") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                )

                AccountOptionItem(
                    title = "Favorite Trucks",
                    subtitle = "Manage saved food trucks",
                    icon = Icons.Rounded.Favorite,
                    onClick = {
                        if (navController.currentDestination?.route != "favorites") {
                            navController.navigate("favorites") {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )

                // General Section
                SectionHeader("General Settings")

                AccountOptionItem(
                    title = "Food Truck for Business",
                    subtitle = "Register your food truck",
                    icon = Icons.Rounded.Business,
                    onClick = { navController.navigate("food_truck_business") }
                )

                AccountOptionItem(
                    title = "Terms & Policies",
                    subtitle = "Privacy and legal information",
                    icon = Icons.Rounded.Description,
                    onClick = { navController.navigate("term") }
                )

                // Support Section
                SectionHeader("Support")

                AccountOptionItem(
                    title = "Help Center",
                    subtitle = "Get help and contact support",
                    icon = Icons.Rounded.Help,
                    onClick = { navController.navigate("help") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                Button(
                    onClick = {
                        auth.signOut()
                        navController.navigate("login")
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFFFF6B6B),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(54.dp),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Icon(
                        Icons.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Sign Out",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryColor,
        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Composable
fun AccountOptionItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        backgroundColor = SurfaceColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with circle background
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        color = PrimaryColor.copy(alpha = 0.1f)
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = PrimaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryColor
                )

                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = TextSecondaryColor
                )
            }

            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Navigate",
                tint = TextSecondaryColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}