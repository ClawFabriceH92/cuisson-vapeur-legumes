package com.trucdecomptable.cuissonvapeur.domain.catalog

import com.trucdecomptable.cuissonvapeur.domain.model.ALL_YEAR
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.ANTIOXYDANTS
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.FIBRES
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.HYDRATATION
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.PROTEINES
import com.trucdecomptable.cuissonvapeur.domain.model.NutritionCategory.VITAMINE_C
import com.trucdecomptable.cuissonvapeur.domain.model.Season.AUTOMNE
import com.trucdecomptable.cuissonvapeur.domain.model.Season.ETE
import com.trucdecomptable.cuissonvapeur.domain.model.Season.HIVER
import com.trucdecomptable.cuissonvapeur.domain.model.Season.PRINTEMPS
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable

/**
 * The 28-vegetable catalog (EF-01), copied **exactly** from the spec's
 * Annexe A: same names, ranges, engine durations, benefits, categories,
 * seasons and kcal/100g as the source web page. Do not edit these values
 * without validation from Fabrice Heuvrard (see Annexe A, NB 3).
 *
 * Two things are *not* pinned down by the spec text and were judgment calls
 * for this port (documented in the root README):
 *  - [Vegetable.id]: a stable ascii slug, needed for Room/nav keys, absent
 *    from the spec (which only has display names).
 *  - [Vegetable.emoji]: standard Unicode has no dedicated emoji for several
 *    of these vegetables (turnip, parsnip, leek, artichoke, cauliflower...);
 *    a reasonable close/generic emoji was picked and duplicates happen.
 */
object VegetableCatalog {

