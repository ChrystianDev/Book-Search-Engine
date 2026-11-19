package com.booksearch.app.feature.home

import app.cash.turbine.test
import com.booksearch.app.testing.FakeBookRepository
import com.booksearch.app.testing.MainDispatcherRule
import com.booksearch.app.testing.aBook
import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.model.SearchResult
import com.booksearch.domain.usecase.GetHomeContentUseCase
import com.booksearch.domain.usecase.SearchBooksUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository()

    private fun createViewModel() = HomeViewModel(
        searchBooks = SearchBooksUseCase(repository),
        getHomeContent = GetHomeContentUseCase(repository),
        searchConfig = SearchConfig(debounceMillis = DEBOUNCE),
    )

    @Test
    fun `given a search in flight, when a new query is typed, then the first is cancelled and only the last results are shown`() = runTest {
        // Given
        val viewModel = createViewModel()

        viewModel.uiState.map { it.search }.distinctUntilChanged().test {
            assertEquals(SearchState.Idle, awaitItem())

            viewModel.onQueryChange("tolkien")
            assertEquals(SearchState.Loading("tolkien"), awaitItem())
            advanceTimeBy(DEBOUNCE + LATENCY / 2) // "tolkien" request is now in flight

            // When
            viewModel.onQueryChange("asimov")
            assertEquals(SearchState.Loading("asimov"), awaitItem())
            advanceUntilIdle()

            // Then
            assertEquals(
                SearchState.Success("asimov", listOf(aBook("asimov-1")), fromCache = false),
                awaitItem(),
            )
            expectNoEvents()
        }
        assertEquals(listOf("tolkien", "asimov"), repository.requestedSearches)
        assertEquals(listOf("tolkien"), repository.cancelledSearches)
        assertEquals(listOf("asimov"), repository.completedSearches)
    }

    @Test
    fun `given fast typing, when keystrokes arrive within the debounce window, then a single search is made`() = runTest {
        // Given
        val viewModel = createViewModel()

        // When
        listOf("t", "to", "tol", "tolk").forEach { query ->
            viewModel.onQueryChange(query)
            advanceTimeBy(DEBOUNCE / 3)
        }
        advanceUntilIdle()

        // Then
        assertEquals(listOf("tolk"), repository.requestedSearches)
        assertEquals(SearchState.Success("tolk", listOf(aBook("tolk-1")), false), viewModel.uiState.value.search)
    }

    @Test
    fun `given a search in flight, when cancel is pressed, then the request is cancelled and the state is Cancelled`() = runTest {
        // Given
        val viewModel = createViewModel()
        viewModel.onQueryChange("tolkien")
        advanceTimeBy(DEBOUNCE + LATENCY / 2)

        // When
        viewModel.onCancelSearch()
        advanceUntilIdle()

        // Then
        assertEquals(SearchState.Cancelled("tolkien"), viewModel.uiState.value.search)
        assertEquals("tolkien", viewModel.uiState.value.query)
        assertEquals(listOf("tolkien"), repository.cancelledSearches)
        assertTrue(repository.completedSearches.isEmpty())
    }

    @Test
    fun `given a cancelled search, when search again is pressed, then it runs immediately`() = runTest {
        // Given
        val viewModel = createViewModel()
        viewModel.onQueryChange("tolkien")
        advanceTimeBy(DEBOUNCE + 1)
        viewModel.onCancelSearch()

        // When
        viewModel.onRetrySearch()
        advanceUntilIdle()

        // Then
        assertEquals(listOf("tolkien", "tolkien"), repository.requestedSearches)
        assertTrue(viewModel.uiState.value.search is SearchState.Success)
    }

    @Test
    fun `given a search in flight, when the query is cleared, then it is cancelled and discovery is shown`() = runTest {
        // Given
        val viewModel = createViewModel()
        viewModel.onQueryChange("tolkien")
        advanceTimeBy(DEBOUNCE + LATENCY / 2)

        // When
        viewModel.onClearQuery()
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.uiState.value.query)
        assertEquals(SearchState.Idle, viewModel.uiState.value.search)
        assertEquals(listOf("tolkien"), repository.cancelledSearches)
    }

    @Test
    fun `given a query with only spaces, when typed, then no search is made and discovery stays visible`() = runTest {
        // Given
        val viewModel = createViewModel()

        // When
        viewModel.onQueryChange("   ")
        advanceUntilIdle()

        // Then
        assertEquals(SearchState.Idle, viewModel.uiState.value.search)
        assertTrue(repository.requestedSearches.isEmpty())
    }

    @Test
    fun `given results for a query, when only a trailing space is added, then the search is not repeated`() = runTest {
        // Given
        val viewModel = createViewModel()
        viewModel.onQueryChange("tolkien")
        advanceUntilIdle()

        // When
        viewModel.onQueryChange("tolkien ")
        advanceUntilIdle()

        // Then
        assertEquals(listOf("tolkien"), repository.requestedSearches)
        assertTrue(viewModel.uiState.value.search is SearchState.Success)
    }

    @Test
    fun `given the search returns no books, when it completes, then the state is Empty`() = runTest {
        // Given
        repository.searchResponse = { query -> SearchResult(query, emptyList(), fromCache = false) }
        val viewModel = createViewModel()

        // When
        viewModel.onQueryChange("zzzz")
        advanceUntilIdle()

        // Then
        assertEquals(SearchState.Empty("zzzz"), viewModel.uiState.value.search)
    }

    @Test
    fun `given the search fails, when retry is pressed, then the search is launched again`() = runTest {
        // Given
        repository.searchResponse = { throw DomainException(DomainError.NoConnection) }
        val viewModel = createViewModel()
        viewModel.onQueryChange("tolkien")
        advanceUntilIdle()
        assertEquals(SearchState.Error("tolkien", DomainError.NoConnection), viewModel.uiState.value.search)

        // When
        repository.searchResponse = { query -> SearchResult(query, listOf(aBook("ok")), fromCache = false) }
        viewModel.uiState.map { it.search }.distinctUntilChanged().test {
            skipItems(1) // current Error state
            viewModel.onRetrySearch()

            // Then
            assertEquals(SearchState.Loading("tolkien"), awaitItem())
            advanceUntilIdle()
            assertEquals(SearchState.Success("tolkien", listOf(aBook("ok")), false), awaitItem())
        }
        assertEquals(2, repository.requestedSearches.size)
    }

    @Test
    fun `given a cached result, when searching, then Success reports fromCache`() = runTest {
        // Given
        repository.searchResponse = { query -> SearchResult(query, listOf(aBook("cached")), fromCache = true) }
        val viewModel = createViewModel()

        // When
        viewModel.onQueryChange("tolkien")
        advanceUntilIdle()

        // Then
        val search = viewModel.uiState.value.search as SearchState.Success
        assertTrue(search.fromCache)
    }

    @Test
    fun `given the view model starts, when discovery loads, then trending and sections are shown`() = runTest {
        // When
        val viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val discovery = viewModel.uiState.value.discovery as DiscoveryState.Content
        assertEquals(listOf(aBook("trending")), discovery.trending)
        assertEquals(3, discovery.sections.size)
    }

    private companion object {
        const val DEBOUNCE = 300L
        const val LATENCY = 1_000L
    }
}
