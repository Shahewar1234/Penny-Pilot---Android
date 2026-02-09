package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.NumberFormat

class SplitBillViewModel : ViewModel() {

    private val _totalAmount = MutableStateFlow("")
    val totalAmount: StateFlow<String> = _totalAmount

    private val _numberOfPeople = MutableStateFlow("1")
    val numberOfPeople: StateFlow<String> = _numberOfPeople

    private val _amountPerPerson = MutableStateFlow(0.0)
    val amountPerPerson: StateFlow<Double> = _amountPerPerson

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol

    fun onTotalAmountChange(amount: String) {
        _totalAmount.value = amount
        calculateSplit()
    }

    fun onNumberOfPeopleChange(people: String) {
        _numberOfPeople.value = people
        calculateSplit()
    }

    fun onCurrencySymbolChange(symbol: String) {
        _currencySymbol.value = symbol
    }

    private fun calculateSplit() {
        val total = _totalAmount.value.toDoubleOrNull() ?: 0.0
        val people = _numberOfPeople.value.toIntOrNull() ?: 1
        if (people > 0) {
            _amountPerPerson.value = total / people
        }
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getNumberInstance().apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
        return "${_currencySymbol.value}${format.format(amount)}"
    }
}
