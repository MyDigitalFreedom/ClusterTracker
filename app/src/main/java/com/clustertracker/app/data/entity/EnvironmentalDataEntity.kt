package com.clustertracker.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(
    tableName = "environmental_data",
    foreignKeys = [ForeignKey(
        entity = AttackEntity::class,
        parentColumns = ["id"],
        childColumns = ["attackId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("attackId")]
)
data class EnvironmentalDataEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attackId: Long,
    val cityName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val temperatureCelsius: Double? = null,
    val barometricPressureHpa: Double? = null,
    val humidity: Int? = null,
    val moonPhaseName: String? = null,
    val moonPhaseAngle: Double? = null,
    val moonIlluminationFraction: Double? = null,
    val capturedAt: Instant
)
