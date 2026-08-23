package com.trucdecomptable.cuissonvapeur.ui.screens.alarme

import android.app.KeyguardManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.trucdecomptable.cuissonvapeur.data.local.entity.ThemeMode
import com.trucdecomptable.cuissonvapeur.ui.theme.CuissonVapeurTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * EF-23: the full-screen end-of-cooking alarm — "plein écran, réveil
 * d'écran, son + vibration", shown even over the lock screen and even if
 * the app was fully closed (launched via [com.trucdecomptable.cuissonvapeur.alarm.AlarmReceiver]'s
 * full-screen-intent notification). Deliberately a separate `Activity` from
 * [com.trucdecomptable.cuissonvapeur.MainActivity] so it can be brought up
 * independently of that activity's task/back-stack state.
 */
@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpLockScreenWakeFlags()

        // Not gated behind a Composable LaunchedEffect on purpose: the sound
        // must start the instant this Activity exists, not on first
        // recomposition, and startRinging() is idempotent (it cancels any
        // previous loop first) so a re-entrant onCreate — e.g. after a
        // config change — cannot double-arm it.
        viewModel.startRinging()

        setContent {
            // Always shown regardless of the app-wide theme setting: an
            // urgent full-screen alarm should look the same every time, not
            // shift with the user's light/dark preference (EF-23's
            // affordance intentionally has its own dedicated style — see
            // Theme.CuissonVapeur.Alarm in themes.xml).
            CuissonVapeurTheme(themeMode = ThemeMode.DARK) {
                AlarmScreen(
                    onDismiss = { viewModel.onDismiss(onDone = ::finishAndRemoveTask) },
                    onExtend = { minutes -> viewModel.onExtend(minutes, onDone = ::finishAndRemoveTask) },
                )
            }
        }
    }

    private fun setUpLockScreenWakeFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            getSystemService(KeyguardManager::class.java)?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            )
        }
    }
}
