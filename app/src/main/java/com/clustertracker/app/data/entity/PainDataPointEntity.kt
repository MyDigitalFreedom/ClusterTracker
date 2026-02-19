package com.clustertracker.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "pain_data_points",
    foreignKeys = [ForeignKey(
        entity = AttackEntity::class,
        parentColumns = ["id"],
        childColumns = ["attackId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("attackId")]
)
data class PainDataPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attackId: Long,
    val timestamp: Instant,
    val intensity: Int // KIP scale 0-10
)
