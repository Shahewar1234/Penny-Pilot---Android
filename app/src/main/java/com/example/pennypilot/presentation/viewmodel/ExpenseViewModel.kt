//package com.example.pennypilot.presentation.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.pennypilot.data.local.entity.ExpenseEntity
//import com.example.pennypilot.data.repository.ExpenseRepository
//import dagger.hilt.android.lifecycle.HiltViewModel
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.launch
//import javax.inject.Inject
//
//@HiltViewModel
//class ExpenseViewModel @Inject constructor(
//    private val repository: ExpenseRepository
//) : ViewModel() {
//
//    fun addExpense(expense: ExpenseEntity) {
//        viewModelScope.launch {
//            repository.addExpense(expense)
//        }
//    }
//
//    val expenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
//
//    fun loadExpenses() {
//        viewModelScope.launch {
//            expenses.value = repository.getExpenses()
//        }
//    }
//}
