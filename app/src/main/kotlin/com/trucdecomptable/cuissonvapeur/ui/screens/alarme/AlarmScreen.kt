package com.trucdecomptable.cuissonvapeur.ui.screens.alarme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trucdecomptable.cuissonvapeur.R

/** EF-23: gros bouton "OK, c'est prêt" + "Prolonger +2 min", plein écran. */
@Composable
fun AlarmScreen(onDismiss: () -> Unit, onExtend: (Int) -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "⏰", style = MaterialTheme.typography.displayLarge)

            Text(
                text = stringResource(R.string.alarm_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = 24.dp, bottom = 48.dp),
            )

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(72.dp), // NFR: cible tactile primaire ≥ 64dp
            ) {
                Text(stringResource(R.string.alarm_ok_ready), style = MaterialTheme.typography.titleLarge)
            }

            OutlinedButton(
                onClick = { onExtend(2) },
                modifier = Modifier.fillMaxWidth().height(64.dp).padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.alarm_extend_2min))
            }
        }
    }
}
