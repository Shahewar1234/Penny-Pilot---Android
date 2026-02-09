package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.model.Expense
import com.example.pennypilot.data.model.PaymentType
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

class ValidationException(message: String) : IllegalArgumentException(message)
class AuthenticationException(message: String) : SecurityException(message)
class DataRetrievalException(message: String, cause: Throwable? = null) : Exception(message, cause)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userRepository: UserRepository
) : ViewModel() {

    val amount = MutableStateFlow("")
    val category = MutableStateFlow("")
    val merchantName = MutableStateFlow("")
    val expenseDate = MutableStateFlow<Date?>(Date())
    val notes = MutableStateFlow<String?>(null)
    val selectedPaymentType = MutableStateFlow(PaymentType.OTHER)
    val tags = MutableStateFlow("")
    val iconIdentifier = MutableStateFlow("")
    val isRecurring = MutableStateFlow(false) // New state for recurring expense

    private val _saveExpenseResult = MutableStateFlow<Result<Unit>?>(null)
    val saveExpenseResult: StateFlow<Result<Unit>?> = _saveExpenseResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentSelectedCurrencyCode = MutableStateFlow("USD")

    init {
        fetchCurrentUserCurrency()
    }

    private fun fetchCurrentUserCurrency() {
        firebaseAuth.currentUser?.uid?.let { uid ->
            viewModelScope.launch {
                when (val result = userRepository.getUserProfileOnce(uid)) {
                    is Result.Success -> {
                        val user = result.data
                        if (user != null) {
                            _currentSelectedCurrencyCode.value = user.selectedCurrencyCode
                            Timber.d("User currency fetched: ${user.selectedCurrencyCode}")
                        } else {
                            Timber.w("User profile data is null.")
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.exception, "Failed to fetch user profile for currency: ${result.message}")
                    }
                    is Result.Loading -> { /* No action needed */ }
                }
            }
        }
    }

    fun saveExpense() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            val errorMsg = "User not authenticated."
            Timber.w("SaveExpense: $errorMsg")
            _saveExpenseResult.value = Result.Error(exception = AuthenticationException(errorMsg), message = errorMsg)
            return
        }

        if (amount.value.isBlank()) {
            val errorMsg = "Amount cannot be empty."
            _saveExpenseResult.value = Result.Error(exception = ValidationException(errorMsg), message = errorMsg)
            return
        }
        val amountDouble = amount.value.toDoubleOrNull()
        if (amountDouble == null || amountDouble <= 0) {
            val errorMsg = "Please enter a valid positive amount."
            _saveExpenseResult.value = Result.Error(exception = ValidationException(errorMsg), message = errorMsg)
            return
        }
        if (category.value.isBlank()) {
            val errorMsg = "Category cannot be empty."
            _saveExpenseResult.value = Result.Error(exception = ValidationException(errorMsg), message = errorMsg)
            return
        }

        _isLoading.value = true
        _saveExpenseResult.value = Result.Loading

        viewModelScope.launch {
            try {
                val userProfileResult = userRepository.getUserProfileOnce(uid)
                val userProfileData = when (userProfileResult) {
                    is Result.Success -> userProfileResult.data
                    is Result.Error -> {
                        val errorMsg = "Could not retrieve user profile to update balance."
                        _saveExpenseResult.value = Result.Error(
                            exception = DataRetrievalException(errorMsg, userProfileResult.exception),
                            message = errorMsg
                        )
                        _isLoading.value = false
                        return@launch
                    }
                    else -> {
                        val errorMsg = "Could not retrieve user profile to update balance."
                        _saveExpenseResult.value = Result.Error(
                            exception = DataRetrievalException(errorMsg),
                            message = errorMsg
                        )
                        _isLoading.value = false
                        return@launch
                    }
                }

                if (userProfileData == null) {
                    val errorMsg = "User profile data is null after successful retrieval."
                    _saveExpenseResult.value = Result.Error(
                        exception = DataRetrievalException(errorMsg),
                        message = errorMsg
                    )
                    _isLoading.value = false
                    return@launch
                }

                val newExpense = Expense(
                    userId = uid,
                    amount = amountDouble,
                    category = category.value.trim(),
                    merchantName = merchantName.value.takeIf { it.isNotBlank() },
                    date = expenseDate.value,
                    paymentType = selectedPaymentType.value,
                    notes = notes.value?.takeIf { it.isNotBlank() },
                    tags = tags.value.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    iconIdentifier = iconIdentifier.value.takeIf { it.isNotBlank() },
                    isRecurring = isRecurring.value // Save the recurring status
                )

                firestore.runTransaction { transaction ->
                    val userDocRef = firestore.collection("users").document(uid)
                    val expenseCollRefPath = "users/$uid/expenses"
                    val newExpenseDocRef = firestore.collection(expenseCollRefPath).document()

                    val userSnapshotInTransaction = transaction.get(userDocRef)
                    val balanceInDb = userSnapshotInTransaction.getDouble("currentBalance") ?: 0.0

                    val updatedBalance = balanceInDb - amountDouble

                    transaction.set(newExpenseDocRef, newExpense.copy(id = newExpenseDocRef.id))
                    transaction.update(userDocRef, "currentBalance", updatedBalance)
                    null
                }.await()

                _saveExpenseResult.value = Result.Success(Unit)
                clearForm()

            } catch (e: Exception) {
                _saveExpenseResult.value = Result.Error(exception = e, message = e.localizedMessage ?: "An unknown error occurred.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun clearForm() {
        amount.value = ""
        category.value = ""
        merchantName.value = ""
        expenseDate.value = Date()
        notes.value = null
        selectedPaymentType.value = PaymentType.OTHER
        tags.value = ""
        iconIdentifier.value = ""
        isRecurring.value = false // Reset the recurring status
    }

    fun clearSaveExpenseResult() {
        _saveExpenseResult.value = null
    }
}
