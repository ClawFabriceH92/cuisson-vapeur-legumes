package com.trucdecomptable.cuissonvapeur.domain.goals

import com.trucdecomptable.cuissonvapeur.domain.catalog.VegetableCatalog
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NutritionGoalsCalculatorTest {

    private val catalog = VegetableCatalog.vegetables

    @Test
    fun `empty selection yields all targets unreached with correct target sizes`() {
        val goals = NutritionGoalsCalculator.compute(catalog, emptyList())

        assertEquals(5, goals.size)
        val byCategory = goals.associateBy { it.category }

        assertEquals(8, byCategory.getValue(NutritionCategory.ANTIOXYDANTS).target)
        assertEquals(7, byCategory.getValue(NutritionCategory.FIBRES).target)
        assertEquals(2, byCategory.getValue(NutritionCategory.VITAMINE_C).target)
        assertEquals(1, byCategory.getValue(NutritionCategory.PROTEINES).target)
        assertEquals(2, byCategory.getValue(NutritionCategory.HYDRATATION).target)

        assertTrue(goals.all { it.current == 0 })
        assertTrue(goals.none { it.isReached })
    }

    @Test
    fun `T6 - selecting all 8 antioxydants vegetables reaches that goal only`() {
        val antioxydants = catalog.filter { it.category == NutritionCategory.ANTIOXYDANTS }
        assertEquals(8, antioxydants.size)

        val goals = NutritionGoalsCalculator.compute(catalog, antioxydants)
        val byCategory = goals.associateBy { it.category }

        assertTrue(byCategory.getValue(NutritionCategory.ANTIOXYDANTS).isReached)
        assertEquals(8, byCategory.getValue(NutritionCategory.ANTIOXYDANTS).current)

        // No other goal should be reached (none of its vegetables were selected).
        assertFalse(byCategory.getValue(NutritionCategory.FIBRES).isReached)
        assertFalse(byCategory.getValue(NutritionCategory.VITAMINE_C).isReached)
        assertFalse(byCategory.getValue(NutritionCategory.PROTEINES).isReached)
        assertFalse(byCategory.getValue(NutritionCategory.HYDRATATION).isReached)
    }

    @Test
    fun `goal is not reached with all-but-one vegetable of the category`() {
        val fibres = catalog.filter { it.category == NutritionCategory.FIBRES }
        assertEquals(7, fibres.size)

        val almostAll = fibres.drop(1) // 6 of 7
        val goals = NutritionGoalsCalculator.compute(catalog, almostAll)
        val fibresGoal = goals.first { it.category == NutritionCategory.FIBRES }

        assertEquals(6, fibresGoal.current)
        assertEquals(7, fibresGoal.target)
        assertFalse(fibresGoal.isReached)
    }

    @Test
    fun `single-vegetable category (proteines) is reached with just petits pois`() {
        val petitsPois = catalog.first { it.id == "petits_pois" }

        val goals = NutritionGoalsCalculator.compute(catalog, listOf(petitsPois))
        val proteinesGoal = goals.first { it.category == NutritionCategory.PROTEINES }

        assertEquals(1, proteinesGoal.current)
        assertEquals(1, proteinesGoal.target)
        assertTrue(proteinesGoal.isReached)
    }

    @Test
    fun `selecting all 28 vegetables reaches every goal`() {
        val goals = NutritionGoalsCalculator.compute(catalog, catalog)
        assertTrue(goals.all { it.isReached })
    }

    @Test
    fun `duplicate vegetables in selection are counted once`() {
        val petitsPois = catalog.first { it.id == "petits_pois" }

        val goals = NutritionGoalsCalculator.compute(catalog, listOf(petitsPois, petitsPois))
        val proteinesGoal = goals.first { it.category == NutritionCategory.PROTEINES }

        assertEquals(1, proteinesGoal.current)
        assertTrue(proteinesGoal.isReached)
    }

    @Test
    fun `goals are returned in EF-13 display order`() {
        val goals = NutritionGoalsCalculator.compute(catalog, emptyList())
        assertEquals(
            listOf(
                NutritionCategory.ANTIOXYDANTS,
                NutritionCategory.FIBRES,
                NutritionCategory.VITAMINE_C,
                NutritionCategory.PROTEINES,
                NutritionCategory.HYDRATATION,
            ),
            goals.map { it.category },
        )
    }
}
