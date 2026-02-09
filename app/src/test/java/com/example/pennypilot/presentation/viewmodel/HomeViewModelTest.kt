package com.example.pennypilot.presentation.viewmodel

import com.example.pennypilot.data.model.Expense
import com.example.pennypilot.data.model.PaymentType
import com.example.pennypilot.data.model.User
import com.example.pennypilot.data.repository.ExpenseRepository
import com.example.pennypilot.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@ExperimentalCoroutinesApi
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: HomeViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk(relaxed = true)
        expenseRepository = mockk(relaxed = true)
        firebaseAuth = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization with logged in user success`() = runTest {
        val uid = "test_uid"
        val mockUser = User(uid = uid, displayName = "John Doe", email = "john@doe.com", selectedCurrencyCode = "USD", currentBalance = 1234.56)
        val mockDate = Date()
        val mockExpenses = listOf(
            Expense(id = "1", userId = uid, amount = 50.0, category = "Food", date = mockDate, paymentType = PaymentType.CASH, notes = "Lunch")
        )

        val firebaseUser: FirebaseUser = mockk()
        val authStateListenerSlot = slot<FirebaseAuth.AuthStateListener>()

        every { firebaseAuth.addAuthStateListener(capture(authStateListenerSlot)) } answers { nothing }
        every { firebaseAuth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns uid

        coEvery { userRepository.getUserProfileFlow(uid) } returns flowOf(mockUser)
        coEvery { expenseRepository.getExpensesForUserFlow(uid, 5) } returns flowOf(mockExpenses)

        viewModel = HomeViewModel(userRepository, expenseRepository, firebaseAuth, firestore)

        authStateListenerSlot.captured.onAuthStateChanged(firebaseAuth)

        testDispatcher.scheduler.advanceUntilIdle()

        val expectedBalance = NumberFormat.getCurrencyInstance(Locale.US).format(1234.56)
        assertEquals("John Doe", viewModel.userName.value)
        assertEquals(expectedBalance, viewModel.balanceString.value)

        val uiExpenses = viewModel.uiRecentExpenses.value
        assertEquals(1, uiExpenses.size)
        val firstUiExpense = uiExpenses.first()
        assertEquals("1", firstUiExpense.id)
        assertEquals(50.0, firstUiExpense.originalAmount, 0.0)
        assertEquals("$", firstUiExpense.originalCurrency)
    }

    @Test
    fun `initialization with logged out user shows default state`() = runTest {
        every { firebaseAuth.currentUser } returns null

        viewModel = HomeViewModel(userRepository, expenseRepository, firebaseAuth, firestore)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("User", viewModel.userName.value)
        val expectedBalance = NumberFormat.getCurrencyInstance(Locale.US).format(0.0)
        assertEquals(expectedBalance, viewModel.balanceString.value)
        assertTrue(viewModel.uiRecentExpenses.value.isEmpty())
        assertEquals("User not logged in. Please log in.", viewModel.errorMessage.value)
    }
}
