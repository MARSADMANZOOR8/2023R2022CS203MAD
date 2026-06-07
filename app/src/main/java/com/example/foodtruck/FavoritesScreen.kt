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
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.foodtruck.R
import com.example.foodtruck.ui.theme.BlueAccent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun FavoritesScreen(navController: NavController) {

    val selectedTab = "favorites"
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current

    // Add state for favorites
    var favoritesList by remember { mutableStateOf<List<FavoriteData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Firebase instances
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    // Load favorites when the screen is shown
    LaunchedEffect(Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            try {
                val favoritesSnapshot = db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .orderBy("timestamp")
                    .get()
                    .await()

                favoritesList = favoritesSnapshot.documents.mapNotNull { doc ->
                    doc.toObject(FavoriteData::class.java)
                }
                isLoading = false
            } catch (e: Exception) {
                e.printStackTrace()
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                elevation = 4.dp,
                backgroundColor = Color.White,
                contentColor = BlueAccent,
                modifier = Modifier.height(60.dp),
                title = {
                    Text(
                        text = "Favorites",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            )
        },
        bottomBar = {
            Card(
                elevation = 16.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                backgroundColor = Color.White
            ) {
                BottomNavigation(
                    backgroundColor = Color.White,
                    elevation = 0.dp,
                    modifier = Modifier.height(64.dp)
                ) {
                    BottomNavigationItem(
                        selected = selectedTab == "home",
                        onClick = {
                            try {
                                navController.navigate("home") {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                    // Avoid duplicate destinations
                                    launchSingleTop = true
                                    // Restore state when reselected
                                    restoreState = true
                                }
                            } catch (e: Exception) {
                                // Fallback for navigation issues
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home",
                                tint = if (selectedTab == "home") BlueAccent else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Home",
                                color = if (selectedTab == "home") BlueAccent else Color.Gray
                            )
                        }
                    )
                    BottomNavigationItem(
                        selected = selectedTab == "favorites",
                        onClick = {
                            // Already on this screen, no navigation needed
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Favorites",
                                tint = if (selectedTab == "favorites") BlueAccent else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Favorites",
                                color = if (selectedTab == "favorites") BlueAccent else Color.Gray
                            )
                        }
                    )
                    BottomNavigationItem(
                        selected = selectedTab == "orders",
                        onClick = {
                            try {
                                navController.navigate("orders") {
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } catch (e: Exception) {
                                // Fallback for navigation issues
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = "Orders",
                                tint = if (selectedTab == "orders") BlueAccent else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Orders",
                                color = if (selectedTab == "orders") BlueAccent else Color.Gray
                            )
                        }
                    )
                    BottomNavigationItem(
                        selected = selectedTab == "profile",
                        onClick = {
                            try {
                                navController.navigate("account") {
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } catch (e: Exception) {
                                // Fallback for navigation issues
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile",
                                tint = if (selectedTab == "profile") BlueAccent else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Profile",
                                color = if (selectedTab == "profile") BlueAccent else Color.Gray
                            )
                        }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)
        ) {
            if (isLoading) {
                // Show loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BlueAccent)
                }
            } else if (favoritesList.isEmpty()) {
                // Empty state view
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Empty state illustration
                    Image(
                        painter = painterResource(id = R.drawable.foodtruck),
                        contentDescription = "No Favorites",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .padding(bottom = 24.dp)
                    )

                    Text(
                        text = "No Favorite Food Trucks Yet",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "You haven't added any food trucks to your favorites. Explore food trucks and tap the heart icon to add them here!",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            try {
                                navController.navigate("home") {
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            } catch (e: Exception) {
                                // Fallback if navigation fails
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = BlueAccent)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Explore",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Explore Food Trucks",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Show list of favorites
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp)
                ) {
                    items(favoritesList) { favorite ->
                        FavoriteItemCard(
                            favorite = favorite,
                            navController = navController  // Pass navController to the card
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItemCard(favorite: FavoriteData, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable {
                // Navigation when the entire card is clicked
                try {
                    // Using the same route format as in FoodTruckCarousel
                    navController.navigate("food_truck_detail/${favorite.organization}")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Truck Image
            Image(
                painter = rememberAsyncImagePainter(model = favorite.restaurantImage),
                contentDescription = "Food Truck Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Food Truck Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = favorite.organization,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "Location",
                        tint = BlueAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = favorite.location,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rating stars
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (i <= favorite.averageRating) Color(0xFFFF6D00) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = String.format("%.1f", favorite.averageRating),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Text(
                        text = "(${favorite.reviewsCount})",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            IconButton(
                onClick = {
                    try {
                        navController.navigate("food_truck_detail/${favorite.organization}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View Details",
                    tint = BlueAccent
                )
            }
        }
    }
}