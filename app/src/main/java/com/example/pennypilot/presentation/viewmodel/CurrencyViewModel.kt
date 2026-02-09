package com.example.pennypilot.presentation.viewmodel

import CurrencyApiService // Keep this if other parts of the file use it
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// ▼▼▼ FIX: Import the correct response type ▼▼▼
import com.example.pennypilot.data.model.ExchangeRatesResponse
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    // ▼▼▼ FIX: Change the type to match what the repository returns ▼▼▼
    private val _exchangeRates = MutableStateFlow<Result<ExchangeRatesResponse>>(Result.Loading)
    val exchangeRates = _exchangeRates.asStateFlow()

    fun fetchRates(baseCurrency: String) {
        viewModelScope.launch {
            _exchangeRates.value = Result.Loading
            // Collect the flow from the repository
            currencyRepository.getLatestRates(baseCurrency).collect { result ->
                // The types will now match
                _exchangeRates.value = result
            }
        }
    }
}
