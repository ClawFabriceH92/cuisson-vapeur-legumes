package com.trucdecomptable.cuissonvapeur.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingPlan
import com.trucdecomptable.cuissonvapeur.domain.plan.CookingStep
import com.trucdecomptable.cuissonvapeur.ui.common.seasonLabel

/**
 * EF-17: the "Ordre optimal de cuisson" confirmation modal — numbered steps
 * (icon, name, season, cooking time, "Maintenant" or "Dans N min — Ajouter
 * au panier"), an instructions block (total time, chronological order,
 * "tous finissent en même temps"), and Démarrer/Annuler.
 */
@Composable
fun CookingOrderConfirmModal(
    plan: CookingPlan,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.modal_title)) },
        text = {
            // A plain, non-lazy Column: the step list is capped at 28 items
            // (the whole catalog) and this whole block already scrolls via
            // verticalScroll below, so a LazyColumn here would need its own
            // explicit height (to avoid Compose's "unbounded height" crash)
            // for nothing gained.
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                plan.steps.forEachIndexed { index, step ->
                    StepRow(index = index + 1, step = step)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = stringResource(R.string.modal_instructions_title),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.modal_instructions_total_time, plan.totalMinutes),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(R.string.modal_instructions_order),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = stringResource(R.string.modal_instructions_same_time),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.modal_start_cooking)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_annuler)) }
        },
    )
}

@Composable
private fun StepRow(index: Int, step: CookingStep) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("$index.", style = MaterialTheme.typography.labelLarge)
            Text(step.vegetable.emoji, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(step.vegetable.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.modal_step_season_time, seasonLabel(step.vegetable.seasons), step.vegetable.displayedRange),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = if (step.isImmediate) {
                    stringResource(R.string.modal_step_now)
                } else {
                    stringResource(R.string.modal_step_in_n_min, step.startOffsetMinutes)
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (step.isImmediate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
