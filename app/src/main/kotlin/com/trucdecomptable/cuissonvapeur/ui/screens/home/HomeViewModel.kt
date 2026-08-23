package com.trucdecomptable.cuissonvapeur.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trucdecomptable.cuissonvapeur.alarm.CookingTimerService
import com.trucdecomptable.cuissonvapeur.data.local.entity.CookingSessionEntity
import com.trucdecomptable.cuissonvapeur.data.repository.CookingSessionRepository
import com.trucdecomptable.cuissonvapeur.data.repository.VegetableRepository
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlan
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlanCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val cart: List<Vegetable> = emptyList(),
    val activeSession: CookingSessionEntity? = null,
    val confirmModalPlan: CookingPlan? = null,
)

/**
 * EF-06..EF-10: the cart, plus EF-17's "Ordre optimal de cuisson"
 * confirmation modal — computing the [CookingPlan] purely as a *preview*
 * here (the real, timestamped session is only created in
 * [CookingSessionRepository.startCooking] once the user confirms).
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vegetableRepository: VegetableRepository,
    private val cookingSessionRepository: CookingSessionRepository,
) : ViewModel() {

    private val confirmModalPlan = MutableStateFlow<CookingPlan?>(null)

    /**
     * A session only counts as "active" for the UI while it is genuinely
     * running (or paused with time left). An expired-but-not-stopped session
     * (e.g. the app was closed before pressing Arrêter) must NOT hijack the
     * Home screen into a dead Timer at 00:00 — that made it impossible to
     * start a new cooking (fix 23/08/2026).
     */
    private fun CookingSessionEntity.isLive(): Boolean {
        if (!isActive) return false
        return if (isPaused) {
            remainingMillisWhenPaused > 0
        } else {
            endEpochMillis > System.currentTimeMillis()
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        vegetableRepository.observeCart(),
        cookingSessionRepository.observeSession(),
        confirmModalPlan,
    ) { cart, session, modalPlan ->
        HomeUiState(cart = cart, activeSession = session?.takeIf { it.isLive() }, confirmModalPlan = modalPlan)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onRemoveFromCart(vegetableId: String) {
        viewModelScope.launch { vegetableRepository.removeFromCart(vegetableId) }
    }

    /** EF-10: "un moyen de vider le panier en un geste". */
    fun onClearCart() {
        viewModelScope.launch { vegetableRepository.clearCart() }
    }

    /** EF-09/EF-17: only reachable when the cart isn't empty; computes the modal's preview plan. */
    fun onStartCookingClicked() {
        val cart = uiState.value.cart
        if (cart.isEmpty()) return
        confirmModalPlan.value = CookingPlanCalculator.compute(cart)
    }

    fun onDismissConfirmModal() {
        confirmModalPlan.value = null
    }

    /** Stops a running session from the Home banner (EF-21, fix 23/08/2026). */
    fun onStopSession() {
        viewModelScope.launch {
            cookingSessionRepository.stop()
            CookingTimerService.stop(context)
        }
    }

    /** EF-17 "Démarrer la cuisson": commits the real, timestamped session and its alarms. */
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
}
