package com.example.weathersnap.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level wrapper for the weather forecast API response.
 * The API wraps current conditions inside a "current" key:
 * {
 *   "current": {
 *     "temperature_2m": 32.5,
 *     ...
 *   }
 * }
 */
data class WeatherResponse(

    @SerializedName("current")
    val current: CurrentWeather
)