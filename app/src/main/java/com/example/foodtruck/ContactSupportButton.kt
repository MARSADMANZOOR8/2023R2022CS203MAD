package com.example.foodtruck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data classes for chat messages
data class ChatMessage(
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ContactSupportButton(onClick: () -> Unit) {
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
                .clickable(onClick = onClick)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatSupportPopup(
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var chatInput by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf<ChatMessage>() }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    // Add initial welcome message
    LaunchedEffect(key1 = isVisible) {
        if (isVisible && chatMessages.isEmpty()) {
            chatMessages.add(
                ChatMessage(
                    content = "Hi there! I'm your support assistant. How can I help you today?",
                    isFromUser = false
                )
            )
        }
    }

    // Auto-scroll to the latest message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Function to process user input and generate bot responses
    fun processUserInput(input: String) {
        if (input.isBlank()) return

        // Add user message
        chatMessages.add(ChatMessage(content = input, isFromUser = true))

        // Clear input field
        chatInput = ""

        // Show typing indicator
        coroutineScope.launch {
            delay(500) // Simulate thinking time

            // Generate response based on input
            val response = when {
                input.contains("refund", ignoreCase = true) ->
                    "To request a refund, please provide your order number and reason for refund. Our team will review it within 24-48 hours."

                input.contains("cancel", ignoreCase = true) || input.contains("order", ignoreCase = true) ->
                    "You can cancel an order within 5 minutes of placing it. Go to 'Orders' tab and select the order you wish to cancel."

                input.contains("payment", ignoreCase = true) || input.contains("card", ignoreCase = true) ->
                    "We accept all major credit/debit cards, mobile wallets, and in-app credit. All payments are processed securely."

                input.contains("hello", ignoreCase = true) || input.contains("hi", ignoreCase = true) ->
                    "Hello! How can I assist you with your food truck ordering experience today?"

                input.contains("thank", ignoreCase = true) ->
                    "You're welcome! Is there anything else I can help you with?"

                input.contains("human", ignoreCase = true) || input.contains("agent", ignoreCase = true) ||
                        input.contains("person", ignoreCase = true) || input.contains("representative", ignoreCase = true) ->
                    "I'll connect you with a human support agent. Please note that response times are typically within 24 hours. Would you like to continue with your current query or start a new support ticket?"

                else ->
                    "Thank you for your message. I'm still learning! For specific questions about your account or orders, please provide more details or consider reaching out to our support team directly at support@foodtruckapp.com."
            }

            // Add bot response
            chatMessages.add(ChatMessage(content = response, isFromUser = false))
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Semi-transparent overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onDismiss() }
            )

            // Chat UI container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF969BD0),
                                        Color(0xFF5960AB)
                                    )
                                )
                            )
                    ) {
                        IconButton(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Support Chat",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Chat messages
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        items(chatMessages) { message ->
                            ChatMessageItem(message)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // Input area
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = chatInput,
                                onValueChange = { chatInput = it },
                                placeholder = { Text("Type a message...") },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = Color(0xFFF5F7FA),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    processUserInput(chatInput)
                                    focusManager.clearFocus()
                                },
                                enabled = chatInput.isNotBlank(),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1E88E5),
                                    disabledContainerColor = Color.LightGray
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                        bottomEnd = if (message.isFromUser) 4.dp else 16.dp
                    )
                )
                .background(
                    if (message.isFromUser)
                        Color(0xFF1E88E5)
                    else
                        Color(0xFFEEEEEE)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.content,
                color = if (message.isFromUser) Color.White else Color.Black,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ContactSupportManager() {
    var showChatSupport by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Add the contact support button to your UI
        ContactSupportButton {
            showChatSupport = true
        }

        // The chat support popup will only be visible when showChatSupport is true
        ChatSupportPopup(
            isVisible = showChatSupport,
            onDismiss = { showChatSupport = false }
        )
    }
}