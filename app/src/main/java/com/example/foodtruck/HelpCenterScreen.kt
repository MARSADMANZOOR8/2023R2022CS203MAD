import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FA)
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
                                        Color(0xFF969BD0),
                                        Color(0xFF5960AB)
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
                            text = "Help Center",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Main Content - FAQ Categories
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        item {
                            Text(
                                text = "Frequently Asked Questions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E88E5),
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }

                        items(faqCategories) { category ->
                            FAQCategoryItem(category)
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Need more help?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF1E88E5),
                                modifier = Modifier.padding(vertical = 16.dp)
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate("contact_support") }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                color = Color(0xFF1E88E5),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubble,
                                            contentDescription = "Contact Support",
                                            tint = Color.White
                                        )
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 16.dp)
                                    ) {
                                        Text(
                                            text = "Contact Support",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            text = "We typically reply within 24 hours",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Go to Contact Support",
                                        tint = Color(0xFF1E88E5)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data model for FAQ items
data class FAQItem(
    val question: String,
    val answer: String
)

// Data model for FAQ categories
data class FAQCategory(
    val title: String,
    val icon: ImageVector,
    val faqs: List<FAQItem>
)

// Sample FAQ data
val faqCategories = listOf(
    FAQCategory(
        title = "Account & Profile",
        icon = Icons.Default.Person,
        faqs = listOf(
            FAQItem(
                question = "How do I create an account?",
                answer = "To create an account, download our app and tap on 'Sign Up' on the login screen. You can register using your email, phone number, or social media accounts."
            ),
            FAQItem(
                question = "How do I reset my password?",
                answer = "Tap on 'Forgot Password' on the login screen and follow the instructions to reset your password. We'll send a reset link to your registered email address."
            )
        )
    ),
    FAQCategory(
        title = "Ordering",
        icon = Icons.Default.ShoppingCart,
        faqs = listOf(
            FAQItem(
                question = "How do I place an order?",
                answer = "Browse food trucks near you, select items you want to order, add them to your cart, and proceed to checkout. You can pay using various payment methods available in the app."
            ),
            FAQItem(
                question = "Can I schedule an order for later?",
                answer = "Yes, you can schedule orders for later pickup. During checkout, select 'Schedule for Later' and choose your preferred pickup time."
            )
        )
    ),
    FAQCategory(
        title = "Payment",
        icon = Icons.Default.CreditCard,
        faqs = listOf(
            FAQItem(
                question = "What payment methods are accepted?",
                answer = "We accept credit/debit cards, mobile wallets, and in-app credit. All payments are processed securely through our payment partners."
            ),
            FAQItem(
                question = "Is my payment information secure?",
                answer = "Yes, we use industry-standard encryption to protect your payment information. We do not store your full card details on our servers."
            )
        )
    ),
    FAQCategory(
        title = "Tracking & Pickup",
        icon = Icons.Default.LocationOn,
        faqs = listOf(
            FAQItem(
                question = "How do I track my order status?",
                answer = "After placing an order, you can track its status in the 'Orders' section. You'll receive notifications when your order is being prepared and when it's ready for pickup."
            ),
            FAQItem(
                question = "What if I'm running late for pickup?",
                answer = "If you're running late, you can contact the food truck directly through the app. Most food trucks will hold your order for up to 15 minutes after the scheduled pickup time."
            )
        )
    )
)

@Composable
fun FAQCategoryItem(category: FAQCategory) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Category Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Color(0xFF1E88E5).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.title,
                        tint = Color(0xFF1E88E5)
                    )
                }

                Text(
                    text = category.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp)
                )

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            // FAQs List
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = if (expanded) 16.dp else 0.dp
                    )
                ) {
                    category.faqs.forEach { faq ->
                        FAQItem(faq)
                    }
                }
            }
        }
    }
}

@Composable
fun FAQItem(faq: FAQItem) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F7FA)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Question
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationState),
                    tint = Color(0xFF1E88E5)
                )
            }

            // Answer
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 12.dp
                    )
                )
            }
        }
    }
}