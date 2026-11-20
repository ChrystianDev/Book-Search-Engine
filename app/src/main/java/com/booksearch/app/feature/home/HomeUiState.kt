package com.booksearch.app.feature.home

import com.booksearch.domain.error.DomainError
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.HomeSection

/**
 * Single immutable state for the home screen.
 *
 * @property query exactly what the user typed (the search field is driven by it).
 * @property search state of the results section; [SearchState.Idle] shows [discovery] instead.
 */
data class HomeUiState(
    val query: String = "",
    val search: SearchState = SearchState.Idle,
    val discovery: DiscoveryState = DiscoveryState.Loading,
)

/** Results section. Every non-idle state carries the (trimmed) query it belongs to. */
sealed interface SearchState {
    data object Idle : SearchState
    data class Loading(val query: String) : SearchState
    data class Success(val query: String, val books: List<Book>, val fromCache: Boolean) : SearchState
    data class Empty(val query: String) : SearchState
    data class Error(val query: String, val error: DomainError) : SearchState
    data class Cancelled(val query: String) : SearchState

    /** Fewer characters than the API accepts: nothing is searched, a hint is shown instead. */
    data class TooShort(val query: String) : SearchState
}

/** Discovery content shown while there is no active search. */
sealed interface DiscoveryState {
    data object Loading : DiscoveryState
    data class Content(val trending: List<Book>, val sections: List<HomeSection>) : DiscoveryState
    data class Error(val error: DomainError) : DiscoveryState
}
