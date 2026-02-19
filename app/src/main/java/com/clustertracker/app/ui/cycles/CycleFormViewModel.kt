package com.clustertracker.app.ui.cycles

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.clustertracker.app.data.repository.CycleRepository
import com.clustertracker.app.domain.model.Cycle
import com.clustertracker.app.ui.navigation.CycleFormRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CycleFormState(
    val name: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val hasEndDate: Boolean = false,
    val notes: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false
)

@HiltViewModel
class CycleFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cycleRepository: CycleRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<CycleFormRoute>()
    private val cycleId = route.cycleId

    private val _state = MutableStateFlow(CycleFormState())
    val state: StateFlow<CycleFormState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<FormEvent>()
    val events: SharedFlow<FormEvent> = _events.asSharedFlow()

    init {
        if (cycleId != 0L) {
            viewModelScope.launch {
                cycleRepository.getCycleById(cycleId).first()?.let { cycle ->
                    _state.value = CycleFormState(
                        name = cycle.name,
                        startDate = cycle.startDate,
                        endDate = cycle.endDate,
                        hasEndDate = cycle.endDate != null,
                        notes = cycle.notes ?: "",
                        isEditing = true
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _state.value = _state.value.copy(name = name)
    }

    fun updateStartDate(date: LocalDate) {
        _state.value = _state.value.copy(startDate = date)
    }

    fun updateEndDate(date: LocalDate?) {
        _state.value = _state.value.copy(endDate = date)
    }

    fun toggleEndDate(hasEndDate: Boolean) {
        _state.value = _state.value.copy(
            hasEndDate = hasEndDate,
            endDate = if (hasEndDate) LocalDate.now() else null
        )
    }

    fun updateNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }

    fun save() {
        val current = _state.value
        if (current.name.isBlank()) return

        _state.value = current.copy(isSaving = true)

        viewModelScope.launch {
            val cycle = Cycle(
                id = if (current.isEditing) cycleId else 0L,
                name = current.name.trim(),
                startDate = current.startDate,
                endDate = if (current.hasEndDate) current.endDate else null,
                notes = current.notes.ifBlank { null }
            )
            cycleRepository.saveCycle(cycle)
            _events.emit(FormEvent.Saved)
        }
    }

    sealed class FormEvent {
        data object Saved : FormEvent()
    }
}
