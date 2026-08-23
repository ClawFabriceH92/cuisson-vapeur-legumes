package com.trucdecomptable.cuissonvapeur.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Theme choice (EF-27). "SYSTEM" is the default per D8. */
enum class ThemeMode { LIGHT, DARK, SYSTEM }

/**
 * Alarm sound choice (EF-26/D3: "3 sons"). Real sound assets are not
 * bundled in this v1 port — see README "Limitations connues" (D3): each
 * option currently maps to a system default ringtone/notification sound
 * rather than a custom asset.
 */
enum class AlarmSound { BIP, CARILLON, LONGUE_NOTE }

/**
 * Single-row settings table (EF-26/EF-27/EF-28). Always read/written at
 * [SETTINGS_ROW_ID] — there is exactly one settings row for the whole app,
 * enforced by always using the same primary key.
 */
@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = SETTINGS_ROW_ID,
    /** EF-26: 0f (silent) .. 1f (max). Defaults to max per D3's recommendation. */
    val alarmVolume: Float = 1f,
    val alarmSound: AlarmSound = AlarmSound.CARILLON,
    val vibrationEnabled: Boolean = true,
    /** 0f (light) .. 1f (max) vibration intensity, where the platform supports it. */
    val vibrationIntensity: Float = 1f,
    /** EF-26: alarm duration in seconds, must stay within 3..30. */
    val alarmDurationSeconds: Int = 10,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** BCP-47 language tag, "fr" or "en" (EF-28). */
    val languageTag: String = "fr",
) {
    companion object {
        const val SETTINGS_ROW_ID = 0
        const val ALARM_DURATION_MIN_SECONDS = 3
        const val ALARM_DURATION_MAX_SECONDS = 30
    }
}
