package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.model.SharedWallet
import com.example.pennypilot.data.repository.SharedWalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SharedWalletsViewModel @Inject constructor(
    private val repository: SharedWalletRepository
) : ViewModel() {

    private val _userCurrency = MutableStateFlow("USD")
    val userCurrency: StateFlow<String> = _userCurrency.asStateFlow()

    val wallets: StateFlow<Result<List<SharedWallet>>> = repository.getSharedWallets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = Result.Loading
        )

    private val _createWalletState = MutableStateFlow<Result<Unit>?>(null)
    val createWalletState = _createWalletState.asStateFlow()

    init {
        fetchUserCurrency()
    }

    private fun fetchUserCurrency() {
        viewModelScope.launch {
            _userCurrency.value = repository.getUserCurrency()
        }
    }

    fun createWallet(name: String, budget: String, numberOfPeople: String) {
        val budgetValue = budget.toDoubleOrNull()
        val peopleCount = numberOfPeople.toIntOrNull()
        if (name.isBlank() || budgetValue == null || budgetValue <= 0 || peopleCount == null || peopleCount <= 0) {
            _createWalletState.value = Result.Error(IllegalArgumentException("Invalid name, budget, or number of people."), "Invalid name, budget, or number of people.")
            return
        }

        viewModelScope.launch {
            _createWalletState.value = Result.Loading
            _createWalletState.value = repository.createWallet(name, budgetValue, peopleCount)
        }
    }

    fun deleteWallet(walletId: String) {
        viewModelScope.launch {
            repository.deleteWallet(walletId)
        }
    }

    fun resetCreateWalletState() {
        _createWalletState.value = null
    }
}
