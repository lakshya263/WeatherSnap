package com.example.weathersnap.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Maps to the "current" object inside the weather API response.
 * Example JSON:
 * {
 *   "current": {
 *     "temperature_2m": 32.5,
 *     "relative_humidity_2m": 45,
 *     "wind_speed_10m": 12.3,
 *     "surface_pressure": 1008.0,
 *     "weather_code": 2
 *   }
 * }
 *
 * @SerializedName is required here because the API uses snake_case names
 * like "temperature_2m" while Kotlin fields use camelCase.
 * Without it, Gson would look for a key named "temperature" (not found)
 * and silently default to 0 — a hard-to-spot bug.
 */
data class CurrentWeather(

    @SerializedName("temperature_2m")
    val temperature: Double,

    @SerializedName("relative_humidity_2m")
    val humidity: Int,

    @SerializedName("wind_speed_10m")
    val windSpeed: Double,

    @SerializedName("surface_pressure")
    val pressure: Double,

    // WMO weather interpretation code.
    // 0 = clear sky, 2 = partly cloudy, 61 = rain, etc.
    // Decoded into a human-readable string in WeatherRepository.
    @SerializedName("weather_code")
    val weatherCode: Int
)