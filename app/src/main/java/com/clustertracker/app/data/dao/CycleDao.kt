package com.clustertracker.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.clustertracker.app.data.entity.CycleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycles ORDER BY startDate DESC")
    fun getAllCycles(): Flow<List<CycleEntity>>

    @Query("SELECT * FROM cycles WHERE id = :id")
    fun getCycleById(id: Long): Flow<CycleEntity?>

    @Query("SELECT * FROM cycles WHERE endDate IS NULL ORDER BY startDate DESC LIMIT 1")
    fun getActiveCycle(): Flow<CycleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cycle: CycleEntity): Long

    @Update
    suspend fun update(cycle: CycleEntity)

    @Delete
    suspend fun delete(cycle: CycleEntity)
}
