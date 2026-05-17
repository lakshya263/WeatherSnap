package com.example.weathersnap.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level wrapper for the geocoding API response.
 * The API wraps the city list inside a "results" key:
 * {
 *   "results": [ { ...CityResult }, { ...CityResult } ]
 * }
 *
 * results is nullable — when no cities match the query,
 * the API returns an empty object {} with no "results" key at all.
 * Gson maps that to null, so List<CityResult>? is required here.
 */
data class CitySearchResponse(

    @SerializedName("results")
    val results: List<CityResult>?
)