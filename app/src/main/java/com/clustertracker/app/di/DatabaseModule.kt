package com.clustertracker.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.clustertracker.app.data.dao.AttackDao
import com.clustertracker.app.data.dao.CycleDao
import com.clustertracker.app.data.dao.CycleLogDao
import com.clustertracker.app.data.dao.EnvironmentalDataDao
import com.clustertracker.app.data.dao.OxygenSessionDao
import com.clustertracker.app.data.dao.PainDataPointDao
import com.clustertracker.app.data.dao.TherapyNoteDao
import com.clustertracker.app.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS cycle_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                cycleId INTEGER NOT NULL,
                timestamp INTEGER NOT NULL,
                note TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY (cycleId) REFERENCES cycles(id) ON DELETE CASCADE
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS index_cycle_logs_cycleId ON cycle_logs(cycleId)")
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "cluster_tracker.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    fun provideCycleDao(db: AppDatabase): CycleDao = db.cycleDao()

    @Provides
    fun provideAttackDao(db: AppDatabase): AttackDao = db.attackDao()

    @Provides
    fun providePainDataPointDao(db: AppDatabase): PainDataPointDao = db.painDataPointDao()

    @Provides
    fun provideOxygenSessionDao(db: AppDatabase): OxygenSessionDao = db.oxygenSessionDao()

    @Provides
    fun provideTherapyNoteDao(db: AppDatabase): TherapyNoteDao = db.therapyNoteDao()

    @Provides
    fun provideEnvironmentalDataDao(db: AppDatabase): EnvironmentalDataDao = db.environmentalDataDao()

    @Provides
    fun provideCycleLogDao(db: AppDatabase): CycleLogDao = db.cycleLogDao()
}
