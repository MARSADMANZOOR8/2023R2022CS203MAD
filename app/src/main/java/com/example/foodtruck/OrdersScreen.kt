package com.example.foodtruck.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.History
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Define color palette (same as in OwnerOrdersScreen)
private val MainBlue = Color(0xFF2196F3)
private val TextDark = Color(0xFF263238)
private val TextLight = Color(0xFF78909C)
private val GreenComplete = Color(0xFF4CAF50)
private val OrangeWarning = Color(0xFFFF9800)
private val BackgroundColor = Color.White

@Composable
fun OrdersScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Recent Orders", "Order History")
    var activeOrders by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var completedOrders by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Hold firestore listener registration
    var listenerRegistration by remember { mutableStateOf<ListenerRegistration?>(null) }

    fun loadOrders() {
        isLoading = true
        // Cancel any existing listener
        listenerRegistration?.remove()

        // Set up a real-time listener for orders
        listenerRegistration = db.collection("users")
            .document(userId)
            .collection("orders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CustomerOrders", "Failed to listen for orders", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val pendingOrders = mutableListOf<Map<String, Any>>()
                    val finishedOrders = mutableListOf<Map<String, Any>>()

                    for (document in snapshot.documents) {
                        val orderData = document.data ?: continue

                        if (orderData["status"] == "completed") {
                            finishedOrders.add(orderData)
                        } else {
                            pendingOrders.add(orderData)
                        }
                    }

                    // Sort orders by orderDate (descending)
                    activeOrders = pendingOrders.sortedByDescending { it["orderedAt"] as? Long ?: 0 }
                    completedOrders = finishedOrders.sortedByDescending { it["orderedAt"] as? Long ?: 0 }
                    isLoading = false
                }
            }
    }

    // Clean up listener when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            listenerRegistration?.remove()
        }
    }

    LaunchedEffect(Unit) {
        loadOrders()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DarkBlue, MainBlue)
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "My Orders",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Tab switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = index }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (index == 0) Icons.Rounded.Fastfood else Icons.Rounded.History,
                                contentDescription = title,
                                tint = if (isSelected) MainBlue else TextLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                color = if (isSelected) MainBlue else TextLight,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .height(3.dp)
                                .width(80.dp)
                                .background(
                                    if (isSelected) MainBlue else Color.Transparent,
                                    shape = RoundedCornerShape(1.dp)
                                )
                        )
                    }
                }
            }

            // Divider
            Divider(color = LightBlue, thickness = 1.dp)

            // Orders content
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MainBlue
                    )
                } else {
                    val displayOrders = if (selectedTab == 0) activeOrders else completedOrders
                    if (displayOrders.isEmpty()) {
                        EmptyOrdersPlaceholder(isPending = selectedTab == 0)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayOrders) { order ->
                                CustomerOrderCard(order = order)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyOrdersPlaceholder(isPending: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (isPending) Icons.Rounded.Fastfood else Icons.Rounded.History,
                contentDescription = null,
                tint = TextLight,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isPending) "No active orders" else "No order history yet",
                fontSize = 18.sp,
                color = TextLight,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isPending) "Time to order some delicious food!" else "Complete your first order to see it here",
                fontSize = 14.sp,
                color = TextLight,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@Composable
fun CustomerOrderCard(order: Map<String, Any>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with Order ID and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order["orderId"].toString().takeLast(6)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextDark
                )

                // Status indicator
                val status = order["status"] as? String ?: "pending"
                val (statusColor, statusText) = when (status) {
                    "completed" -> Pair(GreenComplete, "Completed")
                    else -> Pair(OrangeWarning, "Pending")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = LightBlue, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Estimated pickup time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Pickup Time",
                        tint = MainBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Estimated Pickup",
                        fontSize = 12.sp,
                        color = TextLight
                    )
                    Text(
                        text = "${order["estimatedPickupTime"] ?: "Not available"}",
                        fontSize = 16.sp,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Food Truck info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🚚",
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Food Truck",
                        fontSize = 12.sp,
                        color = TextLight
                    )
                    Text(
                        text = "${(order["items"] as? List<Map<String, Any>>)?.firstOrNull()?.get("foodTruckName") ?: "Unknown"}",
                        fontSize = 16.sp,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order items header
            Text(
                text = "Order Items",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Display order items
            val items = order["items"] as? List<Map<String, Any>> ?: emptyList()
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item image if available
                    val imageUrl = item["imageUrl"] as? String
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Food image",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // Item details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${item["name"] ?: "Unknown Item"}",
                            fontSize = 16.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Qty: ${item["quantity"] ?: 1}",
                            fontSize = 14.sp,
                            color = TextLight
                        )
                    }
                }
            }
        }
    }
}

// Helper function that would be used in your navigation setup
fun listenForOrderStatusChange(userId: String, orderId: String, onStatusChange: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("users")
        .document(userId)
        .collection("orders")
        .document(orderId)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("OrderStatus", "Error listening for order status changes", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status") ?: "pending"
                onStatusChange(status)
            }
        }
}

// Helper function to update order status (for use in other parts of your app)
suspend fun updateOrderStatus(userId: String, orderId: String, newStatus: String): Boolean {
    return try {
        val db = FirebaseFirestore.getInstance()
        val orderRef = db.collection("users")
            .document(userId)
            .collection("orders")
            .document(orderId)

        orderRef.update("status", newStatus).await()
        true
    } catch (e: Exception) {
        Log.e("UpdateOrder", "Failed to update order status", e)
        false
    }
}