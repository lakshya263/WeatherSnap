package com.example.weathersnap.ui.screens.createreport

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weathersnap.data.local.enitity.WeatherReport
import com.example.weathersnap.data.repository.WeatherRepository
import com.example.weathersnap.domain.model.WeatherData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// ─── UI State ─────────────────────────────────────────────────────────────────

sealed class SaveReportState {
    object Idle : SaveReportState()
    object Saving : SaveReportState()
    object Success : SaveReportState()
    data class Error(val message: String) : SaveReportState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class CreateReportViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val savedStateHandle: SavedStateHandle   // Hilt injects this automatically
) : ViewModel() {

    companion object {
        // Keys used to store/restore state in SavedStateHandle
        private const val KEY_NOTES = "notes"
        private const val KEY_IMAGE_PATH = "image_path"
        private const val KEY_ORIGINAL_SIZE = "original_size_kb"
        private const val KEY_COMPRESSED_SIZE = "compressed_size_kb"
    }

    // ─── Persisted state via SavedStateHandle ─────────────────────────────────
    // These survive rotation AND process death

    // saveable {} is the Compose-friendly way to use SavedStateHandle
    // It backs the MutableStateFlow directly to the SavedStateHandle bundle
    var notes: String
        get() = savedStateHandle[KEY_NOTES] ?: ""
        set(value) { savedStateHandle[KEY_NOTES] = value }

    var capturedImagePath: String?
        get() = savedStateHandle[KEY_IMAGE_PATH]
        set(value) { savedStateHandle[KEY_IMAGE_PATH] = value }

    var originalSizeKb: Long
        get() = savedStateHandle[KEY_ORIGINAL_SIZE] ?: 0L
        set(value) { savedStateHandle[KEY_ORIGINAL_SIZE] = value }

    var compressedSizeKb: Long
        get() = savedStateHandle[KEY_COMPRESSED_SIZE] ?: 0L
        set(value) { savedStateHandle[KEY_COMPRESSED_SIZE] = value }

    // ─── Non-persisted StateFlows for UI reactivity ───────────────────────────

    // Backed by SavedStateHandle but exposed as StateFlow so UI recomposes
    private val _notesFlow = MutableStateFlow(notes)
    val notesFlow = _notesFlow.asStateFlow()

    private val _imagePathFlow = MutableStateFlow(capturedImagePath)
    val imagePathFlow = _imagePathFlow.asStateFlow()

    private val _imageSizes = MutableStateFlow(
        Pair(originalSizeKb, compressedSizeKb)
    )
    val imageSizes = _imageSizes.asStateFlow()  // Pair(originalKb, compressedKb)

    private val _saveState = MutableStateFlow<SaveReportState>(SaveReportState.Idle)
    val saveState = _saveState.asStateFlow()

    // ─── Called from UI ───────────────────────────────────────────────────────

    fun onNotesChanged(value: String) {
        notes = value                    // persists to SavedStateHandle
        _notesFlow.value = value         // triggers UI recomposition
    }

    fun onPhotoCaptured(
        imagePath: String,
        originalKb: Long,
        compressedKb: Long
    ) {
        // Clean up previous image file if user re-captures
        capturedImagePath?.let { oldPath ->
            val oldFile = File(oldPath)
            if (oldFile.exists()) oldFile.delete()
        }

        // Persist the new values
        capturedImagePath = imagePath
        originalSizeKb = originalKb
        compressedSizeKb = compressedKb

        // Update flows for UI
        _imagePathFlow.value = imagePath
        _imageSizes.value = Pair(originalKb, compressedKb)
    }

    fun saveReport(weatherData: WeatherData) {
        val imagePath = capturedImagePath

        // Guard — cannot save without a photo
        if (imagePath == null) {
            _saveState.value = SaveReportState.Error("Please capture a photo first")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveReportState.Saving

            try {
                val report = WeatherReport(
                    cityName = weatherData.cityName,
                    temperature = weatherData.temperature,
                    condition = weatherData.condition,
                    humidity = weatherData.humidity,
                    windSpeed = weatherData.windSpeed,
                    pressure = weatherData.pressure,
                    imagePath = imagePath,
                    originalSizeKb = originalSizeKb,
                    compressedSizeKb = compressedSizeKb,
                    notes = notes
                )

                repository.saveReport(report)
                _saveState.value = SaveReportState.Success

                // Clear persisted state after successful save
                // so the screen is fresh if the user creates another report
                clearPersistedState()

            } catch (e: Exception) {
                _saveState.value = SaveReportState.Error(
                    e.message ?: "Failed to save report"
                )
            }
        }
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    private fun clearPersistedState() {
        notes = ""
        capturedImagePath = null
        originalSizeKb = 0L
        compressedSizeKb = 0L
        _notesFlow.value = ""
        _imagePathFlow.value = null
        _imageSizes.value = Pair(0L, 0L)
    }

    override fun onCleared() {
        super.onCleared()

        // If ViewModel is cleared WITHOUT a successful save
        // (user navigated away without saving), clean up the temp image file
        if (_saveState.value !is SaveReportState.Success) {
            capturedImagePath?.let { path ->
                val file = File(path)
                if (file.exists()) file.delete()
            }
        }
    }
}