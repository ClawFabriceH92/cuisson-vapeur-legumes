package com.trucdecomptable.cuissonvapeur.data.repository

import com.trucdecomptable.cuissonvapeur.data.local.dao.CartDao
import com.trucdecomptable.cuissonvapeur.data.local.dao.FavoriteDao
import com.trucdecomptable.cuissonvapeur.data.local.entity.CartItemEntity
import com.trucdecomptable.cuissonvapeur.data.local.entity.FavoriteEntity
import com.trucdecomptable.cuissonvapeur.domain.catalog.VegetableCatalog
import com.trucdecomptable.cuissonvapeur.domain.model.Vegetable
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Combines the static `:domain` catalog (EF-01, embedded in the app rather
 * than loaded from a JSON asset — see root README, "Décisions non pinned")
 * with the Room-persisted, per-device state: favorites (EF-11) and the
 * pre-cooking cart selection (EF-06/EF-10).
 */
@Singleton
class VegetableRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val cartDao: CartDao,
) {

    /** EF-01: the full 28-vegetable catalog, static for the app's lifetime. */
    val catalog: List<Vegetable> = VegetableCatalog.vegetables

    fun findById(id: String): Vegetable? = catalog.firstOrNull { it.id == id }

    // --- Favorites (EF-11/EF-12) -------------------------------------------

    fun observeFavoriteIds(): Flow<Set<String>> =
        favoriteDao.observeAll().map { rows -> rows.map { it.vegetableId }.toSet() }

    fun observeFavorites(): Flow<List<Vegetable>> =
        observeFavoriteIds().map { ids -> catalog.filter { it.id in ids } }

    suspend fun toggleFavorite(vegetableId: String) {
        if (favoriteDao.isFavorite(vegetableId)) {
            favoriteDao.removeById(vegetableId)
        } else {
            favoriteDao.add(FavoriteEntity(vegetableId))
        }
    }

    // --- Cart / selection (EF-06/EF-07/EF-08/EF-09/EF-10) -------------------

    fun observeCart(): Flow<List<Vegetable>> =
        cartDao.observeAll().map { rows -> rows.mapNotNull { findById(it.vegetableId) } }

    suspend fun addToCart(vegetableId: String) {
        cartDao.add(CartItemEntity(vegetableId, addedAtMillis = System.currentTimeMillis()))
    }

    suspend fun removeFromCart(vegetableId: String) {
        cartDao.removeById(vegetableId)
    }

    suspend fun toggleCart(vegetableId: String, currentlyInCart: Boolean) {
        if (currentlyInCart) removeFromCart(vegetableId) else addToCart(vegetableId)
    }

    /** EF-10: "un moyen de vider le panier en un geste". */
    suspend fun clearCart() {
        cartDao.clearAll()
    }

    /** EF-29: part of "réinitialisation des données locales". */
    suspend fun clearFavorites() {
        favoriteDao.clearAll()
    }
}
