package com.trucdecomptable.cuissonvapeur.ui.screens.catalogue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.ui.common.CategoryBadge
import com.trucdecomptable.cuissonvapeur.ui.common.SeasonBadge

/** EF-05: the vegetable detail sheet — time, season, calories, benefits, category, icon. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VegetableDetailScreen(
    vegetableId: String,
    onBack: () -> Unit,
    viewModel: VegetableDetailViewModel = hiltViewModel(),
) {
    val vegetable = viewModel.findVegetable(vegetableId) ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vegetable.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = vegetable.emoji, style = MaterialTheme.typography.displayMedium)

            Text(
                text = stringResource(R.string.vegetable_duration_range, vegetable.displayedRange),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.vegetable_kcal, vegetable.kcalPer100g),
                style = MaterialTheme.typography.bodyLarge,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SeasonBadge(seasons = vegetable.seasons)
                vegetable.category?.let { CategoryBadge(category = it) }
            }

            Text(text = stringResource(R.string.vegetable_benefits_title), style = MaterialTheme.typography.titleSmall)
            vegetable.benefits.forEach { benefit ->
                Text(text = "• $benefit", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
