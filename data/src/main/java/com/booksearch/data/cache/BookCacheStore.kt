package com.booksearch.data.cache

import com.booksearch.data.LOG_TAG
import com.booksearch.data.cache.room.RoomBookCaches
import com.booksearch.domain.model.CacheType
import com.booksearch.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chooses which cache backend the repository uses, based on the user preference
 * (Settings → Caché). Defaults to memory; "disk" also survives a full app restart.
 *
 * The preference is read per operation, so a change in Settings applies to the next
 * request without restarting the app. The two backends are independent: entries are
 * not migrated, and each keeps its own TTL.
 */
@Singleton
class BookCacheStore @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val inMemory: InMemoryBookCaches,
    private val onDisk: RoomBookCaches,
) {
    // Logged only when the backend changes: the cache status panel reads it every second.
    @Volatile
    private var lastType: CacheType? = null

    suspend fun current(): BookCaches {
        val type = preferencesRepository.preferences.first().cacheType
        if (type != lastType) {
            lastType = type
            Timber.tag(LOG_TAG).d("cache backend: %s", type.name.lowercase())
        }
        return if (type == CacheType.DISK) onDisk else inMemory
    }
}
