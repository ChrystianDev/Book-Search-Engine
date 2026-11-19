package com.booksearch.app.feature.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.model.BookDetail
import com.booksearch.domain.usecase.GetBookDetailUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** The work id comes from the type-safe [com.booksearch.app.navigation.DetailRoute] via assisted injection. */
@HiltViewModel(assistedFactory = DetailViewModel.Factory::class)
class DetailViewModel @AssistedInject constructor(
    @Assisted private val workId: String,
    private val getBookDetail: GetBookDetailUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            _uiState.value = try {
                DetailUiState.Content(getBookDetail(workId))
            } catch (e: DomainException) {
                DetailUiState.Error(e.error)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(workId: String): DetailViewModel
    }
}

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Content(val book: BookDetail) : DetailUiState
    data class Error(val error: DomainError) : DetailUiState
}
