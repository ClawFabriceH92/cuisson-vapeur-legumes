package com.trucdecomptable.cuissonvapeur.domain.goals

import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory

/**
 * Live state of one of the 5 nutrition goals (EF-13/EF-14 Option A).
 *
 * @property category which of the 5 categories this is.
 * @property current how many vegetables of [category] are in the current selection.
 * @property target total number of vegetables in the whole catalog tagged
 *   [category] (8/7/2/1/2 for antioxydants/fibres/vitamineC/proteines/hydratation).
 */
data class NutritionGoal(
    val category: NutritionCategory,
    val current: Int,
    val target: Int,
) {
    /** EF-13: reached once *all* catalog vegetables of this category are selected. */
    val isReached: Boolean get() = target > 0 && current >= target
}
