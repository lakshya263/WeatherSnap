package com.example.weathersnap.data.repository

import com.example.weathersnap.data.local.dao.ReportDao
import com.example.weathersnap.data.local.enitity.WeatherReport
import com.example.weathersnap.data.remote.api.GeocodingApi
import com.example.weathersnap.data.remote.api.WeatherApi
import com.example.weathersnap.domain.model.City
import com.example.weathersnap.domain.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi,
    private val geocodingApi: GeocodingApi,
    private val reportDao: ReportDao
) {

    // ─── In-memory cache for city suggestions ───────────────────────────────
    // Key = query string (e.g. "Bho"), Value = list of City results
    // Lives as long as the app process — cleared when app is killed
    private val cityCache = mutableMapOf<String, List<City>>()

    // ─── City Search ─────────────────────────────────────────────────────────

    suspend fun searchCities(query: String): Result<List<City>> {
        // 1. Check cache first — if we already fetched this query, return it immediately
        cityCache[query]?.let { cachedCities ->
            return Result.success(cachedCities)
        }

        // 2. Not cached — hit the API
        return withContext(Dispatchers.IO) {
            try {
                val response = geocodingApi.searchCities(name = query)

                // API returns null results when nothing matches (e.g. "xyzxyz")
                val cities = response.results
                    ?.map { cityResult ->
                        City(
                            id = cityResult.id,
                            name = cityResult.name,
                            country = cityResult.country ?: "Unknown",
                            latitude = cityResult.latitude,
                            longitude = cityResult.longitude,
                            // Pre-build the display string here so the UI
                            // just shows city.displayName without logic
                            displayName = buildString {
                                append(cityResult.name)
                                cityResult.country?.let { append(", $it") }
                            }
                        )
                    }
                    ?: emptyList()

                // 3. Store in cache before returning
                cityCache[query] = cities

                Result.success(cities)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ─── Weather Fetch ────────────────────────────────────────────────────────

    suspend fun getWeather(city: City): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = weatherApi.getWeather(
                    latitude = city.latitude,
                    longitude = city.longitude
                )

                val weatherData = WeatherData(
                    cityName = city.displayName,
                    latitude = city.latitude,
                    longitude = city.longitude,
                    temperature = response.current.temperature,
                    condition = decodeWeatherCode(response.current.weatherCode),
                    humidity = response.current.humidity,
                    windSpeed = response.current.windSpeed,
                    pressure = response.current.pressure
                )

                Result.success(weatherData)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ─── Room Operations ──────────────────────────────────────────────────────

    suspend fun saveReport(report: WeatherReport) {
        withContext(Dispatchers.IO) {
            reportDao.insertReport(report)
        }
    }

    // Flow — Room pushes updates automatically whenever a new report is saved
    // No Dispatchers.IO needed here; Room handles it internally for Flow
    fun getAllReports(): Flow<List<WeatherReport>> {
        return reportDao.getAllReports()
    }

    // ─── Weather Code Decoder ─────────────────────────────────────────────────
    // WMO weather codes → human-readable condition strings
    // Full code table: https://open-meteo.com/en/docs#weathervariables

    private fun decodeWeatherCode(code: Int): String = when (code) {
        0            -> "Clear Sky"
        1            -> "Mainly Clear"
        2            -> "Partly Cloudy"
        3            -> "Overcast"
        45, 48       -> "Foggy"
        51, 53, 55   -> "Drizzle"
        61, 63, 65   -> "Rain"
        71, 73, 75   -> "Snow"
        77           -> "Snow Grains"
        80, 81, 82   -> "Rain Showers"
        85, 86       -> "Snow Showers"
        95           -> "Thunderstorm"
        96, 99       -> "Thunderstorm with Hail"
        else         -> "Unknown"
    }
}