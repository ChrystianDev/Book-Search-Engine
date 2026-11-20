package com.booksearch.data.repository

import com.booksearch.data.LOG_TAG
import com.booksearch.data.cache.BookCacheStore
import com.booksearch.data.di.IoDispatcher
import com.booksearch.data.error.safeApiCall
import com.booksearch.data.mapper.authorIds
import com.booksearch.data.mapper.toDomain
import com.booksearch.data.remote.OpenLibraryApi
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.error.catchDomainError
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail
import com.booksearch.domain.model.BookSubject
import com.booksearch.domain.model.SearchResult
import com.booksearch.domain.repository.BookRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache-first repository: a valid cached entry is returned without touching the network;
 * a missing or expired one is fetched, stored and returned. Failed requests are never cached.
 *
 * The cache backend (memory or disk/Room) is chosen per operation by [BookCacheStore],
 * following the user preference set in Settings.
 */
@Singleton
class BookRepositoryImpl @Inject constructor(
    private val api: OpenLibraryApi,
    private val caches: BookCacheStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BookRepository {

    override suspend fun searchBooks(query: String): SearchResult = withContext(ioDispatcher) {
        val cacheKey = query.toCacheKey()
        val backend = caches.current()
        val cache = backend.search
        val source = backend.type.name.lowercase()

        val cached = cache.get(cacheKey)
        if (cached != null) {
            Timber.tag(LOG_TAG).d("cache hit (%s): \"%s\" (%d books)", source, cacheKey, cached.size)
            return@withContext SearchResult(query, cached, fromCache = true)
        }

        Timber.tag(LOG_TAG).d("cache miss (%s): \"%s\" -> network request", source, cacheKey)
        val books = logged("search \"$query\"") {
            api.search(query).docs.mapNotNull { it.toDomain() }
        }
        cache.put(cacheKey, books)
        SearchResult(query, books, fromCache = false)
    }

    override suspend fun getTrendingBooks(): List<Book> =
        cachedHomeList(TRENDING_KEY) {
            api.getTrending().works.mapNotNull { it.toDomain() }
        }

    override suspend fun getBooksBySubject(subject: BookSubject): List<Book> =
        cachedHomeList(subject.slug) {
            api.getSubject(subject.slug).works.mapNotNull { it.toDomain() }
        }

    override suspend fun getBookDetail(workId: String): BookDetail = withContext(ioDispatcher) {
        val cache = caches.current().detail
        cache.get(workId)?.let { return@withContext it }

        coroutineScope {
            // Many works lack `first_publish_date`; the search index always has the year.
            // Requested in parallel with the work, so it adds no latency.
            val searchYear = async { fetchFirstPublishYear(workId) }

            val work = logged("work $workId") { api.getWork(workId) }
            val authorIds = work.authorIds()
            val authorNames = fetchAuthorNames(authorIds)
            val detail = work.toDomain(workId, authorNames, searchYear.await())

            // Only cache complete details, so a transient author failure is retried next time.
            if (authorNames.size == authorIds.size) cache.put(workId, detail)
            detail
        }
    }

    /** Optional enrichment: a failure just means "no year from search" (cancellation still propagates). */
    private suspend fun fetchFirstPublishYear(workId: String): Int? =
        catchDomainError {
            logged("year $workId") {
                api.search(query = "key:/works/$workId", fields = YEAR_FIELDS, limit = 1)
            }
        }.getOrNull()?.docs?.firstOrNull()?.firstPublishYear

    /**
     * Resolves author names in parallel. A failing author is skipped instead of failing the
     * whole detail. Only DomainException is caught, so cancellation still propagates.
     */
    private suspend fun fetchAuthorNames(authorIds: List<String>): List<String> = coroutineScope {
        authorIds
            .map { authorId ->
                async {
                    catchDomainError { logged("author $authorId") { api.getAuthor(authorId) } }
                        .getOrNull()
                        ?.let { it.name ?: it.personalName }
                }
            }
            .awaitAll()
            .filterNotNull()
    }

    private suspend fun cachedHomeList(key: String, fetch: suspend () -> List<Book>): List<Book> =
        withContext(ioDispatcher) {
            val cache = caches.current().home
            cache.get(key) ?: logged("home \"$key\"") { fetch() }.also { cache.put(key, it) }
        }

    private suspend fun <T> logged(label: String, block: suspend () -> T): T {
        val result = try {
            safeApiCall(block)
        } catch (e: DomainException) {
            Timber.tag(LOG_TAG).w("network error: %s -> %s", label, e.error)
            throw e
        }
        Timber.tag(LOG_TAG).d("network success: %s", label)
        return result
    }

    private companion object {
        const val TRENDING_KEY = "trending"
        const val YEAR_FIELDS = "key,first_publish_year"
    }
}

/** Cache key: same results for "Tolkien " and "tolkien". */
internal fun String.toCacheKey(): String = trim().lowercase()

internal val BookSubject.slug: String
    get() = when (this) {
        BookSubject.SCIENCE_FICTION -> "science_fiction"
        BookSubject.HISTORY -> "history"
        BookSubject.PROGRAMMING -> "programming"
    }
