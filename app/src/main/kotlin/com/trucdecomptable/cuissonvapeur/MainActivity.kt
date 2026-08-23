package com.trucdecomptable.cuissonvapeur

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

        setContent {
            val themeMode by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            CuissonVapeurTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppRoot()
                }
            }
        }
    }
}
