package com.example.pennypilot.data.repository

import com.example.pennypilot.data.model.User // Your Firestore User model
import com.example.pennypilot.data.model.Result // Import YOUR custom Result class
// DO NOT import kotlin.Result if you intend to use your custom one exclusively here.

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserRepository { // Ensure UserRepository interface also expects your custom Result

    companion object {
        private const val USERS_COLLECTION = "users"
    }

    override suspend fun createOrUpdateUserProfile(user: User): Result<Unit> { // Returns your custom Result
        return try {
            Timber.d("Creating/Updating user profile in Firestore for UID: ${user.uid}")
            firestore.collection(USERS_COLLECTION).document(user.uid)
                .set(user, SetOptions.merge())
                .await()
            Result.Success(Unit) // Creates your custom Result.Success
        } catch (e: Exception) {
            Timber.e(e, "Error creating/updating user profile for UID: ${user.uid}")
            // Adjust constructor if your Result.Error is different
            Result.Error(e, e.message ?: "Failed to create/update profile.")
        }
    }

    override fun getUserProfileFlow(uid: String): Flow<User?> = callbackFlow {
        Timber.d("Setting up Firestore listener for user profile: $uid")
        val docRef = firestore.collection(USERS_COLLECTION).document(uid)

        val listenerRegistration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.w(error, "UserProfile listener error for UID: $uid")
                trySend(null).isFailure // This is for the Channel, not directly your Result type
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val user = snapshot.toObject<User>()
                Timber.d("UserProfile updated via listener for UID: $uid, User: $user")
                trySend(user).isSuccess // This is for the Channel
            } else {
                Timber.d("User profile null or does not exist via listener for UID: $uid")
                trySend(null).isSuccess // This is for the Channel
            }
        }
        awaitClose {
            Timber.d("Closing UserProfile listener for UID: $uid")
            listenerRegistration.remove()
        }
    }


    override suspend fun getUserProfileOnce(uid: String): Result<User?> { // Returns your custom Result
        return try {
            val documentSnapshot = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            if (documentSnapshot.exists()) {
                Result.Success(documentSnapshot.toObject<User>()) // Creates your custom Result.Success
            } else {
                Result.Success(null) // Creates your custom Result.Success
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user profile once for UID: $uid")
            Result.Error(e, e.message ?: "Failed to fetch profile.") // Adjust
        }
    }

    override suspend fun updateSelectedCurrency(uid: String, currencyCode: String): Result<Unit> { // Returns your custom Result
        return try {
            firestore.collection(USERS_COLLECTION).document(uid)
                .update("selectedCurrencyCode", currencyCode).await()
            Result.Success(Unit) // Creates your custom Result.Success
        } catch (e: Exception) {
            Timber.e(e, "Error updating currency for UID: $uid")
            Result.Error(e, e.message ?: "Failed to update currency.") // Adjust
        }
    }

    override suspend fun updateUserBalance(uid: String, newBalance: Double): Result<Unit> { // Returns your custom Result
        return try {
            firestore.collection(USERS_COLLECTION).document(uid)
                .update("currentBalance", newBalance).await()
            Result.Success(Unit) // Creates your custom Result.Success
        } catch (e: Exception) {
            Timber.e(e, "Error updating balance for UID: $uid")
            Result.Error(e, e.message ?: "Failed to update balance.") // Adjust
        }
    }

    override suspend fun updateUserLastLoginTimestamp(uid: String): Result<Unit> { // Returns your custom Result
        return try {
            firestore.collection(USERS_COLLECTION).document(uid)
                .update("lastLoginDate", FieldValue.serverTimestamp())
                .await()
            Result.Success(Unit) // Creates your custom Result.Success
        } catch (e: Exception) {
            Timber.e(e, "Error updating last login timestamp for UID: $uid")
            Result.Error(e, e.message ?: "Failed to update last login.") // Adjust
        }
    }

    override suspend fun doesUserProfileExist(uid: String): Result<Boolean> { // Returns your custom Result
        return try {
            val document = firestore.collection(USERS_COLLECTION).document(uid).get().await()
            Result.Success(document.exists()) // Creates your custom Result.Success
        } catch (e: Exception) {
            Timber.e(e, "Error checking if user profile exists for UID: $uid")
            Result.Error(e, e.message ?: "Failed to check profile existence.") // Adjust
        }
    }
}

