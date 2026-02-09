package com.example.pennypilot.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennypilot.data.model.CountryCurrency
import com.example.pennypilot.data.model.CurrencyData
import com.example.pennypilot.data.model.Result
import com.example.pennypilot.data.model.Trip
import com.example.pennypilot.data.repository.TripRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TravelScreenUiState(
    val trips: List<Trip> = emptyList(),
    val isLoadingTrips: Boolean = true,
    val tripsErrorMessage: String? = null,
    val createTripMessage: String? = null,
    val countries: List<CountryCurrency> = emptyList()
)

@HiltViewModel
class TravelModeViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(TravelScreenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(countries = CurrencyData.countries) }
        loadTrips()
    }

    private fun loadTrips() {
        tripRepository.getTrips()
            .onEach { result ->
                _uiState.update {
                    when (result) {
                        is Result.Success -> it.copy(trips = result.data, isLoadingTrips = false)
                        is Result.Error -> it.copy(isLoadingTrips = false, tripsErrorMessage = result.message)
                        is Result.Loading -> it.copy(isLoadingTrips = true)
                    }
                }
            }.launchIn(viewModelScope)
    }

    fun createTrip(name: String, budget: Double, currency: String) {
        viewModelScope.launch {
            if (name.isBlank() || budget <= 0) {
                _uiState.update { it.copy(createTripMessage = "Invalid trip name or budget.") }
                return@launch
            }

            when (val result = tripRepository.createTrip(name, budget, currency)) {
                is Result.Success -> {
                    // Don't need to manually add, Firestore listener will do it.
                    _uiState.update { it.copy(createTripMessage = "Trip '$name' created!") }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(createTripMessage = "Failed to create trip: ${result.message}") }
                }
                is Result.Loading -> { /* No action needed */ }
            }
        }
    }

    fun deleteTrip(tripId: String) {
        viewModelScope.launch {
            tripRepository.deleteTrip(tripId) // The flow will automatically update the list
        }
    }

    fun clearCreateTripMessage() {
        viewModelScope.launch {
            delay(100) // Small delay to prevent UI flickering
            _uiState.update { it.copy(createTripMessage = null) }
        }
    }
}
