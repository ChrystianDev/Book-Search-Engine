package com.booksearch.app.feature.home

/**
 * @property debounceMillis short on purpose (300 ms): long enough to group fast typing, short
 * enough that a slower request is often still in flight when the next one starts, so real
 * request cancellations happen (not just debounce discards).
 */
data class SearchConfig(
    val debounceMillis: Long = 300L,
)
