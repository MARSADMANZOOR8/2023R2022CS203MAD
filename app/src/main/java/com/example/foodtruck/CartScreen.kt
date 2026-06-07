package com.example.foodtruck.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.NumberFormat
import java.util.*

@Composable
fun CartScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State variables
    var cartItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Modern color palette
    val primaryColor = Color(0xFF5960AB)
    val accentColor = Color(0xFFFF6D00)
    val backgroundColor = Color(0xFFF5F5F7)
    val cardColor = Color.White
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFF0F3FF)
        )
    )

    // Calculate total price
    val totalPrice = cartItems.sumOf { it.price * it.quantity }
    val formattedTotal = NumberFormat.getCurrencyInstance(Locale("en", "PK")).apply {
        currency = Currency.getInstance("PKR")
    }.format(totalPrice)


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

    // Function to update item quantity
    fun updateItemQuantity(item: CartItem, newQuantity: Int) {
        coroutineScope.launch {
            try {
                if (newQuantity <= 0) {
                    // Remove item
                    db.collection("users")
                        .document(userId ?: "")
                        .collection("cart")
                        .document(item.id)
                        .delete()
                        .await()

                    // Update local state
                    cartItems = cartItems.filter { it.id != item.id }
                } else {
                    // Update quantity
                    db.collection("users")
                        .document(userId ?: "")
                        .collection("cart")
                        .document(item.id)
                        .update("quantity", newQuantity)
                        .await()

                    // Update local state
                    cartItems = cartItems.map {
                        if (it.id == item.id) it.copy(quantity = newQuantity) else it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Function to clear cart
    fun clearCart() {
        coroutineScope.launch {
            try {
                isProcessing = true

                // Delete all cart items
                val batch = db.batch()
                cartItems.forEach { item ->
                    val docRef = db.collection("users")
                        .document(userId ?: "")
                        .collection("cart")
                        .document(item.id)
                    batch.delete(docRef)
                }
                batch.commit().await()

                // Update local state
                cartItems = emptyList()
                showCheckoutDialog = false
                isProcessing = false
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
                        "My Cart",
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
                actions = {
                    AnimatedVisibility(
                        visible = cartItems.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300))
                    ) {
                        IconButton(onClick = {
                            // Show confirmation dialog
                            coroutineScope.launch {
                                if (cartItems.isNotEmpty()) {
                                    showCheckoutDialog = true
                                }
                            }
                        }) {
                            Icon(Icons.Filled.ShoppingCart, "Checkout", tint = Color.White)
                        }
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
                    EmptyCartMessage(
                        navController = navController,
                        primaryColor = primaryColor
                    )
                }
                else -> {
                    // Show cart items with checkout button at bottom
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 80.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            // Group items by food truck
                            val groupedItems = cartItems.groupBy { it.foodTruckName }

                            groupedItems.forEach { (truckName, items) ->
                                item {
                                    FoodTruckHeader(truckName)
                                }

                                items(items) { cartItem ->
                                    CartItemCard(
                                        cartItem = cartItem,
                                        cardColor = cardColor,
                                        primaryColor = primaryColor,
                                        accentColor = accentColor,
                                        onQuantityChanged = { newQuantity ->
                                            updateItemQuantity(cartItem, newQuantity)
                                        }
                                    )
                                }

                                // Add spacing between food truck groups
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        // Checkout button fixed at bottom
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            backgroundColor = Color.White,
                            elevation = 16.dp,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Total",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        formattedTotal,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor
                                    )
                                }

                                Button(
                                    onClick = { navController.navigate("checkout") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = primaryColor
                                    )
                                ) {
                                    Text(
                                        "CHECKOUT",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Checkout confirmation dialog
            if (showCheckoutDialog) {
                AlertDialog(
                    onDismissRequest = {
                        if (!isProcessing) showCheckoutDialog = false
                    },
                    title = { Text("Complete Order") },
                    text = {
                        Column {
                            Text("Your total is $formattedTotal")
                            Text("Would you like to place this order?")

                            if (isProcessing) {
                                LinearProgressIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { clearCart() },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(backgroundColor = primaryColor)
                        ) {
                            Text("Place Order", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCheckoutDialog = false },
                            enabled = !isProcessing
                        ) {
                            Text("Cancel")
                        }
                    },
                    backgroundColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}

@Composable
fun FoodTruckHeader(truckName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Restaurant,
            contentDescription = null,
            tint = Color(0xFF3D5AFE)
        )

        Text(
            text = truckName,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    cardColor: Color,
    primaryColor: Color,
    accentColor: Color,
    onQuantityChanged: (Int) -> Unit
) {

    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "PK")).apply {
        currency = Currency.getInstance("PKR")
    }
    val itemPrice = currencyFormatter.format(cartItem.price)
    val totalPrice = currencyFormatter.format(cartItem.price * cartItem.quantity)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = cardColor,
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item image
            Image(
                painter = rememberAsyncImagePainter(cartItem.imageUrl),
                contentDescription = "Food Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Item details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cartItem.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = itemPrice,
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Total for this item
                Text(
                    text = "Total: $totalPrice",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Quantity control
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = { onQuantityChanged(cartItem.quantity + 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Increase",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = cartItem.quantity.toString(),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                IconButton(
                    onClick = { onQuantityChanged(cartItem.quantity - 1) },
                    modifier = Modifier
                        .size(32.dp)
                        .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        Icons.Filled.Remove,
                        contentDescription = "Decrease",
                        tint = primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyCartMessage(navController: NavController, primaryColor: Color) {
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Looks like you haven't added any items to your cart yet",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
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