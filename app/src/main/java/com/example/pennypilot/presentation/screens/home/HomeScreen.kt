package com.example.pennypilot.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login // For "Smart Expenses" button
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Import this
import androidx.navigation.NavController
import com.example.pennypilot.R
import com.example.pennypilot.data.model.UiExpenseItem
import com.example.pennypilot.presentation.viewmodel.HomeViewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.AsyncImage
import coil.request.ImageRequest
import timber.log.Timber

// --- Theme Colors (Keep as is) ---
private val HomeBackground = Color(0xFF001A0E)
private val HomePrimaryText = Color.White
private val HomeSecondaryText = Color.LightGray
private val HomeAccentGreen = Color(0xFF61BC79)
private val HomeMutedGreen = Color(0xFF004D2B)
private val HomeCardBg = Color.White.copy(alpha = 0.08f)

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@Composable
private fun QuickCategoryChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = HomeMutedGreen),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp) // Adjusted for better visuals
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = HomePrimaryText, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(label, color = HomePrimaryText, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current // For Toasts or other context needs

    // Collect states from HomeViewModel
    val currentUserName by viewModel.userName.collectAsStateWithLifecycle()
    val currentBalanceString by viewModel.balanceString.collectAsStateWithLifecycle()
    val currentBalanceChangePercentage by viewModel.balanceChangePercentage.collectAsStateWithLifecycle()
    val recentExpenseItems by viewModel.uiRecentExpenses.collectAsStateWithLifecycle()
    val userHomeCurrency by viewModel.userHomeCurrency.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    // For Bottom Navigation
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val navItems = listOf(
        BottomNavItem("Home", Icons.Default.Home, "home"),
        BottomNavItem("Add", Icons.Default.AddCircle, "add_expense"), // Changed label for clarity
        BottomNavItem("Analytics", Icons.Default.BarChart, "analytics"),
        BottomNavItem("Profile", Icons.Default.Person, "profile")
    )

    val screenGradient = Brush.verticalGradient(
        colors = listOf(HomeMutedGreen, HomeBackground, Color.Black),
        startY = 0.0f,
        endY = Float.POSITIVE_INFINITY
    )

    // For SetBalanceDialog
    var showSetBalanceDialog by remember { mutableStateOf(false) }
    var balanceDialogInput by remember { mutableStateOf("") } // Renamed to avoid conflict
    val keyboardController = LocalSoftwareKeyboardController.current

    // Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Timber.e("HomeScreen Error: $it")
            // Ideally, show this in a Snackbar
            viewModel.clearErrorMessage() // Clear error after handling
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.logo)
                                .error(R.drawable.ic_launcher_foreground) // More specific fallback
                                .build(),
                            contentDescription = "PennyPilot Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PENNY", color = HomePrimaryText, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("PILOT", color = HomeAccentGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = HomePrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = HomeBackground.copy(alpha = 0.95f),
                tonalElevation = 0.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            if (selectedItemIndex != index) {
                                selectedItemIndex = index
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = HomeAccentGreen,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = HomeAccentGreen,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = HomeMutedGreen.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) { // Box to overlay CircularProgressIndicator
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenGradient)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Text("Hello, $currentUserName!", fontSize = 18.sp, color = HomeSecondaryText)
                Text("Welcome Back!", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = HomePrimaryText)

                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currentBalanceString, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = HomePrimaryText)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentBalanceChangePercentage, // Use dynamic value
                        fontSize = 14.sp,
                        color = if (currentBalanceChangePercentage.startsWith("-")) Color.Red.copy(alpha = 0.9f) else HomeAccentGreen,
                        modifier = Modifier
                            .background(
                                (if (currentBalanceChangePercentage.startsWith("-")) Color.Red else HomeAccentGreen).copy(
                                    alpha = 0.15f
                                ),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomeActionButton(label = "Set Balance", icon = Icons.Default.Edit, onClick = { showSetBalanceDialog = true })
                    HomeActionButton(label = "Add Expense", icon = Icons.Default.Add, onClick = { navController.navigate("add_expense") })
                    HomeActionButton(label = "Smart Sync", icon = Icons.AutoMirrored.Filled.Login, onClick = {
                        // This should navigate to a specific screen for syncing
                         navController.navigate("expenses")
                        viewModel.initiateSmartLogin() // Placeholder
                    })
                    HomeActionButton(label = "AI Assistant", icon = Icons.Default.Psychology, onClick = { navController.navigate("ai_assistant") })
                    HomeActionButton(label = "Analytics", icon = Icons.Default.BarChart, onClick = { navController.navigate("analytics") })
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text("Quick Categories", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = HomePrimaryText)
                Spacer(modifier = Modifier.height(10.dp))

                // ✅ UPDATED SECTION
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickCategoryChip("Shared Wallets", Icons.Default.Group) {
                        navController.navigate("shared_wallet") // Example for future
                    }
                    QuickCategoryChip("Travel Mode", Icons.Default.Flight) {
                        // Only navigate if the home currency has been loaded and is not empty.
                        if (userHomeCurrency.isNotBlank()) {
                            navController.navigate("travel/$userHomeCurrency")
                        } else {
                            // Optional: Show a message to the user or log a warning
                            Timber.w("Cannot navigate to Travel Mode: userHomeCurrency is not available yet.")
                            // You could show a Toast message here if you want to give user feedback.
                        }
                    }
                    QuickCategoryChip("AI Suggest", Icons.Default.AutoAwesome) { // Using a suitable icon for AI
                        // Later, this can navigate to a screen with AI-powered suggestions
                        navController.navigate("ai_assistant")
                        Timber.d("AI Suggest chip clicked")
                    }
                }

                Spacer(modifier = Modifier.height(28.dp)) // Increased spacing

                // Recent Expenses Section
                Text("Recent Expenses", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = HomePrimaryText)
                Spacer(modifier = Modifier.height(10.dp))

                if (recentExpenseItems.isEmpty() && !isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent expenses to show.", color = HomeSecondaryText)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentExpenseItems, key = { it.id }) { expense ->
                            RecentExpenseRow(expense)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Loading Indicator - centered on the screen
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = HomeAccentGreen
                )
            }
        }
    }

    if (showSetBalanceDialog) {
        SetBalanceDialog(
            currentBalanceInput = balanceDialogInput,
            onBalanceInputChange = { balanceDialogInput = it },
            onDismiss = {
                showSetBalanceDialog = false
                balanceDialogInput = "" // Reset input
            },
            onConfirm = {
                if (balanceDialogInput.isNotBlank()) {
                    viewModel.updateUserSetBalance(balanceDialogInput) // Pass string directly
                }
                showSetBalanceDialog = false
                balanceDialogInput = "" // Reset input
                keyboardController?.hide()
            }
        )
    }
}

