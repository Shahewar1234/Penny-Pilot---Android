package com.example.pennypilot.presentation.screens.expenses

import android.R.attr.enabled
import android.app.DatePickerDialog
import android.widget.DatePicker // Standard Android DatePicker
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.* // Keep all icon imports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pennypilot.data.model.PaymentType
// Import HomeViewModel instead of AddExpenseViewModel
import com.example.pennypilot.presentation.viewmodel.HomeViewModel
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    homeViewModel: HomeViewModel = hiltViewModel() // Use HomeViewModel
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Observe states from HomeViewModel
    val isLoading by homeViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by homeViewModel.errorMessage.collectAsStateWithLifecycle()
    val userProfile by homeViewModel.userProfile.collectAsStateWithLifecycle()

    // Local states for form inputs
    var amountInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("") }
    var merchantNameInput by remember { mutableStateOf("") }
    // For user-selected date (optional if relying on server timestamp)
    var selectedDate by remember { mutableStateOf<Date?>(Date()) } // Default to current date
    var notesInput by remember { mutableStateOf("") }
    var selectedPaymentType by remember { mutableStateOf(PaymentType.OTHER) }
    var tagsInput by remember { mutableStateOf("") } // Comma-separated for simplicity
    var iconIdentifierInput by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }


    // --- Define Colors (Copied from your original) ---
    val primaryGreen = Color(0xFF009146)
    val accentGreen = Color(0xFF61BC79)
    val lightTextGreen = Color(0xFF80C692)
    val darkBackgroundStart = primaryGreen
    val darkBackgroundEnd = Color.Black
    val textColorPrimary = Color.White
    // val textColorSecondary = lightTextGreen // Not used directly here, but good to have
    val textFieldFocusedBorderColor = accentGreen
    val textFieldUnfocusedBorderColor = Color.Gray.copy(alpha = 0.7f)
    val textFieldTextColor = Color.White
    val textFieldLabelColor = lightTextGreen.copy(alpha = 0.9f)
    val sectionBackgroundColor = Color.White.copy(alpha = 0.05f)


    // --- Date Picker State & Logic ---
    val calendar = Calendar.getInstance()
    selectedDate?.let { calendar.time = it } // Initialize calendar with selectedDate

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val newCal = Calendar.getInstance().apply { set(year, month, dayOfMonth) }
            selectedDate = newCal.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    val dateFormatter = remember { SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()) }


    // --- Effect to handle success/error messages and navigation ---
    var hasAttemptedSave by remember { mutableStateOf(false) }
    LaunchedEffect(errorMessage, isLoading) {
        if (hasAttemptedSave && !isLoading) { // Only react after a save attempt and loading is finished
            if (errorMessage == null) {
                // Success (no error message means the transaction likely succeeded)
                Toast.makeText(context, "Expense added successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
                // No need to clear result state, HomeViewModel handles its own state
            } else {
                // Error
                Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                homeViewModel.clearErrorMessage() // Clear the error message after showing it
            }
            hasAttemptedSave = false // Reset for next save attempt
        }
    }

    fun handleSaveExpense() {
        // Basic validation (can be more robust)
        if (amountInput.isBlank() || categoryInput.isBlank()) {
            Toast.makeText(context, "Amount and Category are required.", Toast.LENGTH_SHORT).show()
            return
        }
        hasAttemptedSave = true // Indicate that a save operation is being initiated
        homeViewModel.addExpense(
            amountInput = amountInput,
            category = categoryInput,
            notes = notesInput.takeIf { it.isNotBlank() },
            merchantName = merchantNameInput.takeIf { it.isNotBlank() },
            paymentType = selectedPaymentType,
            tags = tagsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            iconIdentifier = iconIdentifierInput.takeIf { it.isNotBlank() },
            isRecurring = isRecurring
            // If you want user-set date: pass selectedDate, and modify HomeViewModel.addExpense
            // For now, relies on @ServerTimestamp in Expense model (date will be set by Firestore)
        )
    }


    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Expense", style = MaterialTheme.typography.headlineSmall, color = textColorPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColorPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryGreen.copy(alpha = 0.8f),
                    navigationIconContentColor = textColorPrimary,
                    titleContentColor = textColorPrimary
                )
            )
        },
        floatingActionButton = {
            if (!isLoading) {
                ExtendedFloatingActionButton(
                    text = { Text("Save Expense", color = textColorPrimary) },
                    icon = { Icon(Icons.Filled.Save, contentDescription = "Save", tint = textColorPrimary) },
                    onClick = { handleSaveExpense() },
                    containerColor = primaryGreen,
                    contentColor = textColorPrimary
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(colors = listOf(darkBackgroundStart, darkBackgroundEnd)))
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Amount Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount", color = textFieldLabelColor) },
                    leadingIcon = {
                        // Consider using the currency symbol from homeViewModel.userProfile.value?.selectedCurrencyCode
                        val currencySymbol = userProfile?.getCurrencySymbol() ?: "$"
                        Text(
                            text = currencySymbol, // Use the dynamic symbol
                            style = MaterialTheme.typography.titleMedium,
                            color = accentGreen
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 28.sp, color = textFieldTextColor),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors( /* Your colors */)
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Section for Core Details
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sectionBackgroundColor, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("Expense Details", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp), color = accentGreen)

                    // Category
                    OutlinedTextField(
                        value = categoryInput,
                        onValueChange = { categoryInput = it },
                        label = { Text("Category (e.g., Food, Travel)", color = textFieldLabelColor) },
                        leadingIcon = { Icon(Icons.Filled.Category, contentDescription = "Category", tint = accentGreen) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( /* Your colors */)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Merchant Name
                    OutlinedTextField(
                        value = merchantNameInput,
                        onValueChange = { merchantNameInput = it },
                        label = { Text("Merchant Name (Optional)", color = textFieldLabelColor) },
                        leadingIcon = { Icon(Icons.Filled.Store, contentDescription = "Merchant", tint = accentGreen) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) /*datePickerDialog.show()*/ }), // Consider focus to date or payment
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( /* Your colors */)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Date Picker (Optional if using server timestamp for actual expense record)
                    OutlinedTextField(
                        value = selectedDate?.let { dateFormatter.format(it) } ?: "Select Date (Defaults to Now)",
                        onValueChange = { /* Read Only */ },
                        label = { Text("Date of Expense (Optional)", color = textFieldLabelColor) },
                        readOnly = true,
                        leadingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Expense Date", tint = accentGreen) },
                        trailingIcon = { Icon(Icons.Filled.CalendarToday, contentDescription = "Show Date Picker", modifier = Modifier.clickable { datePickerDialog.show() }, tint = accentGreen) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { datePickerDialog.show() },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( /* Your colors, ensure disabled state is themed */)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Section for Payment & Other Info
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sectionBackgroundColor, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text("Payment & Other Info", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp), color = accentGreen)

                    // Payment Type Dropdown
                    var paymentTypeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = paymentTypeExpanded,
                        onExpandedChange = { paymentTypeExpanded = !paymentTypeExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val fillMaxWidth = Modifier.fillMaxWidth()
                        OutlinedTextField(
                            value = selectedPaymentType.name.replace("_", " ").formatForDisplay(),
                            onValueChange = { /* Read only */ },
                            label = { Text("Payment Type", color = textFieldLabelColor) },
                            leadingIcon = { Icon(Icons.Filled.Payments, contentDescription = "Payment Type", tint = accentGreen) }, // Added tint
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentTypeExpanded) },
                            modifier = fillMaxWidth.menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors( /* Your colors, ensure disabled state is themed */ )
                        )
                        ExposedDropdownMenu(
                            expanded = paymentTypeExpanded,
                            onDismissRequest = { paymentTypeExpanded = false },
                            modifier = Modifier.background(darkBackgroundEnd.copy(alpha = 0.95f))
                        ) {
                            PaymentType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.replace("_", " ").formatForDisplay(), color = textColorPrimary) },
                                    onClick = {
                                        selectedPaymentType = type
                                        paymentTypeExpanded = false
                                        focusManager.moveFocus(FocusDirection.Down)
                                    },
                                    colors = MenuDefaults.itemColors(textColor = textColorPrimary)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                     Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Mark as Recurring", color = textColorPrimary)
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = accentGreen,
                                checkedTrackColor = primaryGreen,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.DarkGray
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))


                    // Tags Input
                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text("Tags (Optional, comma-separated)", color = textFieldLabelColor) },
                        leadingIcon = { Icon(Icons.Filled.NewLabel, contentDescription = "Tags", tint = accentGreen) }, // Example icon
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true, // Or allow multiple lines if tags can be long
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( /* Your colors */)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Icon Identifier (Simple text field for now, can be improved later)
                    OutlinedTextField(
                        value = iconIdentifierInput,
                        onValueChange = { iconIdentifierInput = it },
                        label = { Text("Icon Key (Optional, e.g., 'food')", color = textFieldLabelColor) },
                        leadingIcon = { Icon(Icons.Filled.ImageSearch, contentDescription = "Icon Key", tint = accentGreen) }, // Example icon
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( /* Your colors */)
                    )
                    Spacer(modifier = Modifier.height(12.dp))


                    // Notes
                    OutlinedTextField(
                        value = notesInput,
                        onValueChange = { notesInput = it },
                        label = { Text("Notes (Optional)", color = textFieldLabelColor) },
                        leadingIcon = { Icon(Icons.Filled.EditNote, contentDescription = "Notes", tint = accentGreen) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            handleSaveExpense()
                        }),
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors( /* Your colors */)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(visible = isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp), color = accentGreen)
                }
                Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
            }
        }
    }
}

// Keep your formatForDisplay extension function
fun String.formatForDisplay(): String {
    return this.split(" ")
        .joinToString(" ") { word ->
            word.lowercase(Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}
