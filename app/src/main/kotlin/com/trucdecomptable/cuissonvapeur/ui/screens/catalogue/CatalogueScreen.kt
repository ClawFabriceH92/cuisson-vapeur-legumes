package com.trucdecomptable.cuissonvapeur.ui.screens.catalogue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.ui.common.SortMode
import com.trucdecomptable.cuissonvapeur.ui.common.VegetableCard

/** EF-01..EF-05: the 28-vegetable catalog grid, with search/sort/season filter. */
@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CatalogueScreen(
    onOpenDetail: (String) -> Unit,
    viewModel: CatalogueViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_catalogue)) }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.catalogue_search_hint)) },
                singleLine = true,
            )

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                SegmentedButton(
                    selected = state.sortMode == SortMode.TEMPS_CROISSANT,
                    onClick = { viewModel.onSortModeChange(SortMode.TEMPS_CROISSANT) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text(stringResource(R.string.catalogue_sort_time)) }
                SegmentedButton(
                    selected = state.sortMode == SortMode.NOM_ALPHABETIQUE,
                    onClick = { viewModel.onSortModeChange(SortMode.NOM_ALPHABETIQUE) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text(stringResource(R.string.catalogue_sort_name)) }
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SeasonQuickFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.seasonFilter == filter,
                        onClick = { viewModel.onSeasonFilterToggle(filter) },
                        label = { Text(seasonFilterLabel(filter)) },
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.vegetables, key = { it.vegetable.id }) { item ->
                    VegetableCard(
                        vegetable = item.vegetable,
                        isInCart = item.isInCart,
                        isFavorite = item.isFavorite,
                        onToggleCart = { viewModel.onToggleCart(item.vegetable.id, item.isInCart) },
                        onToggleFavorite = { viewModel.onToggleFavorite(item.vegetable.id) },
                        onOpenDetail = { onOpenDetail(item.vegetable.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun seasonFilterLabel(filter: SeasonQuickFilter): String = when (filter) {
    SeasonQuickFilter.PRINTEMPS -> stringResource(R.string.season_printemps)
    SeasonQuickFilter.ETE -> stringResource(R.string.season_ete)
    SeasonQuickFilter.AUTOMNE -> stringResource(R.string.season_automne)
    SeasonQuickFilter.HIVER -> stringResource(R.string.season_hiver)
    SeasonQuickFilter.TOUTE_ANNEE -> stringResource(R.string.season_all_year)
}
