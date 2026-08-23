package com.trucdecomptable.cuissonvapeur.ui.screens.favoris

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trucdecomptable.cuissonvapeur.data.repository.VegetableRepository
import com.trucdecomptable.cuissonvapeur.ui.common.VegetableUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** EF-12: the favorites list, with a clickable "Sélectionné / Sélectionner" state per item. */
@HiltViewModel
class FavorisViewModel @Inject constructor(
    private val vegetableRepository: VegetableRepository,
) : ViewModel() {

    val favorites: StateFlow<List<VegetableUiModel>> = combine(
        vegetableRepository.observeFavorites(),
        vegetableRepository.observeCart(),
    ) { favoriteVegetables, cart ->
        val cartIds = cart.map { it.id }.toSet()
        favoriteVegetables.map { veg -> VegetableUiModel(veg, isInCart = veg.id in cartIds, isFavorite = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onToggleCart(vegetableId: String, currentlyInCart: Boolean) {
        viewModelScope.launch { vegetableRepository.toggleCart(vegetableId, currentlyInCart) }
    }

    /** EF-12: "retrait du favori". */
    fun onRemoveFavorite(vegetableId: String) {
        viewModelScope.launch { vegetableRepository.toggleFavorite(vegetableId) }
    }
}
