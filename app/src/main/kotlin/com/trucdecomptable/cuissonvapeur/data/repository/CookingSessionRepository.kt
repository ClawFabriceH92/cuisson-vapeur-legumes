package com.trucdecomptable.cuissonvapeur.data.repository

import com.trucdecomptable.cuissonvapeur.alarm.AlarmScheduler
import com.trucdecomptable.cuissonvapeur.data.local.dao.CookingSessionDao
import com.trucdecomptable.cuissonvapeur.data.local.entity.CookingSessionEntity
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlan
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlanCalculator
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Owns the *active* cooking session: starting it (EF-16/EF-17), pausing /
 * resuming / stopping it (EF-21), extending it (EF-24), and reconstructing
 * it after process death or a reboot (§12.2).
 *
 * Central rule enforced here, straight from §12.2: **every** transition
 * that changes timing — start, pause, resume, extend, reboot — cancels
 * *all* pending alarms and reschedules them from scratch against the
 * recomputed plan. There is deliberately no code path that nudges an
 * existing alarm; alarms are always fully torn down and rebuilt from the
 * current [CookingSessionEntity] + a freshly recomputed [CookingPlan].
 */
@Singleton
class CookingSessionRepository @Inject constructor(
    private val cookingSessionDao: CookingSessionDao,
    private val alarmScheduler: AlarmScheduler,
    private val vegetableRepository: VegetableRepository,
) {

    fun observeSession(): Flow<CookingSessionEntity?> = cookingSessionDao.observe()

    suspend fun currentSessionOnce(): CookingSessionEntity? = cookingSessionDao.getOnce()

    /** EF-16/EF-17: start a new cooking session from the confirmed cart selection. */
    suspend fun startCooking(selection: List<Vegetable>) {
        require(selection.isNotEmpty()) { "EF-09: cannot start cooking with an empty cart." }

        val plan = CookingPlanCalculator.compute(selection)
        val endEpochMillis = System.currentTimeMillis() + plan.totalMinutes * MILLIS_PER_MINUTE

        val session = CookingSessionEntity(
            isActive = true,
            isPaused = false,
            vegetableIdsCsv = selection.joinToString(",") { it.id },
            totalMinutes = plan.totalMinutes,
            endEpochMillis = endEpochMillis,
            remainingMillisWhenPaused = 0L,
            extendedMinutesTotal = 0,
        )
        cookingSessionDao.upsert(session)
        rescheduleAllAlarms(session, plan)
    }

    /** EF-21 "Pause": freezes the remaining duration and cancels all pending alarms. */
    suspend fun pause() {
        val session = cookingSessionDao.getOnce() ?: return
        if (!session.isActive || session.isPaused) return

        val remaining = (session.endEpochMillis - System.currentTimeMillis()).coerceAtLeast(0)
        cookingSessionDao.upsert(session.copy(isPaused = true, remainingMillisWhenPaused = remaining))
        alarmScheduler.cancelAll()
    }

    /**
     * EF-21 "Reprise" — T5: resuming recomputes a brand-new end timestamp
     * from "now + frozen remaining duration", then reschedules every
     * alarm (end + remaining per-step reminders) against that new
     * timestamp, so the resumed countdown lands on the frozen remaining
     * value to within ±1 s, exactly as T5 requires.
     */
    suspend fun resume() {
        val session = cookingSessionDao.getOnce() ?: return
        if (!session.isActive || !session.isPaused) return

        val newEndEpochMillis = System.currentTimeMillis() + session.remainingMillisWhenPaused
        val resumed = session.copy(isPaused = false, endEpochMillis = newEndEpochMillis)
        cookingSessionDao.upsert(resumed)
        rescheduleAllAlarms(resumed, recomputePlan(resumed))
    }

    /**
     * EF-24: +1 / +2 / +5 min. Whether running or paused, this always
     * recalculates and reschedules every remaining alarm so "toutes les
     * étiquettes 'Dans N min' restent cohérentes" (EF-24).
     */
    suspend fun extend(minutes: Int) {
        val session = cookingSessionDao.getOnce() ?: return
        if (!session.isActive) return

        val updated = if (session.isPaused) {
            session.copy(
                remainingMillisWhenPaused = session.remainingMillisWhenPaused + minutes * MILLIS_PER_MINUTE,
                extendedMinutesTotal = session.extendedMinutesTotal + minutes,
            )
        } else {
            session.copy(
                endEpochMillis = session.endEpochMillis + minutes * MILLIS_PER_MINUTE,
                extendedMinutesTotal = session.extendedMinutesTotal + minutes,
            )
        }
        cookingSessionDao.upsert(updated)
        if (!updated.isPaused) {
            rescheduleAllAlarms(updated, recomputePlan(updated))
        }
    }

    /** EF-21 "Arrêt": clears state and cancels all alarms. Also used to dismiss the end alarm. */
    suspend fun stop() {
        alarmScheduler.cancelAll()
        cookingSessionDao.clear()
    }

    /**
     * §12.2: called from [com.trucdecomptable.cuissonvapeur.alarm.BootReceiver].
     * `ELAPSED_REALTIME` resets on reboot, so every previously-scheduled
     * alarm is gone; re-derive fresh ones from the persisted absolute
     * [CookingSessionEntity.endEpochMillis] (paused sessions need no
     * alarms — they're re-armed on the next explicit "Reprendre").
     */
    suspend fun rearmAfterReboot() {
        val session = cookingSessionDao.getOnce() ?: return
        if (!session.isActive || session.isPaused) return

        if (session.endEpochMillis <= System.currentTimeMillis()) {
            // The cooking end time already passed while the phone was
            // rebooting — fire the end alarm right away instead of missing it.
            alarmScheduler.scheduleEndAlarm(System.currentTimeMillis())
            return
        }
        rescheduleAllAlarms(session, recomputePlan(session))
    }

    private fun recomputePlan(session: CookingSessionEntity): CookingPlan {
        val ids = session.vegetableIdsCsv.split(",").filter { it.isNotBlank() }
        val vegetables = ids.mapNotNull(vegetableRepository::findById)
        return CookingPlanCalculator.compute(vegetables)
    }

    private fun rescheduleAllAlarms(session: CookingSessionEntity, plan: CookingPlan) {
        alarmScheduler.cancelAll()

        alarmScheduler.scheduleEndAlarm(session.endEpochMillis)

        val startEpochMillis = session.endEpochMillis - plan.totalMinutes * MILLIS_PER_MINUTE
        val now = System.currentTimeMillis()

        plan.steps.forEachIndexed { index, step ->
            if (step.startOffsetMinutes <= 0) return@forEachIndexed // "Maintenant": no reminder needed
            val triggerEpochMillis = startEpochMillis + step.startOffsetMinutes * MILLIS_PER_MINUTE
            if (triggerEpochMillis <= now) return@forEachIndexed // already past, e.g. right after resume
            alarmScheduler.scheduleStepAlarm(index, step.vegetable.id, triggerEpochMillis)
        }
    }

    companion object {
        private const val MILLIS_PER_MINUTE = 60_000L
    }
}
