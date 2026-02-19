package com.clustertracker.app.data.repository

import com.clustertracker.app.data.dao.AttackDao
import com.clustertracker.app.data.dao.EnvironmentalDataDao
import com.clustertracker.app.data.dao.OxygenSessionDao
import com.clustertracker.app.data.dao.PainDataPointDao
import com.clustertracker.app.data.dao.TherapyNoteDao
import com.clustertracker.app.data.entity.AttackEntity
import com.clustertracker.app.data.entity.OxygenSessionEntity
import com.clustertracker.app.data.entity.PainDataPointEntity
import com.clustertracker.app.data.entity.TherapyNoteEntity
import com.clustertracker.app.domain.mapper.toDomain
import com.clustertracker.app.domain.model.Attack
import com.clustertracker.app.domain.model.FlowRate
import com.clustertracker.app.domain.model.OxygenSession

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttackRepository @Inject constructor(
    private val attackDao: AttackDao,
    private val painDao: PainDataPointDao,
    private val oxygenDao: OxygenSessionDao,
    private val therapyNoteDao: TherapyNoteDao,
    private val envDao: EnvironmentalDataDao
) {
    fun getAttacksForCycle(cycleId: Long): Flow<List<Attack>> =
        attackDao.getAttacksForCycle(cycleId).map { attacks ->
            attacks.map { it.toDomain() }
        }

    fun getFullAttack(attackId: Long): Flow<Attack?> =
        combine(
            attackDao.getAttackById(attackId),
            painDao.getDataPointsForAttack(attackId),
            oxygenDao.getSessionsForAttack(attackId),
            therapyNoteDao.getNotesForAttack(attackId),
            envDao.getDataForAttack(attackId)
        ) { attack, painPoints, o2Sessions, notes, envData ->
            attack?.toDomain(
                painDataPoints = painPoints.map { it.toDomain() },
                oxygenSessions = o2Sessions.map { it.toDomain() },
                therapyNotes = notes.map { it.toDomain() },
                environmentalData = envData?.toDomain()
            )
        }

    fun getActiveAttack(): Flow<Attack?> =
        attackDao.getActiveAttack().map { it?.toDomain() }

    fun getAttackCountForCycle(cycleId: Long): Flow<Int> =
        attackDao.getAttackCountForCycle(cycleId)

    fun getActiveO2Session(attackId: Long): Flow<OxygenSession?> =
        oxygenDao.getActiveSession(attackId).map { it?.toDomain() }

    suspend fun startAttack(cycleId: Long, shadowOnset: Instant): Long =
        attackDao.insert(
            AttackEntity(cycleId = cycleId, shadowOnsetTime = shadowOnset)
        )

    suspend fun endAttack(attackId: Long, endTime: Instant) {
        val attack = attackDao.getAttackById(attackId).first()
        attack?.let {
            attackDao.update(it.copy(endTime = endTime))
        }
    }

    suspend fun addPainDataPoint(attackId: Long, intensity: Int, timestamp: Instant = Instant.now()): Long =
        painDao.insert(
            PainDataPointEntity(
                attackId = attackId,
                timestamp = timestamp,
                intensity = intensity
            )
        )

    suspend fun startO2Session(attackId: Long, flowRateLpm: Int): Long =
        oxygenDao.insert(
            OxygenSessionEntity(
                attackId = attackId,
                startTime = Instant.now(),
                flowRate = FlowRate.toStorageString(flowRateLpm)
            )
        )

    suspend fun stopO2Session(sessionId: Long, session: OxygenSession) {
        oxygenDao.update(
            OxygenSessionEntity(
                id = sessionId,
                attackId = session.attackId,
                startTime = session.startTime,
                stopTime = Instant.now(),
                flowRate = FlowRate.toStorageString(session.flowRate)
            )
        )
    }

    suspend fun insertO2Session(attackId: Long, startTime: Instant, stopTime: Instant, flowRateLpm: Int): Long =
        oxygenDao.insert(
            OxygenSessionEntity(
                attackId = attackId,
                startTime = startTime,
                stopTime = stopTime,
                flowRate = FlowRate.toStorageString(flowRateLpm)
            )
        )

    suspend fun addTherapyNote(attackId: Long, note: String, timestamp: Instant = Instant.now()): Long =
        therapyNoteDao.insert(
            TherapyNoteEntity(
                attackId = attackId,
                timestamp = timestamp,
                note = note
            )
        )

    suspend fun updateAttackTimes(attackId: Long, onset: Instant, endTime: Instant) {
        val attack = attackDao.getAttackById(attackId).first()
        attack?.let {
            attackDao.update(it.copy(shadowOnsetTime = onset, endTime = endTime))
        }
    }

    suspend fun updateTherapyNote(noteId: Long, attackId: Long, timestamp: Instant, note: String) {
        therapyNoteDao.update(
            TherapyNoteEntity(id = noteId, attackId = attackId, timestamp = timestamp, note = note)
        )
    }

    suspend fun deleteTherapyNote(noteId: Long, attackId: Long, timestamp: Instant, note: String) {
        therapyNoteDao.delete(
            TherapyNoteEntity(id = noteId, attackId = attackId, timestamp = timestamp, note = note)
        )
    }

    suspend fun getPeakIntensity(attackId: Long): Int? =
        painDao.getPeakIntensityForAttack(attackId)

    suspend fun deleteAttack(attackId: Long) {
        val attack = attackDao.getAttackById(attackId).first()
        attack?.let { attackDao.delete(it) }
    }

    suspend fun getLastPainIntensity(attackId: Long): Int? =
        painDao.getLastIntensityForAttack(attackId)

    fun getAttackCountsPerCycle(): Flow<Map<Long, Int>> =
        attackDao.getAttackCountsPerCycle().map { counts ->
            counts.associate { it.cycleId to it.count }
        }
}
