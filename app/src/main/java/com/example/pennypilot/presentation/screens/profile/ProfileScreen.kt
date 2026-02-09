package com.example.pennypilot.presentation.screens.profile

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pennypilot.presentation.viewmodel.ProfileTabViewModel
import java.text.SimpleDateFormat
import java.util.*

// THEME: Define your app's specific brand colors for reuse.
private val PennyPilotGreen = Color(0xFF61BC79)
private val PennyPilotGreenLight = Color(0xFF80C692)
private val PennyPilotGreenDark = Color(0xFF006837)
private val PennyPilotText = Color.White
private val PennyPilotCardBackground = Color.White.copy(alpha = 0.1f)

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileTabViewModel = hiltViewModel()
) {
    val user by viewModel.userProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // --- START: NEW STATE FOR DIALOG ---
    var showSignOutDialog by remember { mutableStateOf(false) }
    // --- END: NEW STATE ---

    // THEME: Apply the same dark gradient background as the LoginScreen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PennyPilotGreenDark, Color.Black)
                )
            )
    ) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PennyPilotGreen)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: $errorMessage",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            user != null -> {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(bottom = 16.dp)
                ) {
                    ProfileHeader(
                        name = user!!.displayName,
                        email = user!!.email,
                        avatarUrl = user!!.profileImageUrl
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    UserDetailsSection(
                        currency = user!!.getCurrencySymbol(), // Using the helper function from your User model
                        balance = user!!.currentBalance,
                        lastLogin = user!!.lastLoginDate
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    SettingsSection(onSignOut = {
                        // --- MODIFIED: Show the dialog instead of signing out directly ---
                        showSignOutDialog = true
                    })
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No user profile found. Please try logging in again.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                        color = PennyPilotText.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // --- START: NEW DIALOG COMPOSABLE ---
        if (showSignOutDialog) {
            SignOutConfirmationDialog(
                onConfirm = {
                    showSignOutDialog = false
                    viewModel.signOut()
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                },
                onDismiss = {
                    showSignOutDialog = false
                }
            )
        }
        // --- END: NEW DIALOG COMPOSABLE ---
    }
}

// --- START: NEW DIALOG COMPOSABLE FUNCTION ---
@Composable
private fun SignOutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Sign Out",
                fontWeight = FontWeight.Bold,
                color = PennyPilotText
            )
        },
        text = {
            Text(
                text = "Are you sure you want to sign out?",
                color = PennyPilotText.copy(alpha = 0.8f)
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE57373), // Destructive red color
                    contentColor = Color.White
                )
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, PennyPilotGreenLight)
            ) {
                Text("Cancel", color = PennyPilotGreenLight)
            }
        },
        // THEME: Style the dialog to match the app theme
        containerColor = Color(0xFF2E2E2E),
        icon = {
            Icon(
                Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Sign Out Icon",
                tint = PennyPilotText
            )
        }
    )
}
// --- END: NEW DIALOG COMPOSABLE FUNCTION ---


@Composable
private fun ProfileHeader(name: String, email: String, avatarUrl: String?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 24.dp), // Adjusted padding for a modern look
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = avatarUrl ?: "https://www.gravatar.com/avatar/${email.hashCode()}?d=identicon"
            ),
            contentDescription = "User Avatar",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = PennyPilotText // THEME: Use white text.
        )
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = PennyPilotText.copy(alpha = 0.7f) // THEME: Use slightly transparent white for subtext.
        )
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun UserDetailsSection(currency: String, balance: Double, lastLogin: Date?) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }

    // THEME: Wrap details in a styled Card, just like the login screen.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = PennyPilotCardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "User Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PennyPilotGreenLight, // THEME: Use light green for headers.
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DetailItem(icon = Icons.Default.AttachMoney, label = "Primary Currency", value = currency)
            HorizontalDivider(color = PennyPilotText.copy(alpha = 0.2f))
            DetailItem(icon = Icons.Default.AccountBalanceWallet, label = "Current Balance", value = String.format("%.2f", balance))
            HorizontalDivider(color = PennyPilotText.copy(alpha = 0.2f))
            if (lastLogin != null) {
                DetailItem(icon = Icons.Default.History, label = "Last Seen", value = dateFormatter.format(lastLogin))
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = PennyPilotGreenLight) // THEME: Use light green for icons.
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = PennyPilotText.copy(alpha = 0.7f) // THEME: Use transparent white.
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = PennyPilotText // THEME: Use white text.
            )
        }
    }
}

@Composable
private fun SettingsSection(onSignOut: () -> Unit) {
    // THEME: Wrap settings in a styled Card.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = PennyPilotCardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PennyPilotGreenLight, // THEME: Use light green for headers.
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsItem(icon = Icons.Default.AccountCircle, title = "Edit Profile", onClick = { /* TODO */ })
            HorizontalDivider(color = PennyPilotText.copy(alpha = 0.2f))
            SettingsItem(icon = Icons.Default.Notifications, title = "Notifications", onClick = { /* TODO */ })
            HorizontalDivider(color = PennyPilotText.copy(alpha = 0.2f))
            SettingsItem(icon = Icons.Default.Security, title = "Security & Password", onClick = { /* TODO */ })
            HorizontalDivider(color = PennyPilotText.copy(alpha = 0.2f))
            SettingsItem(icon = Icons.AutoMirrored.Filled.ExitToApp, title = "Sign Out", onClick = onSignOut, isDestructive = true)
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    // THEME: Use error color for destructive actions, light green for normal icons.
    val itemColor = if (isDestructive) Color(0xFFE57373) else PennyPilotGreenLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = itemColor)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, color = if (isDestructive) itemColor else PennyPilotText, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = itemColor.copy(alpha = 0.7f))
    }
}
