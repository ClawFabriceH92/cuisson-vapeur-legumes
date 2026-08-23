package com.trucdecomptable.cuissonvapeur

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.trucdecomptable.cuissonvapeur.data.local.entity.ThemeMode
import com.trucdecomptable.cuissonvapeur.ui.AppRoot
import com.trucdecomptable.cuissonvapeur.ui.MainViewModel
import com.trucdecomptable.cuissonvapeur.ui.theme.CuissonVapeurTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-activity host for the whole Compose UI graph (spec §5's screen
 * tree), except [com.trucdecomptable.cuissonvapeur.ui.screens.alarme.AlarmActivity]
 * (EF-23's full-screen alarm gets its own Activity so it can be launched
 * over the lock screen independently of this one's task/back-stack).
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            CuissonVapeurTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }

    /**
     * Android 13+ requires the runtime POST_NOTIFICATIONS grant before any
     * notification (step reminders "ajoute ce légume maintenant", end alarm)
     * can be shown. Without it the step alerts silently never appear —
     * Fabrice's "alerte quand faut mettre le deuxième légume" (fix 23/08/2026).
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ_NOTIFICATIONS)
        }
    }

    companion object {
        private const val REQ_NOTIFICATIONS = 1001
    }
}
