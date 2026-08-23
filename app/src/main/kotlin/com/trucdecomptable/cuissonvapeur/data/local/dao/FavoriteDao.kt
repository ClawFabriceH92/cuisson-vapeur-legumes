package com.trucdecomptable.cuissonvapeur.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.trucdecomptable.cuissonvapeur.data.local.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteEntity)

    @Delete
    suspend fun remove(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE vegetableId = :vegetableId")
    suspend fun removeById(vegetableId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE vegetableId = :vegetableId)")
    suspend fun isFavorite(vegetableId: String): Boolean

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}
