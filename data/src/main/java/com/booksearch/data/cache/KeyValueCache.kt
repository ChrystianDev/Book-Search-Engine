package com.booksearch.data.cache

/**
 * Minimal read/write cache contract implemented by both cache backends:
 * [InMemoryCache] (process memory) and [RoomCache] (local storage).
 *
 * The repository is written against this interface, so switching the active
 * backend at runtime is just choosing which implementation to hand out.
 */
interface KeyValueCache<K : Any, V : Any> {

    /** Returns the cached value, or null if it is absent or expired. */
    suspend fun get(key: K): V?

    suspend fun put(key: K, value: V)
}
