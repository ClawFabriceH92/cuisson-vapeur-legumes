package com.trucdecomptable.cuissonvapeur.domain.model

/**
 * A single catalog entry (EF-01). Faithful port of one row of the web
 * engine's vegetable table (see docs/cahier-des-charges.md, Annexe A).
 *
 * @property id stable, lowercase-ascii identifier used as a DB/nav key
 *   (not part of the spec's table, added for persistence — e.g. "courgettes").
 * @property name display name, e.g. "Courgettes".
 * @property displayedRange the "plage" shown to the user, e.g. "5-7 min".
 * @property durationMinutes the "durée moteur" — high bound of the range,
 *   the only value the cooking-plan algorithm (EF-16) uses.
 * @property benefits the "bienfaits" list, e.g. ["Hydratation", "Faible en calories"].
 * @property category the nutrition-goal category, or null when the catalog
 *   row has no category ("—" in Annexe A).
 * @property seasons 1..4 seasons; all 4 renders as "Toute l'année".
 * @property kcalPer100g calories per 100g.
 * @property emoji a single emoji representing the vegetable (UI only, not
 *   specified per-vegetable in the spec text — chosen for this port, see README).
 */
data class Vegetable(
    val id: String,
    val name: String,
    val displayedRange: String,
    val durationMinutes: Int,
    val benefits: List<String>,
    val category: NutritionCategory?,
    val seasons: Set<Season>,
    val kcalPer100g: Int,
    val emoji: String,
)
