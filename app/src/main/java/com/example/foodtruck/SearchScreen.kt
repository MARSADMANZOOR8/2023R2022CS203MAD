package com.example.foodtruck.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.RestaurantMenu
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import coil.compose.AsyncImage

val LightBlue = Color(0xFFBB8FCE)
val MediumBlue = Color(0xFF9B59B6)
val DarkBlue = Color(0xFF5960AB)
val VeryLightBlue = Color(0xFFE8DAEF)
val White = Color.White


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<FoodTruck>()) }
    var isSearching by remember { mutableStateOf(false) }
    var searchType by remember { mutableStateOf("organization") }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    val searchTypes = listOf(
        "Organization" to "organization",
        "City" to "city",
        "Industry" to "industry"
    )

    // Background gradient brush
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(VeryLightBlue, White),
        startY = 0f,
        endY = 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Find Food Trucks",
                            fontSize = 20.sp,
                            color = White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkBlue,
                        titleContentColor = White
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = White
                            )
                        }
                    }
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Search card container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = White),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Search Type Dropdown
                        Text(
                            "Find Your Next Meal",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = isDropdownExpanded,
                            onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = searchTypes.first { it.second == searchType }.first,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Search By", color = MediumBlue) },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isDropdownExpanded
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = VeryLightBlue,
                                    unfocusedContainerColor = VeryLightBlue,
                                    focusedIndicatorColor = MediumBlue,
                                    unfocusedIndicatorColor = LightBlue,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            ExposedDropdownMenu(
                                expanded = isDropdownExpanded,
                                onDismissRequest = { isDropdownExpanded = false },
                                modifier = Modifier.background(White)
                            ) {
                                searchTypes.forEach { (displayName, type) ->
                                    DropdownMenuItem(
                                        text = { Text(displayName) },
                                        onClick = {
                                            searchType = type
                                            isDropdownExpanded = false
                                        },
                                        leadingIcon = {
                                            when (type) {
                                                "organization" -> Icon(Icons.Filled.Business, null, tint = MediumBlue)
                                                "city" -> Icon(Icons.Filled.LocationCity, null, tint = MediumBlue)
                                                "industry" -> Icon(Icons.Rounded.RestaurantMenu, null, tint = MediumBlue)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Input
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    "Enter ${searchTypes.first { it.second == searchType }.first.lowercase()} name...",
                                    color = TextSecondary
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = "Search",
                                    tint = MediumBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = "Clear",
                                            tint = TextSecondary
                                        )
                                    }
                                }
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = VeryLightBlue,
                                unfocusedContainerColor = VeryLightBlue,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Search Button with animated background
                        Button(
                            onClick = {
                                if (searchQuery.isBlank()) {
                                    Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                isSearching = true
                                hasSearched = true
                                coroutineScope.launch {
                                    try {
                                        val query = searchQuery.trim().lowercase()
                                        val results = searchFoodTrucks(db, query, searchType)
                                        searchResults = results
                                        isSearching = false

                                        if (results.isEmpty()) {
                                            Toast.makeText(context, "No food trucks found", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SearchScreen", "Error searching food trucks", e)
                                        Toast.makeText(context, "Error searching food trucks", Toast.LENGTH_SHORT).show()
                                        isSearching = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkBlue,
                                contentColor = White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Find Food Trucks",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search Results
                if (isSearching) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = DarkBlue,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Searching for food trucks...",
                                color = TextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else if (hasSearched) {
                    AnimatedVisibility(
                        visible = searchResults.isNotEmpty(),
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut()
                    ) {
                        Column {
                            // Results header with badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    "Results",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(DarkBlue)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${searchResults.size}",
                                        color = White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(searchResults) { foodTruck ->
                                    FoodTruckSearchResultItem(foodTruck, navController)
                                }

                                // Add padding at the bottom for better scrolling experience
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }

                    // Show empty state when no results found
                    AnimatedVisibility(
                        visible = searchResults.isEmpty() && !isSearching,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Card(
                                modifier = Modifier
                                    .width(200.dp)
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.SearchOff,
                                        contentDescription = null,
                                        tint = LightBlue,
                                        modifier = Modifier.size(64.dp).alpha(0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "No Results",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Try a different search term",
                                        color = TextSecondary,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Initial state before any search - more attractive
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .padding(16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 6.dp
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                // Animated pulsating effect
                                val infiniteTransition = rememberInfiniteTransition()
                                val scale by infiniteTransition.animateFloat(
                                    initialValue = 0.95f,
                                    targetValue = 1.05f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(2000),
                                        repeatMode = RepeatMode.Reverse
                                    )
                                )

                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .scale(scale)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(LightBlue, DarkBlue)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.TravelExplore,
                                        contentDescription = null,
                                        tint = White,
                                        modifier = Modifier.size(60.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    "Discover Food Trucks",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    "Find delicious meals on wheels near you!",
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
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
fun FoodTruckSearchResultItem(foodTruck: FoodTruck, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("food_truck_detail/${foodTruck.organization}")
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Restaurant Image with GlideImage
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(LightBlue)
            ) {
                if (foodTruck.restaurantImage.isNotEmpty()) {
                    AsyncImage(
                        model = foodTruck.restaurantImage,
                        contentDescription = "Restaurant Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback when no image is available
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(LightBlue, MediumBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestaurantMenu,
                            contentDescription = null,
                            tint = White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = foodTruck.organization,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Location with custom background
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(VeryLightBlue)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationCity,
                        contentDescription = null,
                        tint = MediumBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = foodTruck.city,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Industry with styled chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LightBlue.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestaurantMenu,
                        contentDescription = null,
                        tint = DarkBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = foodTruck.industry,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = DarkBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Status indicator based on food truck status (if available)
            if (foodTruck.status.isNotEmpty()) {
                val statusColor = when (foodTruck.status.lowercase()) {
                    "open" -> Color(0xFF4CAF50)  // Green
                    "closed" -> Color(0xFFF44336)  // Red
                    "coming soon" -> Color(0xFFFF9800)  // Orange
                    else -> Color(0xFF9E9E9E)  // Gray
                }

                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "View Details",
                tint = MediumBlue
            )
        }
    }
}
suspend fun searchFoodTrucks(db: FirebaseFirestore, query: String, searchField: String = "organization"): List<FoodTruck> {
    val queryLower = query.trim().lowercase()

    return try {
        // Get all food truck documents and then filter client-side
        val snapshot = db.collection("food_truck_owners")
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            // Get the field value and convert to lowercase for comparison
            val fieldValue = doc.getString(searchField)?.lowercase() ?: ""

            // Check if the field contains the query
            if (fieldValue.contains(queryLower)) {
                FoodTruck(
                    organization = doc.getString("organization") ?: "",
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    city = doc.getString("city") ?: "",
                    industry = doc.getString("industry") ?: "",
                    location = doc.getString("location") ?: "",
                    latitude = doc.getDouble("latitude") ?: 0.0,
                    longitude = doc.getDouble("longitude") ?: 0.0,
                    id = doc.id,
                    status = doc.getString("status") ?: "",
                    restaurantImage = doc.getString("restaurantImage") ?: ""
                )
            } else null
        }
    } catch (e: Exception) {
        Log.e("SearchFoodTrucks", "Error searching food trucks", e)
        emptyList()
    }
}