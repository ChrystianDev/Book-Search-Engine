package com.booksearch.app.feature.settings

import app.cash.turbine.test
import com.booksearch.app.di.BuildInfo
import com.booksearch.app.testing.FakePreferencesRepository
import com.booksearch.app.testing.MainDispatcherRule
import com.booksearch.domain.model.CacheType
import com.booksearch.domain.model.ColorPalette
import com.booksearch.domain.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakePreferencesRepository()

    @Test
    fun `given the settings screen, when a palette and theme are selected, then they are persisted and exposed`() = runTest {
        // Given
        val viewModel = SettingsViewModel(repository, BuildInfo(isDebug = true))

        viewModel.uiState.test {
            assertEquals(SettingsUiState(showDemoLatency = true), awaitItem())

            // When
            viewModel.onColorPaletteSelected(ColorPalette.FOREST)
            viewModel.onThemeModeSelected(ThemeMode.DARK)

            testScheduler.advanceUntilIdle()

            // Then: StateFlow is conflated, so assert on the latest state
            val state = expectMostRecentItem()
            assertEquals(ColorPalette.FOREST, state.colorPalette)
            assertEquals(ThemeMode.DARK, state.themeMode)
        }
    }

    @Test
    fun `given the settings screen, when a cache type is selected, then it is persisted and exposed`() = runTest {
        // Given
        val viewModel = SettingsViewModel(repository, BuildInfo(isDebug = true))

        viewModel.uiState.test {
            assertEquals(CacheType.MEMORY, awaitItem().cacheType)

            // When
            viewModel.onCacheTypeSelected(CacheType.DISK)
            testScheduler.advanceUntilIdle()

            // Then
            assertEquals(CacheType.DISK, expectMostRecentItem().cacheType)
            assertEquals(CacheType.DISK, repository.preferences.value.cacheType)
        }
    }

    @Test
    fun `given a release build, when demo latency is toggled, then the option is hidden and ignored`() = runTest {
        // Given
        val viewModel = SettingsViewModel(repository, BuildInfo(isDebug = false))

        // When
        viewModel.onDemoLatencyChanged(true)
        testScheduler.advanceUntilIdle()

        // Then
        assertFalse(viewModel.uiState.value.showDemoLatency)
        assertFalse(repository.preferences.value.demoLatencyEnabled)
    }
}
