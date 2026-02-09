package com.example.pennypilot.data.repository

import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.model.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    fun getTrips(): Flow<Result<List<Trip>>> {
        val userId = currentUserId
            ?: return kotlinx.coroutines.flow.flowOf(Result.Error(Exception("User not authenticated"), "User not authenticated"))

        return firestore.collection("trips")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                try {
                    val trips = snapshot.toObjects<Trip>()
                    Result.Success(trips)
                } catch (e: Exception) {
                    Result.Error(e, "Failed to parse trips: ${e.message}")
                }
            }
    }

    suspend fun createTrip(name: String, budget: Double, currency: String): Result<Trip> {
        val userId = currentUserId ?: return Result.Error(Exception("User not authenticated"), "User not authenticated")

        return try {
            val newTripRef = firestore.collection("trips").document()
            val trip = Trip(
                id = newTripRef.id,
                userId = userId,
                name = name,
                budget = budget,
                destinationCurrency = currency
            )
            newTripRef.set(trip).await()
            Result.Success(trip)
        } catch (e: Exception) {
            Result.Error(e, "Database error: ${e.message}")
        }
    }

    suspend fun deleteTrip(tripId: String): Result<Unit> {
        currentUserId ?: return Result.Error(Exception("User not authenticated"), "User not authenticated")

        return try {
            firestore.collection("trips").document(tripId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e, "Database error: ${e.message}")
        }
    }
}
