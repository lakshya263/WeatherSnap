package com.example.weathersnap.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.weathersnap.data.local.dao.ReportDao
import com.example.weathersnap.data.local.enitity.WeatherReport

/**
 * Room database — the entry point to the local SQLite database.
 *
 * @Database tells Room:
 *   - which tables exist (entities list)
 *   - the current schema version
 *
 * Room uses KSP to generate a concrete subclass (AppDatabase_Impl) at
 * compile time. You never instantiate AppDatabase directly — Hilt builds
 * it via DatabaseModule and provides the generated implementation.
 *
 * IMPORTANT — version field:
 * If you ever add/remove/rename a column in WeatherReport, you MUST:
 *   1. Increment version (e.g. version = 2)
 *   2. Provide a Migration object in DatabaseModule
 * Without this, Room throws IllegalStateException on devices with the
 * old schema and the app crashes on launch.
 * For this assignment, version = 1 is correct.
 *
 * exportSchema = false suppresses the "provide a schema location" warning.
 * In production apps, set to true and commit the schema JSON to version control.
 */
@Database(
    entities = [WeatherReport::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Room generates the implementation of this function.
     * Hilt calls database.reportDao() in DatabaseModule to provide
     * ReportDao wherever it is injected.
     */
    abstract fun reportDao(): ReportDao
}