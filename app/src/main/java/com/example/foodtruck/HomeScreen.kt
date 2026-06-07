package com.example.foodtruck.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.foodtruck.R
import com.example.foodtruck.ui.theme.*
import com.example.foodtruck.utils.rememberUserLocation
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.graphics.graphicsLayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import kotlin.math.absoluteValue
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onShowNearbyFoodTrucks: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("home") }
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val userLocation = rememberUserLocation(context)
    val scrollState = rememberScrollState()

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var userName by remember { mutableStateOf("") }

    // Hero image resources
    val heroImages = listOf(
        R.drawable.truck_1,
        R.drawable.truck_2,
        R.drawable.truck_3
    )

    // Pager state for the image carousel
    val pagerState = rememberPagerState(pageCount = { heroImages.size })

    // Auto-scrolling effect
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Change image every 3 seconds
            val nextPage = (pagerState.currentPage + 1) % heroImages.size
            pagerState.animateScrollToPage(nextPage)
        }
    }

    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.uid?.let { uid ->
            coroutineScope.launch {
                try {
                    val doc = db.collection("users").document(uid).get().await()
                    userName = doc.getString("name") ?: "User"
                } catch (e: Exception) {
                    userName = "User"
                }
            }
        }
    }

    val greeting = getGreetingMessage()

    // Animation state
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                elevation = 4.dp,
                backgroundColor = Color.White,
                contentColor = Color(0xFF5960AB),
                modifier = Modifier.height(70.dp),
                title = {
                    Column(
                        modifier = Modifier.padding(start = 4.dp)
                    ) {

                        Text(
                            text = "$greeting, $userName",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )


                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFF5960AB),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = userLocation,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate("cart")
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF5960AB))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White
                        )
                    }
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
                        selected = navController.currentDestination?.route == "home",
                        onClick = {
                            if (navController.currentDestination?.route != "home") {
                                navController.navigate("home") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Home,
                                contentDescription = "Home",
                                tint = if (navController.currentDestination?.route == "home") Color(0xFF5960AB) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Home",
                                color = if (navController.currentDestination?.route == "home") Color(0xFF5960AB) else Color.Gray
                            )
                        }
                    )

                    BottomNavigationItem(
                        selected = navController.currentDestination?.route == "favorites",
                        onClick = {
                            if (navController.currentDestination?.route != "favorites") {
                                navController.navigate("favorites") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = "Favorites",
                                tint = if (navController.currentDestination?.route == "favorites") Color(0xFF5960AB) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Favorites",
                                color = if (navController.currentDestination?.route == "favorites") Color(0xFF5960AB) else Color.Gray
                            )
                        }
                    )

                    BottomNavigationItem(
                        selected = navController.currentDestination?.route == "orders",
                        onClick = {
                            if (navController.currentDestination?.route != "orders") {
                                navController.navigate("orders") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = "Orders",
                                tint = if (navController.currentDestination?.route == "orders") Color(0xFF5960AB) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Orders",
                                color = if (navController.currentDestination?.route == "orders") Color(0xFF5960AB) else Color.Gray
                            )
                        }
                    )

                    BottomNavigationItem(
                        selected = navController.currentDestination?.route == "account",
                        onClick = {
                            if (navController.currentDestination?.route != "account") {
                                navController.navigate("account") {
                                    popUpTo("home") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile",
                                tint = if (navController.currentDestination?.route == "account") Color(0xFF5960AB) else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                "Profile",
                                color = if (navController.currentDestination?.route == "account") Color(0xFF5960AB) else Color.Gray
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
                .padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
            ) {
                // Image Carousel implementation
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                // Apply some zoom animation
                                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                                lerp(
                                    start = 0.85f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                ).also { scale ->
                                    scaleX = scale
                                    scaleY = scale
                                }
                                alpha = lerp(
                                    start = 0.5f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                            }
                    ) {
                        Image(
                            painter = painterResource(id = heroImages[page]),
                            contentDescription = "Food Truck Hero ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Page indicator dots
                Row(
                    Modifier
                        .height(20.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(heroImages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) BlueAccent else Color.LightGray
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = 25.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.navigate("search")
                        }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(50.dp)
                            .align(Alignment.Center),
                        elevation = 6.dp,
                        shape = RoundedCornerShape(28.dp),
                        backgroundColor = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Search for food trucks...",
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 125.dp, start = 16.dp)
                ){
                    AnimatedVisibility(
                        visible = animationPlayed,
                        enter = fadeIn(animationSpec = tween(1000)) +
                                slideInVertically(animationSpec = tween(1000)) { it / 2 }
                    ) {
                        Text(
                            text = "Discover Food Trucks",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    AnimatedVisibility(
                        visible = animationPlayed,
                        enter = fadeIn(animationSpec = tween(1500)) +
                                slideInVertically(animationSpec = tween(1500)) { it / 2 }
                    ) {
                        Text(
                            text = "Order delicious street food near you",
                            fontSize = 14.sp,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 230.dp)
                    .padding(bottom = 16.dp)
                    .verticalScroll(scrollState)
            ) {

                Spacer(modifier = Modifier.height(35.dp))
                Button(
                    onClick = onShowNearbyFoodTrucks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 14.dp)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF5960AB))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "Nearby",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show Nearby Food Trucks",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Popular Categories",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CategoryItem(
                        icon = R.drawable.burger,
                        title = "Burgers",
                        color = Color(0xFF5960AB),
                        onCategorySelected = { category ->
                            navController.navigate("category_results/$category")
                        }
                    )
                    CategoryItem(
                        icon = R.drawable.rolls,
                        title = "Rolls",
                        color = Color(0xFF5960AB),
                        onCategorySelected = { category ->
                            navController.navigate("category_results/$category")
                        }
                    )
                    CategoryItem(
                        icon = R.drawable.pizza,
                        title = "Pizza",
                        color = Color(0xFF5960AB),
                        onCategorySelected = { category ->
                            navController.navigate("category_results/$category")
                        }
                    )
                    CategoryItem(
                        icon = R.drawable.bbq,
                        title = "BBQ",
                        color = Color(0xFF5960AB),
                        onCategorySelected = { category ->
                            navController.navigate("category_results/$category")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(bottom = 16.dp)
                ) {
                    FoodTruckCarousel(navController)
                }
            }
        }
    }
}

// Function for linear interpolation
private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

fun getGreetingMessage(): String {
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (currentHour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        in 17..24 -> "Good Evening"
        else -> "Good Night"
    }
}

@Composable
fun CategoryItem(
    icon: Int,
    title: String,
    color: Color,
    onCategorySelected: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable { onCategorySelected(title) }
    ) {
        Card(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            backgroundColor = color.copy(alpha = 0.1f),
            elevation = 0.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}