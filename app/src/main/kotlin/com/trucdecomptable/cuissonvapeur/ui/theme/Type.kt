package com.trucdecomptable.cuissonvapeur.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Default Material 3 type scale, with `displayLarge` bumped up for the
 * active-timer countdown (NFR §4: "décompte ≥ 72 dp").
 */
val CuissonVapeurTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 72.sp,
        lineHeight = 80.sp,
    ),
)
