package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.pennypilot.data.model.Result

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            when (val result = expenseRepository.getExpenses()) {
                is Result.Success -> {
                    val expenses = result.data
                    val totalSpent = expenses.sumOf { it.amount }
                    val dailyAverage = if (expenses.isNotEmpty()) totalSpent / expenses.size else 0.0
                    val spendingByCategory = expenses.groupBy { it.category }
                        .mapValues { (_, expenses) -> expenses.sumOf { it.amount }.toFloat() }
                    val topCategory = spendingByCategory.maxByOrNull { it.value }?.key ?: "N/A"

                    _uiState.update {
                        it.copy(
                            totalSpent = totalSpent,
                            dailyAverage = dailyAverage,
                            topCategory = topCategory,
                            spendingByCategory = spendingByCategory,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}
