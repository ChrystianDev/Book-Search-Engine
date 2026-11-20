package com.booksearch.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Outcome colours for diagnostics (search log): success = teal, cancelled = amber, failed = red.
 * They are the same for every palette so the meaning never changes, with a light and a dark variant.
 * Cancelled is amber rather than deep orange because deep orange was too close to the failure red.
 *
 * Contrast against the surface: every colour reaches 4.5:1 except the light amber (≈3:1), so the
 * text label next to it carries the meaning. Always pair these colours with a text label.
 */
@Immutable
data class StatusColors(
    val success: Color,
    val cancelled: Color,
    val failed: Color,
)

internal val LightStatusColors = StatusColors(
    success = Color(0xFF00796B), // Teal 700
    cancelled = Color(0xFFD77914), // Amber, dark
    failed = Color(0xFFC62828), // Red 800
)

internal val DarkStatusColors = StatusColors(
    success = Color(0xFF4DB6AC), // Teal 300
    cancelled = Color(0xFFF3B231), // Amber, light
    failed = Color(0xFFEF9A9A), // Red 200
)

val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

val MaterialTheme.statusColors: StatusColors
    @Composable @ReadOnlyComposable get() = LocalStatusColors.current
