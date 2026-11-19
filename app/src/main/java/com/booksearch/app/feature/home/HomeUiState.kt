package com.booksearch.app.feature.home

import com.booksearch.domain.error.DomainError
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.HomeSection

data class HomeUiState(
    val discovery: DiscoveryState = DiscoveryState.Loading,
)

/** Discovery content shown while there is no active search. */
sealed interface DiscoveryState {
    data object Loading : DiscoveryState
    data class Content(val trending: List<Book>, val sections: List<HomeSection>) : DiscoveryState
    data class Error(val error: DomainError) : DiscoveryState
}
