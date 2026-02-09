package com.example.pennypilot.data.repository

import com.example.pennypilot.data.model.CountryCurrency
import com.example.pennypilot.data.model.CurrencyData
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.model.ExchangeRatesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class CurrencyRepository @Inject constructor() {

    fun getLatestRates(base: String): Flow<Result<ExchangeRatesResponse>> {
        // In a real app, you would make the actual API call here.
        // For now, we return sample data.
        val sampleRates = mapOf("USD" to 1.0, "EUR" to 0.92, "JPY" to 157.5, "GBP" to 0.78, "CAD" to 1.37, "AUD" to 1.51, "CHF" to 0.90, "INR" to 83.5)
        val response = ExchangeRatesResponse(result = "success", documentation = "", terms_of_use = "", time_last_update_unix = 0, time_last_update_utc = "", time_next_update_unix = 0, time_next_update_utc = "", base_code = base, conversion_rates = sampleRates)
        return flowOf(Result.Success(response))
    }

    fun getHistoricalRates(base: String, target: String): Flow<Result<List<Float>>> {
        // This would also be a real API call. Returning sample data for the chart.
        val sampleHistory = List(7) { 0.92f + ((-1..1).random() * 0.01f * it) }
        return flowOf(Result.Success(sampleHistory))
    }

    fun getCountries(): Flow<List<CountryCurrency>> = flow {
        // For now, this returns the hardcoded list.
        // In the future, this could come from an API or a local database.
        emit(CurrencyData.countries)
    }
}
