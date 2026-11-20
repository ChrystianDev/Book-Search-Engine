package com.booksearch.app.testing

import com.booksearch.domain.model.SearchLogEntry
import com.booksearch.domain.repository.SearchLogRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSearchLogRepository(entries: List<SearchLogEntry> = emptyList()) : SearchLogRepository {

    override val entries = MutableStateFlow(entries)

    override fun clear() {
        entries.value = emptyList()
    }
}
