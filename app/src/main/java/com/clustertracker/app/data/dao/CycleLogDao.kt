package com.clustertracker.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.clustertracker.app.data.entity.CycleLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleLogDao {
    @Query("SELECT * FROM cycle_logs WHERE cycleId = :cycleId ORDER BY timestamp DESC")
    fun getLogsForCycle(cycleId: Long): Flow<List<CycleLogEntity>>

    @Insert
    suspend fun insert(log: CycleLogEntity): Long

    @Update
    suspend fun update(log: CycleLogEntity)

    @Delete
    suspend fun delete(log: CycleLogEntity)
}
