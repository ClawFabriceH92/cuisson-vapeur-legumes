package com.trucdecomptable.cuissonvapeur.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable

/**
 * EF-06/EF-11/EF-15: one catalog card — tap to select/deselect into the cart
 * (bordered + checkmark when selected, per EF-06's "comme le web"), a heart
 * to favorite (EF-11), the season + category badges (EF-04/EF-15), and long
 * press to open the detail sheet (EF-05, mirrored by the chevron affordance
 * described in spec §5 — implemented here as long-press since a full sheet
 * needs more chrome than fits on the card itself).
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun VegetableCard(
    vegetable: Vegetable,
    isInCart: Boolean,
    isFavorite: Boolean,
    onToggleCart: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (isInCart) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggleCart, onLongClick = onOpenDetail)
            .semantics {
                contentDescription = vegetable.name +
                    (if (isInCart) " — sélectionné" else "")
            },
        border = BorderStroke(if (isInCart) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = vegetable.emoji, style = MaterialTheme.typography.headlineMedium)

                Row {
                    if (isInCart) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    FavoriteToggle(isFavorite = isFavorite, onClick = onToggleFavorite)
                }
            }

            Text(text = vegetable.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.vegetable_duration_range, vegetable.displayedRange),
                style = MaterialTheme.typography.bodyMedium,
            )

            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                SeasonBadge(seasons = vegetable.seasons)
                vegetable.category?.let { CategoryBadge(category = it) }
            }
        }
    }
}

@Composable
private fun RowScope.FavoriteToggle(isFavorite: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite,
            ),
            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
