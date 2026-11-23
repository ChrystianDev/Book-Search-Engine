package com.booksearch.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.booksearch.domain.model.CacheType
import com.booksearch.domain.model.ColorPalette
import com.booksearch.domain.model.ThemeMode
import com.booksearch.domain.model.UserPreferences
import com.booksearch.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PreferencesRepository {

    override val preferences: Flow<UserPreferences> = dataStore.data
        // A corrupted/unreadable file falls back to defaults instead of crashing the app.
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { it.toUserPreferences() }
        .distinctUntilChanged()

    override suspend fun setOnboardingCompleted() {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = true }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    override suspend fun setColorPalette(colorPalette: ColorPalette) {
        dataStore.edit { it[Keys.COLOR_PALETTE] = colorPalette.name }
    }

    override suspend fun setDemoLatencyEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.DEMO_LATENCY] = enabled }
    }

    override suspend fun setCacheType(cacheType: CacheType) {
        dataStore.edit { it[Keys.CACHE_TYPE] = cacheType.name }
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val defaults = UserPreferences()
        return UserPreferences(
            onboardingCompleted = this[Keys.ONBOARDING_COMPLETED] ?: defaults.onboardingCompleted,
            themeMode = enumValueOrDefault(this[Keys.THEME_MODE], defaults.themeMode),
            colorPalette = enumValueOrDefault(this[Keys.COLOR_PALETTE], defaults.colorPalette),
            demoLatencyEnabled = this[Keys.DEMO_LATENCY] ?: defaults.demoLatencyEnabled,
            cacheType = enumValueOrDefault(this[Keys.CACHE_TYPE], defaults.cacheType),
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_PALETTE = stringPreferencesKey("color_palette")
        val DEMO_LATENCY = booleanPreferencesKey("demo_latency_enabled")
        val CACHE_TYPE = stringPreferencesKey("cache_type")
    }
}
