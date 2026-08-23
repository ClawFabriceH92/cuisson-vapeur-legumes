package com.trucdecomptable.cuissonvapeur.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.trucdecomptable.cuissonvapeur.R

/**
 * Top-bar gear opening Réglages — available on every main screen so settings
 * are never unreachable (fix 23/08/2026: they used to be only on Home, which
 * a stuck cooking session could make inaccessible).
 */
@Composable
fun SettingsAction(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.nav_reglages))
    }
}
