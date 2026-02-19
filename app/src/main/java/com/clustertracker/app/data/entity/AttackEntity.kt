package com.clustertracker.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "attacks",
    foreignKeys = [ForeignKey(
        entity = CycleEntity::class,
        parentColumns = ["id"],
        childColumns = ["cycleId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cycleId")]
)
data class AttackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cycleId: Long,
    val shadowOnsetTime: Instant,
    val endTime: Instant? = null,
    val createdAt: Instant = Instant.now()
)
