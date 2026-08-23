package com.trucdecomptable.cuissonvapeur.ui.screens.alarme

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trucdecomptable.cuissonvapeur.data.local.entity.AlarmSound
import com.trucdecomptable.cuissonvapeur.data.local.entity.SettingsEntity
import com.trucdecomptable.cuissonvapeur.data.repository.CookingSessionRepository
import com.trucdecomptable.cuissonvapeur.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * EF-23: owns the actual sound + vibration loop while [AlarmActivity] is on
 * screen (a `ViewModel` — not the receiver — so it's cleanly stopped via
 * [onCleared] whichever way the activity goes away).
 *
 * Sound: no custom sound assets are bundled in this v1 port (see root
 * README, D3 in "Limitations connues") — each of the 3 [AlarmSound] choices
 * maps to a distinct **system** ringtone/notification sound instead of a
 * bundled asset.
 */
@HiltViewModel
class AlarmViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val cookingSessionRepository: CookingSessionRepository,
) : ViewModel() {

    private var ringtone: Ringtone? = null
    private var loopJob: Job? = null

    fun startRinging() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            val settings = settingsRepository.observeSettings().first()
            playLoop(settings)
        }
    }

    private suspend fun playLoop(settings: SettingsEntity) {
        val uri = soundUriFor(settings.alarmSound)
        val tone = RingtoneManager.getRingtone(context, uri) ?: return
        ringtone = tone

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            tone.volume = settings.alarmVolume.coerceIn(0f, 1f)
        }

        if (settings.vibrationEnabled) startVibration(settings)

        val durationMillis = (settings.alarmDurationSeconds.coerceIn(
            SettingsEntity.ALARM_DURATION_MIN_SECONDS,
            SettingsEntity.ALARM_DURATION_MAX_SECONDS,
        ) * 1000).toLong()

        // EF-23: "son ≥ 3 s (répétable)" — keep re-triggering play() every
        // `alarmDurationSeconds` until the user dismisses or extends.
        while (true) {
            tone.play()
            delay(durationMillis)
            tone.stop()
            delay(200)
        }
    }

    private fun startVibration(settings: SettingsEntity) {
        val vibrator = vibratorService()
        val amplitude = (settings.vibrationIntensity.coerceIn(0f, 1f) * 255).toInt().coerceIn(1, 255)
        val pattern = longArrayOf(0, 500, 250, 500, 250)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = intArrayOf(0, amplitude, 0, amplitude, 0)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, 0)
        }
    }

    private fun vibratorService(): Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    private fun soundUriFor(sound: AlarmSound): Uri = when (sound) {
        AlarmSound.BIP -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        AlarmSound.CARILLON -> RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        AlarmSound.LONGUE_NOTE -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    }

    private fun stopRinging() {
        loopJob?.cancel()
        loopJob = null
        ringtone?.stop()
        ringtone = null
        vibratorService().cancel()
    }

    /** EF-23: "OK, c'est prêt" — dismiss alarm and end the session. */
    fun onDismiss(onDone: () -> Unit) {
        stopRinging()
        viewModelScope.launch {
            cookingSessionRepository.stop()
            onDone()
        }
    }

    /** EF-23/EF-24/D6: "Prolonger +2 min" from the alarm screen itself. */
    fun onExtend(minutes: Int, onDone: () -> Unit) {
        stopRinging()
        viewModelScope.launch {
            cookingSessionRepository.extend(minutes)
            onDone()
        }
    }

    override fun onCleared() {
        stopRinging()
        super.onCleared()
    }
}
