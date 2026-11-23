package com.booksearch.domain.time

/** Abstraction over the clock so time-based logic (cache expiration) can be tested without real waits. */
fun interface TimeProvider {
    fun currentTimeMillis(): Long
}
