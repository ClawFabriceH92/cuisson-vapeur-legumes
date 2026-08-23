package com.trucdecomptable.cuissonvapeur.ui

import androidx.lifecycle.ViewModel
import com.trucdecomptable.cuissonvapeur.data.local.entity.ThemeMode
import com.trucdecomptable.cuissonvapeur.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Only reason this lives at the Activity level: EF-27's theme must wrap the whole nav graph. */
@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val themeMode: Flow<ThemeMode> = settingsRepository.observeSettings().map { it.themeMode }
}
