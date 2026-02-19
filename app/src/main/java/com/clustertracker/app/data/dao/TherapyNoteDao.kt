package com.clustertracker.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.clustertracker.app.data.entity.TherapyNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TherapyNoteDao {
    @Query("SELECT * FROM therapy_notes WHERE attackId = :attackId ORDER BY timestamp ASC")
    fun getNotesForAttack(attackId: Long): Flow<List<TherapyNoteEntity>>

    @Insert
    suspend fun insert(note: TherapyNoteEntity): Long

    @Update
    suspend fun update(note: TherapyNoteEntity)

    @Delete
    suspend fun delete(note: TherapyNoteEntity)

    @Query("DELETE FROM therapy_notes WHERE attackId = :attackId")
    suspend fun deleteAllForAttack(attackId: Long)
}
