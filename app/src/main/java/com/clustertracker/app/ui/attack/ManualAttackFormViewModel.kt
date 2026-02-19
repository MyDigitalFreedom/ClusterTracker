package com.clustertracker.app.ui.attack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.clustertracker.app.data.repository.AttackRepository
import com.clustertracker.app.domain.model.FlowRate
import com.clustertracker.app.ui.navigation.ManualAttackRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ManualAttackFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attackRepository: AttackRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<ManualAttackRoute>()
    val cycleId = route.cycleId

    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    private val _startTime = MutableStateFlow(LocalTime.of(0, 0))
    val startTime: StateFlow<LocalTime> = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow(LocalTime.of(1, 0))
    val endTime: StateFlow<LocalTime> = _endTime.asStateFlow()

    private val _averageKip = MutableStateFlow(5)
    val averageKip: StateFlow<Int> = _averageKip.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _o2FlowRate = MutableStateFlow(FlowRate.DEFAULT)
    val o2FlowRate: StateFlow<Int> = _o2FlowRate.asStateFlow()

    private val _o2Minutes = MutableStateFlow(0)
    val o2Minutes: StateFlow<Int> = _o2Minutes.asStateFlow()

    private val _events = MutableSharedFlow<FormEvent>()
    val events: SharedFlow<FormEvent> = _events.asSharedFlow()

    fun updateDate(date: LocalDate) { _date.value = date }
    fun updateStartTime(time: LocalTime) { _startTime.value = time }
    fun updateEndTime(time: LocalTime) { _endTime.value = time }
    fun updateAverageKip(kip: Int) { _averageKip.value = kip }
    fun updateNotes(text: String) { _notes.value = text }
    fun updateO2FlowRate(lpm: Int) { _o2FlowRate.value = lpm }
    fun updateO2Minutes(min: Int) { _o2Minutes.value = min }

    fun save() {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val startInstant = _date.value.atTime(_startTime.value)
                .atZone(zone).toInstant()
            val endInstant = _date.value.atTime(_endTime.value)
                .atZone(zone).toInstant()

            // Create the attack
            val attackId = attackRepository.startAttack(cycleId, startInstant)

            // End it immediately with the specified end time
            attackRepository.endAttack(attackId, endInstant)

            // Add average pain level at attack start time (not Instant.now())
            attackRepository.addPainDataPoint(attackId, _averageKip.value, startInstant)

            // Add notes as therapy note at start time if provided
            val noteText = _notes.value.trim()
            if (noteText.isNotBlank()) {
                attackRepository.addTherapyNote(attackId, noteText, startInstant)
            }

            // Add O2 session if minutes > 0
            val o2Min = _o2Minutes.value
            if (o2Min > 0) {
                val o2Stop = startInstant.plusSeconds(o2Min.toLong() * 60)
                attackRepository.insertO2Session(
                    attackId = attackId,
                    startTime = startInstant,
                    stopTime = o2Stop,
                    flowRateLpm = _o2FlowRate.value
                )
            }

            _events.emit(FormEvent.Saved)
        }
    }

    sealed class FormEvent {
        data object Saved : FormEvent()
    }
}
