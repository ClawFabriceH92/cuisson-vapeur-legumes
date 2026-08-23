package com.trucdecomptable.cuissonvapeur.alarm

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.trucdecomptable.cuissonvapeur.data.repository.CookingSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles action buttons that can fire from a background context: the
 * persistent cooking notification's "Prolonger" action (EF-18) and the
 * end-of-cooking notification's "Prolonger +2 min" / dismiss actions
 * (EF-23/EF-24/D6). A `BroadcastReceiver` (not the Activity/Service
 * directly) because notification actions must work even when neither
 * [com.trucdecomptable.cuissonvapeur.MainActivity] nor
 * [com.trucdecomptable.cuissonvapeur.ui.screens.alarme.AlarmActivity] is
 * currently on screen.
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var cookingSessionRepository: CookingSessionRepository

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        // Own supervisor scope (not the fixed GlobalScope) so this receiver
        // controls its own short lifetime, matching goAsync()'s contract:
        // finish() must be called once the coroutine work is done.
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    ACTION_EXTEND -> {
                        val minutes = intent.getIntExtra(EXTRA_EXTEND_MINUTES, 2)
                        cookingSessionRepository.extend(minutes)
                    }

                    ACTION_DISMISS_ALARM -> {
                        cookingSessionRepository.stop()
                        context.getSystemService(NotificationManager::class.java)
                            ?.cancel(NOTIFICATION_ID_END_ALARM)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_EXTEND = "com.trucdecomptable.cuissonvapeur.action.EXTEND"
        const val ACTION_DISMISS_ALARM = "com.trucdecomptable.cuissonvapeur.action.DISMISS_ALARM"
        const val EXTRA_EXTEND_MINUTES = "extend_minutes"
        private const val NOTIFICATION_ID_END_ALARM = 1
    }
}
