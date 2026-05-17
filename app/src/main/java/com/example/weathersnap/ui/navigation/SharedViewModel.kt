package com.example.weathersnap.ui.navigation


import androidx.lifecycle.ViewModel
import com.example.weathersnap.domain.model.WeatherData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor() : ViewModel() {

    private val _selectedWeather = MutableStateFlow<WeatherData?>(null)
    val selectedWeather = _selectedWeather.asStateFlow()

    fun setSelectedWeather(data: WeatherData) {
        _selectedWeather.value = data
    }
}