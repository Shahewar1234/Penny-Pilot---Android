package com.example.pennypilot.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class UiExpenseItem(
    val id: String,
    val description: String,
    val category: String,
    val displayDate: String,
    // --- MODIFICATION 1: Store original amount and currency ---
    val originalAmount: Double,
    val originalCurrency: String,
    // --- MODIFICATION 2: Store the converted home currency amount ---
    val amountInHomeCurrency: Double,
    val icon: ImageVector,
    val paymentType: PaymentType,
    val displayAmount: Double
)
