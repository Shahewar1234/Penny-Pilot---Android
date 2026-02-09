package com.example.pennypilot.data.repository

import android.annotation.SuppressLint
import com.example.pennypilot.data.model.SampleUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleUserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) {

    @SuppressLint("RestrictedApi")
    fun getSampleUserFlow(): Flow<SampleUser?> {
        val currentUser = auth.currentUser

        // --- FIX #1: Immediately stop if the user is not logged in ---
        // If there's no user, there's no data to fetch. Return a flow that emits null.
        if (currentUser == null) {
            Timber.w("getSampleUserFlow called but user is not authenticated.")
            return flowOf(null)
        }

        // --- FIX #2: Point to the correct, specific path for the current user ---
        // Your rules only allow reading from "/users/{auth.uid}", so we build that exact path.
        val userRef = database.getReference("users").child(currentUser.uid)

        // Use callbackFlow to wrap the listener in a modern coroutine Flow
        return callbackFlow {
            val valueEventListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val sampleUser = snapshot.getValue(SampleUser::class.java)
                        Timber.d("User data received for user ID: ${currentUser.uid}")
                        // Send the user data into the flow.
                        trySend(sampleUser)
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing user data from snapshot.")
                        // If parsing fails, send null.
                        trySend(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // "Permission Denied" errors will be caught here.
                    // This will now only happen if there's a genuine rules mismatch,
                    // not a timing issue.
                    Timber.e(error.toException(), "Database read for user ${currentUser.uid} was cancelled.")
                    // Close the flow with the exception.
                    close(error.toException())
                }
            }

            Timber.d("Attaching listener to path: ${userRef.path}")
            userRef.addValueEventListener(valueEventListener)

            // When the coroutine listening to this flow is cancelled, remove the listener.
            awaitClose {
                Timber.d("Removing listener from path: ${userRef.path}")
                userRef.removeEventListener(valueEventListener)
            }
        }
    }
}
