package com.booksearch.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.usecase.GetHomeContentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeContent: GetHomeContentUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var discoveryJob: Job? = null

    init {
        loadDiscovery()
    }

    fun onRetryDiscovery() = loadDiscovery()

    private fun loadDiscovery() {
        discoveryJob?.cancel()
        discoveryJob = viewModelScope.launch {
            _uiState.update { it.copy(discovery = DiscoveryState.Loading) }
            val discovery = try {
                val content = getHomeContent()
                DiscoveryState.Content(content.trending, content.sections)
            } catch (e: DomainException) {
                DiscoveryState.Error(e.error)
            }
            _uiState.update { it.copy(discovery = discovery) }
        }
    }
}
