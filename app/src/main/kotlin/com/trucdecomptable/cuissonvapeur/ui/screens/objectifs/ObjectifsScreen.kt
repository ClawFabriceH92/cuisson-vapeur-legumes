package com.trucdecomptable.cuissonvapeur.ui.screens.objectifs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.domain.goals.NutritionGoal
import com.trucdecomptable.cuissonvapeur.ui.common.categoryLabel
import com.trucdecomptable.cuissonvapeur.ui.common.SettingsAction
import com.trucdecomptable.cuissonvapeur.ui.theme.GoalPendingRed
import com.trucdecomptable.cuissonvapeur.ui.theme.GoalReachedGreen

/**
 * EF-13/EF-14 Option A: 5 nutrition goals, "x/cible" + a reached/unreached
 * state that never relies on color alone (icon + text, per §12.1's
 * accessibility note on WCAG 1.4.1) — a check icon and "Atteint !" text
 * when reached, an outline icon and "Non atteint" text otherwise.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ObjectifsScreen(
    onNavigateToReglages: () -> Unit,
    viewModel: ObjectifsViewModel = hiltViewModel(),
) {
    val goals by viewModel.goals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_objectifs)) },
                actions = { SettingsAction(onClick = onNavigateToReglages) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(goals, key = { it.category }) { goal ->
                GoalCard(goal)
            }
        }
    }
}

@Composable
private fun GoalCard(goal: NutritionGoal) {
    val stateColor = if (goal.isReached) GoalReachedGreen else GoalPendingRed
    val stateLabel = stringResource(if (goal.isReached) R.string.objectif_atteint else R.string.objectif_non_atteint)

    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 0.dp)) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = categoryLabel(goal.category), style = MaterialTheme.typography.titleMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.semantics { contentDescription = "$stateLabel — ${goal.current} sur ${goal.target}" },
                ) {
                    Icon(
                        imageVector = if (goal.isReached) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = null,
                        tint = stateColor,
                    )
                    Text(text = stateLabel, color = stateColor, style = MaterialTheme.typography.labelLarge)
                }
            }

            Text(
                text = stringResource(R.string.objectif_counter, goal.current, goal.target),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )

            val progress = if (goal.target > 0) goal.current.toFloat() / goal.target else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = stateColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
