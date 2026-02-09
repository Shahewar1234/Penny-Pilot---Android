package com.example.pennypilot.data.model

data class SampleBankDetails(
    val bankName: String = "",
    val accountNumber: String = "", // As per your JSON, store full (display masked)
    val ifscCode: String = "",
    val branch: String = "",
    val accountType: String = "",
    val upiId: String = ""
)