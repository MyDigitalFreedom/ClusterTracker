package com.clustertracker.app.ui.cycles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clustertracker.app.data.repository.AttackRepository
import com.clustertracker.app.data.repository.CycleRepository
import com.clustertracker.app.domain.model.Attack
import com.clustertracker.app.domain.model.Cycle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CycleListViewModel @Inject constructor(
    cycleRepository: CycleRepository,
    attackRepository: AttackRepository
) : ViewModel() {

    val cycles: StateFlow<List<Cycle>> = cycleRepository.getAllCycles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAttack: StateFlow<Attack?> = attackRepository.getActiveAttack()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val attackCounts: StateFlow<Map<Long, Int>> = attackRepository.getAttackCountsPerCycle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}
