package com.clustertracker.app.domain.model

import java.time.Instant

data class PainDataPoint(
    val id: Long = 0,
    val attackId: Long,
    val timestamp: Instant,
    val intensity: Int // KIP scale 0-10
)
