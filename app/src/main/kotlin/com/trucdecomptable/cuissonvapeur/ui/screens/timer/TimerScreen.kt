package com.trucdecomptable.cuissonvapeur.ui.screens.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.ui.theme.GoalReachedGreen

/**
 * EF-18/EF-20..EF-24: the active-timer screen — MM:SS countdown + progress
 * ring, the per-step list (à venir / AJOUTER MAINTENANT / ajoutée ✓), and
 * the pause/resume/extend/stop action bar.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(onSessionEnded: () -> Unit, viewModel: TimerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isActive) {
        if (!state.isActive) onSessionEnded()
    }

    if (!state.isActive) return

    // A session whose countdown has reached zero is finished: show it as
    // such instead of a frozen 00:00, so the user knows to press Arrêter
    // and start a new cooking (fix 23/08/2026 — the Home screen no longer
    // redirects to an expired session either).
    val isFinished = !state.isPaused && state.remainingSeconds == 0

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isFinished) {
                Text(
                    text = stringResource(R.string.timer_finished),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 40.dp),
                )
                Text(
                    text = stringResource(R.string.timer_finished_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                CountdownRing(remainingSeconds = state.remainingSeconds, progressFraction = state.progressFraction)
            }

            if (state.isPaused) {
                Text(
                    text = stringResource(R.string.timer_paused),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Text(
                text = stringResource(R.string.timer_steps_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            )

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(state.steps, key = { it.step.vegetable.id }) { timerStep ->
                    StepListItem(timerStep)
                }
            }

            ExtendRow(onExtend = viewModel::onExtend)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = viewModel::onPauseResumeToggle,
                    modifier = Modifier.weight(1f).height(64.dp), // NFR: cibles ≥ 64dp
                    enabled = !isFinished,
                ) {
                    Text(stringResource(if (state.isPaused) R.string.timer_resume else R.string.timer_pause))
                }
                Button(
                    onClick = viewModel::onStop,
                    modifier = Modifier.weight(1f).height(64.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text(stringResource(R.string.timer_stop))
                }
            }
        }
    }
}

@Composable
private fun CountdownRing(remainingSeconds: Int, progressFraction: Float) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    Box(
        modifier = Modifier
            .padding(top = 24.dp)
            .aspectRatio(1f)
            .size(220.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 10.dp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // NFR: décompte ≥ 72dp — displayLarge is set to 72sp in ui/theme/Type.kt.
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "${(progressFraction * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun StepListItem(timerStep: TimerStep) {
    val step = timerStep.step
    val (containerColor, labelRes) = when (timerStep.state) {
        StepState.A_VENIR -> MaterialTheme.colorScheme.surfaceVariant to R.string.step_state_a_venir
        StepState.AJOUTER_MAINTENANT -> MaterialTheme.colorScheme.tertiaryContainer to R.string.step_state_ajouter_maintenant
        StepState.AJOUTEE -> GoalReachedGreen.copy(alpha = 0.18f) to R.string.step_state_ajoutee
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(step.vegetable.emoji, style = MaterialTheme.typography.titleLarge)
                Text(step.vegetable.name, style = MaterialTheme.typography.bodyLarge)
            }
            Text(stringResource(labelRes), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ExtendRow(onExtend: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        listOf(1, 2, 5).forEach { minutes ->
            TextButton(onClick = { onExtend(minutes) }, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.timer_extend_n, minutes))
            }
        }
    }
}
