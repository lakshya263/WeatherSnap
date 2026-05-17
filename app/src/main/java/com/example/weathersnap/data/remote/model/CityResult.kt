package com.example.weathersnap.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Maps to one object inside the "results" array from the geocoding API.
 * Example JSON:
 * {
 *   "id": 1273294,
 *   "name": "Bhopal",
 *   "country": "India",
 *   "latitude": 23.2599,
 *   "longitude": 77.4126
 * }
 */
data class CityResult(

    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    // Nullable — some entries in Open-Meteo have no country field
    @SerializedName("country")
    val country: String?,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)