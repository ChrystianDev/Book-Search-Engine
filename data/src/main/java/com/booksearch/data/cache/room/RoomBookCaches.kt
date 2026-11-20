package com.booksearch.data.cache.room

import com.booksearch.data.cache.BookCaches
import com.booksearch.data.cache.CacheConfig
import com.booksearch.data.cache.KeyValueCache
import com.booksearch.data.mapper.toCached
import com.booksearch.data.mapper.toDomain
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail
import com.booksearch.domain.time.TimeProvider
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * The disk backend: the same three caches, backed by Room.
 *
 * Values are stored as JSON using the cache models ([CachedBook], [CachedBookDetail]), never the
 * domain models directly. A value that fails to decode (e.g. written by an older
 * app version with a different model) is treated as absent instead of crashing.
 */
class RoomBookCaches @Inject constructor(
    dao: CacheEntryDao,
    timeProvider: TimeProvider,
    config: CacheConfig,
) : BookCaches {

    private val json = Json { ignoreUnknownKeys = true }

    override val search: KeyValueCache<String, List<Book>> = RoomCache(
        dao = dao,
        scope = SCOPE_SEARCH,
        timeProvider = timeProvider,
        ttlMillis = config.ttlMillis,
        maxEntries = config.maxEntries,
        encode = { books -> json.encodeToString(books.map { it.toCached() }) },
        decode = { runCatching { json.decodeFromString<List<CachedBook>>(it).map { it.toDomain() } }.getOrNull() },
    )

    override val home: KeyValueCache<String, List<Book>> = RoomCache(
        dao = dao,
        scope = SCOPE_HOME,
        timeProvider = timeProvider,
        ttlMillis = config.ttlMillis,
        maxEntries = config.maxEntries,
        encode = { books -> json.encodeToString(books.map { it.toCached() }) },
        decode = { runCatching { json.decodeFromString<List<CachedBook>>(it).map { it.toDomain() } }.getOrNull() },
    )

    override val detail: KeyValueCache<String, BookDetail> = RoomCache(
        dao = dao,
        scope = SCOPE_DETAIL,
        timeProvider = timeProvider,
        ttlMillis = config.ttlMillis,
        maxEntries = config.maxEntries,
        encode = { json.encodeToString(it.toCached()) },
        decode = { runCatching { json.decodeFromString<CachedBookDetail>(it).toDomain() }.getOrNull() },
    )

    private companion object {
        const val SCOPE_SEARCH = "search"
        const val SCOPE_HOME = "home"
        const val SCOPE_DETAIL = "detail"
    }
}
