package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.local.entity.ExpenseEntity
import com.example.pennypilot.data.model.Expense
import com.example.pennypilot.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.pennypilot.data.model.Result // Import your custom Result class

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntity>> = _expenses

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    // Assuming you also have a loading state if your Result has it
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadExpenses() {
        viewModelScope.launch {
            _isLoading.value = true // Set loading before making the call
            _errorState.value = null // Clear previous errors

            // Explicitly type 'result' with your custom Result type
            val result: com.example.pennypilot.data.model.Result<List<Expense>> = repository.getExpenses()

            when (result) {
                is Result.Success -> {
                    val expenseModels = result.data
                    _expenses.value = expenseModels.map { expenseModel ->
                        // Correctly map all fields from Expense to ExpenseEntity
                        ExpenseEntity(
                            id = expenseModel.id,
                            userId = expenseModel.userId,
                            amount = expenseModel.amount,
                            category = expenseModel.category,
                            merchantName = expenseModel.merchantName,
                            date = expenseModel.date?.time ?: 0L,
                            paymentType = expenseModel.paymentType,
                            notes = expenseModel.notes,
                            currencyCode = expenseModel.currencyCode,
                            iconIdentifier = expenseModel.iconIdentifier,
                            tags = expenseModel.tags,
                            isRecurring = expenseModel.isRecurring
                        )
                    }
                    _isLoading.value = false
                }
                is Result.Error -> {
                    // 'result.exception' is already a String in your Result.kt
                    _expenses.value = emptyList() // Clear expenses on error
                    _errorState.value = "Failed to load expenses: ${result.exception}" // Use the string directly
                    _isLoading.value = false
                    // If you want to log, you can log this string
                    // Log.e("DashboardViewModel", "Error loading expenses: ${result.exception}")
                }
                is Result.Loading -> {
                    // This case might be handled by the initial _isLoading.value = true
                    // or you might want specific logic if the repository emits Result.Loading
                    _isLoading.value = true
                }
            }
        }
    }
}
