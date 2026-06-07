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
import com.example.foodtruck.ui.FoodTruck

@Composable
fun AllFoodTrucksScreen(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var foodTrucks by remember { mutableStateOf<List<FoodTruck>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                foodTrucks = fetchFoodTrucks(firestore)
                isLoading = false
            } catch (e: Exception) {
                error = "Failed to load food trucks: ${e.message}"
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Food Trucks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                backgroundColor = Color(0xFF5960AB),
                contentColor = Color.White
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF2C3E50)
                    )
                }
                error != null -> {
                    Text(
                        text = error ?: "Unknown error",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                foodTrucks.isEmpty() -> {
                    Text(
                        text = "No food trucks available",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(foodTrucks) { truck ->
                            FoodTruckListItem(truck, navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodTruckListItem(truck: FoodTruck, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("food_truck_detail/${truck.organization}")
            },
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Truck Image
            Image(
                painter = rememberAsyncImagePainter(truck.restaurantImage),
                contentDescription = "Food Truck Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Truck Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = truck.organization,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Status indicator with colored text
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status: ",
                        fontSize = 14.sp,
                        color = Color(0xFF7F8C8D)
                    )
                    Text(
                        text = truck.status,
                        color = if (truck.status.lowercase() == "open") Color(0xFF2ECC71) else Color(0xFFE74C3C),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = truck.location,
                    fontSize = 14.sp,
                    color = Color(0xFF7F8C8D),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}