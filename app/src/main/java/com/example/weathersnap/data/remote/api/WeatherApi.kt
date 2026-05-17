package com.example.weathersnap.data.remote.api

import com.example.weathersnap.data.remote.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Open-Meteo forecast API.
 * Base URL: https://api.open-meteo.com/
 *
 * Retrofit reads this interface at compile time and generates
 * the full HTTP implementation automatically — you never write
 * the actual network code yourself.
 */
interface WeatherApi {

    /**
     * Fetches current weather conditions for a given lat/lon.
     *
     * Full URL example:
     * https://api.open-meteo.com/v1/forecast
     *   ?latitude=23.25
     *   &longitude=77.40
     *   &current=temperature_2m,relative_humidity_2m,wind_speed_10m,surface_pressure,weather_code
     *
     * @param latitude   Decimal degrees latitude of the city
     * @param longitude  Decimal degrees longitude of the city
     * @param current    Comma-separated list of fields to request.
     *                   Defaulted to the exact 5 fields the assignment requires.
     */
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,relative_humidity_2m,wind_speed_10m,surface_pressure,weather_code"
    ): WeatherResponse
}