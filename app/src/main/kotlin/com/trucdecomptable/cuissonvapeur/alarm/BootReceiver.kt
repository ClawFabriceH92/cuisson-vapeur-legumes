package com.trucdecomptable.cuissonvapeur.alarm

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
 * §12.2: "Reconstruction d'état après kill process ou redémarrage du
 * téléphone" — `ELAPSED_REALTIME` resets to 0 across a reboot, so every
 * alarm scheduled by [AlarmScheduler] is implicitly lost when the phone
 * restarts. This receiver re-arms them from the persisted absolute end
 * timestamp ([com.trucdecomptable.cuissonvapeur.data.local.entity.CookingSessionEntity.endEpochMillis])
 * if a cooking session was active at reboot time.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var cookingSessionRepository: CookingSessionRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                cookingSessionRepository.rearmAfterReboot()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