@Composable
fun HomeActionButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    // This composable is well-defined. No changes needed.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .widthIn(min = 70.dp, max = 80.dp)
    ) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            modifier = Modifier.size(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HomeMutedGreen),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(icon, contentDescription = label, tint = HomePrimaryText, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = HomeSecondaryText, fontSize = 11.sp, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBalanceDialog(
    currentBalanceInput: String,
    onBalanceInputChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // This composable is well-defined. No changes needed.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Your Balance", color = HomePrimaryText, fontWeight = FontWeight.SemiBold) },
        text = {
            TextField(
                value = currentBalanceInput,
                onValueChange = onBalanceInputChange,
                label = { Text("Balance amount", color = HomeSecondaryText.copy(alpha = 0.7f)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = HomePrimaryText,
                    unfocusedTextColor = HomePrimaryText,
                    cursorColor = HomeAccentGreen,
                    focusedIndicatorColor = HomeAccentGreen,
                    unfocusedIndicatorColor = HomeSecondaryText.copy(alpha = 0.5f),
                    focusedContainerColor = HomeMutedGreen.copy(alpha = 0.2f),
                    unfocusedContainerColor = HomeMutedGreen.copy(alpha = 0.1f),
                    focusedLabelColor = HomeAccentGreen,
                    unfocusedLabelColor = HomeSecondaryText.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = HomeAccentGreen)) {
                Text("Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = HomeSecondaryText)) {
                Text("Cancel")
            }
        },
        containerColor = HomeBackground.copy(alpha = 0.98f),
        shape = RoundedCornerShape(16.dp)
    )
}

// This composable is also correct as it uses the shared UiExpenseItem.
@Composable
fun RecentExpenseRow(expense: UiExpenseItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HomeCardBg)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = expense.icon,
                    contentDescription = expense.category,
                    tint = HomeAccentGreen,
                    modifier = Modifier
                        .size(36.dp)
                        .background(HomePrimaryText.copy(alpha = 0.08f), CircleShape)
                        .padding(7.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(expense.description, fontWeight = FontWeight.Medium, color = HomePrimaryText, fontSize = 15.sp, maxLines = 1)
                    Text(expense.displayDate, color = HomeSecondaryText, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            //Text(expense.displayAmount, fontWeight = FontWeight.SemiBold, color = HomePrimaryText, fontSize = 15.sp)
        }
    }
}
