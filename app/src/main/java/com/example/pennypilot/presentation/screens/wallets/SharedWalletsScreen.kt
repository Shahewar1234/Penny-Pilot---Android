package com.example.pennypilot.presentation.screens.wallets

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pennypilot.data.model.CurrencyData
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.model.SharedWallet
import com.example.pennypilot.presentation.viewmodel.SharedWalletsViewModel
import com.example.pennypilot.presentation.viewmodel.SplitBillViewModel
import java.text.NumberFormat

// --- Theme Colors from your app ---
private val WalletBackground = Color(0xFF001A0E)
private val WalletPrimaryText = Color.White
private val WalletSecondaryText = Color.LightGray
private val WalletAccentGreen = Color(0xFF61BC79)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedWalletsScreen(
    navController: NavController,
    viewModel: SharedWalletsViewModel = hiltViewModel(),
    splitBillViewModel: SplitBillViewModel = hiltViewModel()
) {
    val walletsState by viewModel.wallets.collectAsStateWithLifecycle()
    val createWalletState by viewModel.createWalletState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showCreateDialog by remember { mutableStateOf(false) }
    var showSplitBillDialog by remember { mutableStateOf(false) }
    var walletToDelete by remember { mutableStateOf<SharedWallet?>(null) }


    LaunchedEffect(createWalletState) {
        when (val result = createWalletState) {
            is Result.Success -> {
                showCreateDialog = false
                Toast.makeText(context, "Wallet created successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateWalletState()
            }
            is Result.Error -> {
                Toast.makeText(context, result.message ?: "An error occurred", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateWalletState()
            }
            else -> Unit
        }
    }

    val screenGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF004D2B), WalletBackground, Color.Black),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shared Wallets", color = WalletPrimaryText) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = WalletPrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = WalletAccentGreen,
                contentColor = WalletBackground
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Shared Wallet")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenGradient)
                .padding(paddingValues)
        ) {
            when (val state = walletsState) {
                is Result.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = WalletAccentGreen)
                }
                is Result.Success -> {
                    if (state.data.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(state.data, key = { it.id }) { wallet ->
                                SharedWalletCard(
                                    wallet = wallet,
                                    onSplitBillClick = {
                                        val currencyInfo = CurrencyData.getCountryByCode(wallet.currencyCode)
                                        val currencySymbol = currencyInfo?.currencySymbol ?: "$"

                                        splitBillViewModel.onTotalAmountChange(wallet.totalBudget.toString())
                                        splitBillViewModel.onNumberOfPeopleChange(wallet.numberOfPeople.toString())
                                        splitBillViewModel.onCurrencySymbolChange(currencySymbol)
                                        showSplitBillDialog = true
                                    },
                                    onDeleteClick = { walletToDelete = wallet }
                                )
                            }
                        }
                    }
                }
                is Result.Error -> {
                    Text(
                        text = state.message ?: "Failed to load wallets",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateWalletDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, budget, numberOfPeople ->
                viewModel.createWallet(name, budget, numberOfPeople)
            },
            isLoading = createWalletState is Result.Loading
        )
    }

    if (showSplitBillDialog) {
        SplitBillDialog(
            viewModel = splitBillViewModel,
            onDismiss = { showSplitBillDialog = false }
        )
    }

    walletToDelete?.let { wallet ->
        AlertDialog(
            onDismissRequest = { walletToDelete = null },
            title = { Text("Delete Wallet") },
            text = { Text("Are you sure you want to delete '${wallet.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteWallet(wallet.id)
                        walletToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { walletToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SplitBillDialog(
    viewModel: SplitBillViewModel,
    onDismiss: () -> Unit
) {
    val totalAmount by viewModel.totalAmount.collectAsStateWithLifecycle()
    val numberOfPeople by viewModel.numberOfPeople.collectAsStateWithLifecycle()
    val amountPerPerson by viewModel.amountPerPerson.collectAsStateWithLifecycle()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Split a Bill", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = WalletPrimaryText)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = totalAmount,
                    onValueChange = { viewModel.onTotalAmountChange(it) },
                    label = { Text("Total Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WalletPrimaryText,
                        unfocusedTextColor = WalletPrimaryText,
                        cursorColor = WalletAccentGreen,
                        focusedBorderColor = WalletAccentGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = WalletAccentGreen,
                        unfocusedLabelColor = WalletSecondaryText
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = numberOfPeople,
                    onValueChange = { viewModel.onNumberOfPeopleChange(it) },
                    label = { Text("Number of People") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WalletPrimaryText,
                        unfocusedTextColor = WalletPrimaryText,
                        cursorColor = WalletAccentGreen,
                        focusedBorderColor = WalletAccentGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = WalletAccentGreen,
                        unfocusedLabelColor = WalletSecondaryText
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text("Amount per Person", fontSize = 16.sp, color = WalletSecondaryText)
                Text(
                    text = viewModel.formatCurrency(amountPerPerson),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WalletPrimaryText
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WalletAccentGreen)
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun CreateWalletDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var numberOfPeople by remember { mutableStateOf("1") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Create New Wallet", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = WalletPrimaryText)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Wallet Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WalletPrimaryText,
                        unfocusedTextColor = WalletPrimaryText,
                        cursorColor = WalletAccentGreen,
                        focusedBorderColor = WalletAccentGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = WalletAccentGreen,
                        unfocusedLabelColor = WalletSecondaryText
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = budget,
                    onValueChange = { budget = it },
                    label = { Text("Total Budget") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WalletPrimaryText,
                        unfocusedTextColor = WalletPrimaryText,
                        cursorColor = WalletAccentGreen,
                        focusedBorderColor = WalletAccentGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = WalletAccentGreen,
                        unfocusedLabelColor = WalletSecondaryText
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = numberOfPeople,
                    onValueChange = { numberOfPeople = it },
                    label = { Text("Number of People") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WalletPrimaryText,
                        unfocusedTextColor = WalletPrimaryText,
                        cursorColor = WalletAccentGreen,
                        focusedBorderColor = WalletAccentGreen,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = WalletAccentGreen,
                        unfocusedLabelColor = WalletSecondaryText
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = WalletSecondaryText)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onConfirm(name, budget, numberOfPeople) },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WalletAccentGreen)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Create", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SharedWalletCard(
    wallet: SharedWallet,
    onSplitBillClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val currencyInfo = remember(wallet.currencyCode) {
        CurrencyData.getCountryByCode(wallet.currencyCode)
    }
    val currencySymbol = currencyInfo?.currencySymbol ?: "$"

    val numberFormat = remember {
        NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    val remainingFormatted = "$currencySymbol${numberFormat.format(wallet.totalBudget - wallet.amountSpent)}"
    val totalFormatted = "$currencySymbol${numberFormat.format(wallet.totalBudget)}"

    val progress = if (wallet.totalBudget > 0) (wallet.amountSpent / wallet.totalBudget).toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "budgetProgress")

    val progressColor = if (progress > 0.9f) Color(0xFFCF6679) else WalletAccentGreen

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        onClick = { /* TODO: Navigate to wallet details screen */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // --- Row 1: Wallet Name and Member Avatars ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = wallet.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = WalletPrimaryText
                )
                PeopleIcons(count = wallet.numberOfPeople)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Row 2: Amount Remaining ---
            Text(
                text = "$remainingFormatted Left",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WalletPrimaryText
            )
            Text(
                text = "out of $totalFormatted",
                fontSize = 14.sp,
                color = WalletSecondaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Row 3: Visual Progress Bar ---
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = progressColor,
                trackColor = Color.Gray.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Row 4: Action Buttons ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onSplitBillClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WalletAccentGreen),
                    border = BorderStroke(1.dp, WalletAccentGreen)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Group, contentDescription = "Split Bill")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Split Bill")
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Wallet", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
private fun PeopleIcons(count: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy((-10).dp), // Negative spacing for overlap
        verticalAlignment = Alignment.CenterVertically
    ) {
        val maxIconsToShow = 3
        val iconsToShow = count.coerceAtMost(maxIconsToShow)

        repeat(iconsToShow) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Member",
                tint = WalletSecondaryText,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray.copy(alpha = 0.5f))
                    .border(2.dp, WalletBackground, CircleShape)
                    .padding(4.dp)
            )
        }

        if (count > maxIconsToShow) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(WalletAccentGreen.copy(alpha = 0.3f))
                    .border(2.dp, WalletBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+${count - maxIconsToShow}",
                    color = WalletPrimaryText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "No Wallets",
                tint = WalletAccentGreen,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Shared Wallets",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = WalletPrimaryText
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Create a new wallet to start collaborating on budgets with friends or family.",
                color = WalletSecondaryText,
                textAlign = TextAlign.Center
            )
        }
    }
}
