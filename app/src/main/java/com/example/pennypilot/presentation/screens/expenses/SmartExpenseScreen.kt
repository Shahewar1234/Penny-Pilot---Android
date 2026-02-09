package com.example.pennypilot.presentation.screens.expenses

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.widget.DatePicker
import android.widget.Toast // For showing delete confirmation/error
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.* // Keep all icon imports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.pennypilot.R
import com.example.pennypilot.data.model.Expense // Your Expense data model
import com.example.pennypilot.data.model.PaymentType // For Preview
import com.example.pennypilot.presentation.viewmodel.SmartExpenseViewModel
import timber.log.Timber // For logging
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale

// Theme Colors (Keep as is)
private val PennyPilotScreenBackground = Color(0xFF001A0E)
private val PennyPilotPrimaryText = Color.White
private val PennyPilotSecondaryText = Color.LightGray
private val PennyPilotAccentGreen = Color(0xFF61BC79)
private val PennyPilotMutedGreen = Color(0xFF004D2B)
private val PennyPilotSurfaceVariant = Color.White.copy(alpha = 0.08f)
private val PennyPilotOutline = PennyPilotAccentGreen.copy(alpha = 0.3f)
private val PennyPilotErrorColor = Color(0xFFCF6679)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartExpenseScreen(
    navController: NavController,
    viewModel: SmartExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val (startDate, endDate) = uiState.selectedDateRange
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }


    fun showDatePickerDialog(isStartDatePicker: Boolean) {
        val calendar = Calendar.getInstance().apply {
            time = if (isStartDatePicker) startDate else endDate
        }
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val newDate = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    if (isStartDatePicker) {
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    } else {
                        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                    }
                }.time

                if (isStartDatePicker) {
                    if (newDate.after(endDate)) {
                        Timber.d("New start date $newDate is after end date $endDate, setting both to new start date")
                        viewModel.changeSelectedDateRange(newDate, newDate)
                    } else {
                        viewModel.changeSelectedDateRange(newDate, endDate)
                    }
                } else {
                    if (newDate.before(startDate)) {
                        Timber.d("New end date $newDate is before start date $startDate, setting both to new end date")
                        viewModel.changeSelectedDateRange(newDate, newDate)
                    } else {
                        viewModel.changeSelectedDateRange(startDate, newDate)
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "PennyPilot Logo",
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Smart Expenses", color = PennyPilotPrimaryText)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDatePickerDialog(isStartDatePicker = true) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = "Start Date", tint = PennyPilotPrimaryText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(dateFormat.format(startDate), color = PennyPilotPrimaryText, fontSize = 12.sp)
                    }
                    Text("-", color = PennyPilotPrimaryText, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDatePickerDialog(isStartDatePicker = false) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(dateFormat.format(endDate), color = PennyPilotPrimaryText, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Filled.CalendarToday, contentDescription = "End Date", tint = PennyPilotPrimaryText, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { /* TODO: Open Filter Bottom Sheet */ }) {
                        Icon(Icons.Default.FilterList, "Filter Expenses", tint = PennyPilotPrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PennyPilotMutedGreen,
                    titleContentColor = PennyPilotPrimaryText,
                    actionIconContentColor = PennyPilotPrimaryText
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addexpense_screen_route") },
                containerColor = PennyPilotAccentGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Filled.Add, "Add new expense")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(PennyPilotScreenBackground)
        ) {
            ExpenseSummarySection(
                totalSpent = uiState.totalSpentForPeriod,
                topCategory = uiState.topSpendingCategory,
                largestTransaction = uiState.largestTransaction,
                aiSuggestions = uiState.aiSuggestions,
                currencyCode = uiState.userPreferredCurrencyCode
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                thickness = 0.5.dp,
                color = PennyPilotOutline
            )
            when {
                uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PennyPilotAccentGreen)
                    }
                }
                uiState.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            "Error: ${uiState.errorMessage}",
                            color = PennyPilotErrorColor,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                uiState.expenses.isEmpty() && uiState.errorMessage == null -> {
                    EmptyExpensesView(modifier = Modifier.fillMaxSize().weight(1f))
                }
                else -> {
                    val groupedExpenses = uiState.expenses
                        .sortedByDescending { it.date }
                        .groupBy { expense ->
                            val cal = Calendar.getInstance().apply {
                                expense.date?.let { time = it } ?: run { time = Date(0) }
                            }
                            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                            cal.time // This is the key for grouping
                        }.toSortedMap(compareByDescending { it })

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 72.dp)
                    ) {
                        groupedExpenses.entries.forEach { (date, expensesOnDate) ->
                            // Add the DateHeader as a single item
                            item(key = "header_${date.time}") { // Provide a stable key for the header
                                DateHeader(date = date)
                            }
                            items(
                                items = expensesOnDate, // Pass the list of expenses for this date
                                key = { expense -> "expense_${expense.id}" } // Ensure unique keys for expenses
                            )
                            { expense ->
                                // Assuming this is around line 332
                                SmartExpenseListItem(
                                    expense = expense,
                                    onClick = { /* TODO: Navigate to expense detail */ },
                                    onEditClick = { /* TODO: Navigate to edit expense */ },
                                    onDeleteClick = {
                                        expenseToDelete = expense
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                expenseToDelete = null
            },
            title = { Text("Confirm Deletion", color = PennyPilotPrimaryText) },
            text = { Text("Are you sure you want to delete the expense: '${expenseToDelete?.merchantName ?: expenseToDelete?.category}'?", color = PennyPilotSecondaryText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        expenseToDelete?.let { viewModel.deleteExpense(it.id) }
                        showDeleteDialog = false
                        expenseToDelete = null
                        Toast.makeText(context, "Expense deleted (if successful)", Toast.LENGTH_SHORT).show()
                    }
                ) { Text("Delete", color = PennyPilotErrorColor) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        expenseToDelete = null
                    }
                ) { Text("Cancel", color = PennyPilotAccentGreen) }
            },
            containerColor = PennyPilotMutedGreen,
            titleContentColor = PennyPilotPrimaryText,
            textContentColor = PennyPilotSecondaryText
        )
    }
}

