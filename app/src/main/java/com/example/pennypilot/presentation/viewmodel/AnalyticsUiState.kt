package com.example.pennypilot.presentation.viewmodel

data class AnalyticsUiState(
    val totalSpent: Double = 0.0,
    val dailyAverage: Double = 0.0,
    val topCategory: String = "N/A",
    val spendingByCategory: Map<String, Float> = emptyMap(),
    val spendingTrend: List<Float> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
