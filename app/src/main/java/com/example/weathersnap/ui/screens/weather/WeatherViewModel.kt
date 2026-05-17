package com.example.weathersnap.ui.screens.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.repository.WeatherRepository
import com.example.weathersnap.domain.model.City
import com.example.weathersnap.domain.model.WeatherData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.fold

// ─── UI State Sealed Classes ──────────────────────────────────────────────────

sealed class CitySuggestionsState {
    object Idle : CitySuggestionsState()
    object Loading : CitySuggestionsState()
    data class Success(val cities: List<City>) : CitySuggestionsState()
    data class Error(val message: String) : CitySuggestionsState()
}

sealed class WeatherUiState {
    object Idle : WeatherUiState()
    object Loading : WeatherUiState()
    data class Success(val data: WeatherData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    // What the user typed in the search field
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // City suggestion dropdown state
    private val _suggestionsState = MutableStateFlow<CitySuggestionsState>(CitySuggestionsState.Idle)
    val suggestionsState = _suggestionsState.asStateFlow()

    // Weather card state
    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val weatherState = _weatherState.asStateFlow()

    // Currently loaded weather — used by SharedViewModel when navigating
    private val _currentWeather = MutableStateFlow<WeatherData?>(null)
    val currentWeather = _currentWeather.asStateFlow()

    init {
        observeSearchQuery()
    }

    // ─── Search Query Observation ─────────────────────────────────────────────

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        _searchQuery
            .debounce(400)              // wait 400ms after user stops typing
            .distinctUntilChanged()     // skip if query hasn't actually changed
            .filter { it.length > 2 }  // only search after 3+ characters (assignment rule)
            .onEach { query ->
                fetchCitySuggestions(query)
            }
            .launchIn(viewModelScope)
    }

    // ─── Called from UI ───────────────────────────────────────────────────────

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query

        // If user clears the field or types ≤2 chars, reset suggestions
        if (query.length <= 2) {
            _suggestionsState.value = CitySuggestionsState.Idle
        }
    }

    fun onCitySelected(city: City) {
        // Hide the suggestions dropdown
        _suggestionsState.value = CitySuggestionsState.Idle

        // Fetch weather for the selected city
        fetchWeather(city)
    }

    // ─── API Calls ────────────────────────────────────────────────────────────

    private fun fetchCitySuggestions(query: String) {
        viewModelScope.launch {
            _suggestionsState.value = CitySuggestionsState.Loading

            repository.searchCities(query).fold(
                onSuccess = { cities ->
                    _suggestionsState.value = if (cities.isEmpty()) {
                        CitySuggestionsState.Idle
                    } else {
                        CitySuggestionsState.Success(cities)
                    }
                },
                onFailure = { error ->
                    _suggestionsState.value = CitySuggestionsState.Error(
                        error.message ?: "Failed to load suggestions"
                    )
                }
            )
        }
    }

    private fun fetchWeather(city: City) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading

            repository.getWeather(city).fold(
                onSuccess = { weatherData ->
                    _weatherState.value = WeatherUiState.Success(weatherData)
                    _currentWeather.value = weatherData   // store for SharedViewModel
                },
                onFailure = { error ->
                    _weatherState.value = WeatherUiState.Error(
                        error.message ?: "Failed to load weather"
                    )
                }
            )
        }
    }
}