package com.booksearch.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.booksearch.domain.model.ColorPalette
import com.booksearch.domain.model.ThemeMode

/**
 * App design system: brand palette (light/dark), Nunito Sans type scale, shapes, spacing and
 * the outcome colours of the diagnostics sheets ([StatusColors]).
 * Dynamic color is intentionally not used so the chosen brand palette is always respected.
 */
@Composable
fun BookSearchTheme(
    palette: ColorPalette = ColorPalette.LIBRARY,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = themeMode.isDarkTheme()

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalCoverSizes provides CoverSizes(),
        LocalStatusColors provides if (darkTheme) DarkStatusColors else LightStatusColors,
    ) {
        MaterialTheme(
            colorScheme = colorSchemeFor(palette, darkTheme),
            typography = BookSearchTypography,
            shapes = BookSearchShapes,
            content = content,
        )
    }
}

/** Resolves the user's theme choice against the system setting. */
@Composable
fun ThemeMode.isDarkTheme(): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}

fun colorSchemeFor(palette: ColorPalette, darkTheme: Boolean): ColorScheme = when (palette) {
    ColorPalette.LIBRARY -> if (darkTheme) LibraryDark else LibraryLight
    ColorPalette.FOREST -> if (darkTheme) ForestDark else ForestLight
    ColorPalette.GRAPHITE -> if (darkTheme) GraphiteDark else GraphiteLight
}
