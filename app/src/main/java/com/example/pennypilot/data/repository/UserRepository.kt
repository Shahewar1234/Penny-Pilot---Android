package com.example.pennypilot.data.repository

import com.example.pennypilot.data.model.User
import com.example.pennypilot.data.model.Result // <-- IMPORT YOUR CUSTOM RESULT
import kotlinx.coroutines.flow.Flow


/**
 * Repository interface for managing user profile data in Firestore.
 */
interface UserRepository {

    suspend fun createOrUpdateUserProfile(user: User): Result<Unit> // Uses your custom Result

    fun getUserProfileFlow(uid: String): Flow<User?> // This is fine

    suspend fun getUserProfileOnce(uid: String): Result<User?> // Uses your custom Result


    suspend fun updateSelectedCurrency(uid: String, currencyCode: String): Result<Unit>

    /**
     * Updates the current balance for a given user in Firestore.
     * @param uid The unique ID of the user.
     * @param newBalance The new balance amount.
     * @return Result<Unit> indicating success or failure.
     */
    suspend fun updateUserBalance(uid: String, newBalance: Double): Result<Unit>

    /**
     * Updates the last login timestamp for the user.
     * @param uid The unique ID of the user.
     * @return Result<Unit> indicating success or failure.
     */
    suspend fun updateUserLastLoginTimestamp(uid: String): Result<Unit>

    // The following methods are typically handled by Firebase Authentication directly,
    // not by a Firestore profile repository. You'll call Firebase Auth methods
    // in your ViewModel or an AuthRepository.
    //
    // suspend fun isUserExists(emailOrPhone: String): Boolean // Handled by Firebase Auth (e.g. fetchSignInMethodsForEmail)
    // suspend fun registerUser(emailOrPhone: String, password: String): Boolean // Handled by Firebase Auth
    // suspend fun loginUser(emailOrPhone: String, password: String): UserEntity? // Handled by Firebase Auth

    // If you need to check if a profile document exists for a UID:
    suspend fun doesUserProfileExist(uid: String): Result<Boolean>
}

