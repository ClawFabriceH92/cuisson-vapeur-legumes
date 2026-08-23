package com.trucdecomptable.cuissonvapeur.ui.navigation

/** Route names for the Compose Navigation graph (spec §5's UX tree). */
object Destinations {
    const val HOME = "home"
    const val CATALOGUE = "catalogue"
    const val VEGETABLE_DETAIL = "vegetable_detail/{vegetableId}"
    const val FAVORIS = "favoris"
    const val OBJECTIFS = "objectifs"
    const val CONSEILS = "conseils"
    const val REGLAGES = "reglages"
    const val TIMER = "timer"

    fun vegetableDetail(vegetableId: String) = "vegetable_detail/$vegetableId"
}
