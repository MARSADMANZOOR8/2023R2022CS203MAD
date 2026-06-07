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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Define a modern blue color palette
private val MainBlue = Color(0xFF2196F3)
private val TextDark = Color(0xFF263238)
private val TextLight = Color(0xFF78909C)
private val GreenComplete = Color(0xFF4CAF50)
private val BackgroundColor = Color.White

@Composable
fun OwnerOrdersScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val ownerId = auth.currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Active Orders", "Completed Orders")
    var allOrders by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var completedOrders by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var foodTruckName by remember { mutableStateOf("") }

    // Get the current owner's food truck name
    LaunchedEffect(ownerId) {
        if (ownerId != null) {
            try {
                val ownerDocRef = db.collection("food_truck_owners")
                    .whereEqualTo("ownerId", ownerId)
                    .limit(1)
                    .get()
                    .await()



                if (!ownerDocRef.isEmpty) {
                    foodTruckName = ownerDocRef.documents[0].getString("name") ?: ""
                }
            } catch (e: Exception) {
                Log.e("OwnerOrders", "Failed to fetch food truck name", e)
            }
        }
    }

    fun loadOrders() {
        if (foodTruckName.isEmpty()) return

        isLoading = true
        coroutineScope.launch {
            try {
                val usersSnapshot = db.collection("users").get().await()
                val fetchedOrders = mutableListOf<Map<String, Any>>()
                val fetchedCompleted = mutableListOf<Map<String, Any>>()

                for (userDoc in usersSnapshot.documents) {
                    val userId = userDoc.id
                    val ordersSnapshot = db.collection("users")
                        .document(userId)
                        .collection("orders")
                        .get()
                        .await()

                    for (orderDoc in ordersSnapshot.documents) {
                        val order = orderDoc.data ?: continue
                        val items = order["items"] as? List<Map<String, Any>> ?: continue
                        if (items.any { it["ownerId"] == ownerId }) {
                            val orderWithOwnerDetails = order.toMutableMap()
                            val ownerItems = items.filter { it["ownerId"] == ownerId }
                            orderWithOwnerDetails["ownerItems"] = ownerItems
                            val ownerTotal = ownerItems.sumOf {
                                ((it["price"] as? Number)?.toDouble() ?: 0.0) *
                                        ((it["quantity"] as? Number)?.toInt() ?: 1)
                            }
                            orderWithOwnerDetails["ownerTotal"] = ownerTotal

                            if ((order["status"] ?: "") == "completed") {
                                fetchedCompleted.add(orderWithOwnerDetails)
                            } else {
                                fetchedOrders.add(orderWithOwnerDetails)
                            }
                        }

                    }
                }

                allOrders = fetchedOrders
                completedOrders = fetchedCompleted
                isLoading = false
            } catch (e: Exception) {
                Log.e("OwnerOrders", "Failed to fetch orders", e)
                isLoading = false
            }
        }
    }

    fun markOrderAsCompleted(order: Map<String, Any>) {
        coroutineScope.launch {
            try {
                val userId = order["userId"] as? String ?: return@launch
                val orderId = order["orderId"] as? String ?: return@launch

                val orderRef = db.collection("users").document(userId).collection("orders").document(orderId)
                orderRef.update("status", "completed").await()

                // Reload
                loadOrders()
            } catch (e: Exception) {
                Log.e("OwnerOrders", "Failed to mark order complete", e)
            }
        }
    }

    LaunchedEffect(foodTruckName) {
        if (foodTruckName.isNotEmpty()) {
            loadOrders()
        }
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
                            colors = listOf(Color(0xFF5960AB), Color(0xFF969BD0))
                        )
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = "Order Management",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Custom tab row
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
                                imageVector = if (index == 0) Icons.Rounded.List else Icons.Rounded.History,
                                contentDescription = title,
                                tint = if (isSelected) MainBlue else TextLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                color = if (isSelected) Color(0xFF969BD0) else TextLight,
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
                if (foodTruckName.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No food truck found for this owner",
                            fontSize = 18.sp,
                            color = TextLight,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MainBlue
                    )
                } else {
                    val displayOrders = if (selectedTab == 0) allOrders else completedOrders
                    if (displayOrders.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Default.Fastfood else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = TextLight,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No ${if (selectedTab == 0) "active" else "completed"} orders available",
                                    fontSize = 18.sp,
                                    color = TextLight,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(displayOrders) { order ->
                                OrderCard(
                                    order = order,
                                    isActive = selectedTab == 0,
                                    onMarkCompleted = { markOrderAsCompleted(order) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: Map<String, Any>,
    isActive: Boolean,
    onMarkCompleted: () -> Unit
) {
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
            // Header with Order ID
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

                if (!isActive) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenComplete.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Completed",
                            color = GreenComplete,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = LightBlue, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Customer details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📍",
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Pickup Information",
                        fontSize = 12.sp,
                        color = TextLight
                    )
                    Text(
                        text = "Self-pickup (${order["estimatedPickupTime"]})",
                        fontSize = 16.sp,
                        color = TextDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment information
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(LightBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "💳",
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Payment Method",
                        fontSize = 12.sp,
                        color = TextLight
                    )
                    Text(
                        text = "${order["paymentMethod"]}".replace("_", " ").capitalize(),
                        fontSize = 16.sp,
                        color = TextDark
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MainBlue.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "PKR ${order["ownerTotal"]}",
                        color = Color(0xFF5960AB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Order items
            Text(
                text = "Order Items",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Use ownerItems instead of items
            val items = order["ownerItems"] as? List<Map<String, Any>> ?: emptyList()
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(LightBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "×${item["quantity"]}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MainBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item["name"]}",
                            fontSize = 15.sp,
                            color = TextDark
                        )
                    }

                    if (item.containsKey("price")) {
                        Text(
                            text = "PKR ${item["price"]}",
                            fontSize = 15.sp,
                            color = TextLight
                        )
                    }
                }
            }

            // Action button for active orders
            if (isActive) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onMarkCompleted,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MainBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.elevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Mark as Completed",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// Extension function to capitalize first letter of each word
fun String.capitalize(): String {
    return split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}