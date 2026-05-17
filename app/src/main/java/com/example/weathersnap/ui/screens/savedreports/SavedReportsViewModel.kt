package com.example.weathersnap.ui.screens.savedreports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.local.enitity.WeatherReport
import com.example.weathersnap.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class ReportsUiState {
    object Loading : ReportsUiState()
    object Empty : ReportsUiState()
    data class Success(val reports: List<WeatherReport>) : ReportsUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class SavedReportsViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    // Convert the raw Flow<List<WeatherReport>> from Room into a UI state Flow.
    // stateIn() converts a cold Flow into a hot StateFlow that the UI can collect.
    val reportsState: StateFlow<ReportsUiState> = repository
        .getAllReports()
        .map { reports ->
            when {
                reports.isEmpty() -> ReportsUiState.Empty
                else              -> ReportsUiState.Success(reports)
            }
        }
        .stateIn(
            scope = viewModelScope,
            // WhileSubscribed(5000) keeps the flow active for 5 seconds after
            // the last subscriber leaves — handles rotation without restarting
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReportsUiState.Loading
        )
}