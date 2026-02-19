package com.clustertracker.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class GeocodingResult(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
) {
    val displayName: String
        get() = buildString {
            append(name)
            state?.let { append(", $it") }
            append(", $country")
        }
}
