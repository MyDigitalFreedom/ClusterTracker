package com.clustertracker.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "oxygen_sessions",
    foreignKeys = [ForeignKey(
        entity = AttackEntity::class,
        parentColumns = ["id"],
        childColumns = ["attackId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("attackId")]
)
data class OxygenSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attackId: Long,
    val startTime: Instant,
    val stopTime: Instant? = null,
    val flowRate: String // "8", "10", "12", "15", "Higher"
)
