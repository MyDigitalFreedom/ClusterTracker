package com.clustertracker.app.ui.attack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.clustertracker.app.data.repository.AttackRepository
import com.clustertracker.app.domain.model.TherapyNote
import com.clustertracker.app.ui.navigation.EditAttackRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class EditAttackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attackRepository: AttackRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EditAttackRoute>()
    val attackId = route.attackId

    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    private val _startTime = MutableStateFlow(LocalTime.of(0, 0))
    val startTime: StateFlow<LocalTime> = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow(LocalTime.of(1, 0))
    val endTime: StateFlow<LocalTime> = _endTime.asStateFlow()

    private val _therapyNotes = MutableStateFlow<List<TherapyNote>>(emptyList())
    val therapyNotes: StateFlow<List<TherapyNote>> = _therapyNotes.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _events = MutableSharedFlow<EditEvent>()
    val events: SharedFlow<EditEvent> = _events.asSharedFlow()

    init {
        loadAttack()
    }

    private fun loadAttack() {
        viewModelScope.launch {
            val attack = attackRepository.getFullAttack(attackId).first() ?: return@launch
            val zone = ZoneId.systemDefault()
            val onsetZoned = attack.shadowOnsetTime.atZone(zone)
            _date.value = onsetZoned.toLocalDate()
            _startTime.value = onsetZoned.toLocalTime()
            attack.endTime?.let { end ->
                _endTime.value = end.atZone(zone).toLocalTime()
            }
            _therapyNotes.value = attack.therapyNotes
            _loading.value = false
        }
    }

    fun updateDate(date: LocalDate) { _date.value = date }
    fun updateStartTime(time: LocalTime) { _startTime.value = time }
    fun updateEndTime(time: LocalTime) { _endTime.value = time }

    fun updateNoteText(noteId: Long, newText: String) {
        _therapyNotes.value = _therapyNotes.value.map {
            if (it.id == noteId) it.copy(note = newText) else it
        }
    }

    fun deleteNote(noteId: Long) {
        val note = _therapyNotes.value.find { it.id == noteId } ?: return
        _therapyNotes.value = _therapyNotes.value.filter { it.id != noteId }
        viewModelScope.launch {
            attackRepository.deleteTherapyNote(
                noteId = note.id,
                attackId = note.attackId,
                timestamp = note.timestamp,
                note = note.note
            )
        }
    }

    fun addNote(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val onset = _date.value.atTime(_startTime.value).atZone(zone).toInstant()
            val noteId = attackRepository.addTherapyNote(attackId, text.trim(), onset)
            _therapyNotes.value = _therapyNotes.value + TherapyNote(
                id = noteId,
                attackId = attackId,
                timestamp = onset,
                note = text.trim()
            )
        }
    }

    fun save() {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val onset = _date.value.atTime(_startTime.value).atZone(zone).toInstant()
            val endInstant = _date.value.atTime(_endTime.value).atZone(zone).toInstant()

            attackRepository.updateAttackTimes(attackId, onset, endInstant)

            // Update any modified therapy notes
            for (note in _therapyNotes.value) {
                attackRepository.updateTherapyNote(
                    noteId = note.id,
                    attackId = note.attackId,
                    timestamp = note.timestamp,
                    note = note.note
                )
            }

            _events.emit(EditEvent.Saved)
        }
    }

    sealed class EditEvent {
        data object Saved : EditEvent()
    }
}
