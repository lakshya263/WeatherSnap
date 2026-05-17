package com.example.weathersnap.domain.model

/**
 * Clean domain model representing weather data for the UI.
 *
 * This is intentionally separate from the raw API model (CurrentWeather).
 * Differences:
 *   - Has cityName and coordinates (the API response doesn't include these)
 *   - Has condition: String instead of weatherCode: Int
 *     (the Repository decodes the WMO code into a readable string here)
 *   - No @SerializedName annotations — nothing API-specific leaks into the UI layer
 *
 * The UI and ViewModels only ever see WeatherData, never CurrentWeather or WeatherResponse.
 * If the API changes its field names, only the Repository mapping needs to change.
 */
data class WeatherData(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val temperature: Double,
    val condition: String,      // e.g. "Clear Sky", "Partly Cloudy", "Rain"
    val humidity: Int,          // percentage e.g. 65
    val windSpeed: Double,      // km/h
    val pressure: Double        // hPa
)