package com.trucdecomptable.cuissonvapeur.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trucdecomptable.cuissonvapeur.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

/** EF-06/EF-07/EF-10: the persisted pre-cooking selection ("panier de sélection"). */
@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY addedAtMillis ASC")
    fun observeAll(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE vegetableId = :vegetableId")
    suspend fun removeById(vegetableId: String)

    /** EF-10: "un moyen de vider le panier en un geste". */
    @Query("DELETE FROM cart_items")
    suspend fun clearAll()
}
