package com.example.pennypilot.presentation.viewmodel

import com.example.pennypilot.data.local.entity.ExpenseEntity
import com.example.pennypilot.data.model.Expense
import com.example.pennypilot.data.model.PaymentType
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.repository.ExpenseRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date

@ExperimentalCoroutinesApi
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: DashboardViewModel
    private lateinit var expenseRepository: ExpenseRepository

    @Before
    fun setUp() {
        expenseRepository = mockk()
        viewModel = DashboardViewModel(expenseRepository)
    }

    @Test
    fun `loadExpenses with success`() = runTest {
        // Arrange
        val date = Date()
        val expenses = listOf(
            Expense(id = "1", userId = "user1", amount = 50.0, category = "Food", date = date, notes = "Lunch", paymentType = PaymentType.CASH, currencyCode = "USD", isRecurring = false)
        )
        val expectedEntities = listOf(
            ExpenseEntity(
                id = "1",
                userId = "user1",
                amount = 50.0,
                category = "Food",
                date = date.time,
                notes = "Lunch",
                merchantName = null,
                paymentType = PaymentType.CASH,
                currencyCode = "USD",
                iconIdentifier = null,
                tags = emptyList(),
                isRecurring = false
            )
        )
        coEvery { expenseRepository.getExpenses() } returns Result.Success(expenses)

        // Act
        viewModel.loadExpenses()
        // Ensure the coroutine launched in the ViewModel completes
        advanceUntilIdle()

        // Assert
        assertEquals(expectedEntities, viewModel.expenses.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorState.value)
    }

    @Test
    fun `loadExpenses with empty list`() = runTest {
        // Arrange
        coEvery { expenseRepository.getExpenses() } returns Result.Success(emptyList())

        // Act
        viewModel.loadExpenses()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.expenses.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorState.value)
    }

    @Test
    fun `loadExpenses with error`() = runTest {
        // Arrange
        val exception = Exception("Database error")
        coEvery { expenseRepository.getExpenses() } returns Result.Error(exception, "Database error")

        // Act
        viewModel.loadExpenses()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.expenses.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertEquals("Failed to load expenses: $exception", viewModel.errorState.value)
    }
}
