package com.trucdecomptable.cuissonvapeur.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.trucdecomptable.cuissonvapeur.data.local.entity.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Query("SELECT * FROM settings WHERE id = ${SettingsEntity.SETTINGS_ROW_ID}")
    fun observe(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = ${SettingsEntity.SETTINGS_ROW_ID}")
    suspend fun getOnce(): SettingsEntity?

    @Upsert
    suspend fun upsert(settings: SettingsEntity)

    /** EF-29: reset to defaults (part of "réinitialisation des données locales"). */
    @Query("DELETE FROM settings")
    suspend fun clear()
}
