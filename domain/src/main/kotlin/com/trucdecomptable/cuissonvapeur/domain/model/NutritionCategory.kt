package com.trucdecomptable.cuissonvapeur.domain.model

/**
 * The 5 nutrition-goal categories from the spec (§3.4, EF-13/EF-14).
 * Internal keys match the web engine's: antioxydants, fibres, vitamineC,
 * proteines, hydratation. Not every vegetable has one (see [Vegetable.category] == null).
 */
enum class NutritionCategory {
    ANTIOXYDANTS,
    FIBRES,
    VITAMINE_C,
    PROTEINES,
    HYDRATATION,
}
