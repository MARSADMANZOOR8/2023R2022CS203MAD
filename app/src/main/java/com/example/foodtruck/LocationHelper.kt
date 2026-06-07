package com.example.foodtruck.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import java.util.*

@Composable
fun rememberUserLocation(context: Context): String {
    var locationName by remember { mutableStateOf("Fetching location...") }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(context) { location ->
                locationName = location ?: "Unknown location"
            }
        } else {
            locationName = "Permission Denied"
        }
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(permission)
        } else {
            getCurrentLocation(context) { location ->
                locationName = location ?: "Unknown location"
            }
        }
    }

    return locationName
}

@SuppressLint("MissingPermission")
fun getCurrentLocation(context: Context, onLocationReceived: (String?) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        onLocationReceived("GPS is off")
        return
    }

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                val address = getAddressFromLocation(context, location)
                onLocationReceived(address)
            } else {
                onLocationReceived("Location not available")
            }
        }
        .addOnFailureListener {
            onLocationReceived("Failed to get location")
        }
}

fun getAddressFromLocation(context: Context, location: Location): String {
    val geocoder = Geocoder(context, Locale.getDefault())
    return try {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            "${address.locality}, ${address.countryName}"
        } else {
            "Unknown location"
        }
    } catch (e: Exception) {
        Log.e("LocationHelper", "Geocoder failed: ${e.message}")
        "Unknown location"
    }
}
