package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.model.User
import com.example.pennypilot.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileTabViewModel @Inject constructor(
    private val userRepository: UserRepository, // Uses the same repo as SignUpViewModel
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                _errorMessage.value = "User not logged in."
                _isLoading.value = false
                Timber.e("Cannot fetch profile, user is not authenticated.")
                return@launch
            }

            Timber.d("Fetching profile for user ID: $currentUserId")
            userRepository.getUserProfileFlow(currentUserId)
                .catch { exception ->
                    Timber.e(exception, "Error fetching user profile from repository.")
                    _errorMessage.value = "Failed to load profile: ${exception.message}"
                    _isLoading.value = false
                }
                .collect { user ->
                    _userProfile.value = user
                    _isLoading.value = false
                    if (user == null) {
                        Timber.w("User profile data is null for user ID: $currentUserId")
                        _errorMessage.value = "Could not find user profile."
                    } else {
                        Timber.d("Successfully loaded user profile: ${user.displayName}")
                    }
                }
        }
    }

    fun signOut() {
        auth.signOut()
        // The navigation to the login screen should be handled by observing
        // the auth state in your main activity or root composable.
    }
}
