package com.clustertracker.app.domain.model

import java.time.Instant

data class CycleLog(
    val id: Long = 0,
    val cycleId: Long,
    val timestamp: Instant,
    val note: String
)
