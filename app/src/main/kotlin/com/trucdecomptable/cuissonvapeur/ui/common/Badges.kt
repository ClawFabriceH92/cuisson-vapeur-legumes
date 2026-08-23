package com.trucdecomptable.cuissonvapeur.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
        BadgeContent(label)
    }
}

@Composable
fun seasonLabel(seasons: Set<Season>): String {
    if (seasons == ALL_YEAR) return stringResource(R.string.season_all_year)
    // Built with an explicit for-loop, not joinToString { }: the transform
    // lambda's declared type isn't @Composable, so calling seasonName (a
    // @Composable function) from inside it is rejected by the Compose
    // compiler even though joinToString itself is inline.
    val sortedSeasons = seasons.sortedBy { it.ordinal }
    val builder = StringBuilder()
    for ((index, season) in sortedSeasons.withIndex()) {
        if (index > 0) builder.append("/")
        builder.append(seasonName(season))
    }
    return builder.toString()
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
        BadgeContent(categoryLabel(category))
    }
}

/**
 * Shared badge body: fixed 28dp height, single line, centered — so every
 * "bulle" (season, category, …) has exactly the same size regardless of its
 * label length (Fabrice's feedback, v1.9). Width is controlled by the caller
 * (natural width or weight(1f) in a Row for equal split).
 */
@Composable
private fun BadgeContent(label: String) {
    Box(
        modifier = Modifier.height(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
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
