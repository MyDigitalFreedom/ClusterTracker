package com.clustertracker.app.data.repository

import com.clustertracker.app.data.dao.EnvironmentalDataDao
import com.clustertracker.app.data.datastore.PreferencesManager
import com.clustertracker.app.data.entity.EnvironmentalDataEntity
import com.clustertracker.app.data.remote.WeatherApiService
import com.clustertracker.app.util.MoonPhaseCalculator
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvironmentalRepository @Inject constructor(
    private val envDao: EnvironmentalDataDao,
    private val weatherApi: WeatherApiService,
    private val preferencesManager: PreferencesManager
) {
    suspend fun captureEnvironmentalData(attackId: Long) {
        try {
            val prefs = preferencesManager.preferencesFlow.first()
            val cityName = prefs.cityName
            val lat = prefs.cityLat
            val lon = prefs.cityLon
            val apiKey = prefs.weatherApiKey

            var temperature: Double? = null
            var pressure: Double? = null
            var humidity: Int? = null

            if (lat != null && lon != null && apiKey.isNotBlank()) {
                try {
                    val weather = weatherApi.getCurrentWeather(
                        latitude = lat,
                        longitude = lon,
                        apiKey = apiKey
                    )
                    temperature = weather.main.temp
                    pressure = weather.main.pressure
                    humidity = weather.main.humidity
                } catch (_: Exception) {
                    // Weather fetch failed — continue with what we have
                }
            }

            val now = ZonedDateTime.now()
            val moonPhase = MoonPhaseCalculator.getMoonPhase(
                dateTime = now,
                lat = lat ?: 0.0,
                lon = lon ?: 0.0
            )

            envDao.insert(
                EnvironmentalDataEntity(
                    attackId = attackId,
                    cityName = cityName,
                    latitude = lat,
                    longitude = lon,
                    temperatureCelsius = temperature,
                    barometricPressureHpa = pressure,
                    humidity = humidity,
                    moonPhaseName = moonPhase.phaseName,
                    moonPhaseAngle = moonPhase.phaseAngle,
                    moonIlluminationFraction = moonPhase.illuminationFraction,
                    capturedAt = Instant.now()
                )
            )
        } catch (_: Exception) {
            // Environmental data is non-critical — never crash the app
        }
    }
}
