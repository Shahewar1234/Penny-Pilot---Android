package com.example.pennypilot.presentation.viewmodel

import app.cash.turbine.test
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.model.User
import com.example.pennypilot.data.repository.UserRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AddExpenseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: AddExpenseViewModel
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        firebaseAuth = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)

        val firebaseUser: FirebaseUser = mockk()
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "test_uid"

        val mockUser = User(uid = "test_uid", selectedCurrencyCode = "CAD")
        coEvery { userRepository.getUserProfileOnce("test_uid") } returns Result.Success(mockUser)

        viewModel = AddExpenseViewModel(firebaseAuth, firestore, userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveExpense with valid inputs succeeds`() = runTest {
        viewModel.amount.value = "120.50"
        viewModel.category.value = "Groceries"
        val mockUser = User(uid = "test_uid", email = "test@example.com", selectedCurrencyCode = "USD", currentBalance = 1000.0)
        coEvery { userRepository.getUserProfileOnce("test_uid") } returns Result.Success(mockUser)

        every { firestore.runTransaction<Void>(any()) } returns Tasks.forResult(null)

        viewModel.saveExpense()

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveExpenseResult.test {
            assertTrue(awaitItem() is Result.Success<*>)
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals("", viewModel.amount.value)
        assertEquals("", viewModel.category.value)
    }

    @Test
    fun `saveExpense with empty amount fails validation`() = runTest {
        viewModel.amount.value = ""
        viewModel.category.value = "Groceries"

        viewModel.saveExpense()

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveExpenseResult.test {
            val result = awaitItem() as Result.Error
            assertTrue(result.exception is ValidationException)
            assertEquals("Amount cannot be empty.", result.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveExpense when not authenticated fails`() = runTest {
        every { firebaseAuth.currentUser } returns null
        viewModel = AddExpenseViewModel(firebaseAuth, firestore, userRepository)

        viewModel.saveExpense()

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveExpenseResult.test {
            val result = awaitItem() as Result.Error
            assertTrue(result.exception is AuthenticationException)
            assertEquals("User not authenticated.", result.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveExpense with zero amount fails validation`() = runTest {
        viewModel.amount.value = "0"
        viewModel.category.value = "Groceries"

        viewModel.saveExpense()

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveExpenseResult.test {
            val result = awaitItem() as Result.Error
            assertTrue(result.exception is ValidationException)
            assertEquals("Please enter a valid positive amount.", result.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveExpense with empty category fails validation`() = runTest {
        viewModel.amount.value = "100"
        viewModel.category.value = ""

        viewModel.saveExpense()

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveExpenseResult.test {
            val result = awaitItem() as Result.Error
            assertTrue(result.exception is ValidationException)
            assertEquals("Category cannot be empty.", result.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveExpense when user profile fetch fails`() = runTest {
        viewModel.amount.value = "100"
        viewModel.category.value = "Groceries"
        val exception = Exception("Failed to fetch profile")
        coEvery { userRepository.getUserProfileOnce("test_uid") } returns Result.Error(exception, "Failed to fetch profile")

        viewModel.saveExpense()

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveExpenseResult.test {
            val result = awaitItem() as Result.Error
            assertTrue(result.exception is DataRetrievalException)
            assertEquals("Could not retrieve user profile to update balance.", result.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `saveExpense when firestore transaction fails`() = runTest {
        viewModel.amount.value = "100"
        viewModel.category.value = "Groceries"
        val mockUser = User(uid = "test_uid", email = "test@example.com", selectedCurrencyCode = "USD", currentBalance = 1000.0)
        coEvery { userRepository.getUserProfileOnce("test_uid") } returns Result.Success(mockUser)

        val exception = Exception("Firestore error")
        every { firestore.runTransaction<Void>(any()) } returns Tasks.forException(exception)

        viewModel.saveExpense()

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.saveExpenseResult.test {
            val result = awaitItem() as Result.Error
            assertEquals(exception, result.exception)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
