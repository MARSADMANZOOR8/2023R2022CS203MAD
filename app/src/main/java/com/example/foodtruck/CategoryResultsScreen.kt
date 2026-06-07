package com.example.foodtruck.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.foodtruck.ui.theme.IndigoPrimary

@Composable
fun CategoryResultsScreen(
    navController: NavController,
    category: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()

    var foodTrucks by remember { mutableStateOf<List<FoodTruck>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Load food trucks that have items in the selected category
    LaunchedEffect(category) {
        coroutineScope.launch {
            isLoading = true
            try {
                // Get all food truck owners
                val ownersSnapshot = firestore.collection("food_truck_owners").get().await()
                val trucksWithCategory = mutableListOf<FoodTruck>()

                // For each owner, check if they have menu items in the selected category
                for (ownerDoc in ownersSnapshot.documents) {
                    val ownerId = ownerDoc.id
                    val organization = ownerDoc.getString("organization") ?: "Unknown"
                    val image = ownerDoc.getString("restaurantImage") ?: ""
                    val status = ownerDoc.getString("restaurant_status") ?: "Closed"
                    val location = ownerDoc.getString("location") ?: "Unknown"

                    // Check if owner has menu items in the selected category
                    val menuSnapshot = firestore.collection("food_truck_owners")
                        .document(ownerId)
                        .collection("menu_items")
                        .whereArrayContains("tags", category.lowercase())
                        .limit(1)
                        .get()
                        .await()

                    // If there's at least one menu item with this category, add the truck
                    if (!menuSnapshot.isEmpty) {
                        trucksWithCategory.add(
                            FoodTruck(
                                organization = organization,
                                restaurantImage = image,
                                status = status,
                                location = location
                            )
                        )
                    }
                }

                foodTrucks = trucksWithCategory
            } catch (e: Exception) {
                // Handle errors
                foodTrucks = emptyList()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$category Food Trucks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                backgroundColor = IndigoPrimary,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = IndigoPrimary
                )
            } else if (foodTrucks.isEmpty()) {
                // Show empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = "No Food Trucks",
                        tint = IndigoPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No food truck is offering $category",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Try searching for a different category",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Show food trucks list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(foodTrucks) { truck ->
                        CategoryFoodTruckCard(truck, navController)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFoodTruckCard(truck: FoodTruck, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable {
                navController.navigate("food_truck_detail/${truck.organization}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Food truck image
            Image(
                painter = rememberAsyncImagePainter(truck.restaurantImage),
                contentDescription = "Food Truck Image",
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                contentScale = ContentScale.Crop
            )

            // Food truck info
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = truck.organization,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = truck.location,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = truck.status,
                    color = if (truck.status.lowercase() == "open") Color(0xFF2ECC71) else Color(0xFFE74C3C),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

