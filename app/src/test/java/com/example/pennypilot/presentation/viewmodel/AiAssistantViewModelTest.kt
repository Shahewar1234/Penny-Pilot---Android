package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.pennypilot.data.repository.AiRepository
import com.example.pennypilot.presentation.viewmodel.ChatMessage
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AiAssistantViewModelTest {

    private lateinit var viewModel: AiAssistantViewModel
    private lateinit var aiRepository: AiRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        aiRepository = mockk()
        savedStateHandle = SavedStateHandle()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadConversation updates state to success`() = runTest {
        val conversationId = "test_id"
        val messages = listOf(ChatMessage(text = "Hello", isFromUser = true))
        coEvery { aiRepository.getConversationMessages(conversationId) } returns messages
        savedStateHandle["conversationId"] = conversationId

        viewModel = AiAssistantViewModel(aiRepository, savedStateHandle)

        viewModel.uiState.test {
            assertEquals(AiAssistantState.Initial, awaitItem())
            assertEquals(AiAssistantState.Loading(), awaitItem())
            val successState = awaitItem() as AiAssistantState.Success
            assertEquals(messages, successState.conversation)
        }

        coVerify { aiRepository.getConversationMessages(conversationId) }
    }

    @Test
    fun `getSuggestion creates new conversation and updates state`() = runTest {
        val prompt = "New prompt"
        val newConversationId = "new_id"
        val aiResponse = ChatMessage(text = "AI Response", isFromUser = false)

        coEvery { aiRepository.createNewConversation(prompt) } returns newConversationId
        coEvery { aiRepository.getAiResponse(prompt, newConversationId) } returns aiResponse

        viewModel = AiAssistantViewModel(aiRepository, savedStateHandle)

        viewModel.getSuggestion(prompt)

        viewModel.uiState.test {
            assertEquals(AiAssistantState.Initial, awaitItem())

            val loadingState = awaitItem() as AiAssistantState.Loading
            assertEquals(1, loadingState.conversation.size)
            assertEquals(prompt, loadingState.conversation.first().text)
            assertTrue(loadingState.conversation.first().isFromUser)

            val successState = awaitItem() as AiAssistantState.Success
            assertEquals(2, successState.conversation.size)
            assertEquals(prompt, successState.conversation[0].text)
            assertEquals(aiResponse.text, successState.conversation[1].text)
        }

        coVerify { aiRepository.createNewConversation(prompt) }
        coVerify { aiRepository.getAiResponse(prompt, newConversationId) }
    }

    @Test
    fun `resetState resets to initial state`() = runTest {
        viewModel = AiAssistantViewModel(aiRepository, savedStateHandle)
        viewModel.resetState()
        viewModel.uiState.test {
            assertEquals(AiAssistantState.Initial, awaitItem())
        }
    }
}
