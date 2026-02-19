package com.clustertracker.app.domain.mapper

import com.clustertracker.app.data.entity.AttackEntity
import com.clustertracker.app.data.entity.CycleEntity
import com.clustertracker.app.data.entity.CycleLogEntity
import com.clustertracker.app.data.entity.EnvironmentalDataEntity
import com.clustertracker.app.data.entity.OxygenSessionEntity
import com.clustertracker.app.data.entity.PainDataPointEntity
import com.clustertracker.app.data.entity.TherapyNoteEntity
import com.clustertracker.app.domain.model.Attack
import com.clustertracker.app.domain.model.Cycle
import com.clustertracker.app.domain.model.CycleLog
import com.clustertracker.app.domain.model.EnvironmentalData
import com.clustertracker.app.domain.model.FlowRate
import com.clustertracker.app.domain.model.OxygenSession

import com.clustertracker.app.domain.model.PainDataPoint
import com.clustertracker.app.domain.model.TherapyNote

// Cycle
fun CycleEntity.toDomain() = Cycle(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    notes = notes
)

fun Cycle.toEntity() = CycleEntity(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    notes = notes
)

// Attack
fun AttackEntity.toDomain(
    painDataPoints: List<PainDataPoint> = emptyList(),
    oxygenSessions: List<OxygenSession> = emptyList(),
    therapyNotes: List<TherapyNote> = emptyList(),
    environmentalData: EnvironmentalData? = null
) = Attack(
    id = id,
    cycleId = cycleId,
    shadowOnsetTime = shadowOnsetTime,
    endTime = endTime,
    painDataPoints = painDataPoints,
    oxygenSessions = oxygenSessions,
    therapyNotes = therapyNotes,
    environmentalData = environmentalData
)

fun Attack.toEntity() = AttackEntity(
    id = id,
    cycleId = cycleId,
    shadowOnsetTime = shadowOnsetTime,
    endTime = endTime
)

// PainDataPoint
fun PainDataPointEntity.toDomain() = PainDataPoint(
    id = id,
    attackId = attackId,
    timestamp = timestamp,
    intensity = intensity
)

fun PainDataPoint.toEntity() = PainDataPointEntity(
    id = id,
    attackId = attackId,
    timestamp = timestamp,
    intensity = intensity
)

// OxygenSession
fun OxygenSessionEntity.toDomain() = OxygenSession(
    id = id,
    attackId = attackId,
    startTime = startTime,
    stopTime = stopTime,
    flowRate = FlowRate.fromStorageString(flowRate)
)

fun OxygenSession.toEntity() = OxygenSessionEntity(
    id = id,
    attackId = attackId,
    startTime = startTime,
    stopTime = stopTime,
    flowRate = FlowRate.toStorageString(flowRate)
)

// TherapyNote
fun TherapyNoteEntity.toDomain() = TherapyNote(
    id = id,
    attackId = attackId,
    timestamp = timestamp,
    note = note
)

fun TherapyNote.toEntity() = TherapyNoteEntity(
    id = id,
    attackId = attackId,
    timestamp = timestamp,
    note = note
)

// EnvironmentalData
fun EnvironmentalDataEntity.toDomain() = EnvironmentalData(
    id = id,
    attackId = attackId,
    cityName = cityName,
    latitude = latitude,
    longitude = longitude,
    temperatureCelsius = temperatureCelsius,
    barometricPressureHpa = barometricPressureHpa,
    humidity = humidity,
    moonPhaseName = moonPhaseName,
    moonPhaseAngle = moonPhaseAngle,
    moonIlluminationFraction = moonIlluminationFraction,
    capturedAt = capturedAt
)

fun EnvironmentalData.toEntity() = EnvironmentalDataEntity(
    id = id,
    attackId = attackId,
    cityName = cityName,
    latitude = latitude,
    longitude = longitude,
    temperatureCelsius = temperatureCelsius,
    barometricPressureHpa = barometricPressureHpa,
    humidity = humidity,
    moonPhaseName = moonPhaseName,
    moonPhaseAngle = moonPhaseAngle,
    moonIlluminationFraction = moonIlluminationFraction,
    capturedAt = capturedAt
)

// CycleLog
fun CycleLogEntity.toDomain() = CycleLog(
    id = id,
    cycleId = cycleId,
    timestamp = timestamp,
    note = note
)

fun CycleLog.toEntity() = CycleLogEntity(
    id = id,
    cycleId = cycleId,
    timestamp = timestamp,
    note = note
)
