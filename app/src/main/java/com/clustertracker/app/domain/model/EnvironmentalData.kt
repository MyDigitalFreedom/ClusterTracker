package com.clustertracker.app.domain.model

import java.time.Instant

data class EnvironmentalData(
    val id: Long = 0,
    val attackId: Long,
    val cityName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val temperatureCelsius: Double? = null,
    val barometricPressureHpa: Double? = null,
    val humidity: Int? = null,
    val moonPhaseName: String? = null,
    val moonPhaseAngle: Double? = null,
    val moonIlluminationFraction: Double? = null,
    val capturedAt: Instant
)
