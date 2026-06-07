package com.example.foodtruck.ui

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import com.example.foodtruck.ui.theme.IndigoPrimary
import com.example.foodtruck.ui.theme.IndigoLight
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class MenuItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "Main",
    val available: Boolean = true,
    val tags: List<String> = emptyList()
)


@Composable
fun ManageMenuScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val ownerId = auth.currentUser?.uid ?: ""
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var menuItems by remember { mutableStateOf<List<MenuItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf<MenuItem?>(null) }

    // Load menu items from Firestore
    LaunchedEffect(ownerId) {
        if (ownerId.isNotEmpty()) {
            try {
                val menuSnapshot = db.collection("food_truck_owners")
                    .document(ownerId)
                    .collection("menu_items")
                    .get()
                    .await()

                menuItems = menuSnapshot.documents.mapNotNull { doc ->
                    val item = doc.toObject(MenuItem::class.java)
                    item?.copy(id = doc.id)
                }
            } catch (e: Exception) {
                Log.e("ManageMenu", "Error loading menu items", e)
                Toast.makeText(context, "Failed to load menu items", Toast.LENGTH_SHORT).show()
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Manage Menu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                backgroundColor = IndigoPrimary,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        selectedMenuItem = null
                        showAddDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Item",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            IndigoBackground,
                            Color.White
                        )
                    )
                )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = IndigoPrimary
                )
            } else if (menuItems.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestaurantMenu,
                        contentDescription = "Empty Menu",
                        tint = IndigoPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Your menu is empty",
                        fontSize = 20.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            selectedMenuItem = null
                            showAddDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = IndigoPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add First Menu Item",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(menuItems) { item ->
                        MenuItemCard(
                            menuItem = item,
                            onEdit = {
                                selectedMenuItem = item
                                showAddDialog = true
                            },
                            onDelete = { deleteMenuItem(item.id, db, ownerId, context) {
                                menuItems = menuItems.filter { it.id != item.id }
                            }}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Floating action button to add new menu item
            FloatingActionButton(
                onClick = {
                    selectedMenuItem = null
                    showAddDialog = true
                },
                backgroundColor = IndigoPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Menu Item")
            }
        }

        // Add/Edit Menu Item Dialog
        if (showAddDialog) {
            AddEditMenuItemDialog(
                menuItem = selectedMenuItem,
                onDismiss = { showAddDialog = false },
                onSave = { newItem, imageUri ->
                    coroutineScope.launch {
                        saveMenuItem(newItem, imageUri, db, auth, context) { savedItem ->
                            if (selectedMenuItem != null) {
                                // Update existing item
                                menuItems = menuItems.map { if (it.id == savedItem.id) savedItem else it }
                            } else {
                                // Add new item
                                menuItems = menuItems + savedItem
                            }
                            showAddDialog = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun Chip(
    text: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(IndigoLight.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = IndigoPrimary
        )

        Spacer(modifier = Modifier.width(4.dp))

        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = "Remove Tag",
            tint = IndigoPrimary,
            modifier = Modifier
                .size(16.dp)
                .clickable { onDelete() }
        )
    }
}

@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = 4.dp,
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Item image
                if (menuItem.imageUrl.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(menuItem.imageUrl),
                        contentDescription = menuItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(IndigoLight.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Restaurant,
                            contentDescription = "No Image",
                            tint = IndigoPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Item details
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = menuItem.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Text(
                        text = menuItem.description,
                        fontSize = 14.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PKR ${menuItem.price}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = IndigoPrimary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (menuItem.available) {
                            Card(
                                backgroundColor = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(12.dp),
                                elevation = 0.dp
                            ) {
                                Text(
                                    text = "Available",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Card(
                                backgroundColor = Color(0xFFFFEBEE),
                                shape = RoundedCornerShape(12.dp),
                                elevation = 0.dp
                            ) {
                                Text(
                                    text = "Unavailable",
                                    color = Color(0xFFF44336),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IndigoLight.copy(alpha = 0.1f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Display tags if available
            if (menuItem.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocalOffer,
                        contentDescription = "Tags",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Tags:",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = menuItem.tags.joinToString(", "),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditMenuItemDialog(
    menuItem: MenuItem?,
    onDismiss: () -> Unit,
    onSave: (MenuItem, Uri?) -> Unit
) {
    var name by remember { mutableStateOf(menuItem?.name ?: "") }
    var description by remember { mutableStateOf(menuItem?.description ?: "") }
    var price by remember { mutableStateOf(menuItem?.price?.toString() ?: "") }
    var category by remember { mutableStateOf(menuItem?.category ?: "Main") }
    var available by remember { mutableStateOf(menuItem?.available ?: true) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val existingImageUrl = menuItem?.imageUrl ?: ""

    // Add tag-related state
    var currentTag by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf<MutableList<String>>(menuItem?.tags?.toMutableList() ?: mutableListOf()) }
    var tagError by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
        }
    }

    val categories = listOf("Main", "Appetizer", "Dessert", "Beverage", "Special")

    // Function to add a tag
    fun addTag() {
        if (currentTag.isBlank()) {
            tagError = "Tag cannot be empty"
            return
        }

        val trimmedTag = currentTag.trim()

        if (tags.contains(trimmedTag)) {
            tagError = "Tag already exists"
            return
        }

        if (tags.size >= 6) {
            tagError = "Maximum 6 tags allowed"
            return
        }

        tags.add(trimmedTag)
        currentTag = ""
        tagError = ""
    }

    // Function to remove a tag
    fun removeTag(tag: String) {
        tags.remove(tag)
        tagError = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (menuItem == null) "Add Menu Item" else "Edit Menu Item",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = IndigoPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp) // Increased height to accommodate tags
            ) {
                item {
                    // Image selection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray)
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Selected Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (existingImageUrl.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(existingImageUrl),
                                contentDescription = "Existing Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AddPhotoAlternate,
                                    contentDescription = "Add Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Tap to add an image",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Item Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Price field
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Price (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category dropdown
                    Text(
                        "Category",
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(12.dp)
                    ) {
                        var expanded by remember { mutableStateOf(false) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = true },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category)
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categories.forEach { option ->
                                DropdownMenuItem(onClick = {
                                    category = option
                                    expanded = false
                                }) {
                                    Text(text = option)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Tags section
                    Text(
                        "Tags (min 3, max 6)",
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tag input field with add button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = currentTag,
                            onValueChange = { currentTag = it },
                            label = { Text("Add tag") },
                            singleLine = true,
                            isError = tagError.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = { addTag() },
                            modifier = Modifier
                                .size(48.dp)
                                .background(IndigoPrimary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Tag",
                                tint = Color.White
                            )
                        }
                    }

                    // Tag error message
                    if (tagError.isNotEmpty()) {
                        Text(
                            text = tagError,
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Display tags
                    if (tags.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FlowRow(
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp
                            ) {
                                tags.forEach { tag ->
                                    Chip(
                                        text = tag,
                                        onDelete = { removeTag(tag) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Availability toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Item Availability",
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )

                        Switch(
                            checked = available,
                            onCheckedChange = { available = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = IndigoPrimary,
                                checkedTrackColor = IndigoLight
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate and create item
                    if (name.isBlank()) {
                        // Show error
                        return@Button
                    }

                    // Check if minimum tags requirement is met
                    if (tags.size < 3) {
                        tagError = "At least 3 tags are required"
                        return@Button
                    }

                    val priceValue = price.toDoubleOrNull() ?: 0.0
                    val newItem = MenuItem(
                        id = menuItem?.id ?: UUID.randomUUID().toString(),
                        name = name,
                        description = description,
                        price = priceValue,
                        imageUrl = menuItem?.imageUrl ?: "",
                        category = category,
                        available = available,
                        tags = tags.toList() // Convert tags to immutable list
                    )

                    onSave(newItem, imageUri)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = IndigoPrimary)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        },
        backgroundColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: Dp = 0.dp,
    crossAxisSpacing: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        // Measure and categorize the children
        measurables.forEach { measurable ->
            // Measure the child
            val placeable = measurable.measure(constraints)

            // Check if this child can be placed in the current sequence
            val wouldExceedMaxWidth = currentMainAxisSize > 0 &&
                    currentMainAxisSize + mainAxisSpacing.roundToPx() + placeable.width > constraints.maxWidth

            if (wouldExceedMaxWidth) {
                // This child starts a new sequence
                sequences += currentSequence.toList()
                crossAxisSizes += currentCrossAxisSize
                crossAxisPositions += crossAxisSpace

                crossAxisSpace += currentCrossAxisSize + crossAxisSpacing.roundToPx()
                currentSequence.clear()
                currentMainAxisSize = 0
                currentCrossAxisSize = 0
            }

            // Add the child to the current sequence
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width + if (currentMainAxisSize > 0) mainAxisSpacing.roundToPx() else 0
            currentCrossAxisSize = maxOf(currentCrossAxisSize, placeable.height)
            mainAxisSpace = maxOf(mainAxisSpace, currentMainAxisSize)
        }

        // Add the last sequence
        if (currentSequence.isNotEmpty()) {
            sequences += currentSequence.toList()
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace
            crossAxisSpace += currentCrossAxisSize
        }

        // Set the size of the layout
        val width = mainAxisSpace.coerceAtMost(constraints.maxWidth)
        val height = crossAxisSpace.coerceAtMost(constraints.maxHeight)

        layout(width, height) {
            // Position the children
            sequences.forEachIndexed { i, sequence ->
                var mainAxisPosition = 0

                sequence.forEach { placeable ->
                    placeable.placeRelative(
                        x = mainAxisPosition,
                        y = crossAxisPositions[i]
                    )
                    mainAxisPosition += placeable.width + mainAxisSpacing.roundToPx()
                }
            }
        }
    }
}



private fun deleteMenuItem(
    itemId: String,
    db: FirebaseFirestore,
    ownerId: String,
    context: android.content.Context,
    onComplete: () -> Unit
) {
    db.collection("food_truck_owners")
        .document(ownerId)
        .collection("menu_items")
        .document(itemId)
        .delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show()
            onComplete()
        }
        .addOnFailureListener { e ->
            Toast.makeText(context, "Failed to delete item: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}

private suspend fun saveMenuItem(
    menuItem: MenuItem,
    imageUri: Uri?,
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    context: android.content.Context,
    onComplete: (MenuItem) -> Unit
) {
    val ownerId = auth.currentUser?.uid ?: return

    try {
        // Upload image if provided
        var finalItem = menuItem
        if (imageUri != null) {
            // Using Cloudinary for image uploads
            val imageUrl = uploadToCloudinary(imageUri, context)
            finalItem = menuItem.copy(imageUrl = imageUrl ?: menuItem.imageUrl)
        }

        // Save to Firestore
        val menuRef = db.collection("food_truck_owners")
            .document(ownerId)
            .collection("menu_items")
            .document(finalItem.id)

        menuRef.set(finalItem).await()

        Toast.makeText(
            context,
            if (imageUri != null) "Item saved with image" else "Item saved successfully",
            Toast.LENGTH_SHORT
        ).show()

        onComplete(finalItem)
    } catch (e: Exception) {
        Log.e("SaveMenuItem", "Error saving menu item", e)
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
