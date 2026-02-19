package com.clustertracker.app.ui.attack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.clustertracker.app.data.repository.AttackRepository
import com.clustertracker.app.domain.model.Attack
import com.clustertracker.app.ui.navigation.AttackDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttackDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attackRepository: AttackRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AttackDetailRoute>()
    private val attackId = route.attackId

    val attack: StateFlow<Attack?> = attackRepository.getFullAttack(attackId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()

    fun deleteAttack() {
        viewModelScope.launch {
            attackRepository.deleteAttack(attackId)
            _events.emit(DetailEvent.AttackDeleted)
        }
    }

    sealed class DetailEvent {
        data object AttackDeleted : DetailEvent()
    }
}
