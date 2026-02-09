package com.example.pennypilot.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@IgnoreExtraProperties
data class WalletMember(
    val id: String = "",
    val name: String = "",
    val profileImageUrl: String = ""
)

@IgnoreExtraProperties
data class SharedWallet(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val totalBudget: Double = 0.0,
    val amountSpent: Double = 0.0,
    val currencyCode: String = "USD", // FIX: Renamed from currencySymbol to currencyCode
    val color: String = "#61BC79",
    val numberOfPeople: Int = 1,
    @ServerTimestamp
    val createdAt: Date? = null // Timestamp for burn rate calculation
)
