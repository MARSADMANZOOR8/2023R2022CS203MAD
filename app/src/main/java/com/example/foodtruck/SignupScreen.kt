package com.example.foodtruck.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.foodtruck.ui.theme.PrimaryBlue
import com.example.foodtruck.ui.theme.SecondaryBlue

private val BackgroundColor = Color(0xFFF8FCFF)
private val DarkText = Color(0xFF333333)
private val LightText = Color(0xFF757575)

// Password strength colors
private val WeakColor = Color(0xFFE57373)  // Red
private val MediumColor = Color(0xFFFFB74D)  // Orange/Amber
private val StrongColor = Color(0xFF81C784)  // Green

// Password strength enum
enum class PasswordStrength(val color: Color, val label: String) {
    WEAK(WeakColor, "Weak"),
    MEDIUM(MediumColor, "Medium"),
    STRONG(StrongColor, "Strong")
}

@Composable
fun SignupScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    // Password strength state
    val passwordStrength by derivedStateOf {
        checkPasswordStrength(password)
    }

    // Create gradient background
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
        // Top decorative elements
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
                )
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .padding(top = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Signup card
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
                        text = "Create Account",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkText
                    )

                    Text(
                        text = "Sign up to get started",
                        fontSize = 16.sp,
                        color = LightText,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Name Field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Name Icon",
                                tint = PrimaryBlue
                            )
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

                    // Email Field
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

                    // Password Field
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
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
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

                    // Password strength indicator
                    if (password.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            // Strength bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(when (passwordStrength) {
                                            PasswordStrength.WEAK -> 0.33f
                                            PasswordStrength.MEDIUM -> 0.66f
                                            PasswordStrength.STRONG -> 1f
                                        })
                                        .height(6.dp)
                                        .background(passwordStrength.color, RoundedCornerShape(3.dp))
                                )
                            }

                            // Strength text
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = passwordStrength.label,
                                    color = passwordStrength.color,
                                    fontSize = 14.sp
                                )

                                val requirementText = when (passwordStrength) {
                                    PasswordStrength.WEAK -> "At least 8 chars with upper, lower & digits"
                                    PasswordStrength.MEDIUM -> "Add special chars & make it 10+ chars"
                                    PasswordStrength.STRONG -> "Perfect!"
                                }

                                Text(
                                    text = requirementText,
                                    color = LightText,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Signup Button
                    Button(
                        onClick = {
                            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show()
                            } else if (passwordStrength == PasswordStrength.WEAK) {
                                Toast.makeText(context, "Password too weak! Please use at least 8 characters with uppercase, lowercase, and digits.", Toast.LENGTH_LONG).show()
                            } else {
                                isLoading = true
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val userId = auth.currentUser?.uid ?: ""
                                            val userDocument = db.collection("users").document(userId)

                                            val userData = hashMapOf(
                                                "name" to name,
                                                "email" to email,
                                                "uid" to userId
                                            )

                                            userDocument.set(userData)
                                                .addOnSuccessListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "Signup successful!", Toast.LENGTH_SHORT).show()
                                                    navController.navigate("login")
                                                }
                                                .addOnFailureListener {
                                                    isLoading = false
                                                    Toast.makeText(context, "Failed to save user data!", Toast.LENGTH_SHORT).show()
                                                }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
                        // Loading indicator or text based on state
                        AnimatedVisibility(
                            visible = !isLoading,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = "SIGN UP",
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
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Login option
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already have an account?",
                    color = LightText,
                    fontSize = 16.sp
                )
                TextButton(onClick = { navController.navigate("login") }) {
                    Text(
                        text = "Login",
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// Password strength checker function
fun checkPasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.WEAK

    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    return when {
        // Strong: At least 10 characters with uppercase, lowercase, digit, and special character
        password.length >= 10 && hasUppercase && hasLowercase && hasDigit && hasSpecialChar ->
            PasswordStrength.STRONG

        // Medium: At least 8 characters with uppercase, lowercase, and digit
        password.length >= 8 && hasUppercase && hasLowercase && hasDigit ->
            PasswordStrength.MEDIUM

        // Weak: Anything else
        else -> PasswordStrength.WEAK
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewSignupScreen() {
    SignupScreen(navController = rememberNavController())
}