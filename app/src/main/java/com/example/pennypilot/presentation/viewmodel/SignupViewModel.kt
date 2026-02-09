package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.model.User // Your Firestore User model
import com.example.pennypilot.data.repository.UserRepository // Your UserRepository interface
import com.example.pennypilot.data.model.Result // YOUR custom Result class
// Import your AuthResult if it's in a different package
// import com.example.pennypilot.util.AuthResult // Or wherever it is defined

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import javax.inject.Inject

// Make sure AuthResult is defined and accessible
// (e.g., in this file, or imported from another file like shown above)


@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository // This will return your custom Result
) : ViewModel() {

    private val _signUpResultState = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val signUpResultState: StateFlow<AuthResult> = _signUpResultState.asStateFlow()

    fun signUp(
        email: String,
        password: String,
        displayName: String,
        selectedCurrency: String
    ) {
        if (displayName.isBlank()) {
            _signUpResultState.value = AuthResult.Error("Display Name cannot be empty.")
            return
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {            _signUpResultState.value = AuthResult.Error("Please enter a valid email address.")
            return
        }
        if (password.length < 6) {
            _signUpResultState.value = AuthResult.Error("Password must be at least 6 characters.")
            return
        }
        if (selectedCurrency.isBlank()) {
            _signUpResultState.value = AuthResult.Error("Please select a currency.")
            return
        }

        _signUpResultState.value = AuthResult.Loading
        viewModelScope.launch {
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Update Firebase Auth display name (non-critical path for AuthResult)
                    updateFirebaseAuthProfile(firebaseUser, displayName.trim())

                    // Create profile in Firestore (critical path for overall success)
                    val profileCreationResult = createUserProfileInFirestore(
                        firebaseUser = firebaseUser,
                        displayName = displayName.trim(),
                        selectedCurrencyCode = selectedCurrency
                    )

                    // Check the result based on YOUR custom Result class
                    when (profileCreationResult) {
                        is Result.Success -> {
                            Timber.d("Sign up and profile creation successful for UID: ${firebaseUser.uid}")
                            _signUpResultState.value = AuthResult.Success(firebaseUser.uid)
                        }
                        is Result.Error -> {
                            // Use the message and exception from your custom Result.Error
                            val errorMessage = profileCreationResult.message ?: "Firestore profile save failed."
                            Timber.e(profileCreationResult.exception, "Firestore profile creation failed for UID: ${firebaseUser.uid}. Error: $errorMessage")
                            _signUpResultState.value = AuthResult.Error("Signup succeeded, but profile save failed: $errorMessage")
                            // Optional: Consider deleting firebaseUser here if profile save is critical
                            // and you want to roll back the auth creation.
                            // firebaseUser.delete().await() // Requires careful error handling
                        }
                        is Result.Loading -> {
                            // This case implies your userRepository.createOrUpdateUserProfile
                            // is implemented to return a Loading state before completion,
                            // which is fine, but you need to handle it.
                            // For a simple suspend function, it might directly go to Success/Error.
                            Timber.w("Firestore profile creation is still loading for UID: ${firebaseUser.uid} - This might be unexpected if the repo function is a simple suspend.")
                            // Decide if you want to keep _signUpResultState as AuthResult.Loading
                            // or transition to an error if this state is prolonged or unexpected.
                            // If createOrUpdateUserProfile is a quick suspend function, this case might not be hit
                            // unless you explicitly emit Loading from within it.
                            // _signUpResultState.value = AuthResult.Error("Profile creation is taking too long.")
                        }
                    }
                } else {
                    Timber.w("Firebase Auth user is null after account creation attempt.")
                    _signUpResultState.value = AuthResult.Error("Account creation failed (Firebase user is null).")
                }
            } catch (e: Exception) {
                Timber.e(e, "Sign up error during Firebase Auth operation")
                _signUpResultState.value = AuthResult.Error(e.message ?: "An unknown error occurred during sign up.")
            }
        }
    }

    private suspend fun updateFirebaseAuthProfile(firebaseUser: FirebaseUser, displayName: String) {
        try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()
            Timber.d("Firebase Auth profile updated for: $displayName")
        } catch (e: Exception) {
            Timber.e(e, "Error updating Firebase Auth profile for UID: ${firebaseUser.uid}. This is non-fatal to signup flow.")
            // This error is typically non-fatal for the sign-up flow's success criteria.
            // It's logged, but we don't change _signUpResultState based on this alone.
        }
    }

    // This function now correctly expects and returns your custom Result<Unit>
    private suspend fun createUserProfileInFirestore(
        firebaseUser: FirebaseUser,
        displayName: String,
        selectedCurrencyCode: String
    ): Result<Unit> { // Expects com.example.pennypilot.data.model.Result
        val userProfile = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = displayName,
            // Consider using FieldValue.serverTimestamp() via the repository if preferred
            // and if your User model's lastLoginDate is set up for it (e.g., @ServerTimestamp annotation
            // or is of type com.google.firebase.Timestamp and handled in Firestore directly)
            // For now, setting client-side date.
            lastLoginDate = Date(),
            selectedCurrencyCode = selectedCurrencyCode,
            currentBalance = 0.0,
            profileImageUrl = firebaseUser.photoUrl?.toString()

            // registrationDate could be another FieldValue.serverTimestamp() candidate
        )
        // This call now returns your custom com.example.pennypilot.data.model.Result<Unit>
        return userRepository.createOrUpdateUserProfile(userProfile)
    }

    fun resetSignUpState() {
        _signUpResultState.value = AuthResult.Idle
    }
}

