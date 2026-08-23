package com.trucdecomptable.cuissonvapeur.domain.plan

import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable

/**
 * One entry of a computed [CookingPlan] (EF-16/EF-17).
 *
 * @property vegetable the vegetable to cook.
 * @property startOffsetMinutes "départ(i)" — minutes after the global timer
 *   starts at which this vegetable must be added ("Maintenant" when 0).
 */
data class CookingStep(
    val vegetable: Vegetable,
    val startOffsetMinutes: Int,
) {
    /** Minute at which this vegetable is done cooking (start + its own duration). */
    val readyOffsetMinutes: Int get() = startOffsetMinutes + vegetable.durationMinutes

    /** EF-17: "Maintenant" vs "Dans N min — Ajouter au panier". */
    val isImmediate: Boolean get() = startOffsetMinutes == 0
}
