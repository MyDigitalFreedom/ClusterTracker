package com.clustertracker.app.ui.cycles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.clustertracker.app.data.repository.AttackRepository
import com.clustertracker.app.data.repository.CycleLogRepository
import com.clustertracker.app.data.repository.CycleRepository
import com.clustertracker.app.data.repository.EnvironmentalRepository
import com.clustertracker.app.domain.model.Attack
import com.clustertracker.app.domain.model.Cycle
import com.clustertracker.app.domain.model.CycleLog
import com.clustertracker.app.ui.navigation.CycleDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CycleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cycleRepository: CycleRepository,
    private val attackRepository: AttackRepository,
    private val environmentalRepository: EnvironmentalRepository,
    private val cycleLogRepository: CycleLogRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CycleDetailRoute>()
    val cycleId = route.cycleId

    val cycle: StateFlow<Cycle?> = cycleRepository.getCycleById(cycleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val attacks: StateFlow<List<Attack>> = attackRepository.getAttacksForCycle(cycleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cycleLogs: StateFlow<List<CycleLog>> = cycleLogRepository.getLogsForCycle(cycleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attackCount: StateFlow<Int> = attackRepository.getAttackCountForCycle(cycleId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _showLogDialog = MutableStateFlow(false)
    val showLogDialog: StateFlow<Boolean> = _showLogDialog.asStateFlow()

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    fun startAttack() {
        viewModelScope.launch {
            val attackId = attackRepository.startAttack(cycleId, Instant.now())
            // Fire-and-forget environmental data capture
            launch { environmentalRepository.captureEnvironmentalData(attackId) }
            _events.emit(DetailEvent.AttackStarted(attackId))
        }
    }

    fun showLogDialog() {
        _showLogDialog.value = true
    }

    fun dismissLogDialog() {
        _showLogDialog.value = false
    }

    fun addLog(note: String) {
        if (note.isBlank()) return
        viewModelScope.launch {
            cycleLogRepository.addLog(cycleId, note.trim())
            _showLogDialog.value = false
        }
    }

    fun updateLog(log: CycleLog, newNote: String) {
        if (newNote.isBlank()) return
        viewModelScope.launch {
            cycleLogRepository.updateLog(log, newNote.trim())
        }
    }

    fun deleteLog(log: CycleLog) {
        viewModelScope.launch {
            cycleLogRepository.deleteLog(log)
        }
    }

    fun deleteCycle() {
        viewModelScope.launch {
            cycle.value?.let { cycleRepository.deleteCycle(it) }
            _events.emit(DetailEvent.CycleDeleted)
        }
    }

    sealed class DetailEvent {
        data class AttackStarted(val attackId: Long) : DetailEvent()
        data object CycleDeleted : DetailEvent()
    }
}
