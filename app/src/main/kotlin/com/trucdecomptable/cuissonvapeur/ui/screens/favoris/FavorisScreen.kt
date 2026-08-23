package com.trucdecomptable.cuissonvapeur.ui.screens.favoris

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.ui.common.SettingsAction

/** EF-12: "Favoris" — list with a clickable "Sélectionné / Sélectionner" state + removal. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun FavorisScreen(
    onNavigateToReglages: () -> Unit,
    viewModel: FavorisViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_favoris)) },
                actions = { SettingsAction(onClick = onNavigateToReglages) },
            )
        },
    ) { padding ->
        if (favorites.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.favoris_empty), style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
            items(favorites, key = { it.vegetable.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(item.vegetable.emoji, style = MaterialTheme.typography.headlineSmall)
                            Text(item.vegetable.name, style = MaterialTheme.typography.titleMedium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { viewModel.onToggleCart(item.vegetable.id, item.isInCart) }) {
                                Text(
                                    stringResource(
                                        if (item.isInCart) R.string.favoris_selected else R.string.favoris_select,
                                    ),
                                )
                            }
                            IconButton(onClick = { viewModel.onRemoveFavorite(item.vegetable.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_remove_favorite))
                            }
                        }
                    }
                }
            }
        }
    }
}
