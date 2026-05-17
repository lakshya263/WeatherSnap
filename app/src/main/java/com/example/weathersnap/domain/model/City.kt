package com.example.weathersnap.domain.model

/**
 * Clean domain model representing a city suggestion.
 *
 * Separate from CityResult (the raw API model) for these reasons:
 *   - country is non-nullable here (Repository substitutes "Unknown" if null)
 *   - displayName is pre-built (e.g. "Bhopal, India") so the UI
 *     just shows city.displayName without any formatting logic
 *
 * The suggestions dropdown and city selection flow only ever
 * see City, never CityResult.
 */
data class City(
    val id: Int,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val displayName: String     // pre-formatted e.g. "Bhopal, India"
)