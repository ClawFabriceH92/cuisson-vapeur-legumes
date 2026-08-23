package com.trucdecomptable.cuissonvapeur.ui.screens.objectifs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trucdecomptable.cuissonvapeur.data.repository.VegetableRepository
import com.trucdecomptable.cuissonvapeur.domain.goals.NutritionGoal
import com.trucdecomptable.cuissonvapeur.domain.goals.NutritionGoalsCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * EF-13/EF-14 Option A: 5 live goal counters, computed from the current
 * cart selection via [NutritionGoalsCalculator] — the corrected,
 * always-reachable targets (see spec §1.1, §11 D1).
 */
@HiltViewModel
class ObjectifsViewModel @Inject constructor(
    vegetableRepository: VegetableRepository,
) : ViewModel() {

    val goals: StateFlow<List<NutritionGoal>> = vegetableRepository.observeCart()
        .map { cart -> NutritionGoalsCalculator.compute(vegetableRepository.catalog, cart) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
