package com.example.foodtruck.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class FavoriteData(
    val organization: String = "",
    val location: String = "",
    val restaurantImage: String = "",
    val reviewsCount: Int = 0,
    val averageRating: Double = 0.0,
    val timestamp: String = ""
)

data class Review(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val text: String = "",
    val rating: Int = 3,
    val timestamp: String = ""
)

data class CartItem(
    val id: String = "",
    val foodTruckId: String = "",
    val foodTruckName: String = "",
    val itemId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1,
    val timestamp: String = "",
    val ownerId: String = ""
)

@Composable
fun FoodTruckDetailScreen(navController: NavController, foodTruckName: String) {

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var foodTruck by remember { mutableStateOf<FoodTruck?>(null) }
    var menuItems by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var newReviewText by remember { mutableStateOf("") }
    var selectedRating by remember { mutableStateOf(0) }
    var selectedTab by remember { mutableStateOf(0) }
    var isFavorite by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val cartItemCount = remember { mutableStateOf(0) }

    LaunchedEffect(auth.currentUser?.uid) {
        auth.currentUser?.uid?.let { userId ->
            try {
                val cartSnapshot = db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .get()
                    .await()

                var totalCount = 0
                cartSnapshot.documents.forEach { doc ->
                    val quantity = doc.getLong("quantity")?.toInt() ?: 0
                    totalCount += quantity
                }
                cartItemCount.value = totalCount

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val primaryColor = Color(0xFF5960AB)
    val accentColor = Color(0xFFFF6D00)
    val backgroundColor = Color(0xFFF5F5F7)
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF0F3FF))
    )
    val cardColor = Color.White

    // Check if the food truck is already in favorites
    LaunchedEffect(auth.currentUser?.uid) {
        auth.currentUser?.uid?.let { userId ->
            try {
                val favoriteDoc = db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .whereEqualTo("organization", foodTruckName)
                    .get()
                    .await()

                isFavorite = !favoriteDoc.isEmpty
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(foodTruckName) {
        try {
            val query = db.collection("food_truck_owners")
                .whereEqualTo("organization", foodTruckName)
                .get()
                .await()

            val truckDoc = query.documents.firstOrNull()
            foodTruck = truckDoc?.let { doc ->
                FoodTruck(
                    organization = doc.getString("organization") ?: "Unknown",
                    restaurantImage = doc.getString("restaurantImage") ?: "",
                    status = doc.getString("restaurant_status") ?: "Closed",
                    latitude = doc.getDouble("latitude") ?: 0.0,
                    longitude = doc.getDouble("longitude") ?: 0.0,
                    location = doc.getString("location") ?: "Unknown",
                    ownerId = doc.getString("ownerId") ?: ""
                )
            }
            truckDoc?.id?.let { ownerId ->
                val menuSnapshot = db.collection("food_truck_owners")
                    .document(ownerId)
                    .collection("menu_items")
                    .get()
                    .await()
                menuItems = menuSnapshot.documents.mapNotNull {
                    it.toObject(MenuItem::class.java)
                }

                val reviewSnapshot = db.collection("food_truck_owners")
                    .document(ownerId)
                    .collection("reviews")
                    .orderBy("timestamp")
                    .get()
                    .await()
                reviews = reviewSnapshot.documents.mapNotNull {
                    it.toObject(Review::class.java)?.copy(id = it.id)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Calculate average rating
    val averageRating = if (reviews.isNotEmpty()) {
        reviews.map { it.rating }.average()
    } else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        foodTruck?.organization ?: "Food Truck Details",
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
                    IconButton(onClick = {
                        auth.currentUser?.uid?.let { userId ->
                            coroutineScope.launch {
                                try {
                                    if (isFavorite) {
                                        // Remove from favorites
                                        db.collection("users")
                                            .document(userId)
                                            .collection("favorites")
                                            .whereEqualTo("organization", foodTruck?.organization)
                                            .get()
                                            .await()
                                            .documents
                                            .forEach { doc ->
                                                doc.reference.delete().await()
                                            }
                                        isFavorite = false
                                    } else {
                                        // Add to favorites
                                        val timestamp = SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss",
                                            Locale.getDefault()
                                        ).format(Date())

                                        val favoriteData = FavoriteData(
                                            organization = foodTruck?.organization ?: "",
                                            location = foodTruck?.location ?: "",
                                            restaurantImage = foodTruck?.restaurantImage ?: "",
                                            reviewsCount = reviews.size,
                                            averageRating = averageRating,
                                            timestamp = timestamp
                                        )

                                        db.collection("users")
                                            .document(userId)
                                            .collection("favorites")
                                            .add(favoriteData)
                                            .await()

                                        isFavorite = true
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }) {
                        Icon(
                            if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            "Favorite",
                            tint = if (isFavorite) Color.Red else Color.White
                        )
                    }
                    IconButton(onClick = { /* Share functionality */ }) {
                        Icon(Icons.Filled.Share, "Share", tint = Color.White)
                    }
                },
                elevation = 0.dp
            )
        },
        bottomBar = {
            BottomNavigation(backgroundColor = Color.White) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.Restaurant, "Menu") },
                    label = { Text("Menu") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    selectedContentColor = primaryColor,
                    unselectedContentColor = Color.Gray
                )
                BottomNavigationItem(
                    icon = { Icon(Icons.Filled.RateReview, "Reviews") },
                    label = { Text("Reviews") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    selectedContentColor = primaryColor,
                    unselectedContentColor = Color.Gray
                )
            }
        },
        floatingActionButton = {
            Box(contentAlignment = Alignment.Center) {
                FloatingActionButton(
                    onClick = { navController.navigate("cart") },
                    backgroundColor = accentColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart")
                }

                if (cartItemCount.value > 0) {
                    Surface(
                        shape = CircleShape,
                        color = primaryColor,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 8.dp, y = (-8).dp)
                    ) {
                        Text(
                            text = cartItemCount.value.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    HeaderSection(foodTruck, averageRating, reviews.size, accentColor)
                }
                when (selectedTab) {
                    0 -> {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Menu Items",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )

                                Text(
                                    "${menuItems.size} items",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        if (menuItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No menu items available",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        } else {
                            items(menuItems, key = { it.id }) { menuItem ->
                                MenuItemCard(menuItem, cardColor, primaryColor, foodTruck, cartItemCount)
                            }

                        }
                    }
                    1 -> {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Customer Reviews",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor
                                )

                                Text(
                                    "${reviews.size} reviews",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        item {
                            AddReviewCard(
                                newReviewText = newReviewText,
                                onReviewTextChange = { newReviewText = it },
                                selectedRating = selectedRating,
                                onRatingChange = { selectedRating = it },
                                cardColor = cardColor,
                                primaryColor = primaryColor,
                                accentColor = accentColor,
                                auth = auth,
                                db = db,
                                foodTruck = foodTruck,
                                coroutineScope = coroutineScope,
                                onReviewsUpdated = { updatedReviews ->
                                    reviews = updatedReviews

                                    // Update favorite data if user has favorited this truck
                                    if (isFavorite) {
                                        coroutineScope.launch {
                                            try {
                                                val userId = auth.currentUser?.uid
                                                if (userId != null) {
                                                    val favoriteQuery = db.collection("users")
                                                        .document(userId)
                                                        .collection("favorites")
                                                        .whereEqualTo("organization", foodTruck?.organization)
                                                        .get()
                                                        .await()

                                                    favoriteQuery.documents.firstOrNull()?.reference?.update(
                                                        mapOf(
                                                            "reviewsCount" to updatedReviews.size,
                                                            "averageRating" to if (updatedReviews.isNotEmpty())
                                                                updatedReviews.map { it.rating }.average() else 0.0
                                                        )
                                                    )?.await()
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        if (reviews.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Be the first to review!",
                                        color = Color.Gray,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        } else {
                            items(reviews) { review ->
                                ReviewCard(
                                    review = review,
                                    cardColor = cardColor,
                                    accentColor = accentColor,
                                    isUserReview = review.userId == auth.currentUser?.uid,
                                    onDeleteReview = {
                                        coroutineScope.launch {
                                            try {
                                                val truckQuery = db.collection("food_truck_owners")
                                                    .whereEqualTo("organization", foodTruck!!.organization)
                                                    .get()
                                                    .await()

                                                val truckId = truckQuery.documents.firstOrNull()?.id

                                                if (truckId != null) {
                                                    db.collection("food_truck_owners")
                                                        .document(truckId)
                                                        .collection("reviews")
                                                        .document(review.id)
                                                        .delete()
                                                        .await()

                                                    val updatedReviewSnapshot = db.collection("food_truck_owners")
                                                        .document(truckId)
                                                        .collection("reviews")
                                                        .orderBy("timestamp")
                                                        .get()
                                                        .await()

                                                    val updatedReviews = updatedReviewSnapshot.documents.mapNotNull {
                                                        it.toObject(Review::class.java)?.copy(id = it.id)
                                                    }

                                                    reviews = updatedReviews

                                                    // Update favorite data if user has favorited this truck
                                                    if (isFavorite) {
                                                        val userId = auth.currentUser?.uid
                                                        if (userId != null) {
                                                            val organizationName = foodTruck?.organization
                                                            if (!organizationName.isNullOrBlank()) {
                                                                val favoriteQuery = db.collection("users")
                                                                    .document(userId)
                                                                    .collection("favorites")
                                                                    .whereEqualTo("organization", organizationName)
                                                                    .get()
                                                                    .await()

                                                                favoriteQuery.documents.firstOrNull()?.reference?.update(
                                                                    mapOf(
                                                                        "reviewsCount" to updatedReviews.size,
                                                                        "averageRating" to if (updatedReviews.isNotEmpty())
                                                                            updatedReviews.map { it.rating }.average() else 0.0
                                                                    )
                                                                )?.await()
                                                            }
                                                        }

                                                    }
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
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
fun HeaderSection(foodTruck: FoodTruck?, averageRating: Double, reviewCount: Int, accentColor: Color) {
    Box(modifier = Modifier.height(220.dp)) {
        Image(
            painter = rememberAsyncImagePainter(foodTruck?.restaurantImage),
            contentDescription = "Food Truck Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.1f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopEnd)
        ) {
            val isOpen = foodTruck?.status?.lowercase() == "open"
            val statusColor = if (isOpen) Color(0xFF4CAF50) else Color(0xFFF44336)

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = statusColor,
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = foodTruck?.status ?: "Unknown",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Restaurant info
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = foodTruck?.organization ?: "Loading...",
                style = MaterialTheme.typography.h5,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = foodTruck?.location ?: "Unknown location",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                // Rating stars
                for (i in 1..5) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (i <= averageRating) accentColor else Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = String.format("%.1f", averageRating),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )

                Text(
                    text = "($reviewCount reviews)",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MenuItemCard(menuItem: MenuItem, cardColor: Color, primaryColor: Color, foodTruck: FoodTruck?, cartItemCount: MutableState<Int>) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // State to track quantity in the current session
    var itemQuantityInCart by remember { mutableStateOf(0) }

    // Load initial quantity from Firebase when component is first displayed
    LaunchedEffect(userId, menuItem.id) {
        userId?.let { uid ->
            try {
                val cartItemQuery = db.collection("users")
                    .document(uid)
                    .collection("cart")
                    .whereEqualTo("itemId", menuItem.id)
                    .whereEqualTo("foodTruckId", foodTruck?.organization ?: "")
                    .get()
                    .await()

                val cartItem = cartItemQuery.documents.firstOrNull()?.toObject(CartItem::class.java)
                itemQuantityInCart = cartItem?.quantity ?: 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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
            Image(
                painter = rememberAsyncImagePainter(menuItem.imageUrl),
                contentDescription = "Menu Item Image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    menuItem.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    menuItem.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    "PKR ${String.format("%.2f", menuItem.price)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primaryColor
                )
            }

            if (itemQuantityInCart > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val newQty = itemQuantityInCart - 1
                                updateCartItemQuantity(
                                    userId,
                                    menuItem,
                                    foodTruck,
                                    newQty,
                                    db,
                                    context
                                ) { newTotalCount, updatedQty ->
                                    cartItemCount.value = newTotalCount
                                    itemQuantityInCart = updatedQty
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = "Decrease Quantity",
                            tint = primaryColor
                        )
                    }

                    Text(
                        text = itemQuantityInCart.toString(),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = primaryColor
                    )

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                val newQty = itemQuantityInCart + 1
                                updateCartItemQuantity(
                                    userId,
                                    menuItem,
                                    foodTruck,
                                    newQty,
                                    db,
                                    context
                                ) { newTotalCount, updatedQty ->
                                    cartItemCount.value = newTotalCount
                                    itemQuantityInCart = updatedQty
                                }
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Increase Quantity",
                            tint = primaryColor
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            updateCartItemQuantity(
                                userId,
                                menuItem,
                                foodTruck,
                                1,
                                db,
                                context
                            ) { newTotalCount, updatedQty ->
                                cartItemCount.value = newTotalCount
                                itemQuantityInCart = updatedQty
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = primaryColor),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add to Cart",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", color = Color.White, fontSize = 14.sp)
                }
            }

        }
    }
}

private suspend fun updateCartItemQuantity(
    userId: String?,
    menuItem: MenuItem,
    foodTruck: FoodTruck?,
    newQuantity: Int,
    db: FirebaseFirestore,
    context: android.content.Context,
    onCartCountUpdated: (Int, Int) -> Unit
) {
    if (userId == null || foodTruck == null) return

    try {
        // Query to check if item already exists in cart
        val cartItemQuery = db.collection("users")
            .document(userId)
            .collection("cart")
            .whereEqualTo("itemId", menuItem.id)
            .whereEqualTo("foodTruckId", foodTruck.organization)
            .get()
            .await()

        if (newQuantity <= 0) {
            // Remove item from cart if quantity is 0 or negative
            if (!cartItemQuery.isEmpty) {
                val documentId = cartItemQuery.documents.first().id
                db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(documentId)
                    .delete()
                    .await()

                Toast.makeText(context, "Item removed from cart", Toast.LENGTH_SHORT).show()
            }
        } else {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())

            if (cartItemQuery.isEmpty) {
                // Add new item to cart
                val cartItem = CartItem(
                    id = UUID.randomUUID().toString(),
                    foodTruckId = foodTruck.organization,
                    foodTruckName = foodTruck.organization,
                    itemId = menuItem.id,
                    name = menuItem.name,
                    price = menuItem.price,
                    imageUrl = menuItem.imageUrl,
                    quantity = newQuantity,
                    timestamp = timestamp,
                    ownerId = foodTruck.ownerId ?: ""
                )

                db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .add(cartItem)
                    .await()

                Toast.makeText(context, "Item added to cart", Toast.LENGTH_SHORT).show()
            } else {
                // Update existing item quantity
                val documentId = cartItemQuery.documents.first().id
                db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(documentId)
                    .update("quantity", newQuantity)
                    .await()

                Toast.makeText(context, "Cart updated", Toast.LENGTH_SHORT).show()
            }
        }

        // After completing the update, calculate the total cart count
        val cartSnapshot = db.collection("users")
            .document(userId)
            .collection("cart")
            .get()
            .await()

        var totalCount = 0
        cartSnapshot.documents.forEach { doc ->
            val quantity = doc.getLong("quantity")?.toInt() ?: 0
            totalCount += quantity
        }
        onCartCountUpdated(totalCount, newQuantity)

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error updating cart: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun AddReviewCard(
    newReviewText: String,
    onReviewTextChange: (String) -> Unit,
    selectedRating: Int,
    onRatingChange: (Int) -> Unit,
    cardColor: Color,
    primaryColor: Color,
    accentColor: Color,
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    foodTruck: FoodTruck?,
    coroutineScope: CoroutineScope,
    onReviewsUpdated: (List<Review>) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = cardColor,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Write a Review",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..5) {
                    IconButton(onClick = { onRatingChange(i) }) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= selectedRating) accentColor else Color.LightGray,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = newReviewText,
                onValueChange = onReviewTextChange,
                label = { Text("Share your experience") },
                placeholder = { Text("What did you think about this food truck?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                maxLines = 3
            )

            Button(
                onClick = {
                    val user = auth.currentUser
                    if (user != null && newReviewText.isNotBlank() && selectedRating > 0) {
                        coroutineScope.launch {
                            try {
                                val userDoc = db.collection("users")
                                    .document(user.uid)
                                    .get()
                                    .await()

                                val username = userDoc.getString("name") ?: "Anonymous"
                                val timestamp = SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss",
                                    Locale.getDefault()
                                ).format(Date())

                                // Add review
                                foodTruck?.let { truck ->
                                    val truckQuery = db.collection("food_truck_owners")
                                        .whereEqualTo("organization", truck.organization)
                                        .get()
                                        .await()

                                    val truckId = truckQuery.documents.firstOrNull()?.id

                                    if (truckId != null) {
                                        val review = Review(
                                            userId = user.uid,
                                            username = username,
                                            text = newReviewText,
                                            rating = selectedRating,
                                            timestamp = timestamp
                                        )

                                        db.collection("food_truck_owners")
                                            .document(truckId)
                                            .collection("reviews")
                                            .add(review)
                                            .await()
                                        val updatedReviewSnapshot = db.collection("food_truck_owners")
                                            .document(truckId)
                                            .collection("reviews")
                                            .orderBy("timestamp")
                                            .get()
                                            .await()

                                        val updatedReviews = updatedReviewSnapshot.documents.mapNotNull {
                                            it.toObject(Review::class.java)?.copy(id = it.id)
                                        }

                                        onReviewsUpdated(updatedReviews)
                                        onReviewTextChange("")
                                        onRatingChange(0)
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Submit",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Review", color = Color.White)
            }
        }
    }
}

@Composable
fun ReviewCard(
    review: Review,
    cardColor: Color,
    accentColor: Color,
    isUserReview: Boolean,
    onDeleteReview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = cardColor,
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = review.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Row(modifier = Modifier.padding(top = 4.dp)) {
                        for (i in 1..5) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i <= review.rating) accentColor else Color.LightGray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                if (isUserReview) {
                    IconButton(onClick = onDeleteReview) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.Red
                        )
                    }
                }
            }
            Text(
                text = review.text,
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(vertical = 8.dp)
            )


            Text(
                text = review.timestamp,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}