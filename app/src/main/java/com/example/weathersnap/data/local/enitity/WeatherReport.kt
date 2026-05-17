package com.example.weathersnap.data.local.enitity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity — represents one row in the "reports" SQLite table.
 *
 * Each field becomes a column. Room reads this class at compile time
 * (via KSP) and generates all the SQL CREATE TABLE statements automatically.
 *
 * Key design decisions:
 * - Weather fields store a SNAPSHOT of the conditions at report-creation time.
 *   They are never updated after saving (assignment requirement).
 * - imagePath stores the absolute file path to the compressed JPEG on device.
 *   Images themselves are never stored in SQLite — only the path string.
 * - savedAt stores milliseconds since epoch for reliable sorting and display.
 */
@Entity(tableName = "reports")
data class WeatherReport(

    // Auto-generated primary key — Room assigns 1, 2, 3...
    // Default 0 signals "not yet inserted"; Room replaces it on insert.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // ── Weather snapshot ──────────────────────────────────────────────────────
    // Frozen at report-creation time from the WeatherData domain model.
    // The exact weather the user selected — never re-fetched.
    val cityName: String,
    val temperature: Double,
    val condition: String,      // decoded from WMO weather code e.g. "Partly Cloudy"
    val humidity: Int,
    val windSpeed: Double,
    val pressure: Double,

    // ── Image info ────────────────────────────────────────────────────────────
    val imagePath: String,          // absolute path to compressed JPEG on device
    val originalSizeKb: Long,       // size before compression in KB
    val compressedSizeKb: Long,     // size after compression in KB

    // ── Metadata ──────────────────────────────────────────────────────────────
    val notes: String,
    val savedAt: Long = System.currentTimeMillis()  // epoch ms — set automatically
)