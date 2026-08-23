package com.trucdecomptable.cuissonvapeur.ui.screens.reglages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trucdecomptable.cuissonvapeur.data.local.entity.AlarmSound
import com.trucdecomptable.cuissonvapeur.data.local.entity.SettingsEntity
import com.trucdecomptable.cuissonvapeur.data.local.entity.ThemeMode
import com.trucdecomptable.cuissonvapeur.data.repository.SettingsRepository
import com.trucdecomptable.cuissonvapeur.data.repository.VegetableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** EF-26/EF-27/EF-28/EF-29: alarm, theme, language and data-reset settings. */
@HiltViewModel
class ReglagesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val vegetableRepository: VegetableRepository,
) : ViewModel() {

    val settings: StateFlow<SettingsEntity> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsEntity())

    fun onVolumeChange(volume: Float) = update { it.copy(alarmVolume = volume.coerceIn(0f, 1f)) }

    fun onSoundChange(sound: AlarmSound) = update { it.copy(alarmSound = sound) }

    fun onVibrationEnabledChange(enabled: Boolean) = update { it.copy(vibrationEnabled = enabled) }

    fun onVibrationIntensityChange(intensity: Float) = update { it.copy(vibrationIntensity = intensity.coerceIn(0f, 1f)) }

    /** EF-26: alarm duration must stay within 3..30 s. */
    fun onAlarmDurationChange(seconds: Int) = update {
        it.copy(
            alarmDurationSeconds = seconds.coerceIn(
                SettingsEntity.ALARM_DURATION_MIN_SECONDS,
                SettingsEntity.ALARM_DURATION_MAX_SECONDS,
            ),
        )
    }

    fun onThemeModeChange(themeMode: ThemeMode) = update { it.copy(themeMode = themeMode) }

    fun onLanguageChange(languageTag: String) = update { it.copy(languageTag = languageTag) }

    /** EF-29: "réinitialisation des données locales" — favoris, sélection, réglages. */
    fun onResetLocalData() {
        viewModelScope.launch {
            vegetableRepository.clearCart()
            vegetableRepository.clearFavorites()
            settingsRepository.resetToDefaults()
        }
    }

    private fun update(transform: (SettingsEntity) -> SettingsEntity) {
        viewModelScope.launch { settingsRepository.update(transform) }
    }
}
