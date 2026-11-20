package com.booksearch.app.feature.settings

import app.cash.turbine.test
import com.booksearch.app.testing.FakeCacheStatusRepository
import com.booksearch.app.testing.MainDispatcherRule
import com.booksearch.domain.model.CachedSearch
import com.booksearch.domain.time.TimeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CacheStatusViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCacheStatusRepository()

    // The clock is the virtual test time, so advancing the scheduler also moves the countdown.
    private fun TestScope.createViewModel() =
        CacheStatusViewModel(repository, TimeProvider { testScheduler.currentTime })

    private fun TestScope.advanceSeconds(seconds: Long) {
        testScheduler.advanceTimeBy(seconds * 1_000)
        testScheduler.runCurrent()
    }

    @Test
    fun `given cached searches, when the sheet opens, then available ones come first newest first and expired ones last`() = runTest {
        // Given: the virtual clock starts at 0
        repository.searches = listOf(
            CachedSearch(query = "old", expiresAtMillis = 60_000),
            CachedSearch(query = "expired", expiresAtMillis = 0),
            CachedSearch(query = "new", expiresAtMillis = 180_000),
        )

        createViewModel().uiState.test {
            assertEquals(CacheStatusUiState.Loading, awaitItem())

            // When
            testScheduler.runCurrent()

            // Then
            assertEquals(
                CacheStatusUiState.Content(
                    listOf(
                        CachedSearchItem("new", 180),
                        CachedSearchItem("old", 60),
                        CachedSearchItem("expired", 0),
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `given an open sheet, when time passes, then the countdown ticks every second and an expired entry moves to the end`() = runTest {
        // Given
        repository.searches = listOf(
            CachedSearch(query = "newer", expiresAtMillis = 2_000),
            CachedSearch(query = "older", expiresAtMillis = 1_500),
        )

        createViewModel().uiState.test {
            skipItems(1) // Loading
            testScheduler.runCurrent()
            assertEquals(listOf(CachedSearchItem("newer", 2), CachedSearchItem("older", 2)), (awaitItem() as CacheStatusUiState.Content).items)

            // When / Then: one second later
            advanceSeconds(1)
            assertEquals(listOf(CachedSearchItem("newer", 1), CachedSearchItem("older", 1)), (awaitItem() as CacheStatusUiState.Content).items)

            // When / Then: both reach zero; expired rows stay listed, dimmed by the UI
            advanceSeconds(1)
            val items = (awaitItem() as CacheStatusUiState.Content).items
            assertEquals(listOf(true, true), items.map { it.isExpired })
        }
    }
}
