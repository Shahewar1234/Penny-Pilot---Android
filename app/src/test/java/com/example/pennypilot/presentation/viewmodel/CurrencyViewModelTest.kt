package com.example.pennypilot.presentation.viewmodel

import app.cash.turbine.test
import com.example.pennypilot.data.model.ExchangeRatesResponse
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.repository.CurrencyRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CurrencyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: CurrencyViewModel
    private lateinit var currencyRepository: CurrencyRepository

    @Before
    fun setUp() {
        currencyRepository = mockk()
        viewModel = CurrencyViewModel(currencyRepository)
    }

    @Test
    fun `fetchRates with success`() = runTest {
        // Arrange
        val baseCurrency = "USD"
        val ratesResponse = ExchangeRatesResponse(
            result = "success",
            documentation = "",
            terms_of_use = "",
            time_last_update_unix = 0,
            time_last_update_utc = "",
            time_next_update_unix = 0,
            time_next_update_utc = "",
            base_code = baseCurrency,
            conversion_rates = mapOf("EUR" to 0.9, "GBP" to 0.8)
        )
        val successResult = Result.Success(ratesResponse)
        coEvery { currencyRepository.getLatestRates(baseCurrency) } returns flowOf(successResult)

        // Act & Assert
        viewModel.exchangeRates.test {
            // 1. Consume the initial Loading state from the StateFlow's initialization
            assertEquals(Result.Loading, awaitItem())

            // 2. Trigger the action that will cause the state to change
            viewModel.fetchRates(baseCurrency)

            // 3. Assert the final Success state. The intermediate Loading state is conflated
            //    by the StateFlow because the value doesn't change.
            assertEquals(successResult, awaitItem())
        }
    }

    @Test
    fun `fetchRates with error`() = runTest {
        // Arrange
        val baseCurrency = "USD"
        val exception = Exception("Network Error")
        val errorResult = Result.Error(exception, "Network Error")
        coEvery { currencyRepository.getLatestRates(baseCurrency) } returns flowOf(errorResult)

        // Act & Assert
        viewModel.exchangeRates.test {
            // 1. Consume the initial Loading state
            assertEquals(Result.Loading, awaitItem())

            // 2. Trigger the action
            viewModel.fetchRates(baseCurrency)

            // 3. Assert the final Error state.
            assertEquals(errorResult, awaitItem())
        }
    }
}
