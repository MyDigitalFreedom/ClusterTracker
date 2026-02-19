package com.clustertracker.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "therapy_notes",
    foreignKeys = [ForeignKey(
        entity = AttackEntity::class,
        parentColumns = ["id"],
        childColumns = ["attackId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("attackId")]
)
data class TherapyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attackId: Long,
    val timestamp: Instant,
    val note: String
)
