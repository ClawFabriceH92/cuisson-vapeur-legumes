package com.trucdecomptable.cuissonvapeur.ui.screens.catalogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trucdecomptable.cuissonvapeur.data.repository.VegetableRepository
import com.trucdecomptable.cuissonvapeur.domain.model.ALL_YEAR
import com.trucdecomptable.cuissonvapeur.domain.model.Season
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable
import com.trucdecomptable.cuissonvapeur.ui.common.SortMode
import com.trucdecomptable.cuissonvapeur.ui.common.VegetableUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** EF-04's season quick-filter, including the "Toute l'année"-only pill. */
enum class SeasonQuickFilter { PRINTEMPS, ETE, AUTOMNE, HIVER, TOUTE_ANNEE }

data class CatalogueUiState(
    val query: String = "",
    val sortMode: SortMode = SortMode.TEMPS_CROISSANT,
    val seasonFilter: SeasonQuickFilter? = null,
    val vegetables: List<VegetableUiModel> = emptyList(),
    val cartCount: Int = 0,
)

/** EF-02/EF-03/EF-04: search, sort and season-filter the 28-vegetable catalog. */
@HiltViewModel
class CatalogueViewModel @Inject constructor(
    private val vegetableRepository: VegetableRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val sortMode = MutableStateFlow(SortMode.TEMPS_CROISSANT)
    private val seasonFilter = MutableStateFlow<SeasonQuickFilter?>(null)

    val uiState: StateFlow<CatalogueUiState> = combine(
        query,
        sortMode,
        seasonFilter,
        vegetableRepository.observeCart(),
        vegetableRepository.observeFavoriteIds(),
    ) { q, sort, season, cart, favoriteIds ->
        val cartIds = cart.map { it.id }.toSet()

        val filtered = vegetableRepository.catalog
            .asSequence()
            .filter { matchesQuery(it, q) }
            .filter { matchesSeason(it, season) }
            .let { sequence -> applySort(sequence, sort) }
            .map { veg -> VegetableUiModel(veg, isInCart = veg.id in cartIds, isFavorite = veg.id in favoriteIds) }
            .toList()

        CatalogueUiState(
            query = q,
            sortMode = sort,
            seasonFilter = season,
            vegetables = filtered,
            cartCount = cart.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogueUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun onSortModeChange(newSortMode: SortMode) {
        sortMode.value = newSortMode
    }

    /** Tapping the already-active season pill clears the filter. */
    fun onSeasonFilterToggle(filter: SeasonQuickFilter) {
        seasonFilter.value = if (seasonFilter.value == filter) null else filter
    }

    fun onToggleCart(vegetableId: String, currentlyInCart: Boolean) {
        viewModelScope.launch { vegetableRepository.toggleCart(vegetableId, currentlyInCart) }
    }

    fun onToggleFavorite(vegetableId: String) {
        viewModelScope.launch { vegetableRepository.toggleFavorite(vegetableId) }
    }

    private fun matchesQuery(vegetable: Vegetable, query: String): Boolean {
        if (query.isBlank()) return true
        val needle = query.trim()
        return vegetable.name.contains(needle, ignoreCase = true) ||
            vegetable.benefits.any { it.contains(needle, ignoreCase = true) }
    }

    private fun matchesSeason(vegetable: Vegetable, filter: SeasonQuickFilter?): Boolean = when (filter) {
        null -> true
        SeasonQuickFilter.TOUTE_ANNEE -> vegetable.seasons == ALL_YEAR
        SeasonQuickFilter.PRINTEMPS -> Season.PRINTEMPS in vegetable.seasons
        SeasonQuickFilter.ETE -> Season.ETE in vegetable.seasons
        SeasonQuickFilter.AUTOMNE -> Season.AUTOMNE in vegetable.seasons
        SeasonQuickFilter.HIVER -> Season.HIVER in vegetable.seasons
    }

    private fun applySort(sequence: Sequence<Vegetable>, sortMode: SortMode): Sequence<Vegetable> = when (sortMode) {
        SortMode.TEMPS_CROISSANT -> sequence.sortedBy { it.durationMinutes }
        SortMode.NOM_ALPHABETIQUE -> sequence.sortedBy { it.name }
    }
}
