package com.trucdecomptable.cuissonvapeur.domain.plan

/**
 * Result of [CookingPlanCalculator.compute] (EF-16).
 *
 * @property totalMinutes "T" — the global timer duration, i.e. `max(durée)`
 *   of the selection. Every vegetable finishes exactly at this instant.
 * @property steps the cooking order: sorted by (départ ascending, then
 *   durée descending) per EF-16's tie-break rule.
 */
data class CookingPlan(
    val totalMinutes: Int,
    val steps: List<CookingStep>,
)
