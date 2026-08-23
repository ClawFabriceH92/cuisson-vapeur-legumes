package com.trucdecomptable.cuissonvapeur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** EF-11: persisted favorite state, one row per favorited vegetable id. */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val vegetableId: String,
)
