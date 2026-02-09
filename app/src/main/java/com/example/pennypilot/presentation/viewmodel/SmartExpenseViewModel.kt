package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.model.Expense
// Import your Result class if the repository uses it for single fetch operations
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.repository.ExpenseRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat // For currency formatting in suggestions
import java.util.Calendar
import java.util.Currency
import java.util.Date
import java.util.Locale
import javax.inject.Inject

// Data class to represent the UI state for the SmartExpenseScreen
data class SmartExpenseScreenState(
    val isLoading: Boolean = true,
    val expenses: List<Expense> = emptyList(), // These will be filtered by date range
    val errorMessage: String? = null,
    val selectedDateRange: Pair<Date, Date> = getDefaultDateRange(),
    val totalSpentForPeriod: Double = 0.0,
    val topSpendingCategory: String? = null,
    val largestTransaction: Expense? = null,
    val aiSuggestions: List<String> = emptyList(),
    val userPreferredCurrencyCode: String = "USD" // Default, can be fetched from user profile
)

private fun getDefaultDateRange(): Pair<Date, Date> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1) // First day of current month
    calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0); calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
    val startDate = calendar.time

    calendar.add(Calendar.MONTH, 1) // Move to first day of next month
    calendar.add(Calendar.DAY_OF_MONTH, -1) // Go to last day of current month
    calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59); calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
    val endDate = calendar.time
    return Pair(startDate, endDate)
}

