package com.trucdecomptable.cuissonvapeur.ui.screens.timer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trucdecomptable.cuissonvapeur.alarm.CookingTimerService
import com.trucdecomptable.cuissonvapeur.data.local.entity.CookingSessionEntity
import com.trucdecomptable.cuissonvapeur.data.repository.CookingSessionRepository
import com.trucdecomptable.cuissonvapeur.data.repository.VegetableRepository
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlan
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlanCalculator
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingStep
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** EF-18: a step's lifecycle, corrected vs the web (§1.1) to reflect *actual* readiness, not a fixed 30s guess. */
enum class StepState { A_VENIR, AJOUTER_MAINTENANT, AJOUTEE }

data class TimerStep(val step: CookingStep, val state: StepState)

data class TimerUiState(
    /** False until the session flow has emitted at least once. Without this
     *  flag the initial default state (isActive = false) made TimerScreen pop
     *  the back stack before Room had emitted the just-started session — the
     *  countdown flashed and vanished (fix 23/08/2026, v1.9). */
    val hasLoaded: Boolean = false,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val steps: List<TimerStep> = emptyList(),
) {
    val progressFraction: Float
        get() = if (totalSeconds <= 0) 0f else 1f - (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
}

/**
 * EF-18/EF-20..EF-24: the active-timer screen's state — a countdown derived
 * every second from the session's persisted absolute end timestamp (never
 * from a locally-held "remaining seconds" counter, so it stays correct even
 * if this ViewModel is recreated mid-cook — see §12.2 and
 * [CookingSessionRepository]).
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cookingSessionRepository: CookingSessionRepository,
    private val vegetableRepository: VegetableRepository,
) : ViewModel() {

    private val nowMillis = MutableStateFlow(System.currentTimeMillis())

    init {
        viewModelScope.launch {
            while (true) {
                nowMillis.value = System.currentTimeMillis()
                delay(1_000)
            }
        }
    }

    val uiState: StateFlow<TimerUiState> = combine(
        cookingSessionRepository.observeSession(),
        nowMillis,
    ) { session, now -> toUiState(session, now) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    private fun toUiState(session: CookingSessionEntity?, now: Long): TimerUiState {
        if (session == null || !session.isActive) {
            return TimerUiState(hasLoaded = true, isActive = false)
        }

        val plan = recomputePlan(session)
        val totalSeconds = plan.totalMinutes * 60

        val remainingMillis = if (session.isPaused) {
            session.remainingMillisWhenPaused
        } else {
            (session.endEpochMillis - now).coerceAtLeast(0)
        }
        val remainingSeconds = (remainingMillis / 1000).toInt()

        val startEpochMillis = session.endEpochMillis - plan.totalMinutes * 60_000L
        val elapsedSeconds = if (session.isPaused) {
            totalSeconds - (session.remainingMillisWhenPaused / 1000).toInt()
        } else {
            ((now - startEpochMillis) / 1000).toInt().coerceIn(0, totalSeconds)
        }

        val steps = plan.steps.map { step ->
            val state = when {
                elapsedSeconds >= step.readyOffsetMinutes * 60 -> StepState.AJOUTEE
                elapsedSeconds >= step.startOffsetMinutes * 60 -> StepState.AJOUTER_MAINTENANT
                else -> StepState.A_VENIR
            }
            TimerStep(step, state)
        }

        return TimerUiState(
            hasLoaded = true,
            isActive = true,
            isPaused = session.isPaused,
            totalSeconds = totalSeconds,
            remainingSeconds = remainingSeconds,
            steps = steps,
        )
    }

    private fun recomputePlan(session: CookingSessionEntity): CookingPlan {
        val ids = session.vegetableIdsCsv.split(",").filter { it.isNotBlank() }
        val vegetables = ids.mapNotNull(vegetableRepository::findById)
        return CookingPlanCalculator.compute(vegetables)
    }

    fun onPauseResumeToggle() {
        viewModelScope.launch {
            if (uiState.value.isPaused) cookingSessionRepository.resume() else cookingSessionRepository.pause()
        }
    }

    /** EF-24: +1 / +2 / +5. */
    fun onExtend(minutes: Int) {
        viewModelScope.launch { cookingSessionRepository.extend(minutes) }
    }

    /** EF-21 "Arrêt": réinitialise tout (décompte, étapes, plan masqué). */
    fun onStop() {
        viewModelScope.launch {
            cookingSessionRepository.stop()
            CookingTimerService.stop(context)
        }
    }
}
