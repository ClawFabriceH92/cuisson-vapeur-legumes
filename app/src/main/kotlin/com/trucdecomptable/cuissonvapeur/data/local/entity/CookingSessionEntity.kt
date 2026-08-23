package com.trucdecomptable.cuissonvapeur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row table holding the *active* cooking session, if any (spec §12.2).
 *
 * Design notes (see root README, "Décisions non pinned"):
 *  - While [isPaused] is false and [isActive] is true, [endEpochMillis] is
 *    the **absolute wall-clock end timestamp** (`System.currentTimeMillis()`
 *    at which the timer reaches 0) — an absolute timestamp, not a remaining
 *    duration, exactly as required by §12.2 so the UI and alarms can be
 *    reconstructed after process death or a reboot.
 *  - While paused, there is no valid end timestamp (nothing is counting
 *    down), so [remainingMillisWhenPaused] captures the frozen remaining
 *    duration instead, and [endEpochMillis] is left stale/unused.
 *  - The **alarms themselves** are never scheduled directly against this
 *    wall-clock timestamp: [com.trucdecomptable.cuissonvapeur.alarm.AlarmScheduler]
 *    always converts `endEpochMillis` into a fresh
 *    `SystemClock.elapsedRealtime() + (endEpochMillis - System.currentTimeMillis())`
 *    delay at the moment it calls `setExactAndAllowWhileIdle`, per §12.2's
 *    requirement to schedule on `ELAPSED_REALTIME`, not `RTC`. This is what
 *    lets a single persisted field serve both purposes: cross-reboot
 *    reconstruction (needs an absolute timestamp) and drift-free alarms
 *    (need an elapsed-realtime trigger).
 *  - [vegetableIdsCsv] + [totalMinutes] are enough to recompute the full
 *    [com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlan] again (the
 *    algorithm is deterministic), so individual step states don't need to be
 *    persisted separately — they're derived from `endEpochMillis` +
 *    `totalMinutes` + the recomputed plan.
 */
@Entity(tableName = "cooking_session")
data class CookingSessionEntity(
    @PrimaryKey val id: Int = SESSION_ROW_ID,
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    /** Comma-separated [com.trucdecomptable.cuissonvapeur.domain.model.Vegetable.id] list. */
    val vegetableIdsCsv: String = "",
    /** "T" from EF-16 — the original, un-extended total in minutes. */
    val totalMinutes: Int = 0,
    /** Absolute epoch millis at which the countdown reaches 0. Valid only while running. */
    val endEpochMillis: Long = 0L,
    /** Frozen remaining duration, captured on pause. Valid only while paused. */
    val remainingMillisWhenPaused: Long = 0L,
    /** Cumulative minutes added via "+1 / +2 / +5" (EF-24), for display only. */
    val extendedMinutesTotal: Int = 0,
) {
    companion object {
        const val SESSION_ROW_ID = 0
    }
}
