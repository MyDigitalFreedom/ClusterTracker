package com.clustertracker.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.clustertracker.app.data.dao.AttackDao
import com.clustertracker.app.data.dao.CycleDao
import com.clustertracker.app.data.dao.CycleLogDao
import com.clustertracker.app.data.dao.EnvironmentalDataDao
import com.clustertracker.app.data.dao.OxygenSessionDao
import com.clustertracker.app.data.dao.PainDataPointDao
import com.clustertracker.app.data.dao.TherapyNoteDao
import com.clustertracker.app.data.entity.AttackEntity
import com.clustertracker.app.data.entity.CycleEntity
import com.clustertracker.app.data.entity.CycleLogEntity
import com.clustertracker.app.data.entity.EnvironmentalDataEntity
import com.clustertracker.app.data.entity.OxygenSessionEntity
import com.clustertracker.app.data.entity.PainDataPointEntity
import com.clustertracker.app.data.entity.TherapyNoteEntity

@Database(
    entities = [
        CycleEntity::class,
        AttackEntity::class,
        PainDataPointEntity::class,
        OxygenSessionEntity::class,
        TherapyNoteEntity::class,
        EnvironmentalDataEntity::class,
        CycleLogEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun attackDao(): AttackDao
    abstract fun painDataPointDao(): PainDataPointDao
    abstract fun oxygenSessionDao(): OxygenSessionDao
    abstract fun therapyNoteDao(): TherapyNoteDao
    abstract fun environmentalDataDao(): EnvironmentalDataDao
    abstract fun cycleLogDao(): CycleLogDao
}
