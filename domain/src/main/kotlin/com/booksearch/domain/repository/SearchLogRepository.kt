package com.booksearch.domain.repository

import com.booksearch.domain.model.SearchLogEntry
import kotlinx.coroutines.flow.Flow

/** Read side of the session's search log (Settings → Peticiones / Búsquedas → Info estado). */
interface SearchLogRepository {

    /** The latest searches, newest first. Emits again whenever a search finishes. */
    val entries: Flow<List<SearchLogEntry>>

    fun clear()
}
