package com.booksearch.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.booksearch.domain.model.UserPreferences
import com.booksearch.domain.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** App-level state: preferences that drive the theme and the start destination. */
@HiltViewModel
class MainViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = preferencesRepository.preferences
        .map<UserPreferences, MainUiState> { MainUiState.Ready(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState.Loading)
}

sealed interface MainUiState {
    /** Preferences not read yet: the splash screen stays visible. */
    data object Loading : MainUiState

    data class Ready(val preferences: UserPreferences) : MainUiState
}
