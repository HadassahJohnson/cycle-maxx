package com.pheb.cyclemaxx

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.MenstruationPeriodRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.temporal.ChronoUnit

class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    fun isProviderAvailable(): Boolean {
        return HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
    }

    fun openHealthConnectStore() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata")
            setPackage("com.android.vending")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    suspend fun fetchCurrentPhase(): MenstrualPhase {
        val startTime = getLatestPeriodRecordStart()
        return if (startTime != null) {
            val daysSinceStart = ChronoUnit.DAYS.between(startTime, Instant.now()).toInt() + 1
            calculateCurrentPhase(daysSinceStart)
        } else {
            MenstrualPhase.MENSTRUAL // Default if no record found
        }
    }

    suspend fun getLatestPeriodRecordStart(): Instant? {
        if (!isProviderAvailable()) return null

        return try {
            val now = Instant.now()
            val thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS)

            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = MenstruationPeriodRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(thirtyDaysAgo, now)
                )
            )
            response.records.maxByOrNull { it.startTime }?.startTime
        } catch (e: Exception) {
            null
        }
    }
}