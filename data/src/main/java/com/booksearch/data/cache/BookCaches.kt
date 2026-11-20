
package com.booksearch.data.cache

import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail
import com.booksearch.domain.model.CacheType
import com.booksearch.domain.time.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The three caches the repository needs, one per cached kind of data.
 * Each backend (memory, disk) provides its own instance of this bundle.
 */
interface BookCaches {
    /** Which backend this is; shown in the logs so a cache hit says where it came from. */
    val type: CacheType

    val search: KeyValueCache<String, List<Book>>
    val home: KeyValueCache<String, List<Book>>
    val detail: KeyValueCache<String, BookDetail>
}

/** The default backend: the process-memory caches the app has always used. */
@Singleton
class InMemoryBookCaches @Inject constructor(
    private val timeProvider: TimeProvider,
    config: CacheConfig,
) : BookCaches {
    override val type = CacheType.MEMORY
    override val search = InMemoryCache<String, List<Book>>(timeProvider, config.ttlMillis, config.maxEntries)
    override val home = InMemoryCache<String, List<Book>>(timeProvider, config.ttlMillis, config.maxEntries)
    override val detail = InMemoryCache<String, BookDetail>(timeProvider, config.ttlMillis, config.maxEntries)
}
