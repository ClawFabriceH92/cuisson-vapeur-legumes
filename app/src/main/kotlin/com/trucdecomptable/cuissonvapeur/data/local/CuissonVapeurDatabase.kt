package com.trucdecomptable.cuissonvapeur.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.trucdecomptable.cuissonvapeur.data.local.dao.CartDao
import com.trucdecomptable.cuissonvapeur.data.local.dao.CookingSessionDao
import com.trucdecomptable.cuissonvapeur.data.local.dao.FavoriteDao
import com.trucdecomptable.cuissonvapeur.data.local.dao.SettingsDao
import com.trucdecomptable.cuissonvapeur.data.local.entity.CartItemEntity
import com.trucdecomptable.cuissonvapeur.data.local.entity.CookingSessionEntity
import com.trucdecomptable.cuissonvapeur.data.local.entity.FavoriteEntity
import com.trucdecomptable.cuissonvapeur.data.local.entity.SettingsEntity

/**
 * The app's only Room database. Holds only *local, per-device* state
 * (favorites, cart selection, settings, active cooking session) — the
 * vegetable catalog itself lives in the pure-Kotlin `:domain` module and is
 * never written to this database (see root README, "Décisions non pinned").
 */
@Database(
    entities = [
        FavoriteEntity::class,
        CartItemEntity::class,
        SettingsEntity::class,
        CookingSessionEntity::class,
    ],
    version = 1,
    // No migration test infra in this v1 (no prior schema to migrate from);
    // exportSchema left false to avoid requiring a ksp room.schemaLocation
    // arg. Turn this on once a v2 schema migration is actually needed.
    exportSchema = false,
)
// Room 2.6+ converts enum columns (ThemeMode, AlarmSound) to TEXT
// automatically — no explicit @TypeConverters needed for this schema.
abstract class CuissonVapeurDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cartDao(): CartDao
    abstract fun settingsDao(): SettingsDao
    abstract fun cookingSessionDao(): CookingSessionDao

    companion object {
        const val DATABASE_NAME = "cuisson_vapeur.db"
    }
}
