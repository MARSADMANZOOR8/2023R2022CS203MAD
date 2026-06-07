package com.example.foodtruck.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.foodtruck.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Define theme colors
val IndigoPrimary = Color(0xFF3F51B5)
val IndigoDark = Color(0xFF303F9F)
val IndigoLight = Color(0xFF7986CB)
val IndigoExtraLight = Color(0xFFC5CAE9)
val PureWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF5F5F5)
val StatusOpen = Color(0xFF4CAF50)
val StatusClosed = Color(0xFFF44336)



@Composable
fun MapScreen(navController: NavController) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var foodTrucks by remember { mutableStateOf<List<FoodTruck>>(emptyList()) }
    var selectedTruck by remember { mutableStateOf<FoodTruck?>(null) }
    val firestore = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    // Launcher for location permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
                coroutineScope.launch {
                    foodTrucks = fetchNearbyFoodTrucks(firestore)
                }
            }
        }
    }

    // Check permission on launch
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            getUserLocation(fusedLocationClient) { location ->
                userLocation = location
                coroutineScope.launch {
                    foodTrucks = fetchNearbyFoodTrucks(firestore)
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Find Nearby Food Trucks",
                        color = PureWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                backgroundColor = IndigoPrimary,
                elevation = 8.dp,
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(OffWhite)
        ) {
            GoogleMapView(
                userLocation = userLocation,
                foodTrucks = foodTrucks
            ) { truck ->
                selectedTruck = truck
            }

            // Show info card if a truck is selected
            selectedTruck?.let {
                FoodTruckInfoCard(
                    truck = it,
                    context = context,
                    onClose = {
                        selectedTruck = null
                    },
                    onCardClick = {
                        navController.navigate("food_truck_detail/${it.organization}")
                    }
                )
            }
        }
    }
}

// -- Map composable
@Composable
fun GoogleMapView(
    userLocation: LatLng?,
    foodTrucks: List<FoodTruck>,
    onTruckClick: (FoodTruck) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(userLocation, foodTrucks) {
        if (userLocation != null) {
            val boundsBuilder = LatLngBounds.Builder()
            boundsBuilder.include(userLocation)

            foodTrucks.forEach { truck ->
                boundsBuilder.include(LatLng(truck.latitude, truck.longitude))
            }

            // Animate camera to show all markers + user's location
            cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100))
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false
        )
    ) {
        // Marker for user's location
        userLocation?.let {
            Marker(
                state = MarkerState(position = it),
                title = "You are here",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
            )
        }

        // Markers for food trucks
        foodTrucks.forEach { truck ->
            Marker(
                state = MarkerState(position = LatLng(truck.latitude, truck.longitude)),
                title = truck.organization,
                icon = BitmapDescriptorFactory.fromResource(R.drawable.food_truck),
                onClick = {
                    onTruckClick(truck)
                    true
                }
            )
        }
    }
}

// -- Info card composable (updated to be clickable)
@Composable
fun FoodTruckInfoCard(
    truck: FoodTruck,
    context: Context,
    onClose: () -> Unit,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onCardClick),  // Make the entire card clickable
        shape = RoundedCornerShape(24.dp),
        backgroundColor = PureWhite,
        elevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PureWhite, IndigoExtraLight.copy(alpha = 0.1f))
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image from Firestore URL
                val imagePainter: Painter = rememberAsyncImagePainter(
                    model = truck.restaurantImage
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(OffWhite)
                        .border(2.dp, IndigoLight, CircleShape)
                ) {
                    Image(
                        painter = imagePainter,
                        contentDescription = "Food Truck Image",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Truck info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = truck.organization,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoPrimary
                    )

                    // Display open/closed status with better styling
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (truck.status == "Open") StatusOpen.copy(alpha = 0.1f)
                                else StatusClosed.copy(alpha = 0.1f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (truck.status == "Open") StatusOpen else StatusClosed,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (truck.status == "Open") StatusOpen else StatusClosed,
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (truck.status == "Open") "Open Now" else "Closed",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (truck.status == "Open") StatusOpen else StatusClosed
                            )
                        }
                    }

                    // Added a hint to tap for more details
                    Text(
                        text = "Tap for details",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            // Close button with improved styling
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .size(32.dp)
                    .background(IndigoExtraLight, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = IndigoDark,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// -- Retrieve user's current location (if permission granted)
@SuppressLint("MissingPermission")
fun getUserLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationReceived: (LatLng) -> Unit
) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let {
            onLocationReceived(LatLng(it.latitude, it.longitude))
        }
    }
}

// -- Fetch food truck data from Firestore (updated to include location data)
suspend fun fetchNearbyFoodTrucks(firestore: FirebaseFirestore): List<FoodTruck> {
    return try {
        val result = firestore.collection("food_truck_owners").get().await()
        result.documents.mapNotNull { document ->
            try {
                val organization = document.getString("organization") ?: "Unknown"
                val latitude = document.getDouble("latitude") ?: 0.0
                val longitude = document.getDouble("longitude") ?: 0.0
                val restaurantImage = document.getString("restaurantImage") ?: ""
                val restaurantStatus = document.getString("restaurant_status") ?: "Unknown"
                val location = document.getString("location") ?: "Unknown location"

                FoodTruck(
                    organization = organization,
                    status = restaurantStatus,
                    latitude = latitude,
                    longitude = longitude,
                    restaurantImage = restaurantImage,
                    location = location
                )
            } catch (e: Exception) {
                Log.e("Firestore", "Error parsing document: ${e.message}")
                null
            }
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error fetching food truck data: ${e.message}")
        emptyList()
    }
}
