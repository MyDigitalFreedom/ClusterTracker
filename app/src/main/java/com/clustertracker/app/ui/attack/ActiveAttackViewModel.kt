package com.clustertracker.app.ui.attack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.clustertracker.app.data.repository.AttackRepository
import com.clustertracker.app.domain.model.Attack
import com.clustertracker.app.domain.model.FlowRate
import com.clustertracker.app.domain.model.OxygenSession
import com.clustertracker.app.ui.navigation.ActiveAttackRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ActiveAttackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attackRepository: AttackRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ActiveAttackRoute>()
    val attackId = route.attackId

    val attack: StateFlow<Attack?> = attackRepository.getFullAttack(attackId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeO2Session: StateFlow<OxygenSession?> =
        attackRepository.getActiveO2Session(attackId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentPainLevel = MutableStateFlow(0)
    val currentPainLevel: StateFlow<Int> = _currentPainLevel.asStateFlow()

    private val _selectedFlowRate = MutableStateFlow(FlowRate.DEFAULT)
    val selectedFlowRate: StateFlow<Int> = _selectedFlowRate.asStateFlow()

    private val _showNoteDialog = MutableStateFlow(false)
    val showNoteDialog: StateFlow<Boolean> = _showNoteDialog.asStateFlow()

    private val _events = MutableSharedFlow<AttackEvent>()
    val events: SharedFlow<AttackEvent> = _events.asSharedFlow()

    init {
        // Restore last pain level from DB so it persists across screen navigation
        viewModelScope.launch {
            val lastPain = attackRepository.getLastPainIntensity(attackId)
            if (lastPain != null) {
                _currentPainLevel.value = lastPain
            }
        }

        // Auto-log pain level with 2-second debounce
        @OptIn(FlowPreview::class)
        _currentPainLevel
            .debounce(2000L)
            .distinctUntilChanged()
            .onEach { level ->
                attackRepository.addPainDataPoint(attackId, level)
            }
            .launchIn(viewModelScope)
    }

    fun updatePainLevel(level: Int) {
        _currentPainLevel.value = level
    }

    fun logPainNow() {
        viewModelScope.launch {
            attackRepository.addPainDataPoint(attackId, _currentPainLevel.value)
        }
    }

    fun selectFlowRate(lpm: Int) {
        _selectedFlowRate.value = lpm
    }

    fun toggleO2() {
        viewModelScope.launch {
            val active = activeO2Session.value
            if (active != null) {
                attackRepository.stopO2Session(active.id, active)
            } else {
                attackRepository.startO2Session(attackId, _selectedFlowRate.value)
            }
        }
    }

    fun showNoteDialog() {
        _showNoteDialog.value = true
    }

    fun dismissNoteDialog() {
        _showNoteDialog.value = false
    }

    fun addTherapyNote(note: String) {
        if (note.isBlank()) return
        viewModelScope.launch {
            attackRepository.addTherapyNote(attackId, note.trim())
            _showNoteDialog.value = false
        }
    }

    fun endAttack() {
        viewModelScope.launch {
            // Stop any active O2 session first
            activeO2Session.value?.let { session ->
                attackRepository.stopO2Session(session.id, session)
            }
            // End the attack — pain stays at last recorded level (no drop to 0)
            attackRepository.endAttack(attackId, Instant.now())
            _events.emit(AttackEvent.AttackEnded(attackId))
        }
    }

    sealed class AttackEvent {
        data class AttackEnded(val attackId: Long) : AttackEvent()
    }
}
