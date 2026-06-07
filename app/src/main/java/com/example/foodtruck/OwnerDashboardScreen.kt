package com.example.foodtruck.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.foodtruck.ui.theme.IndigoPrimary
import com.example.foodtruck.ui.theme.IndigoLight
import com.example.foodtruck.ui.theme.IndigoDark
import com.example.foodtruck.ui.theme.IndigoSurface

val IndigoBackground = Color(0xFFF8F9FF)
val TextPrimary = Color(0xFF2B2B2B)
val TextSecondary = Color(0xFF656565)
val AccentOrange = Color(0xFFFF8A00)
val AccentGreen = Color(0xFF2ECC71)
val DashboardGradientStart = Color(0xFF5960AB)
val DashboardGradientEnd = Color(0xFF969BD0)
val CardBorder = Color(0xFFEAEEF7)

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val total: Double = 0.0,
    val deliveryLocation: String = "",
    val paymentMethod: String = "",
    val status: String = "",
    val customerName: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val ownerId: String = "",
    val ownerItems: List<Map<String, Any>> = emptyList(),
    val ownerTotal: Double = 0.0
)

@Composable
fun OwnerDashboardScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var foodTruckName by remember { mutableStateOf("Loading...") }
    var restaurantImage by remember { mutableStateOf<String?>(null) }
    var restaurantStatus by remember { mutableStateOf("Closed") }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val ownerId = auth.currentUser?.uid
        if (ownerId == null) {
            Toast.makeText(context, "Error: No owner ID found!", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
            return@LaunchedEffect
        }

        try {
            val ownerDoc = db.collection("food_truck_owners").document(ownerId).get().await()
            if (ownerDoc.exists()) {
                foodTruckName = ownerDoc.getString("organization") ?: "My Food Truck"
                restaurantStatus = ownerDoc.getString("restaurant_status") ?: "Closed"
                restaurantImage = ownerDoc.getString("restaurantImage")
            } else {
                errorMessage = "Food truck details not found!"
            }

            // Fetch orders
            val usersSnapshot = db.collection("users").get().await()
            val fetchedOrders = mutableListOf<Order>()

            for (userDoc in usersSnapshot.documents) {
                val userId = userDoc.id
                val ordersSnapshot = db.collection("users")
                    .document(userId)
                    .collection("orders")
                    .get()
                    .await()

                for (orderDoc in ordersSnapshot.documents) {
                    val orderData = orderDoc.data ?: continue
                    val items = orderData["items"] as? List<Map<String, Any>> ?: continue

                    if (items.any { it["ownerId"] == ownerId }) {
                        val ownerItems = items.filter { it["ownerId"] == ownerId }
                        val ownerTotal = ownerItems.sumOf {
                            ((it["price"] as? Number)?.toDouble() ?: 0.0) *
                                    ((it["quantity"] as? Number)?.toInt() ?: 1)
                        }

                        fetchedOrders.add(
                            Order(
                                orderId = orderData["orderId"] as? String ?: "",
                                userId = orderData["userId"] as? String ?: "",
                                total = (orderData["total"] as? Number)?.toDouble() ?: 0.0,
                                deliveryLocation = orderData["deliveryLocation"] as? String ?: "",
                                paymentMethod = orderData["paymentMethod"] as? String ?: "",
                                status = orderData["status"] as? String ?: "",
                                customerName = orderData["customerName"] as? String ?: "Customer",
                                items = items,
                                ownerId = ownerId,
                                ownerItems = ownerItems,
                                ownerTotal = ownerTotal
                            )
                        )
                    }
                }
            }

            orders = fetchedOrders
        } catch (e: Exception) {
            Log.e("OwnerDashboard", "Error fetching data", e)
            errorMessage = "Error loading data: ${e.message}"
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Food Truck Dashboard",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                backgroundColor = DashboardGradientStart,
                elevation = 0.dp,
                actions = {
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate("login")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(IndigoBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = IndigoPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                } else {
                    errorMessage?.let {
                        ErrorCard(errorMessage = it)
                    } ?: run {
                        DashboardContent(
                            foodTruckName = foodTruckName,
                            restaurantImage = restaurantImage,
                            restaurantStatus = restaurantStatus,
                            orders = orders,
                            onUpdateStatus = { newStatus ->
                                coroutineScope.launch {
                                    updateRestaurantStatus(db, auth.currentUser?.uid, newStatus)
                                    restaurantStatus = newStatus
                                }
                            },
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(
            name = "Dashboard",
            route = "owner_dashboard",
            icon = Icons.Rounded.Dashboard
        ),
        BottomNavItem(
            name = "Orders",
            route = "owner_orders",
            icon = Icons.Rounded.ListAlt
        ),
        BottomNavItem(
            name = "Menu",
            route = "manage_menu",
            icon = Icons.Rounded.RestaurantMenu
        ),
        BottomNavItem(
            name = "Profile",
            route = "edit_food_truck",
            icon = Icons.Rounded.Person
        )
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomNavigation(
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = item.route == currentRoute
            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.name,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(text = item.name, fontSize = 12.sp) },
                selectedContentColor = IndigoPrimary,
                unselectedContentColor = TextSecondary,
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val name: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun DashboardContent(
    foodTruckName: String,
    restaurantImage: String?,
    restaurantStatus: String,
    orders: List<Order>,
    onUpdateStatus: (String) -> Unit,
    navController: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            HeaderSection(foodTruckName, restaurantImage, restaurantStatus)

            Spacer(modifier = Modifier.height(16.dp))

            RestaurantStatusButton(
                restaurantStatus = restaurantStatus,
                onUpdateStatus = onUpdateStatus
            )

            Spacer(modifier = Modifier.height(24.dp))

            QuickActionsSection(navController)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Orders",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                TextButton(
                    onClick = { navController.navigate("owner_orders") }
                ) {
                    Text(
                        "View All",
                        color = IndigoPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = "View All Orders",
                        tint = IndigoPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (orders.isEmpty()) {
                EmptyOrdersCard()
            } else {
                // Horizontal scrolling orders
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val activeOrders = orders.filter { it.status != "completed" }
                    if (activeOrders.isNotEmpty()) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(activeOrders) { order ->
                                HorizontalOrderCard(order)
                            }
                        }
                    } else {
                        EmptyOrdersCard()
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalOrderCard(order: Order) {
    val orderStatusColor = when(order.status) {
        "Pending" -> AccentOrange
        "completed" -> AccentGreen
        "Cancelled" -> Color.Red
        else -> TextSecondary
    }

    val orderStatusIcon = when(order.status) {
        "Pending" -> Icons.Rounded.Pending
        "completed" -> Icons.Rounded.CheckCircle
        "Cancelled" -> Icons.Rounded.Cancel
        else -> Icons.Rounded.Help
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        elevation = 2.dp,
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Order #${order.orderId.takeLast(6)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(orderStatusColor.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = orderStatusIcon,
                        contentDescription = order.status,
                        tint = orderStatusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = order.status,
                        color = orderStatusColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            Divider(
                color = CardBorder,
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Customer",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        order.customerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Total",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        "PKR ${order.ownerTotal}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )
                }
            }

            // Display first item if available
            if (order.ownerItems.isNotEmpty()) {
                Text(
                    "${order.ownerItems.size} item(s)",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun HeaderSection(foodTruckName: String, restaurantImage: String?, restaurantStatus: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DashboardGradientStart,
                        DashboardGradientEnd
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (restaurantImage != null && restaurantImage.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(restaurantImage),
                            contentDescription = "Food Truck",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Restaurant,
                            contentDescription = "Food Truck",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Welcome to",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        foodTruckName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            StatusIndicator(restaurantStatus)
        }
    }
}

@Composable
fun StatusIndicator(status: String) {
    val isOpen = status == "Open"
    val statusColor = if (isOpen) AccentGreen else Color.Red
    val statusText = if (isOpen) "Currently Open" else "Currently Closed"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun QuickActionsSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionCard(
                title = "Edit Details",
                icon = Icons.Rounded.Edit,
                backgroundColor = Color(0xFFF0F4FF),
                iconTint = IndigoPrimary,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("edit_food_truck") }
            )

            ActionCard(
                title = "Manage Menu",
                icon = Icons.Rounded.RestaurantMenu,
                backgroundColor = Color(0xFFFFF4E6),
                iconTint = AccentOrange,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("manage_menu") }
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 0.dp,
        backgroundColor = backgroundColor,
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RestaurantStatusButton(restaurantStatus: String, onUpdateStatus: (String) -> Unit) {
    val isOpen = restaurantStatus == "Open"
    val newStatus = if (isOpen) "Closed" else "Open"
    val buttonText = if (isOpen) "Close Restaurant" else "Open Restaurant"

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    "Confirm Status Change",
                    fontWeight = FontWeight.Bold,
                    color = IndigoPrimary,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    "Are you sure you want to set the restaurant as $newStatus?",
                    color = TextPrimary,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateStatus(newStatus)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isOpen) Color.Red else AccentGreen
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Confirm",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedBorder.copy(
                        brush = SolidColor(TextSecondary)
                    )
                ) {
                    Text(
                        "Cancel",
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            backgroundColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDialog = true }
                .padding(vertical = 16.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusIcon = if (isOpen) Icons.Rounded.Store else Icons.Rounded.StoreMallDirectory

                val statusColor = if (isOpen) AccentGreen else IndigoPrimary
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Restaurant Status",
                    tint = statusColor,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = if (isOpen) "Restaurant is Open" else "Restaurant is Closed",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "Tap to change status",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            val toggleButtonColor = if (isOpen) Color.Red else AccentGreen
            val toggleButtonText = if (isOpen) "Close" else "Open"

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(backgroundColor = toggleButtonColor),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = toggleButtonText,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

suspend fun updateRestaurantStatus(db: FirebaseFirestore, ownerId: String?, newStatus: String) {
    if (ownerId == null) return
    try {
        db.collection("food_truck_owners").document(ownerId)
            .update("restaurant_status", newStatus)
            .await()
        Log.d("Firestore", "Updated restaurant status to $newStatus")
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating status", e)
    }
}

@Composable
fun EmptyOrdersCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White,
        elevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Receipt,
                contentDescription = "No Orders",
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "No Orders Yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Your orders will appear here when customers place them",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorCard(errorMessage: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        backgroundColor = Color(0xFFFFEBEE),
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Error,
                contentDescription = "Error",
                tint = Color.Red,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Something Went Wrong",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                errorMessage,
                fontSize = 16.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}