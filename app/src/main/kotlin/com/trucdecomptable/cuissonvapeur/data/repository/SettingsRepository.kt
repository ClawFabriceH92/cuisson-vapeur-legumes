package com.trucdecomptable.cuissonvapeur.data.repository

import com.trucdecomptable.cuissonvapeur.data.local.dao.SettingsDao
import com.trucdecomptable.cuissonvapeur.data.local.entity.SettingsEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** EF-26/EF-27/EF-28/EF-29: alarm, theme, language settings. */
@Singleton
class SettingsRepository @Inject constructor(
    private val settingsDao: SettingsDao,
) {

    /** Always emits a value — falls back to [SettingsEntity]'s defaults if no row exists yet. */
    fun observeSettings(): Flow<SettingsEntity> =
        settingsDao.observe().map { it ?: SettingsEntity() }

    suspend fun update(transform: (SettingsEntity) -> SettingsEntity) {
        val current = settingsDao.getOnce() ?: SettingsEntity()
        settingsDao.upsert(transform(current))
    }

    /** EF-29: part of "réinitialisation des données locales". */
    suspend fun resetToDefaults() {
        settingsDao.upsert(SettingsEntity())
    }
}
