package com.trucdecomptable.cuissonvapeur.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.ui.navigation.Destinations
import com.trucdecomptable.cuissonvapeur.ui.screens.catalogue.CatalogueScreen
import com.trucdecomptable.cuissonvapeur.ui.screens.catalogue.VegetableDetailScreen
import com.trucdecomptable.cuissonvapeur.ui.screens.conseils.ConseilsScreen
import com.trucdecomptable.cuissonvapeur.ui.screens.favoris.FavorisScreen
import com.trucdecomptable.cuissonvapeur.ui.screens.home.HomeScreen
import com.trucdecomptable.cuissonvapeur.ui.screens.objectifs.ObjectifsScreen
import com.trucdecomptable.cuissonvapeur.ui.screens.reglages.ReglagesScreen
import com.trucdecomptable.cuissonvapeur.ui.screens.timer.TimerScreen

private data class BottomTab(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomTabs = listOf(
    BottomTab(Destinations.HOME, R.string.nav_accueil, Icons.Filled.Home),
    BottomTab(Destinations.CATALOGUE, R.string.nav_catalogue, Icons.Filled.List),
    BottomTab(Destinations.FAVORIS, R.string.nav_favoris, Icons.Filled.Favorite),
    BottomTab(Destinations.OBJECTIFS, R.string.nav_objectifs, Icons.Filled.TrackChanges),
    BottomTab(Destinations.CONSEILS, R.string.nav_conseils, Icons.Filled.Lightbulb),
)

/**
 * Root composable: bottom navigation bar for the 5 main sections (spec §5's
 * screen tree) + a Compose Navigation host. "Réglages" is reachable via a
 * top-bar action from Accueil rather than as a 6th bottom-bar item — see
 * root README, "Décisions non pinned" (Material's bottom bar guidance caps
 * around 5 destinations).
 */
@Composable
fun AppRoot() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = backStackEntry?.destination

            NavigationBar {
                bottomTabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.HOME,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
        ) {
            composable(Destinations.HOME) {
                HomeScreen(
                    onNavigateToCatalogue = { navController.navigate(Destinations.CATALOGUE) },
                    onNavigateToReglages = { navController.navigate(Destinations.REGLAGES) },
                    onNavigateToTimer = { navController.navigate(Destinations.TIMER) },
                )
            }
            composable(Destinations.CATALOGUE) {
                CatalogueScreen(
                    onOpenDetail = { vegetableId ->
                        navController.navigate(Destinations.vegetableDetail(vegetableId))
                    },
                    onOpenCart = {
                        // Same tab-switch semantics as the bottom bar: back returns
                        // to the catalog, state preserved.
                        navController.navigate(Destinations.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(
                route = Destinations.VEGETABLE_DETAIL,
                arguments = listOf(navArgument("vegetableId") { type = NavType.StringType }),
            ) { backStackEntry ->
                val vegetableId = backStackEntry.arguments?.getString("vegetableId").orEmpty()
                VegetableDetailScreen(vegetableId = vegetableId, onBack = { navController.popBackStack() })
            }
            composable(Destinations.FAVORIS) {
                FavorisScreen()
            }
            composable(Destinations.OBJECTIFS) {
                ObjectifsScreen()
            }
            composable(Destinations.CONSEILS) {
                ConseilsScreen()
            }
            composable(Destinations.REGLAGES) {
                ReglagesScreen(onBack = { navController.popBackStack() })
            }
            composable(Destinations.TIMER) {
                TimerScreen(onSessionEnded = { navController.popBackStack(Destinations.HOME, inclusive = false) })
            }
        }
    }
}
