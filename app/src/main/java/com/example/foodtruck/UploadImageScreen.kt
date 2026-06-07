package com.example.foodtruck.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun UploadImageScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val ownerId = auth.currentUser?.uid ?: ""

    var restaurantImage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Fetch existing image URL on start
    LaunchedEffect(ownerId) {
        if (ownerId.isNotEmpty()) {
            try {
                val ownerDoc = db.collection("food_truck_owners").document(ownerId).get().await()
                if (ownerDoc.exists()) {
                    restaurantImage = ownerDoc.getString("restaurantImage")
                }
            } catch (e: Exception) {
                Log.e("UploadImage", "Error fetching existing image", e)
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            coroutineScope.launch {
                // Using Cloudinary instead of Firebase Storage
                val uploadedUrl = uploadToCloudinary(it, context)
                if (uploadedUrl != null) {
                    restaurantImage = uploadedUrl
                    saveImageUrlToFirestore(db, ownerId, uploadedUrl)
                    Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
                isUploading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Food Truck Image", fontSize = 18.sp, color = Color.Black) },
                backgroundColor = Color.White,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }
                }
            )
        },
        backgroundColor = Color(0xFFF8F8F8)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Upload Restaurant Image", fontSize = 20.sp, color = Color.Black)

            Spacer(modifier = Modifier.height(12.dp))

            // Display Image using Coil
            if (restaurantImage != null) {
                Image(
                    painter = rememberAsyncImagePainter(restaurantImage),
                    contentDescription = "Restaurant Image",
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.LightGray)
                )
            }

            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF303F9F)),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Upload Image", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choose Image", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isUploading) {
                CircularProgressIndicator(color = Color(0xFFFF9800))
            }
        }
    }
}

private suspend fun uploadToCloudinary(imageUri: Uri, context: android.content.Context): String? {
    return suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(imageUri)
            .unsigned("FoodTruck_upload") // Using your upload preset
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String?, resultData: Map<out Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as? String
                    continuation.resume(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload failed: ${error?.description}")
                    continuation.resume(null)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    continuation.resume(null)
                }
            }).dispatch()
    }
}

suspend fun saveImageUrlToFirestore(db: FirebaseFirestore, ownerId: String, imageUrl: String) {
    val foodTruckRef = db.collection("food_truck_owners").document(ownerId)
    try {
        foodTruckRef.set(mapOf("restaurantImage" to imageUrl), SetOptions.merge()).await()
        Log.d("Firestore", "Image URL saved successfully")
    } catch (e: Exception) {
        Log.e("Firestore", "Error saving image URL: ${e.message}")
    }
}
