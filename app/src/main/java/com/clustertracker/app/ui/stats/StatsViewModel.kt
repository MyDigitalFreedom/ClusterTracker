package com.clustertracker.app.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clustertracker.app.data.dao.AttackDao
import com.clustertracker.app.data.dao.OxygenSessionDao
import com.clustertracker.app.data.dao.PainDataPointDao
import com.clustertracker.app.data.repository.CycleRepository
import com.clustertracker.app.domain.model.Cycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

data class OverallStats(
    val totalCycles: Int = 0,
    val totalAttacks: Int = 0,
    val averageDurationMinutes: Long = 0,
    val averagePeakPain: Double = 0.0,
    val totalO2TimeMinutes: Long = 0,
    val timeOfDayDistribution: List<Int> = List(24) { 0 }
)

data class CycleStats(
    val cycle: Cycle,
    val attackCount: Int,
    val avgDurationMinutes: Long,
    val avgPeakPain: Double
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val attackDao: AttackDao,
    private val painDao: PainDataPointDao,
    private val oxygenDao: OxygenSessionDao
) : ViewModel() {

    val cycles: StateFlow<List<Cycle>> = cycleRepository.getAllCycles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _overallStats = MutableStateFlow(OverallStats())
    val overallStats: StateFlow<OverallStats> = _overallStats.asStateFlow()

    private val _cycleStats = MutableStateFlow<List<CycleStats>>(emptyList())
    val cycleStats: StateFlow<List<CycleStats>> = _cycleStats.asStateFlow()

    init {
        viewModelScope.launch { computeStats() }
    }

    private suspend fun computeStats() {
        val allCycles = cycleRepository.getAllCycles().first()
        var totalAttacks = 0
        var totalDurationMs = 0L
        var completedAttacks = 0
        val peakPains = mutableListOf<Int>()
        var totalO2Ms = 0L
        val hourBuckets = IntArray(24)
        val perCycle = mutableListOf<CycleStats>()

        for (cycle in allCycles) {
            val attacks = attackDao.getAttacksForCycle(cycle.id).first()
            totalAttacks += attacks.size
            var cycleDurationMs = 0L
            var cycleCompleted = 0
            val cyclePeaks = mutableListOf<Int>()

            for (attack in attacks) {
                // Time of day distribution
                val hour = attack.shadowOnsetTime
                    .atZone(ZoneId.systemDefault()).hour
                hourBuckets[hour]++

                // Duration
                attack.endTime?.let { end ->
                    val dur = Duration.between(attack.shadowOnsetTime, end).toMillis()
                    totalDurationMs += dur
                    cycleDurationMs += dur
                    completedAttacks++
                    cycleCompleted++
                }

                // Peak pain
                val peak = painDao.getPeakIntensityForAttack(attack.id)
                peak?.let {
                    peakPains.add(it)
                    cyclePeaks.add(it)
                }

                // O2 time
                val o2Sessions = oxygenDao.getSessionsForAttack(attack.id).first()
                o2Sessions.forEach { session ->
                    session.stopTime?.let { stop ->
                        totalO2Ms += Duration.between(session.startTime, stop).toMillis()
                    }
                }
            }

            perCycle.add(
                CycleStats(
                    cycle = cycle,
                    attackCount = attacks.size,
                    avgDurationMinutes = if (cycleCompleted > 0) {
                        Duration.ofMillis(cycleDurationMs / cycleCompleted).toMinutes()
                    } else 0,
                    avgPeakPain = if (cyclePeaks.isNotEmpty()) {
                        cyclePeaks.average()
                    } else 0.0
                )
            )
        }

        _overallStats.value = OverallStats(
            totalCycles = allCycles.size,
            totalAttacks = totalAttacks,
            averageDurationMinutes = if (completedAttacks > 0) {
                Duration.ofMillis(totalDurationMs / completedAttacks).toMinutes()
            } else 0,
            averagePeakPain = if (peakPains.isNotEmpty()) peakPains.average() else 0.0,
            totalO2TimeMinutes = Duration.ofMillis(totalO2Ms).toMinutes(),
            timeOfDayDistribution = hourBuckets.toList()
        )
        _cycleStats.value = perCycle
    }
}
