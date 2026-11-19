package com.booksearch.domain.repository

import com.booksearch.domain.model.CacheType
import com.booksearch.domain.model.ColorPalette
import com.booksearch.domain.model.ThemeMode
import com.booksearch.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    val preferences: Flow<UserPreferences>

    suspend fun setOnboardingCompleted()

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setColorPalette(colorPalette: ColorPalette)

    suspend fun setDemoLatencyEnabled(enabled: Boolean)

    suspend fun setCacheType(cacheType: CacheType)
}
