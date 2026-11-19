package com.booksearch.domain.model

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.LIBRARY,
    val demoLatencyEnabled: Boolean = false,
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class ColorPalette { LIBRARY, FOREST, GRAPHITE }
