package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// This data class should be in its own file, but is here for context
// Make sure it has default values for Firestore deserialization

// UI State definition remains the same
sealed class AiAssistantState {
    object Initial : AiAssistantState()
    data class Loading(val conversation: List<ChatMessage> = emptyList()) : AiAssistantState()
    data class Success(val conversation: List<ChatMessage>) : AiAssistantState()
    data class Error(val message: String) : AiAssistantState()
}

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val savedStateHandle: SavedStateHandle // Used to receive arguments from navigation
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiAssistantState>(AiAssistantState.Initial)
    val uiState: StateFlow<AiAssistantState> = _uiState.asStateFlow()

    // This will hold the ID of the current chat session
    private var currentConversationId: String? = null

    init {
        // Check if a conversationId was passed from the history screen
        val conversationId: String? = savedStateHandle["conversationId"]
        if (conversationId != null) {
            loadConversation(conversationId)
        }
    }

    /**
     * Loads a previous conversation from the repository.
     */
    fun loadConversation(conversationId: String) {
        currentConversationId = conversationId
        viewModelScope.launch {
            _uiState.value = AiAssistantState.Loading()
            try {
                val messages = aiRepository.getConversationMessages(conversationId)
                _uiState.value = AiAssistantState.Success(messages)
            } catch (e: Exception) {
                _uiState.value = AiAssistantState.Error("Failed to load chat: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Sends a new prompt, creating a new conversation if needed, and fetches the AI response.
     */
    fun getSuggestion(prompt: String) {
        viewModelScope.launch {
            try {
                // If this is the first message, create a new conversation document first
                if (currentConversationId == null) {
                    currentConversationId = aiRepository.createNewConversation(prompt)
                }

                val currentConversation = when (val state = _uiState.value) {
                    is AiAssistantState.Success -> state.conversation
                    is AiAssistantState.Loading -> state.conversation
                    else -> emptyList()
                }

                val userMessage = ChatMessage(text = prompt, isFromUser = true)
                val conversationWithUserMessage = currentConversation + userMessage

                // Immediately update the UI with the user's message and enter loading state
                _uiState.value = AiAssistantState.Loading(conversationWithUserMessage)

                // Call the repository to get the AI response and save everything
                val aiMessage = aiRepository.getAiResponse(prompt, currentConversationId!!)

                // Update the UI with the final AI response
                val finalConversation = conversationWithUserMessage + aiMessage
                _uiState.value = AiAssistantState.Success(finalConversation)

            } catch (e: Exception) {
                _uiState.value = AiAssistantState.Error("An error occurred: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Resets the screen to its initial state for a new chat.
     */
    fun resetState() {
        currentConversationId = null
        _uiState.value = AiAssistantState.Initial
    }
}
