package com.trucdecomptable.cuissonvapeur

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point (Hilt root). Also creates the notification
 * channels used across the app: EF-18/EF-20's persistent cooking-progress
 * notification, EF-19's light step-reminder notification, and EF-23's
 * end-of-cooking alarm notification (each needs its own importance/behavior).
 */
@HiltAndroidApp
class CuissonVapeurApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = getSystemService(NotificationManager::class.java) ?: return

        val cookingChannel = NotificationChannel(
            CHANNEL_COOKING_PROGRESS,
            getString(R.string.notification_channel_cooking_progress_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_cooking_progress_desc)
            setShowBadge(false)
        }

        val stepReminderChannel = NotificationChannel(
            CHANNEL_STEP_REMINDER,
            getString(R.string.notification_channel_step_reminder_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = getString(R.string.notification_channel_step_reminder_desc)
        }

        val alarmChannel = NotificationChannel(
            CHANNEL_END_ALARM,
            getString(R.string.notification_channel_end_alarm_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.notification_channel_end_alarm_desc)
            enableVibration(true)
            setBypassDnd(true)
            lockscreenVisibility = NotificationManager.IMPORTANCE_HIGH
        }

        manager.createNotificationChannels(
            listOf(cookingChannel, stepReminderChannel, alarmChannel),
        )
    }

    companion object {
        const val CHANNEL_COOKING_PROGRESS = "cooking_progress"
        const val CHANNEL_STEP_REMINDER = "step_reminder"
        const val CHANNEL_END_ALARM = "end_alarm"
    }
}
