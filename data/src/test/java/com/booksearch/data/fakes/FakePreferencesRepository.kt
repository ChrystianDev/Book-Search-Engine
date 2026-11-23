package com.booksearch.data.fakes

import com.booksearch.domain.model.CacheType
import com.booksearch.domain.model.ColorPalette
import com.booksearch.domain.model.ThemeMode
import com.booksearch.domain.model.UserPreferences
import com.booksearch.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakePreferencesRepository(initial: UserPreferences = UserPreferences()) : PreferencesRepository {

    private val state = MutableStateFlow(initial)
    override val preferences: StateFlow<UserPreferences> = state

    override suspend fun setOnboardingCompleted() = state.update { it.copy(onboardingCompleted = true) }
    override suspend fun setThemeMode(themeMode: ThemeMode) = state.update { it.copy(themeMode = themeMode) }
    override suspend fun setColorPalette(colorPalette: ColorPalette) = state.update { it.copy(colorPalette = colorPalette) }
    override suspend fun setDemoLatencyEnabled(enabled: Boolean) = state.update { it.copy(demoLatencyEnabled = enabled) }
    override suspend fun setCacheType(cacheType: CacheType) = state.update { it.copy(cacheType = cacheType) }
}
