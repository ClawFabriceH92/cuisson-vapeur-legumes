package com.trucdecomptable.cuissonvapeur.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps [AlarmManager.setExactAndAllowWhileIdle] for the end-of-cooking
 * alarm (EF-23) and the per-step reminder alarms (EF-19), per §12.2's
 * technical requirement:
 *
 * "préciser l'usage du temps écoulé (`ELAPSED_REALTIME`, via
 * `setExactAndAllowWhileIdle`) plutôt que de l'horloge murale (`RTC`), pour
 * éviter toute dérive en cas de changement d'heure ou de fuseau pendant une
 * cuisson en cours."
 *
 * Callers (see [com.trucdecomptable.cuissonvapeur.data.repository.CookingSessionRepository])
 * still *persist* an absolute wall-clock end timestamp (also per §12.2, so a
 * killed process or a reboot can reconstruct state) — but every time an
 * alarm is actually armed here, that absolute timestamp is converted to a
 * fresh `SystemClock.elapsedRealtime()`-based trigger, so the alarm itself
 * never drifts with wall-clock/timezone changes. This is why *every*
 * pause/resume/extend/reboot must go through this scheduler again: an
 * elapsed-realtime alarm scheduled once cannot simply be "shifted" from
 * outside; it must be re-armed against elapsed-realtime each time.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
) {

    /** True once the OS has granted exact-alarm scheduling (Android 12+, EF risk in spec §9). */
    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

    /** EF-16/EF-20/EF-23: the alarm that fires when the whole cooking session ends. */
    fun scheduleEndAlarm(endEpochMillis: Long) {
        val pendingIntent = endAlarmPendingIntent()
        setExactFromEpoch(endEpochMillis, pendingIntent)
    }

    fun cancelEndAlarm() {
        alarmManager.cancel(endAlarmPendingIntent())
    }

    /** EF-19: a light reminder to add [vegetableId] to the steamer, at its `départ(i)`. */
    fun scheduleStepAlarm(stepIndex: Int, vegetableId: String, triggerEpochMillis: Long) {
        val pendingIntent = stepAlarmPendingIntent(stepIndex, vegetableId)
        setExactFromEpoch(triggerEpochMillis, pendingIntent)
    }

    private fun setExactFromEpoch(triggerEpochMillis: Long, pendingIntent: PendingIntent) {
        val delayMillis = (triggerEpochMillis - System.currentTimeMillis()).coerceAtLeast(0)
        val triggerElapsedRealtime = SystemClock.elapsedRealtime() + delayMillis
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                triggerElapsedRealtime,
                pendingIntent,
            )
        } catch (_: SecurityException) {
            // Android 12+ (S) without the SCHEDULE_EXACT_ALARM grant throws
            // here (spec §9's flagged risk). Degraded fallback: an inexact
            // alarm still fires, just without the "exact ±1s even in Doze"
            // guarantee — better than the session silently never firing.
            // §12.1 leaves the full "état dégradé" UX as an open, undecided
            // question (a "Proposition", not a pinned EF) — see root README.
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerElapsedRealtime, pendingIntent)
        }
    }

    /**
     * §12.2: cancel *every* pending alarm (end + all per-step reminders) —
     * called before any reschedule (pause/resume/extend/reboot) so stale
     * alarms from before the recalculation can never fire.
     */
    fun cancelAll() {
        cancelEndAlarm()
        for (stepIndex in 0 until MAX_STEPS) {
            alarmManager.cancel(stepAlarmPendingIntent(stepIndex, vegetableId = ""))
        }
    }

    private fun endAlarmPendingIntent(): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_END_ALARM
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_END_ALARM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun stepAlarmPendingIntent(stepIndex: Int, vegetableId: String): PendingIntent {
        // Note: PendingIntent identity (for both scheduling and cancel-by-match)
        // is action + component + requestCode — NOT extras. Each step gets a
        // distinct requestCode so it can be individually scheduled/cancelled;
        // the vegetableId extra just rides along for AlarmReceiver's own use.
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_STEP_REMINDER
            putExtra(AlarmReceiver.EXTRA_VEGETABLE_ID, vegetableId)
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_STEP_BASE + stepIndex,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        private const val REQUEST_CODE_END_ALARM = 9999
        private const val REQUEST_CODE_STEP_BASE = 1000

        // The catalog has 28 vegetables (EF-01); a selection can never need
        // more per-step request codes than that.
        private const val MAX_STEPS = 28
    }
}
