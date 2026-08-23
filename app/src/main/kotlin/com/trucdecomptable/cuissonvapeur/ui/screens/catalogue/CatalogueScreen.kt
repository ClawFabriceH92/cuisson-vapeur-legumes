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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.ui.common.SortMode
import com.trucdecomptable.cuissonvapeur.ui.common.VegetableCard

/**
 * EF-01..EF-05: the 28-vegetable catalog grid, with search/sort/season filter.
 * UI refined 23/08/2026 (Fabrice's feedback): the search bar is hidden behind
 * a magnifier icon, season filters are compact emoji chips, and a cart badge
 * in the top bar shows the selected count and jumps to the home cart.
 */
@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CatalogueScreen(
    onOpenDetail: (String) -> Unit,
    onOpenCart: () -> Unit,
    viewModel: CatalogueViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_catalogue)) },
                actions = {
                    // Loupe : ouvre/referme la recherche pour laisser la place aux légumes.
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            imageVector = if (showSearch) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = stringResource(R.string.catalogue_search_hint),
                        )
                    }
                    // Panier : badge avec le nombre de légumes cochés, mène à l'Accueil.
                    BadgedBox(
                        badge = {
                            if (state.cartCount > 0) {
                                Badge { Text(state.cartCount.toString()) }
                            }
                        },
                    ) {
                        IconButton(onClick = onOpenCart) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = stringResource(R.string.nav_accueil),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showSearch) {
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
            }

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
                    val filterLabel = seasonFilterLabel(filter)
                    FilterChip(
                        selected = state.seasonFilter == filter,
                        onClick = { viewModel.onSeasonFilterToggle(filter) },
                        label = { Text(seasonFilterIcon(filter), style = MaterialTheme.typography.titleMedium) },
                        modifier = Modifier.semantics {
                            contentDescription = filterLabel
                        },
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
private fun seasonFilterIcon(filter: SeasonQuickFilter): String = when (filter) {
    SeasonQuickFilter.PRINTEMPS -> "🌷"
    SeasonQuickFilter.ETE -> "☀️"
    SeasonQuickFilter.AUTOMNE -> "🍂"
    SeasonQuickFilter.HIVER -> "❄️"
    SeasonQuickFilter.TOUTE_ANNEE -> "📅"
}

@Composable
private fun seasonFilterLabel(filter: SeasonQuickFilter): String = when (filter) {
    SeasonQuickFilter.PRINTEMPS -> stringResource(R.string.season_printemps)
    SeasonQuickFilter.ETE -> stringResource(R.string.season_ete)
    SeasonQuickFilter.AUTOMNE -> stringResource(R.string.season_automne)
    SeasonQuickFilter.HIVER -> stringResource(R.string.season_hiver)
    SeasonQuickFilter.TOUTE_ANNEE -> stringResource(R.string.season_all_year)
}
