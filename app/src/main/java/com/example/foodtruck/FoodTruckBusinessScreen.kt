package com.example.foodtruck.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodtruck.R

@Composable
fun FoodTruckBusinessScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food Truck for Business", fontSize = 20.sp, color = Color.White) },
                backgroundColor = Color(0xFF969BD0),
                elevation = 4.dp,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                }
            )
        },
        backgroundColor = Color(0xFFE3F2FD)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFE3F2FD))
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            BusinessOptionCard(
                title = "Register Your Food Truck",
                description = "Expand your business and reach more customers.",
                imageRes = R.drawable.food_icon,
                onClick = { navController.navigate("food_truck_signup") }
            )

            BusinessOptionCard(
                title = "Get Verified",
                description = "Become a verified partner to gain customer trust.",
                imageRes = R.drawable.verification_icon,
                onClick = { /* Future Verification Process */ }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Why Sign Up?",
                fontSize = 22.sp,
                color = Color(0xFF5960AB),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
            )

            WhySignUpItem(
                title = "More Customers",
                description = "Increase your sales and attract more food lovers.",
                imageRes = R.drawable.customers_icon
            )

            WhySignUpItem(
                title = "Easy Management",
                description = "Manage orders, payments, and menu with ease.",
                imageRes = R.drawable.management_icon
            )

            WhySignUpItem(
                title = "Exclusive Support",
                description = "Get dedicated support for your food truck business.",
                imageRes = R.drawable.support_icon
            )
        }
    }
}

@Composable
fun BusinessOptionCard(title: String, description: String, imageRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = 6.dp,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0277BD))
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun WhySignUpItem(title: String, description: String, imageRes: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = title,
            modifier = Modifier.size(54.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF0288D1))
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 14.sp, color = Color.Gray)
        }
    }
}
