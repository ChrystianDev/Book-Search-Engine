package com.booksearch.app.feature.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksearch.app.di.BuildInfo
import com.booksearch.domain.model.SearchLogEntry
import com.booksearch.domain.repository.SearchLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Backs the "Búsquedas" tab of Diagnóstico: the session's searches, newest first, updated live
 * while the tab is open (a search finishing in the background appears at the top).
 */
@HiltViewModel
class SearchLogViewModel @Inject constructor(
    private val searchLogRepository: SearchLogRepository,
    buildInfo: BuildInfo,
) : ViewModel() {

    // The failure trigger (getfail001) only exists in debug builds, so only they explain it.
    private val showFailureHint = buildInfo.isDebug

    val uiState: StateFlow<SearchLogUiState> = searchLogRepository.entries
        .map<List<SearchLogEntry>, SearchLogUiState> { SearchLogUiState.Content(it, showFailureHint) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SearchLogUiState.Loading,
        )

    fun onClear() = searchLogRepository.clear()

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

sealed interface SearchLogUiState {
    data object Loading : SearchLogUiState
    data class Content(
        val entries: List<SearchLogEntry>,
        val showFailureHint: Boolean,
    ) : SearchLogUiState
}
