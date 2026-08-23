package com.trucdecomptable.cuissonvapeur.domain.catalog

import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Sanity checks that the catalog matches Annexe A / EF-01 / D1's expected counts. */
class VegetableCatalogTest {

    @Test
    fun `has exactly 28 vegetables`() {
        assertEquals(28, VegetableCatalog.vegetables.size)
    }

    @Test
    fun `all ids are unique`() {
        val ids = VegetableCatalog.vegetables.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `category counts match EF-14 Option A targets (8-7-2-1-2)`() {
        val byCategory = VegetableCatalog.vegetables.groupingBy { it.category }.eachCount()

        assertEquals(8, byCategory[NutritionCategory.ANTIOXYDANTS])
        assertEquals(7, byCategory[NutritionCategory.FIBRES])
        assertEquals(2, byCategory[NutritionCategory.VITAMINE_C])
        assertEquals(1, byCategory[NutritionCategory.PROTEINES])
        assertEquals(2, byCategory[NutritionCategory.HYDRATATION])
        // The remaining 8 vegetables have no category ("—" in Annexe A):
        // 28 - (8+7+2+1+2) = 8.
        assertEquals(8, byCategory[null])
    }

    @Test
    fun `all durations and kcal values are positive`() {
        assertTrue(VegetableCatalog.vegetables.all { it.durationMinutes > 0 })
        assertTrue(VegetableCatalog.vegetables.all { it.kcalPer100g > 0 })
    }

    @Test
    fun `all vegetables have at least one season`() {
        assertTrue(VegetableCatalog.vegetables.all { it.seasons.isNotEmpty() })
    }

    @Test
    fun `spot check known rows from the spec's short table (section 6)`() {
        val courgettes = VegetableCatalog.vegetables.first { it.id == "courgettes" }
        assertEquals("5-7 min", courgettes.displayedRange)
        assertEquals(7, courgettes.durationMinutes)
        assertEquals(NutritionCategory.HYDRATATION, courgettes.category)
        assertEquals(17, courgettes.kcalPer100g)

        val carottes = VegetableCatalog.vegetables.first { it.id == "carottes" }
        assertEquals(20, carottes.durationMinutes)
        assertEquals(NutritionCategory.FIBRES, carottes.category)
        assertEquals(41, carottes.kcalPer100g)

        val brocoli = VegetableCatalog.vegetables.first { it.id == "brocoli" }
        assertEquals(10, brocoli.durationMinutes)
        assertEquals(NutritionCategory.ANTIOXYDANTS, brocoli.category)
        assertEquals(34, brocoli.kcalPer100g)

        val betteraves = VegetableCatalog.vegetables.first { it.id == "betteraves" }
        assertEquals(25, betteraves.durationMinutes)
        assertEquals(NutritionCategory.ANTIOXYDANTS, betteraves.category)
        assertEquals(43, betteraves.kcalPer100g)

        val epinards = VegetableCatalog.vegetables.first { it.id == "epinards" }
        assertEquals(3, epinards.durationMinutes)
        assertEquals(null, epinards.category)
        assertEquals(23, epinards.kcalPer100g)
    }
}
