package com.booksearch.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksearch.app.LOG_TAG
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.usecase.GetHomeContentUseCase
import com.booksearch.domain.usecase.SearchBooksUseCase
import com.booksearch.domain.usecase.SearchBooksUseCase.Companion.MIN_QUERY_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Search uses one explicitly managed [Job] (`searchJob`). Each new intention — a new query,
 * "Cancelar", clear (X) or retry — first cancels the previous job. Cancelling the job cancels
 * the debounce `delay` or the suspended Retrofit call, and therefore the OkHttp request.
 * When the screen goes away, `viewModelScope` cancels whatever is left.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchBooks: SearchBooksUseCase,
    private val getHomeContent: GetHomeContentUseCase,
    private val searchConfig: SearchConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var discoveryJob: Job? = null

    init {
        loadDiscovery()
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }

        val normalizedQuery = query.trim()
        when {
            // Only whitespace changed (e.g. a trailing space): the search is still valid.
            normalizedQuery == _uiState.value.search.queryOrNull() -> Unit
            normalizedQuery.isEmpty() -> {
                cancelSearch(reason = "query cleared")
                setSearchState(SearchState.Idle)
            }
            // The API rejects it (HTTP 422): cancel whatever was running and explain instead.
            normalizedQuery.length < MIN_QUERY_LENGTH -> {
                cancelSearch(reason = "query too short")
                setSearchState(SearchState.TooShort(normalizedQuery))
            }
            else -> startSearch(normalizedQuery, debounce = true)
        }
    }

    /** Clear (X) button: cancels any search and returns to discovery. */
    fun onClearQuery() {
        cancelSearch(reason = "clear button")
        _uiState.update { it.copy(query = "", search = SearchState.Idle) }
    }

    /** Explicit "Cancelar" button shown next to the loading indicator. */
    fun onCancelSearch() {
        if (cancelSearch(reason = "cancel button")) {
            val query = _uiState.value.query.trim()
            setSearchState(if (query.isEmpty()) SearchState.Idle else SearchState.Cancelled(query))
        }
    }

    /** "Reintentar" after an error, or "Buscar de nuevo" after a cancellation. No debounce. */
    fun onRetrySearch() {
        val query = _uiState.value.query.trim()
        if (query.length >= MIN_QUERY_LENGTH) startSearch(query, debounce = false)
    }

    fun onRetryDiscovery() = loadDiscovery()

    private fun startSearch(query: String, debounce: Boolean) {
        cancelSearch(reason = "new search \"$query\"")
        // Loading is shown right away: the results of a previous query are never left on screen.
        setSearchState(SearchState.Loading(query))

        var requestStarted = false
        searchJob = viewModelScope.launch {
            if (debounce) delay(searchConfig.debounceMillis)

            requestStarted = true
            Timber.tag(LOG_TAG).d("search started: \"%s\"", query)
            val result = try {
                val searchResult = searchBooks(query)
                if (searchResult.books.isEmpty()) {
                    SearchState.Empty(query)
                } else {
                    SearchState.Success(query, searchResult.books, searchResult.fromCache)
                }
            } catch (e: DomainException) {
                // Only domain failures become an error state; CancellationException is never caught.
                SearchState.Error(query, e.error)
            }

            // A cancelled coroutine cannot reach this point, but the guard makes the rule explicit:
            // only the result of the query currently being loaded may replace the state.
            _uiState.update { state ->
                if (state.search == SearchState.Loading(query)) state.copy(search = result) else state
            }
        }.apply {
            // Logged on completion so it also covers viewModelScope being cancelled when the screen goes away.
            invokeOnCompletion { cause ->
                if (cause is CancellationException) logCancellation(query, requestStarted, cause.message)
            }
        }
    }

    private fun logCancellation(query: String, requestStarted: Boolean, reason: String?) {
        if (requestStarted) {
            Timber.tag(LOG_TAG).d("search cancelled: \"%s\" in flight (reason: %s)", query, reason)
        } else {
            Timber.tag(LOG_TAG).v("search discarded during debounce: \"%s\" (reason: %s)", query, reason)
        }
    }

    /** Returns true if there was an active search to cancel. */
    private fun cancelSearch(reason: String): Boolean {
        val job = searchJob ?: return false
        searchJob = null
        if (!job.isActive) return false
        job.cancel(CancellationException(reason))
        return true
    }

    private fun setSearchState(search: SearchState) {
        _uiState.update { it.copy(search = search) }
    }

    private fun loadDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            _uiState.update { it.copy(discovery = DiscoveryState.Loading) }
            val discovery = try {
                val content = getHomeContent()
                DiscoveryState.Content(content.trending, content.sections)
            } catch (e: DomainException) {
                DiscoveryState.Error(e.error)
            }
            _uiState.update { it.copy(discovery = discovery) }
        }
    }
}

private fun SearchState.queryOrNull(): String? = when (this) {
    SearchState.Idle -> null
    is SearchState.Loading -> query
    is SearchState.Success -> query
    is SearchState.Empty -> query
    is SearchState.Error -> query
    is SearchState.Cancelled -> query
    is SearchState.TooShort -> query
}
