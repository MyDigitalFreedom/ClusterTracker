package com.clustertracker.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.clustertracker.app.data.entity.AttackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttackDao {
    @Query("SELECT * FROM attacks WHERE cycleId = :cycleId ORDER BY shadowOnsetTime DESC")
    fun getAttacksForCycle(cycleId: Long): Flow<List<AttackEntity>>

    @Query("SELECT * FROM attacks WHERE id = :id")
    fun getAttackById(id: Long): Flow<AttackEntity?>

    @Query("SELECT * FROM attacks WHERE endTime IS NULL LIMIT 1")
    fun getActiveAttack(): Flow<AttackEntity?>

    @Query("SELECT COUNT(*) FROM attacks WHERE cycleId = :cycleId")
    fun getAttackCountForCycle(cycleId: Long): Flow<Int>

    @Query("SELECT cycleId, COUNT(*) as count FROM attacks GROUP BY cycleId")
    fun getAttackCountsPerCycle(): Flow<List<CycleAttackCount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attack: AttackEntity): Long

    @Update
    suspend fun update(attack: AttackEntity)

    @Delete
    suspend fun delete(attack: AttackEntity)
}
