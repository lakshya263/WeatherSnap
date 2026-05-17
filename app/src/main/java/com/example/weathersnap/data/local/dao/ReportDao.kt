package com.example.weathersnap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.weathersnap.data.local.enitity.WeatherReport
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO — the interface through which all database operations are performed.
 *
 * Room reads this interface at compile time (via KSP) and generates
 * the full SQL implementation. You never write SQL boilerplate manually.
 *
 * Two operations are needed for this assignment:
 *   1. Insert a new report (one-shot, suspend)
 *   2. Observe all reports in real time (ongoing stream, Flow)
 */
@Dao
interface ReportDao {

    /**
     * Inserts a new report into the "reports" table.
     *
     * suspend = this is a one-shot coroutine operation.
     * Room automatically runs it off the main thread when called
     * from a coroutine — no manual Dispatchers.IO needed in the DAO itself,
     * but the Repository wraps it in withContext(Dispatchers.IO) for clarity.
     */
    @Insert
    suspend fun insertReport(report: WeatherReport)

    /**
     * Returns all saved reports, newest first.
     *
     * Returns Flow<List<WeatherReport>> — NOT a suspend function.
     * Flow is an ongoing stream: Room automatically emits a fresh list
     * every time any report is inserted. The SavedReports screen collects
     * this Flow once and stays up to date forever without manual refreshing.
     *
     * ORDER BY savedAt DESC ensures the most recently saved report
     * always appears at the top of the list.
     */
    @Query("SELECT * FROM reports ORDER BY savedAt DESC")
    fun getAllReports(): Flow<List<WeatherReport>>
}