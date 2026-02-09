package com.example.pennypilot.presentation.viewmodel
import com.google.firebase.Timestamp // Important: use the Firebase Timestamp

data class ChatMessage(
    val text: String = "",
    val isFromUser: Boolean = false,
    val timestamp: Timestamp = Timestamp.now() // Add this
)


