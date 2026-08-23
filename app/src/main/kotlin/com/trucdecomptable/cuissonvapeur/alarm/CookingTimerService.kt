package com.trucdecomptable.cuissonvapeur.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.pm.ServiceInfoCompat
import com.trucdecomptable.cuissonvapeur.CuissonVapeurApp
import com.trucdecomptable.cuissonvapeur.MainActivity
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.data.local.entity.CookingSessionEntity
import com.trucdecomptable.cuissonvapeur.data.repository.CookingSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Minimal Android 12+ foreground service (NFR §4: "service de fond minimal
 * sur Android 12+") — its only purpose is to keep the persistent cooking
 * notification alive and up to date (EF-18/EF-20: "notification persistante
 * visible dans le tiroir : icône + temps restant + action Prolonger").
 *
 * It does **not** own the countdown or the alarms itself — those are
 * [AlarmScheduler] (exact alarms, survive the service being killed) and
 * [CookingSessionRepository] (source of truth in Room). This service just
 * mirrors that state into a notification once a second while a session is
 * active, and stops itself once the session ends or is stopped.
 */
@AndroidEntryPoint
class CookingTimerService : Service() {

    @Inject lateinit var cookingSessionRepository: CookingSessionRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tickerJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundWithPlaceholder()
        observeSession()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // START_STICKY: if the OS kills this service under memory pressure,
        // restart it — the alarms themselves are independent of this
        // service's lifetime, but we want the persistent notification back.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        tickerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startForegroundWithPlaceholder() {
        val notification = buildNotification(remainingText = getString(R.string.notification_cooking_starting))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                ServiceInfoCompat.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun observeSession() {
        tickerJob = serviceScope.launch {
            while (true) {
                val session = cookingSessionRepository.currentSessionOnce()
                if (session == null || !session.isActive) {
                    stopSelf()
                    return@launch
                }
                updateNotification(session)
                delay(1_000)
            }
        }
    }

    private fun updateNotification(session: CookingSessionEntity) {
        val remainingMillis = if (session.isPaused) {
            session.remainingMillisWhenPaused
        } else {
            (session.endEpochMillis - System.currentTimeMillis()).coerceAtLeast(0)
        }
        val remainingText = formatRemaining(remainingMillis, session.isPaused)
        val notification = buildNotification(remainingText)
        getSystemService(android.app.NotificationManager::class.java)
            ?.notify(NOTIFICATION_ID, notification)
    }

    private fun formatRemaining(remainingMillis: Long, isPaused: Boolean): String {
        val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val time = String.format("%02d:%02d", minutes, seconds)
        return if (isPaused) getString(R.string.notification_cooking_paused, time) else time
    }

    private fun buildNotification(remainingText: String): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val extendIntent = Intent(this, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_EXTEND
            putExtra(NotificationActionReceiver.EXTRA_EXTEND_MINUTES, 2)
        }
        val extendPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            extendIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CuissonVapeurApp.CHANNEL_COOKING_PROGRESS)
            .setSmallIcon(R.drawable.ic_notification_steam)
            .setContentTitle(getString(R.string.notification_cooking_title))
            .setContentText(remainingText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(0, getString(R.string.action_extend_2min), extendPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 42

        fun start(context: Context) {
            val intent = Intent(context, CookingTimerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CookingTimerService::class.java))
        }
    }
}