@Composable
fun ExpenseSummarySection(
    totalSpent: Double,
    topCategory: String?,
    largestTransaction: Expense?,
    aiSuggestions: List<String>,
    currencyCode: String
) {
    val currencyFormat = remember(currencyCode) {
        NumberFormat.getCurrencyInstance().apply {
            try {
                currency = Currency.getInstance(currencyCode.uppercase(Locale.getDefault()))
            } catch (e: Exception) {
                Timber.e(e, "Invalid currency code for summary: $currencyCode, defaulting to USD.")
                currency = Currency.getInstance("USD")
            }
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            "Total Spent This Period",
            style = MaterialTheme.typography.titleMedium,
            color = PennyPilotSecondaryText
        )
        Text(
            currencyFormat.format(totalSpent),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PennyPilotPrimaryText
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            topCategory?.let {
                item {
                    InsightCard(
                        title = "Top Category", value = it, icon = Icons.Filled.Category,
                        backgroundColor = PennyPilotSurfaceVariant.copy(alpha = 0.2f),
                        contentColor = PennyPilotPrimaryText
                    )
                }
            }
            largestTransaction?.let { expense ->
                item {
                    val largestTxnCurrencyFormat = remember(expense.currencyCode) {
                        NumberFormat.getCurrencyInstance().apply {
                            try {
                                currency = Currency.getInstance(expense.currencyCode.uppercase(Locale.getDefault()))
                            } catch (e: Exception) {
                                Timber.e(e, "Invalid currency code for largest txn: ${expense.currencyCode}, defaulting to USD.")
                                currency = Currency.getInstance("USD")
                            }
                        }
                    }

                    InsightCard(
                        title = "Largest Expense",
                        value = "${expense.merchantName ?: expense.category} (${largestTxnCurrencyFormat.format(expense.amount)})",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        backgroundColor = PennyPilotAccentGreen.copy(alpha = 0.2f),
                        contentColor = PennyPilotAccentGreen,
                    )
                }
            }
            if (aiSuggestions.isNotEmpty()) {
                items(aiSuggestions) { suggestion ->
                    InsightCard(
                        title = "AI Suggestion", value = suggestion, icon = Icons.Filled.Lightbulb,
                        backgroundColor = PennyPilotSurfaceVariant.copy(alpha = 0.15f),
                        contentColor = PennyPilotSecondaryText
                    )
                }
            } else if (topCategory == null && largestTransaction == null && totalSpent == 0.0) {
                item {
                    InsightCard(
                        title = "Get Started",
                        value = "Add expenses to see smart insights here!",
                        icon = Icons.Filled.Analytics,
                        backgroundColor = PennyPilotSurfaceVariant.copy(alpha = 0.15f),
                        contentColor = PennyPilotSecondaryText
                    )
                }
            }
        }
    }
}

@Composable
fun InsightCard(title: String, value: String, icon: ImageVector, backgroundColor: Color, contentColor: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier.width(180.dp).height(IntrinsicSize.Min)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = title, tint = contentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = contentColor)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

