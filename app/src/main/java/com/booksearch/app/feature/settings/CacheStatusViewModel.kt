package com.booksearch.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksearch.domain.model.CachedSearch
import com.booksearch.domain.repository.CacheStatusRepository
import com.booksearch.domain.time.TimeProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Backs the "Info estado" sheet: the cached searches of the active backend with a live countdown.
 *
 * The cache is re-read on every tick, so the list follows what really happens while the sheet is
 * open (a repeated search restarts its countdown, an eviction removes a row, a backend switch
 * shows the other cache). The ticker only runs while the sheet collects the state.
 */
@HiltViewModel
class CacheStatusViewModel @Inject constructor(
    private val cacheStatusRepository: CacheStatusRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    val uiState: StateFlow<CacheStatusUiState> = flow {
        while (true) {
            emit(CacheStatusUiState.Content(loadItems()))
            delay(TICK_MILLIS)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = CacheStatusUiState.Loading,
    )

    private suspend fun loadItems(): List<CachedSearchItem> {
        val now = timeProvider.currentTimeMillis()
        return cacheStatusRepository.cachedSearches()
            .sortedWith(
                // Available first, expired at the end; newest first inside each group.
                compareBy<CachedSearch> { it.expiresAtMillis <= now }
                    .thenByDescending { it.expiresAtMillis }
                    .thenBy { it.query },
            )
            .map { CachedSearchItem(it.query, remainingSeconds(it.expiresAtMillis - now)) }
    }

    // Rounded up: a fresh entry shows 3:00 and 0:00 only appears once it has really expired.
    private fun remainingSeconds(remainingMillis: Long): Long =
        if (remainingMillis <= 0) 0 else (remainingMillis + 999) / 1_000

    private companion object {
        const val TICK_MILLIS = 1_000L
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

sealed interface CacheStatusUiState {
    data object Loading : CacheStatusUiState
    data class Content(val items: List<CachedSearchItem>) : CacheStatusUiState
}

data class CachedSearchItem(
    val query: String,
    val remainingSeconds: Long,
) {
    /** Expired entries are no longer served; they stay listed until the cache removes them. */
    val isExpired: Boolean get() = remainingSeconds == 0L
}
