package com.example.weathersnap.data.remote.api

import com.example.weathersnap.data.remote.model.CitySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Open-Meteo geocoding API.
 * Base URL: https://geocoding-api.open-meteo.com/
 *
 * This is a SEPARATE interface from WeatherApi because Open-Meteo uses
 * two different base URLs — one for geocoding, one for weather data.
 * Retrofit is built around one base URL per instance, so two interfaces
 * and two Retrofit instances are required (wired in NetworkModule).
 */
interface GeocodingApi {

    /**
     * Searches for cities matching the given name query.
     *
     * Full URL example:
     * https://geocoding-api.open-meteo.com/v1/search?name=Bhopal&count=5
     *
     * @param name   The city name to search for (e.g. "Bho", "Mumbai")
     * @param count  Max number of suggestions to return. Defaults to 5.
     */
    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 5
    ): CitySearchResponse
}