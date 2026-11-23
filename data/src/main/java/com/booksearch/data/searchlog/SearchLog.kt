package com.booksearch.data.searchlog

import com.booksearch.domain.error.DomainException
import com.booksearch.domain.model.SearchLogEntry
import com.booksearch.domain.model.SearchOutcome
import com.booksearch.domain.model.SearchResult
import com.booksearch.domain.repository.SearchLogRepository
import com.booksearch.domain.time.TimeProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

/**
 * In-memory log of the session's searches: the latest [MAX_ENTRIES], newest first.
 * It is a diagnostics tool, so it is not persisted and is lost when the process dies.
 *
 * The repository wraps each search in [track], which times it and records how it ended.
 */
@Singleton
class SearchLog @Inject constructor(
    private val timeProvider: TimeProvider,
) : SearchLogRepository {

    private val _entries = MutableStateFlow<List<SearchLogEntry>>(emptyList())
    override val entries: StateFlow<List<SearchLogEntry>> = _entries.asStateFlow()

    private val nextId = AtomicLong()

    override fun clear() = _entries.update { emptyList() }

    /**
     * Runs [search] and records its outcome. Only [DomainException] (a failure) and
     * [CancellationException] are caught, and both are rethrown untouched (rule 7):
     * logging never changes how the search ends. Any other throwable is a bug and is not logged.
     */
    suspend fun track(query: String, search: suspend () -> SearchResult): SearchResult {
        val startedAt = timeProvider.currentTimeMillis()
        fun elapsed() = timeProvider.currentTimeMillis() - startedAt

        val result = try {
            search()
        } catch (e: DomainException) {
            add(query, startedAt, SearchOutcome.Failed(e.error, elapsed()))
            throw e
        } catch (e: CancellationException) {
            add(query, startedAt, SearchOutcome.Cancelled(elapsed()))
            throw e
        }
        add(query, startedAt, SearchOutcome.Success(result.books.size, result.fromCache, elapsed()))
        return result
    }

    private fun add(query: String, startedAt: Long, outcome: SearchOutcome) {
        val entry = SearchLogEntry(nextId.incrementAndGet(), query, startedAt, outcome)
        _entries.update { (listOf(entry) + it).take(MAX_ENTRIES) }
    }

    companion object {
        const val MAX_ENTRIES = 50
    }
}