@Composable
fun EmptyExpensesView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(
                Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = "No Expenses",
                modifier = Modifier.size(64.dp),
                tint = PennyPilotSecondaryText.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No expenses recorded yet.",
                style = MaterialTheme.typography.titleMedium,
                color = PennyPilotSecondaryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tap '+' to add your first expense or link an account to automatically track spending.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = PennyPilotSecondaryText.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
fun DateHeader(date: Date, modifier: Modifier = Modifier) {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val expenseCalendar = Calendar.getInstance().apply { time = date }

    val dateText = when {
        isSameDay(expenseCalendar, today) -> "Today"
        isSameDay(expenseCalendar, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(date)
    }
    Text(
        text = dateText,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        color = PennyPilotSecondaryText,
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable // <<<< ENSURE THIS ANNOTATION IS PRESENT HERE
fun SmartExpenseListItem(
    expense: Expense,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
)
{
    Text("Expense: ${expense.category}")
    val currencyFormat = remember(expense.currencyCode) {
        NumberFormat.getCurrencyInstance().apply {
            try {
                currency = Currency.getInstance(expense.currencyCode.uppercase(Locale.getDefault()))
            } catch (e: Exception) {
                Timber.e(e, "Invalid currency code for expense item: ${expense.currencyCode}, defaulting to USD.")
                currency = Currency.getInstance("USD")
            }
        }
    }

    ListItem(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(PennyPilotSurfaceVariant),
        headlineContent = {
            Text(
                text = expense.merchantName?.takeIf { it.isNotBlank() } ?: expense.category,
                fontWeight = FontWeight.SemiBold,
                color = PennyPilotPrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Text(
                text = if (expense.merchantName?.isNotBlank() == true) expense.category
                else expense.notes?.takeIf { it.isNotBlank() } ?: expense.paymentType.name.replace("_", " "),
                style = MaterialTheme.typography.bodySmall,
                color = PennyPilotSecondaryText.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            val displayIcon: ImageVector = remember(expense.category, expense.iconIdentifier) {
                getIconForExpense(expense.category, expense.iconIdentifier)
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PennyPilotAccentGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = displayIcon,
                    contentDescription = expense.category,
                    modifier = Modifier.size(24.dp),
                    tint = PennyPilotAccentGreen
                )
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    currencyFormat.format(expense.amount),
                    fontWeight = FontWeight.Bold,
                    color = if (expense.amount >= 0) PennyPilotAccentGreen else PennyPilotErrorColor
                )
                expense.date?.let {
                    Text(
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it),
                        style = MaterialTheme.typography.bodySmall,
                        color = PennyPilotSecondaryText.copy(alpha = 0.7f)
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}

fun getIconForExpense(category: String, iconIdentifier: String? = null): ImageVector {
    iconIdentifier?.let {
        return when (it.lowercase(Locale.getDefault())) {
            "restaurant", "food" -> Icons.Filled.Restaurant
            "flight", "travel" -> Icons.Filled.FlightTakeoff
            "movie", "entertainment" -> Icons.Filled.Movie
            "cart", "shopping" -> Icons.Filled.ShoppingCart
            "bill", "receipt" -> Icons.Filled.Receipt
            "grocery" -> Icons.Filled.LocalGroceryStore
            "car", "transport" -> Icons.Filled.DirectionsCar
            "health", "hospital" -> Icons.Filled.LocalHospital
            "money", "salary" -> Icons.Filled.AttachMoney
            "gift" -> Icons.Filled.CardGiftcard
            "education", "school" -> Icons.Filled.School
            "book" -> Icons.AutoMirrored.Filled.MenuBook
            else -> Icons.AutoMirrored.Filled.Label
        }
    }
    return when (category.lowercase(Locale.getDefault())) {
        "food & dining" -> Icons.Filled.Restaurant
        "shopping" -> Icons.Filled.ShoppingCart
        "transport" -> Icons.Filled.DirectionsCar
        "bills" -> Icons.Filled.Receipt
        "entertainment" -> Icons.Filled.Movie
        "groceries" -> Icons.Filled.LocalGroceryStore
        "health" -> Icons.Filled.LocalHospital
        "travel" -> Icons.Filled.FlightTakeoff
        "books" -> Icons.AutoMirrored.Filled.MenuBook
        else -> Icons.Filled.CreditCard
    }
}

@SuppressLint("UnrememberedMutableState", "MutableCollectionMutableState", "ViewModelConstructorInComposable")
@Preview(showBackground = true, name = "Smart Expense Screen - Hardcoded Dark Theme")
@Composable
fun SmartExpenseScreenPreviewHardcoded() {
    MaterialTheme(typography = MaterialTheme.typography) {
        Surface(color = PennyPilotScreenBackground) {
            val previewExpense = Expense(
                id = "prev1",
                amount = 123.45,
                merchantName = "Big Store",
                category = "Shopping",
                date = Date(),
                paymentType = PaymentType.CREDIT_CARD,
                currencyCode = "USD",
                userId = "preview_dummy_user"
            )
            ExpenseSummarySection(
                totalSpent = 1234.56,
                topCategory = "Food & Dining",
                largestTransaction = previewExpense,
                aiSuggestions = listOf("You saved \$20 on groceries!", "Consider reducing coffee shop visits."),
                currencyCode = "USD"
            )
        }
    }
}

@Preview(showBackground = true, name = "Empty Expenses View - Hardcoded Dark Theme")
@Composable
fun EmptyExpensesViewPreviewHardcoded() {
    MaterialTheme(typography = MaterialTheme.typography) {
        Surface(color = PennyPilotScreenBackground) {
            EmptyExpensesView(Modifier.fillMaxSize())
        }
    }
}
