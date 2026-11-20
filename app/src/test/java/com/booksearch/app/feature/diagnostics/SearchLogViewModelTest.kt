package com.booksearch.app.feature.diagnostics

import app.cash.turbine.test
import com.booksearch.app.di.BuildInfo
import com.booksearch.app.testing.FakeSearchLogRepository
import com.booksearch.app.testing.MainDispatcherRule
import com.booksearch.domain.error.DomainError
import com.booksearch.domain.model.SearchLogEntry
import com.booksearch.domain.model.SearchOutcome
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SearchLogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val success = SearchLogEntry(3, "tolkien", 3_000, SearchOutcome.Success(20, fromCache = false, durationMillis = 612))
    private val cancelled = SearchLogEntry(2, "dun", 2_000, SearchOutcome.Cancelled(afterMillis = 412))
    private val failed = SearchLogEntry(1, "getfail001", 1_000, SearchOutcome.Failed(DomainError.Server(404), durationMillis = 35))

    @Test
    fun `given logged searches, when the tab opens, then they are shown as logged`() = runTest {
        // Given
        val repository = FakeSearchLogRepository(listOf(success, cancelled, failed))

        // When / Then
        SearchLogViewModel(repository, BuildInfo(isDebug = false)).uiState.test {
            assertEquals(SearchLogUiState.Loading, awaitItem())
            assertEquals(SearchLogUiState.Content(listOf(success, cancelled, failed), showFailureHint = false), awaitItem())
        }
    }

    @Test
    fun `given the tab is open, when a new search finishes, then it appears at the top`() = runTest {
        // Given
        val repository = FakeSearchLogRepository(listOf(cancelled))
        val viewModel = SearchLogViewModel(repository, BuildInfo(isDebug = false))

        viewModel.uiState.test {
            assertEquals(SearchLogUiState.Loading, awaitItem())
            awaitItem()

            // When
            repository.entries.value = listOf(success, cancelled)

            // Then
            assertEquals(SearchLogUiState.Content(listOf(success, cancelled), showFailureHint = false), awaitItem())
        }
    }

    @Test
    fun `given entries, when clearing, then the list is empty`() = runTest {
        // Given
        val viewModel = SearchLogViewModel(FakeSearchLogRepository(listOf(success)), BuildInfo(isDebug = false))

        viewModel.uiState.test {
            assertEquals(SearchLogUiState.Loading, awaitItem())
            awaitItem()

            // When
            viewModel.onClear()

            // Then
            assertEquals(SearchLogUiState.Content(emptyList(), showFailureHint = false), awaitItem())
        }
    }

    @Test
    fun `given a debug build, when the tab opens, then the failure hint is shown`() = runTest {
        SearchLogViewModel(FakeSearchLogRepository(), BuildInfo(isDebug = true)).uiState.test {
            assertEquals(SearchLogUiState.Loading, awaitItem())
            assertEquals(SearchLogUiState.Content(emptyList(), showFailureHint = true), awaitItem())
        }
    }
}
