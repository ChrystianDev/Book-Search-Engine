package com.booksearch.data.cache

import com.booksearch.domain.time.TimeProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe in-memory cache with a time-to-live per entry and a simple LRU size limit.
 *
 * Thread-safety uses a coroutine [Mutex] rather than a `ConcurrentHashMap` because every
 * operation is compound: `get` checks expiration, may remove the entry and updates the LRU
 * order (an access-ordered `LinkedHashMap` mutates on reads), and `put` may evict the eldest
 * entry. A single lock keeps those steps atomic, and `Mutex` suspends instead of blocking
 * threads. Contention is negligible: the critical sections are a few map operations.
 *
 * Time comes from an injected [TimeProvider], so expiration can be tested without waiting.
 */
class InMemoryCache<K : Any, V : Any>(
    private val timeProvider: TimeProvider,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
) {
    private class Entry<V>(val value: V, val storedAtMillis: Long)

    private val mutex = Mutex()
    private val entries = LinkedHashMap<K, Entry<V>>(16, 0.75f, /* accessOrder = */ true)

    init {
        require(ttlMillis > 0) { "ttlMillis must be positive" }
        require(maxEntries > 0) { "maxEntries must be positive" }
    }

    /** Returns the cached value, or null if it is absent or expired (expired entries are removed). */
    suspend fun get(key: K): V? = mutex.withLock {
        val entry = entries[key] ?: return@withLock null
        if (entry.isExpired()) {
            entries.remove(key)
            null
        } else {
            entry.value
        }
    }

    suspend fun put(key: K, value: V) = mutex.withLock {
        entries[key] = Entry(value, timeProvider.currentTimeMillis())
        if (entries.size > maxEntries) {
            val eldestKey = entries.keys.first()
            entries.remove(eldestKey)
        }
    }

    suspend fun size(): Int = mutex.withLock { entries.size }

    private fun Entry<V>.isExpired(): Boolean =
        timeProvider.currentTimeMillis() - storedAtMillis >= ttlMillis

    companion object {
        const val DEFAULT_TTL_MILLIS: Long = 3 * 60 * 1_000L
        const val DEFAULT_MAX_ENTRIES: Int = 50
    }
}
