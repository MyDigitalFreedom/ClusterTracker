package com.clustertracker.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class WeatherResponse(
    val main: MainWeatherData
)

@Serializable
data class MainWeatherData(
    val temp: Double,
    val pressure: Double,
    val humidity: Int
)
