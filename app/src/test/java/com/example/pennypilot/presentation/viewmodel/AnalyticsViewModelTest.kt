package com.example.pennypilot.presentation.viewmodel

import app.cash.turbine.test
import com.example.pennypilot.data.model.Expense
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AnalyticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var viewModel: AnalyticsViewModel

    @Before
    fun setUp() {
        expenseRepository = mockk()
    }

    @Test
    fun `when initialized, loads data and calculates stats correctly`() = runTest {
        // Arrange
        val expenses = listOf(
            Expense(category = "Food", amount = 50.0),
            Expense(category = "Transport", amount = 30.0),
            Expense(category = "Food", amount = 70.0)
        )
        coEvery { expenseRepository.getExpenses() } returns Result.Success(expenses)

        // Act
        viewModel = AnalyticsViewModel(expenseRepository)

        // Assert
        viewModel.uiState.test {
            val successState = awaitItem()

            assertFalse(successState.isLoading)
            assertNull(successState.errorMessage)
            assertEquals(150.0, successState.totalSpent, 0.001)
            assertEquals(50.0, successState.dailyAverage, 0.001)
            assertEquals("Food", successState.topCategory)
            assertEquals(mapOf("Food" to 120.0f, "Transport" to 30.0f), successState.spendingByCategory)
        }
    }

    @Test
    fun `when repo returns error, enters error state`() = runTest {
        // Arrange
        val errorMessage = "Failed to load expenses."
        coEvery { expenseRepository.getExpenses() } returns Result.Error(Exception(errorMessage), errorMessage)

        // Act
        viewModel = AnalyticsViewModel(expenseRepository)

        // Assert
        viewModel.uiState.test {
            val errorState = awaitItem()

            assertFalse(errorState.isLoading)
            assertEquals(errorMessage, errorState.errorMessage)
        }
    }

    @Test
    fun `when repo returns empty list, calculates zero stats`() = runTest {
        // Arrange
        coEvery { expenseRepository.getExpenses() } returns Result.Success(emptyList<Expense>())

        // Act
        viewModel = AnalyticsViewModel(expenseRepository)

        // Assert
        viewModel.uiState.test {
            val successState = awaitItem()

            assertFalse(successState.isLoading)
            assertNull(successState.errorMessage)
            assertEquals(0.0, successState.totalSpent, 0.001)
            assertEquals(0.0, successState.dailyAverage, 0.001)
            assertEquals("N/A", successState.topCategory)
            assertTrue(successState.spendingByCategory.isEmpty())
        }
    }

    @Test
    fun `refreshData reloads data and updates state`() = runTest {
        // Arrange for initial load
        val initialExpenses = listOf(Expense(category = "Groceries", amount = 100.0))
        coEvery { expenseRepository.getExpenses() } returns Result.Success(initialExpenses)

        // Act: Initialize ViewModel, which triggers the first load.
        viewModel = AnalyticsViewModel(expenseRepository)

        // Assert everything in a single test block to avoid race conditions.
        viewModel.uiState.test {
            // 1. Await and verify the result of the initial load from the init block.
            val initialState = awaitItem()
            assertFalse("Initial state should not be loading", initialState.isLoading)
            assertEquals(100.0, initialState.totalSpent, 0.001)
            assertEquals("Groceries", initialState.topCategory)

            // 2. Arrange for the refresh action.
            val refreshedExpenses = listOf(
                Expense(category = "Dining", amount = 80.0),
                Expense(category = "Entertainment", amount = 120.0)
            )
            coEvery { expenseRepository.getExpenses() } returns Result.Success(refreshedExpenses)

            // 3. Act: Trigger the refresh.
            viewModel.refreshData()

            // 4. Await and verify the loading state. The state updates to isLoading=true while keeping the old data.
            val loadingState = awaitItem()
            assertTrue("State should be loading during refresh", loadingState.isLoading)

            // 5. Await and verify the final success state after the refresh.
            val successState = awaitItem()
            assertFalse("Final state should not be loading", successState.isLoading)
            assertEquals(200.0, successState.totalSpent, 0.001)
            assertEquals("Entertainment", successState.topCategory)
        }
    }
}
