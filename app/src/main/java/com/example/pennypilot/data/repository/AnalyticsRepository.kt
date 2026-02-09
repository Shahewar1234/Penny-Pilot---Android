package com.example.pennypilot.data.repository

import com.example.pennypilot.data.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Repository for fetching analytics data.
 * In a real app, this would query Firestore.
 * For now, it returns sample data.
 */
class AnalyticsRepository @Inject constructor() {

    fun getSpendingByCategory(): Flow<Result<Map<String, Float>>> = flowOf(
        Result.Success(
            mapOf(
                "Food" to 450f,
                "Transport" to 220f,
                "Shopping" to 310f,
                "Entertainment" to 180f,
                "Utilities" to 250f,
                "Other" to 90f
            )
        )
    )

    fun getSpendingTrend(): Flow<Result<List<Float>>> = flowOf(
        Result.Success(
            // Sample data for the last 7 days
            listOf(100f, 120f, 90f, 150f, 130f, 180f, 200f)
        )
    )
}
