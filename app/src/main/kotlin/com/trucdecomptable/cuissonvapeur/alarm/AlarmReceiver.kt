package com.trucdecomptable.cuissonvapeur.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.trucdecomptable.cuissonvapeur.CuissonVapeurApp
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.domain.catalog.VegetableCatalog
import com.trucdecomptable.cuissonvapeur.ui.screens.alarme.AlarmActivity

/**
 * Fires on two occasions, both scheduled by [AlarmScheduler]:
 *  - [ACTION_END_ALARM] — end of cooking (EF-20/EF-23): must wake the
 *    screen, play a sound ≥ 3 s, and vibrate, even if the app is closed or
 *    the phone is locked (T4/T9 in spec §7). The actual sound/vibration
 *    loop is owned by [AlarmActivity] itself (which has a real lifecycle to
 *    stop them on dismiss) — this receiver's job is only to reliably get
 *    that activity on screen, via a full-screen-intent notification (the
 *    Android-recommended pattern; a bare `startActivity` from a
 *    broadcast receiver is blocked on modern Android when the app isn't in
 *    the foreground).
 *  - [ACTION_STEP_REMINDER] — EF-19: a light "add this vegetable now"
 *    reminder, distinct from the end alarm (short vibration + light
 *    notification, no full-screen wake).
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_END_ALARM -> handleEndAlarm(context)
            ACTION_STEP_REMINDER -> handleStepReminder(context, intent)
        }
    }

    private fun handleEndAlarm(context: Context) {
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CuissonVapeurApp.CHANNEL_END_ALARM)
            .setSmallIcon(R.drawable.ic_notification_steam)
            .setContentTitle(context.getString(R.string.alarm_notification_title))
            .setContentText(context.getString(R.string.alarm_notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        NotificationManagerCompatShim.notify(context, NOTIFICATION_ID_END_ALARM, notification)

        // Best-effort direct launch too — succeeds when the app already has
        // a foreground/visible context (e.g. the Timer screen is on screen);
        // the full-screen-intent notification above is the reliable path
        // when the app is fully backgrounded or the screen is off/locked.
        runCatching { context.startActivity(fullScreenIntent) }
    }

    private fun handleStepReminder(context: Context, intent: Intent) {
        val vegetableId = intent.getStringExtra(EXTRA_VEGETABLE_ID)
        val vegetable = VegetableCatalog.vegetables.firstOrNull { it.id == vegetableId }
        val vegetableName = vegetable?.name ?: return

        val contentIntent = Intent(context, AlarmActivity::class.java) // reused for nav target
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, CuissonVapeurApp.CHANNEL_STEP_REMINDER)
            .setSmallIcon(R.drawable.ic_notification_steam)
            .setContentTitle(context.getString(R.string.step_reminder_title, vegetableName))
            .setContentText(context.getString(R.string.step_reminder_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompatShim.notify(context, NOTIFICATION_ID_STEP_BASE + vegetableId.hashCode(), notification)
        vibrateLight(context)
    }

    private fun vibrateLight(context: Context) {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    companion object {
        const val ACTION_END_ALARM = "com.trucdecomptable.cuissonvapeur.action.END_ALARM"
        const val ACTION_STEP_REMINDER = "com.trucdecomptable.cuissonvapeur.action.STEP_REMINDER"
        const val EXTRA_VEGETABLE_ID = "vegetable_id"

        private const val NOTIFICATION_ID_END_ALARM = 1
        private const val NOTIFICATION_ID_STEP_BASE = 2000
    }
}

/** Small indirection so both this receiver and the service post through the same helper. */
internal object NotificationManagerCompatShim {
    fun notify(context: Context, id: Int, notification: android.app.Notification) {
        val hasPermission = androidx.core.content.PermissionChecker.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == androidx.core.content.PermissionChecker.PERMISSION_GRANTED
        // Pre-Android 13 needs no runtime POST_NOTIFICATIONS grant.
        if (hasPermission || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.getSystemService(context, NotificationManager::class.java)
                ?.notify(id, notification)
        }
    }
}
