package com.example.pennypilot.data.repository

import com.example.pennypilot.presentation.viewmodel.ChatMessage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// A data class to represent a conversation in the history list
data class Conversation(
    val id: String,
    val title: String,
    val lastUpdated: Timestamp
)

@Singleton
class AiRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val generativeModel: GenerativeModel
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    // Gets the AI's response and saves both messages to Firestore
    suspend fun getAiResponse(
        prompt: String,
        conversationId: String
    ): ChatMessage {
        val userId = currentUserId ?: throw IllegalStateException("User not logged in")

        // 1. Save the user's message to Firestore
        val userMessage = ChatMessage(text = prompt, isFromUser = true, timestamp = Timestamp.now())
        firestore.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .collection("messages").add(userMessage).await()

        // 2. Get response from the Generative AI model
        val inputContent = content {
            text("You are Penny, a friendly and insightful financial assistant for the PennyPilot app. " +
                    "Your goal is to provide personalized, actionable tips to help users manage their money better. " +
                    "Based on the user's query, offer suggestions to cut costs, optimize savings, or find budget-friendly options. " +
                    "User Query: \"$prompt\"")
        }
        val response = generativeModel.generateContent(inputContent)
        val aiResponseText = response.text ?: "Sorry, I couldn't process that. Please try again."

        // 3. Save the AI's message to Firestore
        val aiMessage = ChatMessage(text = aiResponseText, isFromUser = false, timestamp = Timestamp.now())
        firestore.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .collection("messages").add(aiMessage).await()

        // 4. Update the conversation's "lastUpdated" timestamp
        firestore.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .update("lastUpdated", Timestamp.now()).await()

        return aiMessage
    }

    // Creates a new conversation document in Firestore
    suspend fun createNewConversation(firstMessage: String): String {
        val userId = currentUserId ?: throw IllegalStateException("User not logged in")
        val conversationData = mapOf(
            "title" to firstMessage,
            "lastUpdated" to Timestamp.now()
        )
        val newConversationRef = firestore.collection("users").document(userId)
            .collection("conversations").add(conversationData).await()
        return newConversationRef.id
    }

    // Fetches all messages for a specific conversation
    suspend fun getConversationMessages(conversationId: String): List<ChatMessage> {
        val userId = currentUserId ?: return emptyList()
        val snapshot = firestore.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .collection("messages").orderBy("timestamp", Query.Direction.ASCENDING)
            .get().await()

        return snapshot.toObjects(ChatMessage::class.java)
    }

    // Fetches a list of all past conversations for the user
    suspend fun getConversationHistory(): List<Conversation> {
        val userId = currentUserId ?: return emptyList()
        val snapshot = firestore.collection("users").document(userId)
            .collection("conversations").orderBy("lastUpdated", Query.Direction.DESCENDING)
            .get().await()

        return snapshot.documents.mapNotNull { doc ->
            Conversation(
                id = doc.id,
                title = doc.getString("title") ?: "Untitled",
                lastUpdated = doc.getTimestamp("lastUpdated") ?: Timestamp.now()
            )
        }
    }
}
