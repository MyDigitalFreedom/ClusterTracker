package com.clustertracker.app.domain.model

import java.time.Instant

data class Attack(
    val id: Long = 0,
    val cycleId: Long,
    val shadowOnsetTime: Instant,
    val endTime: Instant? = null,
    val painDataPoints: List<PainDataPoint> = emptyList(),
    val oxygenSessions: List<OxygenSession> = emptyList(),
    val therapyNotes: List<TherapyNote> = emptyList(),
    val environmentalData: EnvironmentalData? = null
)
