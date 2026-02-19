package com.clustertracker.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.clustertracker.app.data.entity.OxygenSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OxygenSessionDao {
    @Query("SELECT * FROM oxygen_sessions WHERE attackId = :attackId ORDER BY startTime ASC")
    fun getSessionsForAttack(attackId: Long): Flow<List<OxygenSessionEntity>>

    @Query("SELECT * FROM oxygen_sessions WHERE attackId = :attackId AND stopTime IS NULL LIMIT 1")
    fun getActiveSession(attackId: Long): Flow<OxygenSessionEntity?>

    @Insert
    suspend fun insert(session: OxygenSessionEntity): Long

    @Update
    suspend fun update(session: OxygenSessionEntity)
}
