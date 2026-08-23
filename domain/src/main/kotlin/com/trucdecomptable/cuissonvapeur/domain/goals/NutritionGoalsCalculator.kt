package com.trucdecomptable.cuissonvapeur.domain.goals

import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable

/**
 * EF-14 **Option A** (the option retained per the spec's D1 decision, §11):
 * for each of the 5 categories, `target` = the number of catalog vegetables
 * tagged with that category, and the goal is "reached" once *all* of them
 * are present in the current selection (EF-13, T6 in §7).
 *
 * This deliberately fixes the web version's bug (§1.1): there, targets were
 * hardcoded and higher than the number of taggable vegetables, making some
 * goals mathematically unreachable (e.g. "Fibres 0/10" with only 7 tagged).
 */
object NutritionGoalsCalculator {

    /** Display order used by the "Objectifs nutritionnels" screen (EF-13). */
    val categoryOrder: List<NutritionCategory> = listOf(
        NutritionCategory.ANTIOXYDANTS,
        NutritionCategory.FIBRES,
        NutritionCategory.VITAMINE_C,
        NutritionCategory.PROTEINES,
        NutritionCategory.HYDRATATION,
    )

    /**
     * @param catalog the full vegetable catalog (used to derive each `target`).
     * @param selection the vegetables currently in the cart (order irrelevant;
     *   duplicates by [Vegetable.id] are counted once).
     * @return one [NutritionGoal] per category, in [categoryOrder].
     */
    fun compute(catalog: List<Vegetable>, selection: List<Vegetable>): List<NutritionGoal> {
        val selectedIds = selection.map { it.id }.toSet()

        return categoryOrder.map { category ->
            val catalogOfCategory = catalog.filter { it.category == category }
            val target = catalogOfCategory.size
            val current = catalogOfCategory.count { it.id in selectedIds }
            NutritionGoal(category = category, current = current, target = target)
        }
    }
}
