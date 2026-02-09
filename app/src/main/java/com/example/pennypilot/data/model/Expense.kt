package com.example.pennypilot.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class PaymentType {
    CREDIT_CARD,
    DEBIT_CARD,
    CASH,
    DIGITAL_WALLET,
    BANK_TRANSFER,
    OTHER
}

data class Expense(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("amount") @set:PropertyName("amount") var amount: Double = 0.0,
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("merchantName") @set:PropertyName("merchantName") var merchantName: String? = null, // Making it nullable if optional

    @ServerTimestamp // Firestore will populate this on creation if it's null
    @get:PropertyName("date") @set:PropertyName("date") var date: Date? = null,

    // Firestore stores enums as strings by default.
    @get:PropertyName("paymentType") @set:PropertyName("paymentType") var paymentType: PaymentType = PaymentType.OTHER,

    @get:PropertyName("notes") @set:PropertyName("notes") var notes: String? = null,

    // Renamed for consistency with User model and general practice
    @get:PropertyName("currencyCode") @set:PropertyName("currencyCode") var currencyCode: String = "USD",

    // @iconResId: Storing Android resource IDs directly in a cross-platform DB like Firestore
    // is generally not recommended as these IDs can change.
    // Consider storing a string identifier for the icon (e.g., "food_icon", "travel_icon")
    // and mapping that to a drawable in your app. For now, I'll keep it but with a comment.
    @get:PropertyName("iconIdentifier") @set:PropertyName("iconIdentifier") var iconIdentifier: String? = null, // e.g., "restaurant", "flight"

    @get:PropertyName("tags") @set:PropertyName("tags") var tags: List<String> = emptyList(),
    @get:PropertyName("isRecurring") @set:PropertyName("isRecurring") var isRecurring: Boolean = false
) {
    // No-argument constructor required by Firestore for deserialization
    constructor() : this(
        id = "",
        userId = "",
        amount = 0.0,
        category = "",
        merchantName = null,
        date = null, // Will be set by @ServerTimestamp
        paymentType = PaymentType.OTHER,
        notes = null,
        currencyCode = "USD",
        iconIdentifier = null,
        tags = emptyList(),
        isRecurring = false
    )
}
