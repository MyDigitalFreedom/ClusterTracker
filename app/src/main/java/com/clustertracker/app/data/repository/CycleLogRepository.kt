package com.clustertracker.app.data.repository

import com.clustertracker.app.data.dao.CycleLogDao
import com.clustertracker.app.data.entity.CycleLogEntity
import com.clustertracker.app.domain.mapper.toDomain
import com.clustertracker.app.domain.model.CycleLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleLogRepository @Inject constructor(
    private val cycleLogDao: CycleLogDao
) {
    fun getLogsForCycle(cycleId: Long): Flow<List<CycleLog>> =
        cycleLogDao.getLogsForCycle(cycleId).map { logs ->
            logs.map { it.toDomain() }
        }

    suspend fun addLog(cycleId: Long, note: String): Long =
        cycleLogDao.insert(
            CycleLogEntity(
                cycleId = cycleId,
                timestamp = Instant.now(),
                note = note
            )
        )

    suspend fun updateLog(log: CycleLog, newNote: String) =
        cycleLogDao.update(
            CycleLogEntity(
                id = log.id,
                cycleId = log.cycleId,
                timestamp = log.timestamp,
                note = newNote
            )
        )

    suspend fun deleteLog(log: CycleLog) =
        cycleLogDao.delete(
            CycleLogEntity(
                id = log.id,
                cycleId = log.cycleId,
                timestamp = log.timestamp,
                note = log.note
            )
        )
}
