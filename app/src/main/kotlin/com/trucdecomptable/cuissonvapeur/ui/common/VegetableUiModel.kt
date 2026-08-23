package com.trucdecomptable.cuissonvapeur.ui.common

import com.trucdecomptable.cuissonvapeur.domain.model.Season
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable

/** A catalog vegetable annotated with this device's per-user state, for card rendering. */
data class VegetableUiModel(
    val vegetable: Vegetable,
    val isInCart: Boolean,
    val isFavorite: Boolean,
)

/** EF-04's season filter choices: the 4 seasons, plus "Toute l'année" as a distinct pill. */
enum class SeasonFilter(val season: Season?) {
    PRINTEMPS(Season.PRINTEMPS),
    ETE(Season.ETE),
    AUTOMNE(Season.AUTOMNE),
    HIVER(Season.HIVER),
    ;
}

/** EF-03: the 2 sort modes, with a visibly active state in the UI. */
enum class SortMode { TEMPS_CROISSANT, NOM_ALPHABETIQUE }