@OptIn(ExperimentalCoroutinesApi::class) // For flatMapLatest
@HiltViewModel
class SmartExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val firebaseAuth: FirebaseAuth
    // TODO: Optionally inject UserRepository if you want to fetch user's preferred currency
) : ViewModel() {

    private val _selectedDateRangeFlow = MutableStateFlow(getDefaultDateRange())
    private val _triggerRefreshFlow = MutableStateFlow(Unit) // To manually trigger re-fetch/re-process

    // Flow that combines date range and refresh triggers to get all expenses for the current user
    // Then filters them by date range and processes for UI display.
    val uiState: StateFlow<SmartExpenseScreenState> =
        _triggerRefreshFlow.flatMapLatest { // Re-fetches all expenses if refresh is triggered
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                Timber.w("SmartExpenseVM: No authenticated user. Emitting error state.")
                flowOf(
                    SmartExpenseScreenState(
                        isLoading = false,
                        errorMessage = "User not authenticated. Please log in.",
                        selectedDateRange = _selectedDateRangeFlow.value
                    )
                )
            } else {
                expenseRepository.getExpensesForUserFlow(currentUserId) // Get all expenses for user
                    .catch { e ->
                        Timber.e(e, "SmartExpenseVM: Error fetching expenses flow.")
                        // Emit an error state if the flow itself throws an exception
                        emit(
                            SmartExpenseScreenState(
                                isLoading = false,
                                errorMessage = "Failed to load expenses: ${e.message}",
                                selectedDateRange = _selectedDateRangeFlow.value
                            )
                        )
                    }
                    .combine(_selectedDateRangeFlow) { allExpenses, dateRange -> // Combine with the current date range
                        Timber.d("SmartExpenseVM: Processing ${allExpenses.size} expenses for date range: $dateRange")
                        processExpenses(allExpenses, dateRange)
                    }
            }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000L),
                initialValue = SmartExpenseScreenState(isLoading = true, selectedDateRange = _selectedDateRangeFlow.value)
            )

    private fun FlowCollector<List<Expense>>.emit(value: SmartExpenseScreenState) {}

    init {
        // Initial load is triggered by the collection of uiState
        Timber.d("SmartExpenseViewModel Initialized")
        // If you need to fetch user's preferred currency, do it here
        // Example: fetchUserPreferredCurrency()
    }

    private fun processExpenses(
        allExpensesFromSource: List<Expense>,
        dateRange: Pair<Date, Date>
    ): SmartExpenseScreenState {
        val (startDate, endDate) = dateRange
        val filteredExpenses = allExpensesFromSource.filter { expense ->
            expense.date?.let { !it.before(startDate) && !it.after(endDate) } ?: false
        }
        Timber.d("SmartExpenseVM: Filtered to ${filteredExpenses.size} expenses.")

        val totalSpent = filteredExpenses.sumOf { it.amount }
        val largestTxn = filteredExpenses.maxByOrNull { it.amount }
        val topCategory = if (filteredExpenses.isNotEmpty()) {
            filteredExpenses
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
                .maxByOrNull { it.value }
                ?.key
        } else {
            null
        }

        // TODO: Get actual user's preferred currency for suggestions
        val userCurrencyCode = uiState.value.userPreferredCurrencyCode // Use current state or fetch new
        val currencyFormat = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance(userCurrencyCode)
        }

        val suggestions = mutableListOf<String>()
        if (totalSpent > 300) { // Example threshold
            suggestions.add("Spending is over ${currencyFormat.format(300)} for this period. Keep an eye on your budget!")
        }
        topCategory?.let { category ->
            val categorySpending = filteredExpenses.filter { it.category == category }.sumOf { it.amount }
            if (category == "Food & Dining" && categorySpending > 80) { // Example threshold
                suggestions.add("High spending in 'Food & Dining'. Consider more home meals.")
            }
            if (category == "Shopping" && categorySpending > 100) { // Example threshold
                suggestions.add("Significant shopping this period. Were these planned purchases?")
            }
        }
        largestTxn?.let {
            if (it.amount > 70) { // Example threshold
                suggestions.add("Notable transaction: ${it.merchantName ?: it.category} for ${currencyFormat.format(it.amount)}.")
            }
        }
        if (suggestions.isEmpty() && filteredExpenses.isNotEmpty()) {
            suggestions.add("Good job managing your finances this period!")
        } else if (filteredExpenses.isEmpty()) {
            suggestions.add("No expenses logged for the selected period. Add transactions to get insights.")
        }

        return SmartExpenseScreenState(
            isLoading = false,
            expenses = filteredExpenses,
            selectedDateRange = dateRange,
            totalSpentForPeriod = totalSpent,
            largestTransaction = largestTxn,
            topSpendingCategory = topCategory,
            aiSuggestions = suggestions,
            errorMessage = if (filteredExpenses.isEmpty() && allExpensesFromSource.isNotEmpty()) "No expenses found for the selected date range." else null,
            userPreferredCurrencyCode = userCurrencyCode
        )
    }

    fun deleteExpense(expenseId: String) {
        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId == null) {
            Timber.w("SmartExpenseVM: Cannot delete expense, user not authenticated.")
            // Optionally update UI with an error message
            // _uiState.update { it.copy(errorMessage = "User not authenticated to delete expense.") }
            return
        }

        viewModelScope.launch {
            // No loading state change for delete, UI updates reactively from the flow
            val result = expenseRepository.deleteExpense(currentUserId, expenseId)
            when (result) {
                is Result.Success -> {
                    Timber.i("SmartExpenseVM: Expense $expenseId deleted successfully.")
                    // The main `uiState` flow will automatically re-emit due to data change in Firestore
                    // if getExpensesForUserFlow listens to snapshots.
                    // If not, or for immediate feedback before flow updates:
                    // onExternalExpenseChange() // or simply re-process with current data (less ideal)
                }

                is Result.Error -> {
                    Timber.e("SmartExpenseVM: Failed to delete expense $expenseId: ${result.exception}")
                    // Update UI with error message, this needs to merge with existing state
                    // This direct update is tricky because uiState is a complex flow.
                    // A better way is to have a separate error StateFlow for actions.
                    // For now, let's log and rely on the main flow for data.
                    // Or, if processExpenses could take an error:
                    // _uiState.update { it.copy(errorMessage = "Failed to delete: ${result.exception}") }
                }
                else -> { /* No-op for Result.Loading in delete if not applicable */ }
            }
        }
    }

    fun changeSelectedDateRange(newStartDate: Date, newEndDate: Date) {
        if (newStartDate.after(newEndDate)) {
            Timber.w("SmartExpenseVM: Start date cannot be after end date.")
            // Optionally update UI with an error message via a separate error flow
            // or by ensuring processExpenses can reflect this in errorMessage
            return
        }
        Timber.d("SmartExpenseVM: Date range changed to $newStartDate - $newEndDate")
        _selectedDateRangeFlow.value = Pair(newStartDate, newEndDate)
        // The `uiState` flow will automatically re-combine and re-process.
    }

    fun onExternalExpenseChange() {
        // This function can be called to manually trigger a re-fetch and re-process
        // if the underlying flow from expenseRepository doesn't automatically pick up
        // all desired changes immediately (e.g., if it's not a snapshot listener).
        Timber.d("SmartExpenseVM: External expense change detected, triggering refresh.")
        _triggerRefreshFlow.value = Unit // Emit a new value to trigger flatMapLatest
    }

    // TODO:
    // private fun fetchUserPreferredCurrency() {
    //     viewModelScope.launch {
    //         firebaseAuth.currentUser?.uid?.let { userId ->
    //             // Assuming userRepository.getUserProfileOnce(userId) returns Result<User?>
    //             val result = userRepository.getUserProfileOnce(userId)
    //             if (result is Result.Success && result.data != null) {
    //                 _uiState.update { it.copy(userPreferredCurrencyCode = result.data.selectedCurrencyCode) }
    //             } else if (result is Result.Error) {
    //                 Timber.e("Failed to fetch user preferred currency: ${result.exception}")
    //             }
    //         }
    //     }
    // }
}

