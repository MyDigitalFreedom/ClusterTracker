package com.clustertracker.app.util

import com.clustertracker.app.data.dao.AttackDao
import com.clustertracker.app.data.dao.CycleDao
import com.clustertracker.app.data.dao.CycleLogDao
import com.clustertracker.app.data.dao.EnvironmentalDataDao
import com.clustertracker.app.data.dao.OxygenSessionDao
import com.clustertracker.app.data.dao.PainDataPointDao
import com.clustertracker.app.data.dao.TherapyNoteDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ExportData(
    val exportDate: String,
    val cycles: List<ExportCycle>
)

@Serializable
data class ExportCycle(
    val id: Long,
    val name: String,
    val startDate: String,
    val endDate: String?,
    val notes: String?,
    val attacks: List<ExportAttack>,
    val cycleLogs: List<ExportCycleLog> = emptyList()
)

@Serializable
data class ExportCycleLog(val timestamp: Long, val note: String)

@Serializable
data class ExportAttack(
    val id: Long,
    val shadowOnsetTime: Long,
    val endTime: Long?,
    val painDataPoints: List<ExportPainPoint>,
    val oxygenSessions: List<ExportO2Session>,
    val therapyNotes: List<ExportNote>,
    val environment: ExportEnvironment?
)

@Serializable
data class ExportPainPoint(val timestamp: Long, val intensity: Int)

@Serializable
data class ExportO2Session(val startTime: Long, val stopTime: Long?, val flowRate: String)

@Serializable
data class ExportNote(val timestamp: Long, val note: String)

@Serializable
data class ExportEnvironment(
    val cityName: String?,
    val lat: Double?,
    val lon: Double?,
    val tempC: Double?,
    val pressureHpa: Double?,
    val humidity: Int?,
    val moonPhase: String?,
    val moonIllumination: Double?
)

private fun csvSafe(value: String): String {
    val escaped = value.replace("\"", "\"\"")
    return if (escaped.firstOrNull() in listOf('=', '+', '-', '@'))
        "\"'$escaped\"" else "\"$escaped\""
}

@Singleton
class DataExporter @Inject constructor(
    private val cycleDao: CycleDao,
    private val attackDao: AttackDao,
    private val painDao: PainDataPointDao,
    private val oxygenDao: OxygenSessionDao,
    private val therapyNoteDao: TherapyNoteDao,
    private val envDao: EnvironmentalDataDao,
    private val cycleLogDao: CycleLogDao
) {
    private val json = Json { prettyPrint = true }

    fun exportToJson(): String = runBlocking {
        val cycles = cycleDao.getAllCycles().first()
        val exportCycles = cycles.map { cycle ->
            val attacks = attackDao.getAttacksForCycle(cycle.id).first()
            val logs = cycleLogDao.getLogsForCycle(cycle.id).first()
            ExportCycle(
                id = cycle.id,
                name = cycle.name,
                startDate = cycle.startDate.toString(),
                endDate = cycle.endDate?.toString(),
                notes = cycle.notes,
                cycleLogs = logs.map { ExportCycleLog(it.timestamp.toEpochMilli(), it.note) },
                attacks = attacks.map { attack ->
                    val pain = painDao.getDataPointsForAttack(attack.id).first()
                    val o2 = oxygenDao.getSessionsForAttack(attack.id).first()
                    val notes = therapyNoteDao.getNotesForAttack(attack.id).first()
                    val env = envDao.getDataForAttack(attack.id).first()
                    ExportAttack(
                        id = attack.id,
                        shadowOnsetTime = attack.shadowOnsetTime.toEpochMilli(),
                        endTime = attack.endTime?.toEpochMilli(),
                        painDataPoints = pain.map { ExportPainPoint(it.timestamp.toEpochMilli(), it.intensity) },
                        oxygenSessions = o2.map { ExportO2Session(it.startTime.toEpochMilli(), it.stopTime?.toEpochMilli(), it.flowRate) },
                        therapyNotes = notes.map { ExportNote(it.timestamp.toEpochMilli(), it.note) },
                        environment = env?.let {
                            ExportEnvironment(
                                cityName = it.cityName,
                                lat = it.latitude,
                                lon = it.longitude,
                                tempC = it.temperatureCelsius,
                                pressureHpa = it.barometricPressureHpa,
                                humidity = it.humidity,
                                moonPhase = it.moonPhaseName,
                                moonIllumination = it.moonIlluminationFraction
                            )
                        }
                    )
                }
            )
        }
        json.encodeToString(
            ExportData(
                exportDate = java.time.LocalDate.now().toString(),
                cycles = exportCycles
            )
        )
    }

    fun exportToCsv(): Map<String, String> = runBlocking {
        val result = mutableMapOf<String, String>()

        // Cycles
        val cycles = cycleDao.getAllCycles().first()
        result["cycles.csv"] = buildString {
            appendLine("id,name,start_date,end_date,notes")
            cycles.forEach { c ->
                appendLine("${c.id},${csvSafe(c.name)},${c.startDate},${c.endDate ?: ""},${csvSafe(c.notes ?: "")}")
            }
        }

        // Attacks
        val allAttacks = cycles.flatMap { cycle ->
            attackDao.getAttacksForCycle(cycle.id).first()
        }
        result["attacks.csv"] = buildString {
            appendLine("id,cycle_id,shadow_onset_time,end_time")
            allAttacks.forEach { a ->
                appendLine("${a.id},${a.cycleId},${a.shadowOnsetTime.toEpochMilli()},${a.endTime?.toEpochMilli() ?: ""}")
            }
        }

        // Pain data points
        result["pain_data.csv"] = buildString {
            appendLine("attack_id,timestamp,intensity")
            allAttacks.forEach { attack ->
                painDao.getDataPointsForAttack(attack.id).first().forEach { p ->
                    appendLine("${p.attackId},${p.timestamp.toEpochMilli()},${p.intensity}")
                }
            }
        }

        // O2 sessions
        result["o2_sessions.csv"] = buildString {
            appendLine("attack_id,start_time,stop_time,flow_rate")
            allAttacks.forEach { attack ->
                oxygenDao.getSessionsForAttack(attack.id).first().forEach { o ->
                    appendLine("${o.attackId},${o.startTime.toEpochMilli()},${o.stopTime?.toEpochMilli() ?: ""},${o.flowRate}")
                }
            }
        }

        // Therapy notes
        result["therapy_notes.csv"] = buildString {
            appendLine("attack_id,timestamp,note")
            allAttacks.forEach { attack ->
                therapyNoteDao.getNotesForAttack(attack.id).first().forEach { n ->
                    appendLine("${n.attackId},${n.timestamp.toEpochMilli()},${csvSafe(n.note)}")
                }
            }
        }

        // Environmental data
        result["environmental.csv"] = buildString {
            appendLine("attack_id,city,lat,lon,temp_c,pressure_hpa,humidity,moon_phase,moon_illumination")
            allAttacks.forEach { attack ->
                envDao.getDataForAttack(attack.id).first()?.let { e ->
                    appendLine("${e.attackId},${csvSafe(e.cityName ?: "")},${e.latitude ?: ""},${e.longitude ?: ""},${e.temperatureCelsius ?: ""},${e.barometricPressureHpa ?: ""},${e.humidity ?: ""},${csvSafe(e.moonPhaseName ?: "")},${e.moonIlluminationFraction ?: ""}")
                }
            }
        }

        result
    }
}
