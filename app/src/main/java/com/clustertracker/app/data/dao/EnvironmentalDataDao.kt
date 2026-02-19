package com.clustertracker.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clustertracker.app.data.entity.EnvironmentalDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EnvironmentalDataDao {
    @Query("SELECT * FROM environmental_data WHERE attackId = :attackId LIMIT 1")
    fun getDataForAttack(attackId: Long): Flow<EnvironmentalDataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: EnvironmentalDataEntity): Long
}
