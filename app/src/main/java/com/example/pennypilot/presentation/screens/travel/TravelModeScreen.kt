package com.example.pennypilot.presentation.screens.travel

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardTravel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pennypilot.data.model.CountryCurrency
import com.example.pennypilot.data.model.CurrencyData
import com.example.pennypilot.data.model.Trip
import com.example.pennypilot.presentation.viewmodel.TravelModeViewModel
import java.text.NumberFormat
import java.util.*

private val TravelBackground = Color(0xFF010A0F)
private val TravelPrimaryText = Color.White
private val TravelSecondaryText = Color(0xFF9E9E9E) // Softer gray
private val TravelAccentGreen = Color(0xFF20C968) // More vibrant green
private val TravelCardBg = Color.White.copy(alpha = 0.05f) // More subtle card background
private val ProgressWarning = Color(0xFFFFA726) // Orange for budget warning
private val ProgressDanger = Color(0xFFE53935) // Red for over budget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelModeScreen(
    navController: NavController,
    viewModel: TravelModeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showCreateTripSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.createTripMessage) {
        uiState.createTripMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearCreateTripMessage()
            showCreateTripSheet = false // Close sheet on success
        }
    }

    val screenGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF003D21), TravelBackground, Color.Black),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Planner", color = TravelPrimaryText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TravelPrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateTripSheet = true },
                containerColor = TravelAccentGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Plan a new Trip")
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(screenGradient)
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Text(
                    text = "My Trips",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TravelPrimaryText
                )
            }

            when {
                uiState.isLoadingTrips -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = TravelAccentGreen)
                        }
                    }
                }
                uiState.tripsErrorMessage != null -> {
                    item {
                        Text(
                            text = uiState.tripsErrorMessage!!,
                            color = ProgressDanger,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(24.dp)
                        )
                    }
                }
                uiState.trips.isEmpty() -> {
                    item { EmptyState() }
                }
                else -> {
                    items(uiState.trips, key = { it.id }) { trip ->
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        TripCard(
                            trip = trip,
                            onClick = { /* TODO: Navigate to trip details screen */ },
                            onLongPress = { showDeleteDialog = true }
                        )

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Trip") },
                                text = { Text("Are you sure you want to delete '${trip.name}'?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            viewModel.deleteTrip(trip.id)
                                            showDeleteDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ProgressDanger)
                                    ) {
                                        Text("Delete")
                                    }
                                },
                                dismissButton = {
                                    Button(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        if (showCreateTripSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCreateTripSheet = false },
                containerColor = Color(0xFF01151F),
                contentColor = TravelPrimaryText
            ) {
                CreateTripSheet(
                    onCreateTrip = viewModel::createTrip
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTripSheet(
    onCreateTrip: (String, Double, String) -> Unit
) {
    var tripName by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val countries = remember { CurrencyData.countries }
    var selectedCountry by remember { mutableStateOf<CountryCurrency?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = TravelPrimaryText,
        unfocusedTextColor = TravelPrimaryText,
        cursorColor = TravelAccentGreen,
        focusedBorderColor = TravelAccentGreen,
        unfocusedBorderColor = TravelSecondaryText,
        focusedLabelColor = TravelSecondaryText,
        unfocusedLabelColor = TravelSecondaryText
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Plan Your Next Trip", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TravelPrimaryText)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = tripName,
            onValueChange = { tripName = it },
            label = { Text("Trip Name (e.g., 'Paris Getaway')") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            textStyle = TextStyle(color = TravelPrimaryText)
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedCountry?.name ?: "Select Destination",
                onValueChange = {},
                readOnly = true,
                label = { Text("Destination Country") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                colors = textFieldColors,
                textStyle = TextStyle(color = TravelPrimaryText)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF011A27))
            ) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(text = "${country.flag} ${country.name}", color = TravelPrimaryText) },
                        onClick = {
                            selectedCountry = country
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("Budget (${selectedCountry?.currencyCode ?: "auto"})") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = textFieldColors,
            enabled = selectedCountry != null,
            textStyle = TextStyle(color = TravelPrimaryText)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val budgetValue = budget.toDoubleOrNull()
                if (tripName.isNotBlank() && budgetValue != null && selectedCountry != null) {
                    onCreateTrip(tripName, budgetValue, selectedCountry!!.currencyCode)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TravelAccentGreen,
                contentColor = Color.White,
                disabledContainerColor = TravelAccentGreen.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            ),
            enabled = tripName.isNotBlank() && budget.isNotBlank() && selectedCountry != null
        ) {
            Text("Create Trip", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
private fun TripCard(trip: Trip, onClick: () -> Unit, onLongPress: () -> Unit) {
    val currencyFormat = remember(trip.destinationCurrency) {
        NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            currency = try {
                Currency.getInstance(trip.destinationCurrency)
            } catch (e: Exception) {
                Currency.getInstance("USD") // Fallback
            }
        }
    }

    val spentFormatted = currencyFormat.format(trip.amountSpent)
    val totalFormatted = currencyFormat.format(trip.budget)
    val progress = if (trip.budget > 0) (trip.amountSpent / trip.budget).toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "budgetProgress")

    val progressColor = when {
        progress > 1.0f -> ProgressDanger
        progress > 0.85f -> ProgressWarning
        else -> TravelAccentGreen
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TravelCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = trip.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TravelPrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Budget: $totalFormatted",
                style = MaterialTheme.typography.bodySmall,
                color = TravelSecondaryText
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.CenterStart) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    color = Color.Gray.copy(alpha = 0.3f)
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    color = progressColor,
                    strokeCap = StrokeCap.Round,
                )
                Text(
                    text = spentFormatted,
                    color = TravelPrimaryText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, bottom = 40.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CardTravel,
            contentDescription = "No Trips",
            tint = TravelAccentGreen,
            modifier = Modifier.size(60.dp)
        )
        Text(
            text = "Your Next Adventure Awaits",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = TravelPrimaryText
        )
        Text(
            text = "Plan your first journey by tapping the '+' button below.",
            style = MaterialTheme.typography.bodyMedium,
            color = TravelSecondaryText,
            textAlign = TextAlign.Center
        )
    }
}
