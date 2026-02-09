package com.example.pennypilot.data.repository

import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.model.SharedWallet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class SharedWalletRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    // A predefined palette of colors that work well with your dark theme
    private val colorPalette = listOf(
        "#61BC79", // Your primary accent
        "#4CAF50", // A slightly deeper green
        "#8BC34A", // A lighter, vibrant green
        "#009688", // A teal shade
        "#26A69A", // A lighter teal
        "#00BCD4", // Cyan
        "#2196F3"  // A friendly blue
    )
    suspend fun getUserCurrency(): String {
        val uid = auth.currentUser?.uid ?: return "USD"
        return try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            userDoc.getString("selectedCurrencyCode") ?: "USD"
        } catch (e: Exception) {
            "USD"
        }
    }

    fun getSharedWallets(): Flow<Result<List<SharedWallet>>> = flow {
        emit(Result.Loading)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(Result.Error(Exception("User not logged in"), "User not logged in"))
            return@flow
        }

        try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            @Suppress("UNCHECKED_CAST")
            val sharedWalletIds = userDoc["sharedWalletIds"] as? List<String> ?: emptyList()

            if (sharedWalletIds.isEmpty()) {
                emit(Result.Success(emptyList()))
                return@flow
            }

            val wallets = firestore.collection("shared_wallets")
                .whereIn("id", sharedWalletIds)
                .get()
                .await()
                .documents.mapNotNull { it.toObject<SharedWallet>() }

            emit(Result.Success(wallets))
        } catch (e: Exception) {
            emit(Result.Error(e, "Failed to fetch shared wallets."))
        }
    }

    suspend fun createWallet(name: String, budget: Double, numberOfPeople: Int): Result<Unit> {
        val currentUser = auth.currentUser
        if (currentUser?.uid == null) {
            return Result.Error(Exception("User not authenticated."), "User not authenticated.")
        }

        return try {
            // Start a transaction to ensure atomicity
            firestore.runTransaction { transaction ->
                val userRef = firestore.collection("users").document(currentUser.uid)
                val userDoc = transaction.get(userRef)

                // Fetch user's currency from their profile, defaulting to USD if not found
                val userCurrency = userDoc.getString("selectedCurrencyCode") ?: "USD"

                val newWalletRef = firestore.collection("shared_wallets").document()

                val wallet = SharedWallet(
                    id = newWalletRef.id,
                    name = name,
                    ownerId = currentUser.uid,
                    memberIds = listOf(currentUser.uid),
                    totalBudget = budget,
                    amountSpent = 0.0,
                    color = colorPalette.random(),
                    currencyCode = userCurrency, // Use the fetched currency
                    numberOfPeople = numberOfPeople,
                    createdAt = Date()
                )

                // Save the new wallet and update the user's wallet list
                transaction.set(newWalletRef, wallet)
                transaction.update(userRef, "sharedWalletIds", FieldValue.arrayUnion(newWalletRef.id))

                null // Transaction must return a value
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to create wallet in database.")
        }
    }


    // --- ADDED FUNCTION ---
    /**
     * Deletes a shared wallet from Firestore and removes its ID from the user's document.
     * This operation is performed in a transaction to ensure data integrity.
     */
    suspend fun deleteWallet(walletId: String): Result<Unit> {
        val currentUser = auth.currentUser
        if (currentUser?.uid == null) {
            return Result.Error(Exception("User not authenticated."), "User not authenticated.")
        }

        val walletRef = firestore.collection("shared_wallets").document(walletId)
        val userRef = firestore.collection("users").document(currentUser.uid)

        return try {
            // Use a transaction to ensure both operations succeed or both fail.
            firestore.runTransaction { transaction ->
                // 1. Delete the shared wallet document itself.
                transaction.delete(walletRef)
                // 2. Remove the wallet's ID from the user's list of shared wallets.
                transaction.update(userRef, "sharedWalletIds", FieldValue.arrayRemove(walletId))
            }.await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Failed to delete wallet.")
        }
    }
}
