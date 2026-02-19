package com.clustertracker.app.util

import androidx.room.withTransaction
import com.clustertracker.app.data.dao.AttackDao
import com.clustertracker.app.data.dao.CycleDao
import com.clustertracker.app.data.dao.CycleLogDao
import com.clustertracker.app.data.dao.EnvironmentalDataDao
import com.clustertracker.app.data.dao.OxygenSessionDao
import com.clustertracker.app.data.dao.PainDataPointDao
import com.clustertracker.app.data.dao.TherapyNoteDao
import com.clustertracker.app.data.db.AppDatabase
import com.clustertracker.app.data.entity.AttackEntity
import com.clustertracker.app.data.entity.CycleEntity
import com.clustertracker.app.data.entity.CycleLogEntity
import com.clustertracker.app.data.entity.EnvironmentalDataEntity
import com.clustertracker.app.data.entity.OxygenSessionEntity
import com.clustertracker.app.data.entity.PainDataPointEntity
import com.clustertracker.app.data.entity.TherapyNoteEntity
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataImporter @Inject constructor(
    private val db: AppDatabase,
    private val cycleDao: CycleDao,
    private val attackDao: AttackDao,
    private val painDao: PainDataPointDao,
    private val oxygenDao: OxygenSessionDao,
    private val therapyNoteDao: TherapyNoteDao,
    private val envDao: EnvironmentalDataDao,
    private val cycleLogDao: CycleLogDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importFromJson(jsonString: String) {
        val data = json.decodeFromString<ExportData>(jsonString)

        db.withTransaction {
            // Clear all existing data
            db.clearAllTables()

            // Import cycles and their children
            for (cycle in data.cycles) {
                val cycleId = cycleDao.insert(
                    CycleEntity(
                        name = cycle.name,
                        startDate = LocalDate.parse(cycle.startDate),
                        endDate = cycle.endDate?.let { LocalDate.parse(it) },
                        notes = cycle.notes
                    )
                )

                // Import cycle logs
                for (log in cycle.cycleLogs) {
                    cycleLogDao.insert(
                        CycleLogEntity(
                            cycleId = cycleId,
                            timestamp = Instant.ofEpochMilli(log.timestamp),
                            note = log.note
                        )
                    )
                }

                // Import attacks
                for (attack in cycle.attacks) {
                    val attackId = attackDao.insert(
                        AttackEntity(
                            cycleId = cycleId,
                            shadowOnsetTime = Instant.ofEpochMilli(attack.shadowOnsetTime),
                            endTime = attack.endTime?.let { Instant.ofEpochMilli(it) }
                        )
                    )

                    // Pain data points
                    for (pain in attack.painDataPoints) {
                        painDao.insert(
                            PainDataPointEntity(
                                attackId = attackId,
                                timestamp = Instant.ofEpochMilli(pain.timestamp),
                                intensity = pain.intensity
                            )
                        )
                    }

                    // O2 sessions
                    for (o2 in attack.oxygenSessions) {
                        oxygenDao.insert(
                            OxygenSessionEntity(
                                attackId = attackId,
                                startTime = Instant.ofEpochMilli(o2.startTime),
                                stopTime = o2.stopTime?.let { Instant.ofEpochMilli(it) },
                                flowRate = o2.flowRate
                            )
                        )
                    }

                    // Therapy notes
                    for (note in attack.therapyNotes) {
                        therapyNoteDao.insert(
                            TherapyNoteEntity(
                                attackId = attackId,
                                timestamp = Instant.ofEpochMilli(note.timestamp),
                                note = note.note
                            )
                        )
                    }

                    // Environmental data
                    attack.environment?.let { env ->
                        envDao.insert(
                            EnvironmentalDataEntity(
                                attackId = attackId,
                                cityName = env.cityName,
                                latitude = env.lat,
                                longitude = env.lon,
                                temperatureCelsius = env.tempC,
                                barometricPressureHpa = env.pressureHpa,
                                humidity = env.humidity,
                                moonPhaseName = env.moonPhase,
                                moonIlluminationFraction = env.moonIllumination,
                                capturedAt = Instant.ofEpochMilli(attack.shadowOnsetTime)
                            )
                        )
                    }
                }
            }
        }
    }
}
