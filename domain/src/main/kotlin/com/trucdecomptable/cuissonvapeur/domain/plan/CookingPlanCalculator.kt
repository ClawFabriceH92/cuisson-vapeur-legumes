package com.trucdecomptable.cuissonvapeur.domain.plan

import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable

/**
 * EF-16 — the "optimal cooking order" algorithm, ported formally from the
 * spec:
 * ```
 * Entrée : sélection S = {v1..vn}, chaque vi a une durée dᵢ (min, borne haute)
 * T        = max(dᵢ)
 * départ(i) = T − dᵢ
 * Résultat : étapes triées par (départ ascendant, puis dᵢ décroissant)
 * Timer    = T minutes
 * ```
 * Every vegetable is added at its own `départ(i)` and finishes cooking at
 * `départ(i) + dᵢ`, which by construction always equals `T` — hence "tous
 * finissent en même temps" (EF-17).
 */
object CookingPlanCalculator {

    /**
     * @param selection the chosen vegetables. Must be non-empty.
     * @throws IllegalArgumentException if [selection] is empty — there is no
     *   sensible plan (and no `T`) for an empty cart; the UI must not call
     *   this while "Démarrer" is disabled (EF-09).
     */
    fun compute(selection: List<Vegetable>): CookingPlan {
        require(selection.isNotEmpty()) {
            "Cannot compute a cooking plan for an empty selection (EF-09: " +
                "'Démarrer' must be disabled while the cart is empty)."
        }

        val totalMinutes = selection.maxOf { it.durationMinutes }

        val steps = selection
            .map { vegetable ->
                CookingStep(
                    vegetable = vegetable,
                    startOffsetMinutes = totalMinutes - vegetable.durationMinutes,
                )
            }
            // Stable sort: (départ ascending, then durée descending); ties
            // beyond that keep the original selection order.
            .sortedWith(
                compareBy<CookingStep> { it.startOffsetMinutes }
                    .thenByDescending { it.vegetable.durationMinutes },
            )

        return CookingPlan(totalMinutes = totalMinutes, steps = steps)
    }
}
