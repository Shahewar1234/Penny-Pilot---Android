package com.example.pennypilot.presentation.viewmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.model.UiExpenseItem
import com.example.pennypilot.data.model.Expense
import com.example.pennypilot.data.model.User
import com.example.pennypilot.data.model.PaymentType
import com.example.pennypilot.data.repository.ExpenseRepository
import com.example.pennypilot.data.repository.UserRepository
import com.example.pennypilot.data.model.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val expenseRepository: ExpenseRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _userIdFlow = MutableStateFlow(firebaseAuth.currentUser?.uid)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<User?> = _userIdFlow.flatMapLatest { uid ->
        if (uid != null) {
            userRepository.getUserProfileFlow(uid)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val userName: StateFlow<String> = userProfile.map { profile ->
        profile?.displayName?.takeIf { it.isNotBlank() } ?: profile?.email?.substringBefore('@') ?: "User"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "User")

    val balanceString: StateFlow<String> = userProfile.map { user ->
        val balanceValue = user?.currentBalance ?: 0.0
        val locale = mapCurrencyCodeToLocale(user?.selectedCurrencyCode ?: "USD")
        formatCurrency(balanceValue, locale)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), formatCurrency(0.0, mapCurrencyCodeToLocale("USD")))

    val userHomeCurrency: StateFlow<String> = userProfile.map { it?.selectedCurrencyCode ?: "USD" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "USD")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private val _recentFirebaseExpensesFlow: Flow<List<Expense>> = _userIdFlow.flatMapLatest { uid ->
        if (uid != null) {
            expenseRepository.getExpensesForUserFlow(uid, limit = 5)
        } else {
            flowOf(emptyList())
        }
    }

    val uiRecentExpenses: StateFlow<List<UiExpenseItem>> = combine(
        _recentFirebaseExpensesFlow,
        userProfile
    ) { firebaseExpenses, user ->
        val homeCurrencySymbol = mapCurrencyCodeToSymbol(user?.selectedCurrencyCode ?: "USD")

        firebaseExpenses.map { expense ->
            UiExpenseItem(
                id = expense.id,
                category = expense.category,
                displayAmount = expense.amount,
                description = expense.merchantName?.takeIf { it.isNotBlank() } ?: expense.notes ?: expense.category,
                displayDate = expense.date?.let { formatDate(it.time) } ?: "N/A",
                originalAmount = expense.amount,
                originalCurrency = homeCurrencySymbol,
                amountInHomeCurrency = expense.amount,
                icon = getIconForCategory(expense.category, expense.iconIdentifier),
                paymentType = expense.paymentType
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val balanceChangePercentage: StateFlow<String> = userProfile.map { it?.currentBalance?.let { balance -> "+0.0%" } ?: "+0.0%" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), "+0.0%")

    private fun mapCurrencyCodeToSymbol(currencyCode: String): String {
        return when (currencyCode.uppercase()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "INR" -> "₹"
            else -> "$"
        }
    }

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _userIdFlow.value = auth.currentUser?.uid
        }

        if (_userIdFlow.value == null) {
            Timber.w("HomeViewModel: No current user UID. User logged out or auth not ready.")
            _errorMessage.value = "User not logged in. Please log in."
        } else {
            Timber.d("HomeViewModel initialized for UID: ${_userIdFlow.value}")
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun updateUserSetBalance(balanceInput: String) {
        val uid = _userIdFlow.value ?: return

        val newBalance = balanceInput.toDoubleOrNull()
        if (newBalance == null) {
            _errorMessage.value = "Invalid balance amount."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            val result = userRepository.updateUserBalance(uid, newBalance)
            if (result is Result.Error) {
                _errorMessage.value = "Failed to update balance: ${result.exception}"
            }
            _isLoading.value = false
        }
    }

    fun addExpense(
        amountInput: String,
        category: String,
        notes: String?,
        merchantName: String?,
        paymentType: PaymentType,
        tags: List<String>?,
        iconIdentifier: String?,
        isRecurring: Boolean
    ) {
        val uid = _userIdFlow.value ?: return

        val amount = amountInput.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _errorMessage.value = "Invalid expense amount."
            return
        }

        val userCurrency = userProfile.value?.selectedCurrencyCode ?: return

        val newExpense = Expense(
            userId = uid,
            amount = amount,
            category = category,
            notes = notes?.takeIf { it.isNotBlank() },
            merchantName = merchantName?.takeIf { it.isNotBlank() },
            paymentType = paymentType,
            currencyCode = userCurrency,
            tags = tags ?: emptyList(),
            iconIdentifier = iconIdentifier,
            isRecurring = isRecurring
        )

        _isLoading.value = true
        viewModelScope.launch {
            try {
                firestore.runTransaction { transaction ->
                    val userDocRef = firestore.collection("users").document(uid)
                    val expenseCollRefPath = "users/$uid/expenses"
                    val newExpenseDocRef = firestore.collection(expenseCollRefPath).document()

                    val userSnapshot = transaction.get(userDocRef)
                    val balanceInDb = userSnapshot.getDouble("currentBalance") ?: 0.0
                    val updatedBalance = balanceInDb - amount

                    transaction.set(newExpenseDocRef, newExpense.copy(id = newExpenseDocRef.id))
                    transaction.update(userDocRef, "currentBalance", updatedBalance)
                    null
                }.await()
            } catch (e: Exception) {
                _errorMessage.value = "Error adding expense: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun initiateSmartLogin() {
        _errorMessage.value = "Smart login feature not yet implemented."
    }

    private fun mapCurrencyCodeToLocale(currencyCode: String): Locale {
        return when (currencyCode.uppercase()) {
            "USD" -> Locale.US
            "EUR" -> Locale.GERMANY
            "GBP" -> Locale.UK
            "INR" -> Locale("en", "IN")
            else -> Locale.getDefault()
        }
    }

    private fun formatCurrency(amount: Double, locale: Locale): String {
        return try {
            NumberFormat.getCurrencyInstance(locale).format(amount)
        } catch (e: Exception) {
            NumberFormat.getCurrencyInstance(mapCurrencyCodeToLocale("USD")).format(amount) + " (Format Error)"
        }
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun getIconForCategory(category: String, iconIdentifier: String? = null): ImageVector {
        iconIdentifier?.let {
            return when (it.lowercase(Locale.getDefault())) {
                "restaurant", "food" -> Icons.Filled.Restaurant
                "flight", "travel" -> Icons.Filled.Flight
                "movie", "entertainment" -> Icons.Filled.Movie
                "cart", "shopping" -> Icons.Filled.ShoppingCart
                "bill", "receipt" -> Icons.Filled.Receipt
                "grocery" -> Icons.Filled.LocalGroceryStore
                "car", "transport" -> Icons.Filled.DirectionsCar
                "health", "hospital" -> Icons.Filled.LocalHospital
                "money", "salary" -> Icons.Filled.AttachMoney
                "gift" -> Icons.Filled.CardGiftcard
                "education", "school" -> Icons.Filled.School
                else -> Icons.Filled.Label
            }
        }
        return when (category.lowercase(Locale.getDefault())) {
            "food" -> Icons.Filled.Restaurant
            "travel" -> Icons.Filled.Flight
            "entertainment" -> Icons.Filled.Movie
            "shopping" -> Icons.Filled.ShoppingCart
            "bills" -> Icons.Filled.Receipt
            "groceries" -> Icons.Filled.LocalGroceryStore
            "transport" -> Icons.Filled.DirectionsCar
            "health" -> Icons.Filled.LocalHospital
            "salary" -> Icons.Filled.AttachMoney
            "gifts" -> Icons.Filled.CardGiftcard
            "education" -> Icons.Filled.School
            "other" -> Icons.Filled.Label
            else -> Icons.Filled.Label
        }
    }
}
