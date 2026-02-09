package com.example.pennypilot.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@IgnoreExtraProperties
data class Trip(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val destinationCurrency: String = "",
    val budget: Double = 0.0,
    val amountSpent: Double = 0.0,
    @ServerTimestamp
    val createdAt: Date? = null
)
