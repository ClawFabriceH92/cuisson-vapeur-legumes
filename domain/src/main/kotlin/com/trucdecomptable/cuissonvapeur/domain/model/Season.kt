package com.trucdecomptable.cuissonvapeur.domain.model

/**
 * A single season badge. A vegetable can be tagged with 1..4 of these
 * (see [Vegetable.seasons]); when all 4 are present it displays as
 * "Toute l'année" per the spec's Annexe A / EF-04 season badges.
 */
enum class Season {
    PRINTEMPS,
    ETE,
    AUTOMNE,
    HIVER,
}

/** All four seasons, i.e. "Toute l'année" in the catalog table. */
val ALL_YEAR: Set<Season> = setOf(Season.PRINTEMPS, Season.ETE, Season.AUTOMNE, Season.HIVER)
