package com.example.pennypilot.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pennypilot.data.model.PaymentType

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Double,
    val category: String,
    val merchantName: String?,
    val date: Long,
    val paymentType: PaymentType,
    val notes: String?,
    val currencyCode: String,
    val iconIdentifier: String?,
    val tags: List<String>,
    val isRecurring: Boolean = false
)
