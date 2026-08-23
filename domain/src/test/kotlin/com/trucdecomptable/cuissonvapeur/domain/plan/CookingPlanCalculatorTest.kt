package com.trucdecomptable.cuissonvapeur.domain.plan

import com.trucdecomptable.cuissonvapeur.domain.catalog.VegetableCatalog
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory
import com.trucdecomptable.cuissonvapeur.domain.model.Season
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CookingPlanCalculatorTest {

    private fun catalogVeg(id: String) = VegetableCatalog.vegetables.first { it.id == id }

    private fun veg(id: String, duration: Int) = Vegetable(
        id = id,
        name = id,
        displayedRange = "$duration min",
        durationMinutes = duration,
        benefits = emptyList(),
        category = null,
        seasons = setOf(Season.ETE),
        kcalPer100g = 1,
        emoji = "🥕",
    )

    // --- Basic / edge cases -------------------------------------------------

    @Test
    fun `empty selection throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            CookingPlanCalculator.compute(emptyList())
        }
    }

    @Test
    fun `single vegetable starts immediately and total equals its duration`() {
        val plan = CookingPlanCalculator.compute(listOf(veg("a", 10)))

        assertEquals(10, plan.totalMinutes)
        assertEquals(1, plan.steps.size)
        assertEquals(0, plan.steps[0].startOffsetMinutes)
        assertTrue(plan.steps[0].isImmediate)
        assertEquals(10, plan.steps[0].readyOffsetMinutes)
    }

    @Test
    fun `multiple vegetables with distinct durations are offset correctly`() {
        // a=20, b=8, c=3 -> T=20; départs = 0, 12, 17
        val a = veg("a", 20)
        val b = veg("b", 8)
        val c = veg("c", 3)

        val plan = CookingPlanCalculator.compute(listOf(c, a, b))

        assertEquals(20, plan.totalMinutes)
        assertEquals(listOf("a", "b", "c"), plan.steps.map { it.vegetable.id })
        assertEquals(listOf(0, 12, 17), plan.steps.map { it.startOffsetMinutes })
        // Everyone finishes exactly at T.
        assertTrue(plan.steps.all { it.readyOffsetMinutes == plan.totalMinutes })
    }

    @Test
    fun `ties in duration keep original relative order and identical start offset`() {
        // Two vegetables with the same duration both start at 0 and are tied
        // on the (départ, durée) sort key -> stable sort preserves input order.
        val a = veg("a", 10)
        val b = veg("b", 10)

        val plan = CookingPlanCalculator.compute(listOf(a, b))

        assertEquals(10, plan.totalMinutes)
        assertEquals(listOf(0, 0), plan.steps.map { it.startOffsetMinutes })
        assertEquals(listOf("a", "b"), plan.steps.map { it.vegetable.id })
    }

    @Test
    fun `tie-break by départ then duration descending when offsets collide`() {
        // a: duration 10 -> départ = 20-10 = 10
        // b: duration 20 -> départ = 20-20 = 0
        // c: duration 10 -> départ = 20-10 = 10 (ties with a on départ, same duration)
        val a = veg("a", 10)
        val b = veg("b", 20)
        val c = veg("c", 10)

        val plan = CookingPlanCalculator.compute(listOf(a, b, c))

        assertEquals(20, plan.totalMinutes)
        // b first (départ 0), then a, c (départ 10, stable order preserved among ties).
        assertEquals(listOf("b", "a", "c"), plan.steps.map { it.vegetable.id })
        assertEquals(listOf(0, 10, 10), plan.steps.map { it.startOffsetMinutes })
    }

    // --- Scenarios from spec section 7 (T1, T2, T6-adjacent) ---------------

    @Test
    fun `T1 - Brocoli alone - modal shows Maintenant, timer 10 min`() {
        val brocoli = catalogVeg("brocoli") // duration 10
        val plan = CookingPlanCalculator.compute(listOf(brocoli))

        assertEquals(10, plan.totalMinutes)
        assertEquals(1, plan.steps.size)
        assertTrue(plan.steps[0].isImmediate)
    }

    @Test
    fun `T2 - Courgettes plus Epinards - courgettes now, epinards in 4 min, timer 7 min`() {
        val courgettes = catalogVeg("courgettes") // duration 7
        val epinards = catalogVeg("epinards") // duration 3

        val plan = CookingPlanCalculator.compute(listOf(courgettes, epinards))

        assertEquals(7, plan.totalMinutes)
        assertEquals(2, plan.steps.size)

        val courgettesStep = plan.steps.first { it.vegetable.id == "courgettes" }
        val epinardsStep = plan.steps.first { it.vegetable.id == "epinards" }

        assertTrue(courgettesStep.isImmediate)
        assertEquals(0, courgettesStep.startOffsetMinutes)
        assertEquals(4, epinardsStep.startOffsetMinutes)
        assertEquals(false, epinardsStep.isImmediate)

        // Order: courgettes (départ 0) before épinards (départ 4).
        assertEquals(listOf("courgettes", "epinards"), plan.steps.map { it.vegetable.id })

        // T3: at t=3min, épinards' départ (4) hasn't been reached yet.
        assertTrue(3 < epinardsStep.startOffsetMinutes)
        // At t=4min it must switch to "AJOUTER MAINTENANT".
        assertEquals(4, epinardsStep.startOffsetMinutes)

        // Both finish together at T = 7.
        assertEquals(7, courgettesStep.readyOffsetMinutes)
        assertEquals(7, epinardsStep.readyOffsetMinutes)
    }

    @Test
    fun `T6 - all 8 antioxydants vegetables selected together produces a valid plan`() {
        val antioxydants = VegetableCatalog.vegetables.filter {
            it.category == NutritionCategory.ANTIOXYDANTS
        }
        assertEquals(8, antioxydants.size)

        val plan = CookingPlanCalculator.compute(antioxydants)

        assertEquals(antioxydants.maxOf { it.durationMinutes }, plan.totalMinutes)
        assertEquals(8, plan.steps.size)
        // Every step must finish exactly at T.
        assertTrue(plan.steps.all { it.readyOffsetMinutes == plan.totalMinutes })
        // Steps are sorted by ascending départ.
        val offsets = plan.steps.map { it.startOffsetMinutes }
        assertEquals(offsets.sorted(), offsets)
    }
}
