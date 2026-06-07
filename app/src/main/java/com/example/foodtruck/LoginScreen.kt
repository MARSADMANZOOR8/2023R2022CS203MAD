package com.example.foodtruck.ui

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.foodtruck.R
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.foodtruck.ui.theme.PrimaryBlue
import com.example.foodtruck.ui.theme.SecondaryBlue

private val BackgroundColor = Color(0xFFF8FCFF)
private val DarkText = Color(0xFF333333)
private val LightText = Color(0xFF757575)

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var showRoleDialog by remember { mutableStateOf(false) }
    var pendingGoogleUser by remember { mutableStateOf<FirebaseUser?>(null) }

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.idToken?.let { idToken ->
                        isLoading = true
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        coroutineScope.launch {
                            try {
                                val authResult = auth.signInWithCredential(credential).await()
                                val firebaseUser = authResult.user
                                if (firebaseUser != null) {
                                    val ownerDoc = db.collection("food_truck_owners")
                                        .document(firebaseUser.uid)
                                        .get()
                                        .await()

                                    if (ownerDoc.exists()) {
                                        isLoading = false
                                        Toast.makeText(context, "Welcome, Food Truck Owner!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("owner_dashboard") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } else {
                                        val userDoc = db.collection("users")
                                            .document(firebaseUser.uid)
                                            .get()
                                            .await()

                                        isLoading = false
                                        if (userDoc.exists()) {
                                            Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            pendingGoogleUser = firebaseUser
                                            showRoleDialog = true
                                        }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Google Sign-In failed!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                Toast.makeText(context, "Authentication failed: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("GoogleSignIn", "Firebase Auth error", e)
                            }
                        }
                    }
                } catch (e: ApiException) {
                    isLoading = false
                    Toast.makeText(context, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("GoogleSignIn", "Google API error", e)
                }
            }
        }
    )

    if (showRoleDialog && pendingGoogleUser != null) {
        AlertDialog(
            onDismissRequest = {
                auth.signOut()
                showRoleDialog = false
                pendingGoogleUser = null
            },
            title = {
                Text(
                    text = "Complete Your Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = DarkText
                )
            },
            text = {
                Text(
                    text = "Please select how you would like to register your account:",
                    fontSize = 16.sp,
                    color = LightText
                )
            },
            buttons = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            val user = pendingGoogleUser
                            if (user != null) {
                                isLoading = true
                                showRoleDialog = false
                                val userData = hashMapOf(
                                    "name" to (user.displayName ?: "Google User"),
                                    "email" to (user.email ?: ""),
                                    "uid" to user.uid
                                )
                                coroutineScope.launch {
                                    try {
                                        db.collection("users").document(user.uid).set(userData).await()
                                        isLoading = false
                                        pendingGoogleUser = null
                                        Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        Toast.makeText(context, "Failed to save user profile: ${e.message}", Toast.LENGTH_LONG).show()
                                        auth.signOut()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Register as Customer", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            showRoleDialog = false
                            pendingGoogleUser = null
                            navController.navigate("food_truck_signup")
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = SecondaryBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Register Food Truck Owner", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            backgroundColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    val gradient = Brush.linearGradient(
        colors = listOf(BackgroundColor, Color.White),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(bottomStart = 80.dp, bottomEnd = 80.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryBlue, SecondaryBlue),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.food_icon),
                    contentDescription = "Food Truck Logo",
                    modifier = Modifier.size(70.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .padding(top = 100.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Text(
                        text = "Sign in to continue",
                        fontSize = 16.sp,
                        color = LightText,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Email Icon",
                                tint = PrimaryBlue
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = LightText.copy(alpha = 0.3f),
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Password Icon",
                                tint = PrimaryBlue
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image =
                                if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = image,
                                    contentDescription = "Toggle Password Visibility",
                                    tint = PrimaryBlue
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = LightText.copy(alpha = 0.3f),
                            cursorColor = PrimaryBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = { navController.navigate("forgot") }) {
                            Text("Forgot Password?", color = PrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(context, "Please enter email and password!", Toast.LENGTH_SHORT).show()
                            } else {
                                isLoading = true
                                coroutineScope.launch {
                                    try {
                                        val result =
                                            auth.signInWithEmailAndPassword(email, password).await()
                                        val userId = result.user?.uid ?: ""

                                        val ownerDoc = db.collection("food_truck_owners")
                                            .document(userId)
                                            .get()
                                            .await()

                                        isLoading = false

                                        if (ownerDoc.exists()) {
                                            Toast.makeText(context, "Welcome, Food Truck Owner!", Toast.LENGTH_SHORT)
                                                .show()
                                            navController.navigate("owner_dashboard") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        } else {
                                            Toast.makeText(context, "Welcome!", Toast.LENGTH_SHORT).show()
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }

                                    } catch (e: Exception) {
                                        isLoading = false
                                        Toast.makeText(context, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("LoginError", e.message ?: "Unknown error")
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        AnimatedVisibility(
                            visible = !isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = "LOGIN",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        AnimatedVisibility(
                            visible = isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = LightText.copy(alpha = 0.3f))
                        Text(
                            text = " OR ",
                            color = LightText,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Divider(modifier = Modifier.weight(1f), color = LightText.copy(alpha = 0.3f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            googleSignInClient.signOut()
                            val signInIntent = googleSignInClient.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = ButtonDefaults.outlinedBorder.copy(
                            brush = Brush.linearGradient(listOf(PrimaryBlue, SecondaryBlue))
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(backgroundColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google),
                                contentDescription = "Google Logo",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Sign in with Google",
                                color = DarkText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Don't have an account?", color = LightText, fontSize = 16.sp)

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { navController.navigate("signup") },
                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Sign Up as Customer", color = Color.White, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { navController.navigate("food_truck_signup") },
                    colors = ButtonDefaults.buttonColors(backgroundColor = SecondaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Register Your Food Truck", color = Color.White, fontSize = 16.sp)
                }
            }
        }
    }
}
