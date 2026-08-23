package com.trucdecomptable.cuissonvapeur.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.trucdecomptable.cuissonvapeur.ui.common.CategoryBadge
import com.trucdecomptable.cuissonvapeur.ui.common.SeasonBadge

/** Spec §5: "Accueil (panier + décompte éventuel)" — the app's home/cart screen. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToCatalogue: () -> Unit,
    onNavigateToReglages: () -> Unit,
    onNavigateToTimer: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    // A session becomes active as soon as it's confirmed in the modal below;
    // this screen also re-navigates to Timer if the app is reopened while a
    // session from a previous launch is still running (EF-10-adjacent).
    LaunchedEffect(state.activeSession?.isActive) {
        if (state.activeSession?.isActive == true) onNavigateToTimer()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onNavigateToReglages) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_reglages))
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // EF-07: "N légumes dans le panier".
                Text(
                    text = stringResource(R.string.home_cart_count, state.cart.size),
                    style = MaterialTheme.typography.titleMedium,
                )
                if (state.cart.isNotEmpty()) {
                    TextButton(onClick = viewModel::onClearCart) {
                        Text(stringResource(R.string.home_clear_cart))
                    }
                }
            }

            if (state.cart.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.home_cart_empty), style = MaterialTheme.typography.bodyLarge)
                        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                        Button(onClick = onNavigateToCatalogue) {
                            Text(stringResource(R.string.home_browse_catalogue))
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(state.cart, key = { it.id }) { vegetable ->
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(vegetable.emoji, style = MaterialTheme.typography.headlineSmall)
                                    Column {
                                        Text(vegetable.name, style = MaterialTheme.typography.titleMedium)
                                        Text(vegetable.displayedRange, style = MaterialTheme.typography.bodySmall)
                                        // Infos (saison + catégorie) affichées sous chaque légume du panier.
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            SeasonBadge(seasons = vegetable.seasons)
                                            vegetable.category?.let { CategoryBadge(category = it) }
                                        }
                                    }
                                }
                                IconButton(onClick = { viewModel.onRemoveFromCart(vegetable.id) }) {
                                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_remove_from_cart))
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = viewModel::onStartCookingClicked,
                    enabled = state.cart.isNotEmpty(), // EF-09
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(64.dp), // NFR: cibles tactiles ≥ 64 dp sur les actions primaires
                ) {
                    Text(stringResource(R.string.home_start_cooking), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    state.confirmModalPlan?.let { plan ->
        CookingOrderConfirmModal(
            plan = plan,
            onConfirm = { viewModel.onConfirmStartCooking(onNavigateToTimer) },
            onDismiss = viewModel::onDismissConfirmModal,
        )
    }
}
