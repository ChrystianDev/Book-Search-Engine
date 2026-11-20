package com.booksearch.app.feature.home

/**
 * @property debounceMillis 600 ms: covers the natural pauses of typing on a phone keyboard, so a
 * word typed in one go ("python") becomes one search instead of several premature ones ("py",
 * "pyth"…). Real network cancellations still happen when the user pauses mid-word; the debug
 * demo latency makes them easy to show.
 */
data class SearchConfig(
    val debounceMillis: Long = 600L,
)
