import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndPoliciesScreen(navController: NavController) {
    val scrollState = rememberScrollState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Terms of Service", "Privacy Policy", "Refund Policy")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF5960AB)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Gradient Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF5960AB),
                                        Color(0xFF969BD0)
                                    )
                                )
                            )
                    ) {
                        // Back Button
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                                .size(36.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(50)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Title
                        Text(
                            text = "Terms & Policies",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.White,
                        contentColor = Color(0xFF1E88E5),
                        edgePadding = 16.dp,
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                height = 3.dp,
                                color = Color(0xFF1E88E5)
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 14.sp,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // Content based on selected tab
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(scrollState)
                    ) {
                        when (selectedTabIndex) {
                            0 -> TermsOfServiceContent()
                            1 -> PrivacyPolicyContent()
                            2 -> RefundPolicyContent()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TermsOfServiceContent() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Terms of Service",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1E88E5)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Last Updated: April 13, 2025",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome to the Food Truck App! These Terms of Service govern your use of our application and services. By accessing or using our application, you agree to be bound by these Terms.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "1. User Accounts",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You must create an account to use certain features of our app. You are responsible for maintaining the confidentiality of your account information and for all activities that occur under your account.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "2. Ordering and Payments",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "When placing an order through our app, you agree to pay the specified price for the items ordered. Payment processing is handled by our secure payment partners.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "3. User Conduct",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You agree not to use our app for any unlawful purpose or in any way that interrupts, damages, or impairs the service. You must not attempt to gain unauthorized access to any part of the app.",
            fontSize = 14.sp
        )

        // Add more content as needed
    }
}

@Composable
fun PrivacyPolicyContent() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Privacy Policy",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1E88E5)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Last Updated: April 13, 2025",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "The Food Truck App is committed to protecting your privacy. This Privacy Policy explains how we collect, use, and disclose information about you when you use our app and services.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "1. Information We Collect",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We collect information you provide directly to us, including your name, email address, phone number, location, and payment information when you create an account or place an order.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "2. How We Use Your Information",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We use the information we collect to process your orders, personalize your experience, send you marketing communications (if you opt-in), and improve our services.",
            fontSize = 14.sp
        )

        // Add more content as needed
    }
}

@Composable
fun RefundPolicyContent() {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Refund Policy",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF1E88E5)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Last Updated: April 13, 2025",
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "We strive to provide the best food and service. However, if you're not satisfied with your order, please read our refund policy below.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "1. Eligible Orders",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Refunds are eligible for orders that are incorrect, incomplete, or of unacceptable quality. Refund requests must be submitted within 24 hours of receiving your order.",
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "2. Refund Process",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To request a refund, please contact our customer support team through the Help Center in the app. Please provide your order number and details about the issue with your order.",
            fontSize = 14.sp
        )
    }
}