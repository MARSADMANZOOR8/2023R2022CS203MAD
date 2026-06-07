package com.example.foodtruck.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale


@Composable
fun CheckoutScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val estimatedPickupTime = "20-30 minutes"

    val primaryColor = Color(0xFF5960AB)
    val accentColor = Color(0xFFFF6D00)
    val backgroundColor = Color(0xFFF5F5F7)
    val cardColor = Color.White
    val disabledColor = Color(0xFFBDBDBD)
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF0F3FF)
        )
    )

    // Calculate total price
    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val formattedSubtotal = NumberFormat.getCurrencyInstance(Locale("en", "PK")).apply {
        currency = Currency.getInstance("PKR")
    }.format(subtotal)

    val formattedTotal = formattedSubtotal

    // Load cart items from Firebase
    LaunchedEffect(userId) {
        userId?.let {
            try {
                val snapshot = db.collection("users")
                    .document(it)
                    .collection("cart")
                    .get()
                    .await()

                cartItems = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                }.sortedBy { item -> item.foodTruckName }

                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    // Process order function
    fun processOrder() {
        coroutineScope.launch {
            try {
                isProcessing = true

                // Ensure we have all required info
                if (selectedPaymentMethod == null) {
                    isProcessing = false
                    return@launch
                }

                val orderRef = db.collection("users")
                    .document(userId ?: "")
                    .collection("orders")
                    .document()

                val orderItems = cartItems.map { item ->
                    mapOf(
                        "foodTruckId" to item.foodTruckId,
                        "foodTruckName" to item.foodTruckName,
                        "itemId" to item.itemId,
                        "name" to item.name,
                        "price" to item.price,
                        "quantity" to item.quantity,
                        "imageUrl" to item.imageUrl,
                        "ownerId" to item.ownerId
                    )
                }

                val orderData = mapOf(
                    "orderId" to orderRef.id,
                    "userId" to userId,
                    "items" to orderItems,
                    "subtotal" to subtotal,
                    "total" to subtotal,
                    "pickupMethod" to "self-pickup",
                    "estimatedPickupTime" to estimatedPickupTime,
                    "paymentMethod" to selectedPaymentMethod,
                    "status" to "pending",
                    "orderedAt" to System.currentTimeMillis()
                )

                // Save order to Firebase
                orderRef.set(orderData).await()

                // Clear cart
                val batch = db.batch()
                cartItems.forEach { item ->
                    val docRef = db.collection("users")
                        .document(userId ?: "")
                        .collection("cart")
                        .document(item.id)
                    batch.delete(docRef)
                }
                batch.commit().await()

                // Show success dialog
                isProcessing = false
                showSuccessDialog = true
            } catch (e: Exception) {
                e.printStackTrace()
                isProcessing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Checkout",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                backgroundColor = primaryColor,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                elevation = 0.dp
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBackground)
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    // Show loading indicator
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                    }
                }
                cartItems.isEmpty() -> {
                    // Show empty cart message
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = Color.LightGray
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "Your cart is empty",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { navController.navigate("home") },
                            colors = ButtonDefaults.buttonColors(backgroundColor = primaryColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(
                                Icons.Filled.Fastfood,
                                contentDescription = null,
                                tint = Color.White
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                "Find Food Trucks",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else -> {
                    // Checkout Screen Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        // Order Summary Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = cardColor,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Order Summary",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                cartItems.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${item.quantity}x ${item.name}",
                                            color = Color.DarkGray,
                                            fontSize = 14.sp
                                        )

                                        Text(
                                            text = NumberFormat.getCurrencyInstance(Locale("en", "PK")).apply {
                                                currency = Currency.getInstance("PKR")
                                            }.format(item.price * item.quantity),
                                            color = Color.DarkGray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }

                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    color = Color.LightGray
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Subtotal",
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )

                                    Text(
                                        text = formattedSubtotal,
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Total",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.Black
                                    )

                                    Text(
                                        text = formattedTotal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = primaryColor
                                    )
                                }
                            }
                        }

                        // Pickup Information Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = cardColor,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Pickup Information",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF0F7FF))
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.AccessTime,
                                        contentDescription = "Pickup Time",
                                        tint = primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Text(
                                            text = "Self-Pickup Service",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            fontSize = 16.sp
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Estimated pickup time: $estimatedPickupTime",
                                            color = Color.DarkGray,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Payment Method Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            backgroundColor = cardColor,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Payment Method",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Cash on Pickup Option
                                Button(
                                    onClick = { selectedPaymentMethod = "cash_on_pickup" },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (selectedPaymentMethod == "cash_on_pickup")
                                            primaryColor else Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                        .padding(bottom = 8.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.elevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 8.dp
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.MonetizationOn,
                                                contentDescription = "Cash",
                                                tint = if (selectedPaymentMethod == "cash_on_pickup")
                                                    Color.White else primaryColor
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = "Cash on Pickup",
                                                color = if (selectedPaymentMethod == "cash_on_pickup")
                                                    Color.White else Color.Black,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        if (selectedPaymentMethod == "cash_on_pickup") {
                                            Icon(
                                                Icons.Filled.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }

                                // Card Payment Option (Disabled)
                                Button(
                                    onClick = { /* Disabled */ },
                                    colors = ButtonDefaults.buttonColors(backgroundColor = disabledColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = ButtonDefaults.elevation(defaultElevation = 2.dp),
                                    enabled = false
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Filled.CreditCard,
                                                contentDescription = "Card",
                                                tint = Color.White
                                            )

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Text(
                                                text = "Pay with Card",
                                                color = Color.White,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }

                                        Text(
                                            text = "Coming Soon",
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Place Order Button
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { processOrder() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(backgroundColor = primaryColor),
                            enabled = !isProcessing && selectedPaymentMethod != null
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "PLACE ORDER - $formattedTotal",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            // Order Success Dialog
            if (showSuccessDialog) {
                AlertDialog(
                    onDismissRequest = { },
                    shape = RoundedCornerShape(16.dp),
                    backgroundColor = Color.White,
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = Color.Green,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Order Placed Successfully!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Your order has been placed successfully. You can track your order in the Orders section.",
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Estimated pickup time: $estimatedPickupTime",
                                textAlign = TextAlign.Center,
                                color = primaryColor
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Total: $formattedTotal",
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                fontSize = 18.sp
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showSuccessDialog = false
                                navController.navigate("orders") {
                                    popUpTo("orders") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = primaryColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 16.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "CONTINUE ORDERING",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }
        }
    }
}