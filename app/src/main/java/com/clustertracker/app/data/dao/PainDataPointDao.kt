package com.clustertracker.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.clustertracker.app.data.entity.PainDataPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PainDataPointDao {
    @Query("SELECT * FROM pain_data_points WHERE attackId = :attackId ORDER BY timestamp ASC")
    fun getDataPointsForAttack(attackId: Long): Flow<List<PainDataPointEntity>>

    @Query("SELECT MAX(intensity) FROM pain_data_points WHERE attackId = :attackId")
    suspend fun getPeakIntensityForAttack(attackId: Long): Int?

    @Query("SELECT intensity FROM pain_data_points WHERE attackId = :attackId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastIntensityForAttack(attackId: Long): Int?

    @Insert
    suspend fun insert(point: PainDataPointEntity): Long

    @Query("DELETE FROM pain_data_points WHERE attackId = :attackId")
    suspend fun deleteAllForAttack(attackId: Long)
}
