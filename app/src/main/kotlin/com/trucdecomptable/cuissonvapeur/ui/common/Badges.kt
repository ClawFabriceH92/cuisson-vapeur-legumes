package com.trucdecomptable.cuissonvapeur.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.domain.model.ALL_YEAR
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory
import com.trucdecomptable.cuissonvapeur.domain.model.Season

/** EF-04's season badges: 4 seasons, "Toute l'année" when all 4 apply, or "X/Y" combos. */
@Composable
fun SeasonBadge(seasons: Set<Season>, modifier: Modifier = Modifier) {
    val label = seasonLabel(seasons)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun seasonLabel(seasons: Set<Season>): String = when {
    seasons == ALL_YEAR -> stringResource(R.string.season_all_year)
    else -> seasons.sortedBy { it.ordinal }.joinToString("/") { seasonName(it) }
}

@Composable
private fun seasonName(season: Season): String = when (season) {
    Season.PRINTEMPS -> stringResource(R.string.season_printemps)
    Season.ETE -> stringResource(R.string.season_ete)
    Season.AUTOMNE -> stringResource(R.string.season_automne)
    Season.HIVER -> stringResource(R.string.season_hiver)
}

/** EF-15: the nutrition-category badge shown on each vegetable card, when it has one. */
@Composable
fun CategoryBadge(category: NutritionCategory, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.tertiaryContainer,
    ) {
        Text(
            text = categoryLabel(category),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun categoryLabel(category: NutritionCategory): String = when (category) {
    NutritionCategory.ANTIOXYDANTS -> stringResource(R.string.category_antioxydants)
    NutritionCategory.FIBRES -> stringResource(R.string.category_fibres)
    NutritionCategory.VITAMINE_C -> stringResource(R.string.category_vitamine_c)
    NutritionCategory.PROTEINES -> stringResource(R.string.category_proteines)
    NutritionCategory.HYDRATATION -> stringResource(R.string.category_hydratation)
}
