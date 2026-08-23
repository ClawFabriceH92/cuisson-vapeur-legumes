package com.trucdecomptable.cuissonvapeur.ui.screens.catalogue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.trucdecomptable.cuissonvapeur.alarm.CookingTimerService
import com.trucdecomptable.cuissonvapeur.data.repository.CookingSessionRepository
import com.trucdecomptable.cuissonvapeur.data.repository.VegetableRepository
import com.trucdecomptable.cuissonvapeur.domain.model.ALL_YEAR
import com.trucdecomptable.cuissonvapeur.domain.model.Season
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlan
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlanCalculator
import com.trucdecomptable.cuissonvapeur.ui.common.SortMode
import com.trucdecomptable.cuissonvapeur.ui.common.VegetableUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val cart: List<Vegetable> = emptyList(),
    val hasActiveSession: Boolean = false,
)

/** EF-02/EF-03/EF-04: search, sort and season-filter the 28-vegetable catalog. */
@HiltViewModel
class CatalogueViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vegetableRepository: VegetableRepository,
    private val cookingSessionRepository: CookingSessionRepository,
) : ViewModel() {

    private data class UiFilters(val query: String, val sort: SortMode, val season: SeasonQuickFilter?)

    private val query = MutableStateFlow("")
    private val sortMode = MutableStateFlow(SortMode.TEMPS_CROISSANT)
    private val seasonFilter = MutableStateFlow<SeasonQuickFilter?>(null)

    // combine() only has typed overloads up to 5 flows; group the 3 UI
    // filters first so the main combine stays within the typed overload.
    private val filters = combine(query, sortMode, seasonFilter) { q, s, f ->
        UiFilters(q, s, f)
    }

    val uiState: StateFlow<CatalogueUiState> = combine(
        filters,
        vegetableRepository.observeCart(),
        vegetableRepository.observeFavoriteIds(),
        cookingSessionRepository.observeSession(),
    ) { filters, cart, favoriteIds, session ->
        val q = filters.query
        val sort = filters.sort
        val season = filters.season
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
            cart = cart,
            hasActiveSession = session?.isActive == true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogueUiState())

    // --- Direct start from the catalog (fix 23/08/2026) ---------------------
    // Fabrice's "Démarrer la cuisson" from the catalog went through Home,
    // which a stuck/forgotten session could hijack. The start flow (modal
    // preview → commit) now lives here so one tap on the catalog button
    // opens the confirmation modal and the timer starts without ever
    // navigating through Home.

    private val confirmModalPlan = MutableStateFlow<CookingPlan?>(null)

    /** Preview-plan shown in the confirmation modal (null = hidden). */
    val confirmModalPlanFlow: StateFlow<CookingPlan?> = confirmModalPlan

    fun onStartCookingClicked() {
        val cart = uiState.value.cart
        if (cart.isEmpty()) return
        if (uiState.value.hasActiveSession) return // banner/other path handles it
        confirmModalPlan.value = CookingPlanCalculator.compute(cart)
    }

    fun onDismissConfirmModal() {
        confirmModalPlan.value = null
    }

    fun onConfirmStartCooking(onStarted: () -> Unit) {
        val cart = uiState.value.cart
        if (cart.isEmpty()) return
        viewModelScope.launch {
            cookingSessionRepository.startCooking(cart)
            confirmModalPlan.value = null
            CookingTimerService.start(context)
            onStarted()
        }
    }

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
