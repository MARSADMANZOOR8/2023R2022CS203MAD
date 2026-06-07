package com.example.foodtruck.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
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
fun FoodTruckCarousel(navController: NavController) {
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    var foodTrucks by remember { mutableStateOf<List<FoodTruck>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            foodTrucks = fetchFoodTrucks(firestore)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(foodTrucks) { truck ->
            FoodTruckCard(truck, navController)
        }
    }
}

@Composable
fun FoodTruckCard(truck: FoodTruck, navController: NavController) {
    Card(
        modifier = Modifier
            .width(220.dp)
            .height(260.dp)
            .clickable {
                navController.navigate("food_truck_detail/${truck.organization}")
            },
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF0F8FF)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = rememberAsyncImagePainter(truck.restaurantImage),
                contentDescription = "Food Truck Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )


            Spacer(modifier = Modifier.height(8.dp))


            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = truck.organization,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF2C3E50)
                )
                Text(
                    text = truck.status,
                    color = if (truck.status.lowercase() == "open") Color(0xFF2ECC71) else Color(0xFFE74C3C),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = truck.location,
                    fontSize = 12.sp,
                    color = Color(0xFF7F8C8D),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

suspend fun fetchFoodTrucks(firestore: FirebaseFirestore): List<FoodTruck> {
    return try {
        val result = firestore.collection("food_truck_owners").get().await()
        result.documents.mapNotNull { document ->
            try {
                FoodTruck(
                    organization = document.getString("organization") ?: "Unknown",
                    restaurantImage = document.getString("restaurantImage") ?: "",
                    status = document.getString("restaurant_status") ?: "Closed",
                    location = document.getString("location") ?: "Unknown"
                )
            } catch (e: Exception) {
                null
            }
        }
    } catch (e: Exception) {
        emptyList()
    }
}