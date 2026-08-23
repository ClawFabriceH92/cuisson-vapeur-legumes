package com.trucdecomptable.cuissonvapeur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * EF-10: the pre-cooking selection ("panier de sélection"), persisted so it
 * survives app restarts and is restored on open (fixes the web version's
 * commented-out `localStorage` reload, see spec §1.1).
 *
 * [addedAtMillis] keeps the selection order stable across process restarts
 * (used only for display ordering — the cooking-plan algorithm itself sorts
 * by départ/durée, not selection order).
 */
@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val vegetableId: String,
    val addedAtMillis: Long,
)
