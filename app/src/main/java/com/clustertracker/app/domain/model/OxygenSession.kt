package com.clustertracker.app.domain.model

import java.time.Instant

data class OxygenSession(
    val id: Long = 0,
    val attackId: Long,
    val startTime: Instant,
    val stopTime: Instant? = null,
    val flowRate: Int
)
