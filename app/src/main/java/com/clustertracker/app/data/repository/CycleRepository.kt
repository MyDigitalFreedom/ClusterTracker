package com.clustertracker.app.data.repository

import com.clustertracker.app.data.dao.CycleDao
import com.clustertracker.app.domain.mapper.toDomain
import com.clustertracker.app.domain.mapper.toEntity
import com.clustertracker.app.domain.model.Cycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepository @Inject constructor(
    private val cycleDao: CycleDao
) {
    fun getAllCycles(): Flow<List<Cycle>> =
        cycleDao.getAllCycles().map { list -> list.map { it.toDomain() } }

    fun getCycleById(id: Long): Flow<Cycle?> =
        cycleDao.getCycleById(id).map { it?.toDomain() }

    fun getActiveCycle(): Flow<Cycle?> =
        cycleDao.getActiveCycle().map { it?.toDomain() }

    suspend fun saveCycle(cycle: Cycle): Long =
        if (cycle.id == 0L) {
            cycleDao.insert(cycle.toEntity())
        } else {
            cycleDao.update(cycle.toEntity())
            cycle.id
        }

    suspend fun deleteCycle(cycle: Cycle) =
        cycleDao.delete(cycle.toEntity())
}