    val vegetables: List<Vegetable> = listOf(
        Vegetable(
            id = "courgettes",
            name = "Courgettes",
            displayedRange = "5-7 min",
            durationMinutes = 7,
            benefits = listOf("Hydratation", "Faible en calories"),
            category = HYDRATATION,
            seasons = setOf(ETE),
            kcalPer100g = 17,
            emoji = "🥒", // 🥒
        ),
        Vegetable(
            id = "carottes",
            name = "Carottes",
            displayedRange = "15-20 min",
            durationMinutes = 20,
            benefits = listOf("Bêta-carotène", "Fibres"),
            category = FIBRES,
            seasons = ALL_YEAR,
            kcalPer100g = 41,
            emoji = "🥕", // 🥕
        ),
        Vegetable(
            id = "haricots_verts",
            name = "Haricots verts",
            displayedRange = "6-8 min",
            durationMinutes = 8,
            benefits = listOf("Fibres", "Vitamine A"),
            category = FIBRES,
            seasons = setOf(ETE),
            kcalPer100g = 31,
            emoji = "🫘", // 🫘
        ),
        Vegetable(
            id = "petits_pois",
            name = "Petits pois",
            displayedRange = "3-5 min",
            durationMinutes = 5,
            benefits = listOf("Protéines végétales", "Fibres"),
            category = PROTEINES,
            seasons = setOf(PRINTEMPS, ETE),
            kcalPer100g = 84,
            emoji = "🫛", // 🫛
        ),
        Vegetable(
            id = "pommes_de_terre",
            name = "Pommes de terre",
            displayedRange = "15-20 min",
            durationMinutes = 20,
            benefits = listOf("Potassium", "Glucides"),
            category = null,
            seasons = ALL_YEAR,
            kcalPer100g = 77,
            emoji = "🥔", // 🥔
        ),
        Vegetable(
            id = "brocoli",
            name = "Brocoli",
            displayedRange = "7-10 min",
            durationMinutes = 10,
            benefits = listOf("Vitamine C & K", "Antioxydants"),
            category = ANTIOXYDANTS,
            seasons = setOf(PRINTEMPS, ETE),
            kcalPer100g = 34,
            emoji = "🥦", // 🥦
        ),
        Vegetable(
            id = "chou_fleur",
            name = "Chou-fleur",
            displayedRange = "8-10 min",
            durationMinutes = 10,
            benefits = listOf("Antioxydants", "Vitamine C"),
            category = ANTIOXYDANTS,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 25,
            emoji = "🥦", // 🥦 (no dedicated cauliflower emoji, see class doc)
        ),
        Vegetable(
            id = "asperges",
            name = "Asperges",
            displayedRange = "4-6 min",
            durationMinutes = 6,
            benefits = listOf("Folates", "Diurétique"),
            category = null,
            seasons = setOf(PRINTEMPS),
            kcalPer100g = 20,
            emoji = "🌱", // 🌱
        ),
        Vegetable(
            id = "choux_de_bruxelles",
            name = "Choux de Bruxelles",
            displayedRange = "10-12 min",
            durationMinutes = 12,
            benefits = listOf("Vitamine C & K"),
            category = VITAMINE_C,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 43,
            emoji = "🍃", // 🍃
        ),
        Vegetable(
            id = "epinards",
            name = "Épinards",
            displayedRange = "2-3 min",
            durationMinutes = 3,
            benefits = listOf("Fer", "Calcium"),
            category = null,
            seasons = setOf(PRINTEMPS, AUTOMNE),
            kcalPer100g = 23,
            emoji = "🥬", // 🥬
        ),
        Vegetable(
            id = "poivrons",
            name = "Poivrons",
            displayedRange = "6-8 min",
            durationMinutes = 8,
            benefits = listOf("Vitamine C", "Antioxydants"),
            category = VITAMINE_C,
            seasons = setOf(ETE),
            kcalPer100g = 31,
            emoji = "🫑", // 🫑
        ),
        Vegetable(
            id = "fenouil",
            name = "Fenouil",
            displayedRange = "10-12 min",
            durationMinutes = 12,
            benefits = listOf("Digestion", "Fibres"),
            category = null,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 31,
            emoji = "🌿", // 🌿
        ),
        Vegetable(
            id = "navets",
            name = "Navets",
            displayedRange = "15-18 min",
            durationMinutes = 18,
            benefits = listOf("Fibres", "Vitamine C"),
            category = FIBRES,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 28,
            emoji = "⚪", // ⚪
        ),
        Vegetable(
            id = "panais",
            name = "Panais",
            displayedRange = "15-18 min",
            durationMinutes = 18,
            benefits = listOf("Vitamine C", "Potassium"),
            category = null,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 75,
            emoji = "🟠", // 🟠
        ),
        Vegetable(
            id = "patate_douce",
            name = "Patate douce",
            displayedRange = "10-15 min",
            durationMinutes = 15,
            benefits = listOf("Bêta-carotène", "Fibres"),
            category = null,
            seasons = setOf(AUTOMNE),
            kcalPer100g = 86,
            emoji = "🍠", // 🍠
        ),
        Vegetable(
            id = "betteraves",
            name = "Betteraves",
            displayedRange = "20-25 min",
            durationMinutes = 25,
            benefits = listOf("Nitrates", "Antioxydants"),
            category = ANTIOXYDANTS,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 43,
            emoji = "🔴", // 🔴
        ),
        Vegetable(
            id = "chou_kale",
            name = "Chou kale",
            displayedRange = "3-5 min",
            durationMinutes = 5,
            benefits = listOf("Vitamine A, C, K"),
            category = ANTIOXYDANTS,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 49,
            emoji = "🥬", // 🥬
        ),
        Vegetable(
            id = "radis",
            name = "Radis",
            displayedRange = "2-3 min",
            durationMinutes = 3,
            benefits = listOf("Antioxydants", "Détox"),
            category = ANTIOXYDANTS,
            seasons = setOf(PRINTEMPS, ETE),
            kcalPer100g = 16,
            emoji = "🌱", // 🌱
        ),
        Vegetable(
            id = "aubergines",
            name = "Aubergines",
            displayedRange = "8-12 min",
            durationMinutes = 12,
            benefits = listOf("Fibres", "Antioxydants"),
            category = ANTIOXYDANTS,
            seasons = setOf(ETE),
            kcalPer100g = 25,
            emoji = "🍆", // 🍆
        ),
        Vegetable(
            id = "tomates",
            name = "Tomates",
            displayedRange = "3-5 min",
            durationMinutes = 5,
            benefits = listOf("Lycopène", "Vitamine C"),
            category = ANTIOXYDANTS,
            seasons = setOf(ETE),
            kcalPer100g = 18,
            emoji = "🍅", // 🍅
        ),
        Vegetable(
            id = "oignons",
            name = "Oignons",
            displayedRange = "8-10 min",
            durationMinutes = 10,
            benefits = listOf("Quercétine", "Soufre"),
            category = null,
            seasons = ALL_YEAR,
            kcalPer100g = 40,
            emoji = "🧅", // 🧅
        ),
        Vegetable(
            id = "ail",
            name = "Ail",
            displayedRange = "5-7 min",
            durationMinutes = 7,
            benefits = listOf("Allicine", "Antioxydants"),
            category = ANTIOXYDANTS,
            seasons = ALL_YEAR,
            kcalPer100g = 149,
            emoji = "🧄", // 🧄
        ),
        Vegetable(
            id = "poireaux",
            name = "Poireaux",
            displayedRange = "8-12 min",
            durationMinutes = 12,
            benefits = listOf("Vitamine K", "Fibres"),
            category = FIBRES,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 61,
            emoji = "🟢", // 🟢
        ),
        Vegetable(
            id = "celeri",
            name = "Céleri",
            displayedRange = "6-8 min",
            durationMinutes = 8,
            benefits = listOf("Vitamine K", "Hydratation"),
            category = HYDRATATION,
            seasons = ALL_YEAR,
            kcalPer100g = 16,
            emoji = "🥬", // 🥬
        ),
        Vegetable(
            id = "endives",
            name = "Endives",
            displayedRange = "4-6 min",
            durationMinutes = 6,
            benefits = listOf("Vitamine B9", "Fibres"),
            category = FIBRES,
            seasons = setOf(AUTOMNE, HIVER),
            kcalPer100g = 17,
            emoji = "🍃", // 🍃
        ),
        Vegetable(
            id = "artichauts",
            name = "Artichauts",
            displayedRange = "15-20 min",
            durationMinutes = 20,
            benefits = listOf("Cynarine", "Fibres"),
            category = FIBRES,
            seasons = setOf(PRINTEMPS, ETE),
            kcalPer100g = 47,
            emoji = "🌿", // 🌿
        ),
        Vegetable(
            id = "champignons",
            name = "Champignons",
            displayedRange = "4-6 min",
            durationMinutes = 6,
            benefits = listOf("Vitamine D", "Sélénium"),
            category = null,
            seasons = ALL_YEAR,
            kcalPer100g = 22,
            emoji = "🍄", // 🍄
        ),
        Vegetable(
            id = "mais",
            name = "Maïs",
            displayedRange = "8-10 min",
            durationMinutes = 10,
            benefits = listOf("Lutéine", "Fibres"),
            category = FIBRES,
            seasons = setOf(ETE),
            kcalPer100g = 86,
            emoji = "🌽", // 🌽
        ),
    )
}
