package com.example.pennypilot.data.mapper

import com.example.pennypilot.data.model.ExchangeRatesResponse
import javax.inject.Inject

class CurrencyMapper @Inject constructor() {

    fun mapRatesResponseToExchangeRateResponse(
        apiResponse: com.example.pennypilot.data.model.ExchangeRatesResponse
    ): ExchangeRatesResponse {
        return apiResponse
    }
}
