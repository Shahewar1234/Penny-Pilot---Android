package com.example.pennypilot.presentation.viewmodel

import app.cash.turbine.test
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.repository.SharedWalletRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SharedWalletsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule() // Ensure this rule is correctly implemented

    private lateinit var viewModel: SharedWalletsViewModel
    private lateinit var repository: SharedWalletRepository

    @Before
    fun setUp() {
        repository = mockk(relaxed = true)
        viewModel = SharedWalletsViewModel(repository)
    }

    @Test
    fun `createWallet with valid inputs succeeds`() = runTest {
        val walletName = "Test Wallet"
        val budget = "100.0"
        val numberOfPeople = "2"

        // Arrange: Mock the repository call to return success
        coEvery { repository.createWallet(walletName, 100.0, 2) } returns Result.Success(Unit)

        // Act & Assert
        viewModel.createWalletState.test {
            // 1. Consume the initial null state from the StateFlow
            assertEquals(null, awaitItem())

            // 2. Trigger the action while collecting
            viewModel.createWallet(walletName, budget, numberOfPeople)

            // 3. Assert the loading state was emitted
            assertEquals(Result.Loading, awaitItem())

            // 4. Assert the success state was emitted
            val successResult = awaitItem()
            assertTrue(successResult is Result.Success)
        }
    }

    @Test
    fun `createWallet with invalid budget fails`() = runTest {
        val walletName = "Test Wallet"
        val budget = "invalid" // Invalid input
        val numberOfPeople = "2"

        // Act & Assert
        viewModel.createWalletState.test {
            // 1. Consume the initial null state
            assertEquals(null, awaitItem())

            // 2. Trigger the action
            viewModel.createWallet(walletName, budget, numberOfPeople)

            // 3. Assert the error state
            val errorResult = awaitItem()
            assertTrue(errorResult is Result.Error)
            val exception = (errorResult as Result.Error).exception
            assertTrue(exception is IllegalArgumentException)
            assertEquals("Invalid name, budget, or number of people.", errorResult.message)
        }
    }

    @Test
    fun `createWallet with blank name fails`() = runTest {
        val walletName = "" // Invalid input
        val budget = "100.0"
        val numberOfPeople = "1"

        // Act & Assert
        viewModel.createWalletState.test {
            // 1. Consume the initial null state
            assertEquals(null, awaitItem())

            // 2. Trigger the action
            viewModel.createWallet(walletName, budget, numberOfPeople)

            // 3. Assert the error state
            val errorResult = awaitItem()
            assertTrue(errorResult is Result.Error)
            assertEquals("Invalid name, budget, or number of people.", (errorResult as Result.Error).message)
        }
    }
}
