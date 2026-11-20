package com.booksearch.data.cache

/**
 * Minimal read/write cache contract implemented by both cache backends:
 * [InMemoryCache] (process memory) and [RoomCache][com.booksearch.data.cache.room.RoomCache] (local storage).
 *
 * The repository is written against this interface, so switching the active
 * backend at runtime is just choosing which implementation to hand out.
 */
interface KeyValueCache<K : Any, V : Any> {

    /** Returns the cached value, or null if it is absent or expired. */
    suspend fun get(key: K): V?

    suspend fun put(key: K, value: V)

    /**
     * Every stored entry, expired ones included (expiration is lazy: they are only removed
     * when read). Read-only: it neither removes entries nor changes the eviction order.
     */
    suspend fun snapshot(): List<CacheSnapshotEntry<K, V>>
}

data class CacheSnapshotEntry<K, V>(
    val key: K,
    val value: V,
    val expiresAtMillis: Long,
)
