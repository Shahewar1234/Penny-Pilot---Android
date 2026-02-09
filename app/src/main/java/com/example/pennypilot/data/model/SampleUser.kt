package com.example.pennypilot.data.model

data class SampleUser(
    val name: String = "",
    val jobTitle: String = "",
    val location: String = "",
    val email: String = "",
    val phone: String = "",
    val bankDetails: SampleBankDetails? = null,
    val avatarUrl: String? = null
)