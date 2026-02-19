package com.clustertracker.app.domain.model

import java.time.Instant

data class TherapyNote(
    val id: Long = 0,
    val attackId: Long,
    val timestamp: Instant,
    val note: String
)
