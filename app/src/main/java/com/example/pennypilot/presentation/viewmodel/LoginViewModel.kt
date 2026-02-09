package com.example.pennypilot.presentation.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import javax.inject.Inject

@Immutable
sealed class AuthResult {
    data class Success(val userId: String) : AuthResult() // Changed parameter name for clarity
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
    object Idle : AuthResult()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    var authResultState by mutableStateOf<AuthResult>(AuthResult.Idle)
        private set

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    fun login(email: String, password: String) {
        if (!isValidEmail(email) || password.isBlank()) {
            authResultState = AuthResult.Error(
                when {
                    !isValidEmail(email) && password.isBlank() -> "Please enter a valid email and password."
                    !isValidEmail(email) -> "Please enter a valid email address."
                    else -> "Password cannot be empty."
                }
            )
            return
        }
        authResultState = AuthResult.Loading
        viewModelScope.launch {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                result.user?.uid?.let { uid -> // Safely access UID
                    authResultState = AuthResult.Success(uid)
                } ?: run {
                    authResultState = AuthResult.Error("Login successful, but user data is null.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                authResultState = AuthResult.Error(e.message ?: "Login failed.")
            }
        }
    }

    fun signUp(email: String, password: String) {
        if (!isValidEmail(email) || password.length < 6) {
            authResultState = AuthResult.Error(
                when {
                    !isValidEmail(email) && password.length < 6 -> "Valid email & password (min 6 chars) required."
                    !isValidEmail(email) -> "Please enter a valid email address."
                    else -> "Password must be at least 6 characters."
                }
            )
            return
        }
        authResultState = AuthResult.Loading
        viewModelScope.launch {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                result.user?.uid?.let { uid -> // Safely access UID
                    authResultState = AuthResult.Success(uid)
                } ?: run {
                    authResultState = AuthResult.Error("Sign up successful, but user data is null.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                authResultState = AuthResult.Error(e.message ?: "Sign up failed.")
            }
        }
    }

    fun handleGoogleIdToken(idToken: String?) {
        if (idToken == null) {
            authResultState = AuthResult.Error("Google Sign-In failed: No ID token received.")
            return
        }
        authResultState = AuthResult.Loading
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val firebaseAuthResult = firebaseAuth.signInWithCredential(credential).await()
                firebaseAuthResult.user?.uid?.let { uid -> // Safely access UID
                    authResultState = AuthResult.Success(uid)
                } ?: run {
                    authResultState = AuthResult.Error("Google Sign-In successful, but user data is null.")
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                authResultState = AuthResult.Error("Firebase Google Auth failed: ${e.message}")
            }
        }
    }

    fun resetAuthState() {
        authResultState = AuthResult.Idle
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()
    }
}
