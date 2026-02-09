package com.example.pennypilot.presentation.viewmodel

import com.example.pennypilot.data.model.User
import com.example.pennypilot.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileTabViewModel
    private lateinit var userRepository: UserRepository
    private lateinit var firebaseAuth: FirebaseAuth
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = mockk()
        firebaseAuth = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchUserProfile when user is logged in success`() = runTest {
        val mockUser = User(uid = "123", displayName = "Test User", email = "test@example.com")
        val firebaseUser = mockk<FirebaseUser>()
        coEvery { firebaseAuth.currentUser } returns firebaseUser
        coEvery { firebaseUser.uid } returns "123"
        coEvery { userRepository.getUserProfileFlow("123") } returns flowOf(mockUser)

        viewModel = ProfileTabViewModel(userRepository, firebaseAuth)

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(mockUser, viewModel.userProfile.value)
        assertEquals(false, viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `fetchUserProfile when user is not logged in`() = runTest {
        coEvery { firebaseAuth.currentUser } returns null

        viewModel = ProfileTabViewModel(userRepository, firebaseAuth)

        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.userProfile.value)
        assertEquals(false, viewModel.isLoading.value)
        assertEquals("User not logged in.", viewModel.errorMessage.value)
    }

    @Test
    fun `signOut calls firebaseAuth signOut`() {
        val firebaseUser = mockk<FirebaseUser>()
        coEvery { firebaseAuth.currentUser } returns firebaseUser
        coEvery { firebaseUser.uid } returns "123"
        coEvery { userRepository.getUserProfileFlow("123") } returns flowOf(null)
        coEvery { firebaseAuth.signOut() } returns Unit

        viewModel = ProfileTabViewModel(userRepository, firebaseAuth)
        viewModel.signOut()

        io.mockk.verify { firebaseAuth.signOut() }
    }
}
