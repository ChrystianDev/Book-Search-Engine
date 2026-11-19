package com.booksearch.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksearch.app.di.BuildInfo
import com.booksearch.domain.model.ColorPalette
import com.booksearch.domain.model.ThemeMode
import com.booksearch.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Changes are persisted in DataStore; MainActivity observes them, so they apply instantly. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    buildInfo: BuildInfo,
) : ViewModel() {

    private val showDemoLatency = buildInfo.isDebug

    val uiState: StateFlow<SettingsUiState> = preferencesRepository.preferences
        .map { preferences ->
            SettingsUiState(
                colorPalette = preferences.colorPalette,
                themeMode = preferences.themeMode,
                demoLatencyEnabled = preferences.demoLatencyEnabled,
                showDemoLatency = showDemoLatency,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(showDemoLatency = showDemoLatency),
        )

    fun onColorPaletteSelected(palette: ColorPalette) {
        viewModelScope.launch { preferencesRepository.setColorPalette(palette) }
    }

    fun onThemeModeSelected(themeMode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(themeMode) }
    }

    fun onDemoLatencyChanged(enabled: Boolean) {
        if (!showDemoLatency) return
        viewModelScope.launch { preferencesRepository.setDemoLatencyEnabled(enabled) }
    }
}

data class SettingsUiState(
    val colorPalette: ColorPalette = ColorPalette.LIBRARY,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val demoLatencyEnabled: Boolean = false,
    /** Demo latency is a debug-only tool: the option does not exist in release builds. */
    val showDemoLatency: Boolean = false,
)
