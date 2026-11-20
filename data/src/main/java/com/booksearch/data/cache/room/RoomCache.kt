package com.booksearch.data.cache.room

import com.booksearch.data.cache.CacheSnapshotEntry
import com.booksearch.data.cache.KeyValueCache
import com.booksearch.domain.time.TimeProvider
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Disk-backed [KeyValueCache] on Room, mirroring the policy of [InMemoryCache][com.booksearch.data.cache.InMemoryCache]:
 * the same TTL per entry and the same LRU entry limit. Values are stored as JSON.
 *
 * Expiration is checked on read (like the in-memory cache): an expired entry is deleted and
 * treated as absent, so nothing needs to be cleaned up on app start. The write time is
 * persisted, so the TTL also applies across full app restarts.
 *
 * Keys are strings because they are stored as-is in the `cache_key` column.
 */
class RoomCache<V : Any>(
    private val dao: CacheEntryDao,
    private val scope: String,
    private val timeProvider: TimeProvider,
    private val ttlMillis: Long,
    private val maxEntries: Int,
    private val encode: (V) -> String,
    private val decode: (String) -> V?,
) : KeyValueCache<String, V> {

    private val mutex = Mutex()

    override suspend fun get(key: String): V? = mutex.withLock {
        val entry = dao.get(scope, key) ?: return@withLock null
        if (isExpired(entry)) {
            dao.delete(scope, key)
            null
        } else {
            decode(entry.json)
        }
    }

    override suspend fun put(key: String, value: V) = mutex.withLock {
        dao.upsert(
            CacheEntryEntity(
                scope = scope,
                cacheKey = key,
                json = encode(value),
                storedAtMillis = timeProvider.currentTimeMillis(),
            ),
        )
        evictOldest()
    }

    /** Entries that fail to decode are skipped, the same as [get] treats them as absent. */
    override suspend fun snapshot(): List<CacheSnapshotEntry<String, V>> = mutex.withLock {
        dao.getAll(scope).mapNotNull { entry ->
            decode(entry.json)?.let { CacheSnapshotEntry(entry.cacheKey, it, entry.storedAtMillis + ttlMillis) }
        }
    }

    private fun isExpired(entry: CacheEntryEntity): Boolean =
        timeProvider.currentTimeMillis() - entry.storedAtMillis >= ttlMillis

    private suspend fun evictOldest() {
        val overflow = dao.count(scope) - maxEntries
        if (overflow > 0) dao.deleteOldest(scope, overflow)
    }
}
