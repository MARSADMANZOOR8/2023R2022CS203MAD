package com.example.foodtruck

import HelpCenterScreen
import TermsAndPoliciesScreen
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.foodtruck.ui.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.cloudinary.android.MediaManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Cloudinary
        val config = mapOf(
            "cloud_name" to "dlpw8u642",
            "api_key" to "171991512353899",
            "api_secret" to "gHrkD2YmafG7uxTkqSwZrZgKVNg"
        )
        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Already initialized
        }

        setContent {
            val navController = rememberNavController()
            val auth = FirebaseAuth.getInstance()
            val db = FirebaseFirestore.getInstance()

            // Navigation graph with fixed startDestination
            NavHost(navController = navController, startDestination = "loading") {
                // Temporary loading screen while checking user status
                composable("loading") {
                    LoadingScreen()
                }

                composable(
                    route = "category_results/{category}",
                    arguments = listOf(navArgument("category") { type = NavType.StringType })
                ) { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    CategoryResultsScreen(
                        navController = navController,
                        category = category
                    )
                }
                // Define all screens here
                composable("signup") { SignupScreen(navController) }
                composable("login") { LoginScreen(navController) }
                composable("home") {
                    HomeScreen(
                        navController = navController,
                        onShowNearbyFoodTrucks = {
                            navController.navigate("map")
                        }
                    )
                }
                composable("all_trucks") { AllFoodTrucksScreen(navController) }
                composable("cart") { CartScreen(navController) }
                composable("checkout") { CheckoutScreen(navController) }
                composable("favorites") { FavoritesScreen(navController) }
                composable("account") { AccountScreen(navController) }
                composable("owner_dashboard") { OwnerDashboardScreen(navController) }
                composable("owner_orders") { OwnerOrdersScreen(navController) }
                composable("food_truck_business") { FoodTruckBusinessScreen(navController) }
                composable("food_truck_signup") { FoodTruckSignupScreen(navController) }
                composable("map") { MapScreen(navController) }
                composable("edit_food_truck") { EditFoodTruckScreen(navController) }
                composable("upload_image") { UploadImageScreen(navController) }
                composable("manage_menu") { ManageMenuScreen(navController) }
                composable("orders") { OrdersScreen(navController) }
                composable("search") { SearchScreen(navController) }
                composable("term") { TermsAndPoliciesScreen(navController) }
                composable("help") { HelpCenterScreen(navController) }
                composable("forgot") { ForgotPasswordScreen(navController) }
                //composable(" contact_support") { ContactSupportButton(navController) }
                composable("contact_support") { ContactSupportManager() }

                composable("food_truck_detail/{truckId}") { backStackEntry ->
                    val truckId = backStackEntry.arguments?.getString("truckId") ?: ""
                    FoodTruckDetailScreen(navController, truckId)
                }
            }

            // Determine and navigate to the correct start screen
            LaunchedEffect(Unit) {
                val currentUser = auth.currentUser
                val destination = try {
                    if (currentUser != null) {
                        val ownerDoc = withContext(Dispatchers.IO) {
                            db.collection("food_truck_owners").document(currentUser.uid).get().await()
                        }
                        if (ownerDoc.exists()) {
                            "owner_dashboard"
                        } else {
                            val userDoc = withContext(Dispatchers.IO) {
                                db.collection("users").document(currentUser.uid).get().await()
                            }
                            if (userDoc.exists()) "home" else "login"
                        }
                    } else {
                        "login"
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error fetching user data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    "login"
                }

                // Navigate to destination and remove "loading" from back stack
                navController.navigate(destination) {
                    popUpTo("loading") { inclusive = true }
                }
            }
        }
    }
}
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
