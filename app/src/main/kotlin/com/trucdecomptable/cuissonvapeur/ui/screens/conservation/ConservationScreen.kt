package com.trucdecomptable.cuissonvapeur.ui.screens.conservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.domain.conservation.ConservationCatalog
import com.trucdecomptable.cuissonvapeur.domain.conservation.FamilleDate
import com.trucdecomptable.cuissonvapeur.domain.conservation.ProduitConservation
import com.trucdecomptable.cuissonvapeur.ui.common.SettingsAction
import com.trucdecomptable.cuissonvapeur.ui.theme.Amber40
import com.trucdecomptable.cuissonvapeur.ui.theme.GoalPendingRed
import com.trucdecomptable.cuissonvapeur.ui.theme.SteamGreen40

/**
 * « Jusqu'à quand je peux manger ça ? »
 *
 * L'écran est coupé en deux blocs volontairement très différents, parce que
 * confondre les deux fait soit jeter de la nourriture encore bonne, soit
 * manger un produit dangereux :
 *
 * 1. **À consommer jusqu'au (DLC)** — date impérative, dépassement = risque
 *    pour la santé (viandes, poissons, œufs, lait frais, traiteur…) ;
 * 2. **À consommer de préférence avant (DDM) et denrées sans date** —
 *    dépassement = perte de qualités, aucun danger (conserves, secs, UHT…).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConservationScreen(onNavigateToReglages: () -> Unit) {
    var recherche by remember { mutableStateOf("") }
    val filtre = remember(recherche) { ConservationCatalog.chercher(recherche) }
    val dlc = remember(filtre) { filtre.filter { it.famille == FamilleDate.DLC } }
    val autres = remember(filtre) {
        filtre.filter { it.famille == FamilleDate.DDM || it.famille == FamilleDate.AUCUNE }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conservation") },
                actions = { SettingsAction(onClick = onNavigateToReglages) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { RappelDeuxDates() }

            item {
                OutlinedTextField(
                    value = recherche,
                    onValueChange = { recherche = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Chercher un produit (œuf, viande, conserve…)") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (recherche.isNotBlank()) {
                            IconButton(onClick = { recherche = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Effacer la recherche")
                            }
                        }
                    },
                )
            }

            if (dlc.isNotEmpty()) {
                item {
                    EnTeteFamille(
                        numero = 1,
                        titre = "À consommer jusqu'au · DLC",
                        sousTitre = FamilleDate.DLC.consequence,
                        couleur = GoalPendingRed,
                        nombre = dlc.size,
                    )
                }
                items(dlc, key = { it.nom }) { produit -> FicheProduit(produit, GoalPendingRed) }
            }

            if (autres.isNotEmpty()) {
                item {
                    EnTeteFamille(
                        numero = if (dlc.isNotEmpty()) 2 else 1,
                        titre = "À consommer de préférence avant · DDM et sans date",
                        sousTitre = "Dépasser cette date ne met pas en danger : le produit perd du goût, " +
                            "de la texture ou des vitamines. On goûte, on regarde, on sent avant de jeter.",
                        couleur = Amber40,
                        nombre = autres.size,
                    )
                }
                items(autres, key = { it.nom }) { produit ->
                    FicheProduit(produit, if (produit.famille == FamilleDate.DDM) Amber40 else SteamGreen40)
                }
            }

            if (dlc.isEmpty() && autres.isEmpty()) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Text(
                            "Aucun produit ne correspond à « $recherche ».",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            item { RappelsEtSources() }
        }
    }
}

/** Le mode d'emploi, en haut : deux dates, deux significations. */
@Composable
private fun RappelDeuxDates() {
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Deux dates à ne pas confondre", style = MaterialTheme.typography.titleMedium)
            LigneFamille(GoalPendingRed, "À consommer jusqu'au (DLC)", FamilleDate.DLC.consequence)
            LigneFamille(Amber40, "À consommer de préférence avant (DDM)", FamilleDate.DDM.consequence)
            LigneFamille(SteamGreen40, "Sans date obligatoire", FamilleDate.AUCUNE.consequence)
        }
    }
}

@Composable
private fun LigneFamille(couleur: Color, titre: String, texte: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Surface(
            color = couleur,
            shape = RoundedCornerShape(3.dp),
            modifier = Modifier.padding(top = 4.dp).fillMaxWidth(0.012f),
        ) {}
        Column {
            Text(titre, style = MaterialTheme.typography.titleSmall, color = couleur, fontWeight = FontWeight.SemiBold)
            Text(texte, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Le bandeau qui ouvre chaque famille : la couleur suffit à savoir de quoi il s'agit. */
@Composable
private fun EnTeteFamille(
    numero: Int,
    titre: String,
    sousTitre: String,
    couleur: Color,
    nombre: Int,
) {
    Card(Modifier.fillMaxWidth().padding(top = 6.dp), colors = CardDefaults.cardColors(containerColor = couleur)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "$numero · $titre",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Text(sousTitre, style = MaterialTheme.typography.bodySmall, color = Color.White)
            Text(
                "$nombre produit(s)",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

/** Une fiche : la date qui compte, puis ce qui la fait tenir ou la casse. */
@Composable
private fun FicheProduit(produit: ProduitConservation, couleur: Color) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    produit.nom,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Surface(color = couleur, shape = RoundedCornerShape(6.dp)) {
                    Text(
                        produit.famille.libelle,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Text(produit.rayon.libelle, style = MaterialTheme.typography.labelSmall)
            if (produit.repereReglementaire.isNotBlank()) {
                Detail("Jusqu'à quand", produit.repereReglementaire)
            }
            if (produit.conservation.isNotBlank()) {
                Detail("À conserver", produit.conservation)
            }
            if (produit.apresOuverture.isNotBlank()) {
                Detail("Une fois ouvert", produit.apresOuverture)
            }
            if (produit.congelation.isNotBlank()) {
                Detail("Congélation", produit.congelation)
            }
            if (produit.vigilance.isNotBlank()) {
                Detail("À jeter si", produit.vigilance, couleur)
            }
        }
    }
}

@Composable
private fun Detail(intitule: String, texte: String, couleur: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "$intitule :",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = couleur,
        )
        Text(texte, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
    }
}

/** Les rappels valables partout, puis les sources — pour ne pas avoir à me croire sur parole. */
@Composable
private fun RappelsEtSources() {
    Card(
        Modifier.fillMaxWidth().padding(top = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Rappels", style = MaterialTheme.typography.titleSmall)
            ConservationCatalog.rappels.forEach { rappel ->
                Text("· $rappel", style = MaterialTheme.typography.bodySmall)
            }
            Text("Sources", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
            ConservationCatalog.sources.forEach { source ->
                Text("· $source", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
