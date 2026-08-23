package com.trucdecomptable.cuissonvapeur.ui.screens.reglages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.trucdecomptable.cuissonvapeur.R
import com.trucdecomptable.cuissonvapeur.data.local.entity.AlarmSound
import com.trucdecomptable.cuissonvapeur.data.local.entity.ThemeMode

/** EF-26..EF-29: alarm sound/volume/vibration/duration, theme, language, data reset. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ReglagesScreen(onBack: () -> Unit, viewModel: ReglagesViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_reglages)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SectionTitle(stringResource(R.string.reglages_section_alarme))

            LabeledSlider(
                label = stringResource(R.string.reglages_volume),
                value = settings.alarmVolume,
                onValueChange = viewModel::onVolumeChange,
            )

            SoundPicker(selected = settings.alarmSound, onSelect = viewModel::onSoundChange)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.reglages_vibration))
                Switch(checked = settings.vibrationEnabled, onCheckedChange = viewModel::onVibrationEnabledChange)
            }

            if (settings.vibrationEnabled) {
                LabeledSlider(
                    label = stringResource(R.string.reglages_vibration_intensite),
                    value = settings.vibrationIntensity,
                    onValueChange = viewModel::onVibrationIntensityChange,
                )
            }

            LabeledSlider(
                label = stringResource(R.string.reglages_duree_alarme, settings.alarmDurationSeconds),
                value = (settings.alarmDurationSeconds - SETTINGS_RANGE_MIN).toFloat() / (SETTINGS_RANGE_MAX - SETTINGS_RANGE_MIN),
                onValueChange = { fraction ->
                    val seconds = SETTINGS_RANGE_MIN + (fraction * (SETTINGS_RANGE_MAX - SETTINGS_RANGE_MIN)).toInt()
                    viewModel.onAlarmDurationChange(seconds)
                },
            )

            HorizontalDivider()
            SectionTitle(stringResource(R.string.reglages_section_apparence))

            ThemePicker(selected = settings.themeMode, onSelect = viewModel::onThemeModeChange)

            HorizontalDivider()
            SectionTitle(stringResource(R.string.reglages_section_langue))
            LanguagePicker(selected = settings.languageTag, onSelect = viewModel::onLanguageChange)

            HorizontalDivider()
            SectionTitle(stringResource(R.string.reglages_section_donnees))
            Text(stringResource(R.string.reglages_privacy_note), style = MaterialTheme.typography.bodySmall)

            OutlinedButton(onClick = { showResetConfirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.reglages_reset_data))
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text(stringResource(R.string.reglages_reset_confirm_title)) },
            text = { Text(stringResource(R.string.reglages_reset_confirm_text)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onResetLocalData()
                    showResetConfirm = false
                }) { Text(stringResource(R.string.reglages_reset_confirm_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text(stringResource(R.string.action_annuler)) }
            },
        )
    }
}

private const val SETTINGS_RANGE_MIN = 3
private const val SETTINGS_RANGE_MAX = 30

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun LabeledSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Slider(value = value, onValueChange = onValueChange)
    }
}

@Composable
private fun SoundPicker(selected: AlarmSound, onSelect: (AlarmSound) -> Unit) {
    Column {
        Text(stringResource(R.string.reglages_son), style = MaterialTheme.typography.bodyMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AlarmSound.entries.forEach { sound ->
                FilterChip(
                    selected = selected == sound,
                    onClick = { onSelect(sound) },
                    label = { Text(soundLabel(sound)) },
                )
            }
        }
    }
}

@Composable
private fun soundLabel(sound: AlarmSound): String = when (sound) {
    AlarmSound.BIP -> stringResource(R.string.sound_bip)
    AlarmSound.CARILLON -> stringResource(R.string.sound_carillon)
    AlarmSound.LONGUE_NOTE -> stringResource(R.string.sound_longue_note)
}

@Composable
private fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { mode ->
            FilterChip(
                selected = selected == mode,
                onClick = { onSelect(mode) },
                label = { Text(themeLabel(mode)) },
            )
        }
    }
}

@Composable
private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.LIGHT -> stringResource(R.string.theme_clair)
    ThemeMode.DARK -> stringResource(R.string.theme_sombre)
    ThemeMode.SYSTEM -> stringResource(R.string.theme_systeme)
}

@Composable
private fun LanguagePicker(selected: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(if (selected == "en") stringResource(R.string.language_en) else stringResource(R.string.language_fr))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.language_fr)) }, onClick = { onSelect("fr"); expanded = false })
            DropdownMenuItem(text = { Text(stringResource(R.string.language_en)) }, onClick = { onSelect("en"); expanded = false })
        }
    }
}
