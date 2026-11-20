package com.booksearch.domain.model

import com.booksearch.domain.error.DomainError

/**
 * One search the user ran during this session, for diagnostics (Settings → Peticiones / Búsquedas).
 *
 * [query] is the trimmed text as typed. [id] is unique per session and stable, so the UI can key
 * and expand rows. Searches discarded during the debounce never started, so they are not logged.
 */
data class SearchLogEntry(
    val id: Long,
    val query: String,
    val startedAtMillis: Long,
    val outcome: SearchOutcome,
)

sealed interface SearchOutcome {

    /** [fromCache]: served from the active cache, without a network request. */
    data class Success(
        val bookCount: Int,
        val fromCache: Boolean,
        val durationMillis: Long,
    ) : SearchOutcome

    /** Cancelled [afterMillis] after it started (new search, "Cancelar", X or leaving the screen). */
    data class Cancelled(val afterMillis: Long) : SearchOutcome

    data class Failed(
        val error: DomainError,
        val durationMillis: Long,
    ) : SearchOutcome
}
