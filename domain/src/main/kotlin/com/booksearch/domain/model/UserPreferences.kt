package com.booksearch.domain.model

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorPalette: ColorPalette = ColorPalette.LIBRARY,
    val demoLatencyEnabled: Boolean = false,
    /** Where search/home/detail results are cached. Memory only lives while the process lives. */
    val cacheType: CacheType = CacheType.MEMORY,
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class ColorPalette { LIBRARY, FOREST, GRAPHITE }

/** MEMORY (default): survives only while the process lives. DISK: also survives a full app restart. */
enum class CacheType { MEMORY, DISK }
