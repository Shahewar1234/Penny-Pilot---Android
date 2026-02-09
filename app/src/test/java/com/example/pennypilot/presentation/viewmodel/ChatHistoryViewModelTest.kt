package com.example.pennypilot.presentation.viewmodel

import app.cash.turbine.test
import com.example.pennypilot.data.repository.AiRepository
import com.example.pennypilot.data.repository.Conversation
import com.google.firebase.Timestamp
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
class ChatHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ChatHistoryViewModel
    private lateinit var aiRepository: AiRepository

    @Before
    fun setUp() {
        aiRepository = mockk()
        viewModel = ChatHistoryViewModel(aiRepository)
    }

    @Test
    fun `loadHistory with conversations succeeds`() = runTest {
        // Arrange
        val conversations = listOf(Conversation(id = "1", title = "Hello", lastUpdated = Timestamp.now()))
        coEvery { aiRepository.getConversationHistory() } returns conversations

        // Act & Assert
        viewModel.uiState.test {
            // 1. Consume the initial Loading state from the StateFlow
            assertEquals(HistoryUiState.Loading, awaitItem())

            // 2. Trigger the action while collecting the flow
            viewModel.loadHistory()

            // 3. Assert the final Success state
            val result = awaitItem() as HistoryUiState.Success
            assertEquals(conversations, result.conversations)
        }
    }

    @Test
    fun `loadHistory with empty list succeeds`() = runTest {
        // Arrange
        coEvery { aiRepository.getConversationHistory() } returns emptyList()

        // Act & Assert
        viewModel.uiState.test {
            // 1. Consume the initial Loading state
            assertEquals(HistoryUiState.Loading, awaitItem())

            // 2. Trigger the action
            viewModel.loadHistory()

            // 3. Assert the final Success state with an empty list
            val result = awaitItem() as HistoryUiState.Success
            assertTrue(result.conversations.isEmpty())
        }
    }

    @Test
    fun `loadHistory with error fails`() = runTest {
        // Arrange
        val exception = Exception("Failed to load history")
        coEvery { aiRepository.getConversationHistory() } throws exception

        // Act & Assert
        viewModel.uiState.test {
            // 1. Consume the initial Loading state
            assertEquals(HistoryUiState.Loading, awaitItem())

            // 2. Trigger the action
            viewModel.loadHistory()

            // 3. Assert the final Error state
            val result = awaitItem() as HistoryUiState.Error
            assertEquals("Failed to load chat history: ${exception.localizedMessage}", result.message)
        }
    }
}
