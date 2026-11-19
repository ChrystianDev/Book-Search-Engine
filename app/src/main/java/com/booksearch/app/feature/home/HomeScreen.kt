package com.booksearch.app.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.booksearch.app.R
import com.booksearch.app.ui.theme.spacing
import com.booksearch.domain.model.Book

@Composable
fun HomeScreen(
    onBookClick: (workId: String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onBookClick = { book -> onBookClick(book.workId) },
        onSettingsClick = onSettingsClick,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::onClearQuery,
        onCancelSearch = viewModel::onCancelSearch,
        onRetrySearch = viewModel::onRetrySearch,
        onRetryDiscovery = viewModel::onRetryDiscovery,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onBookClick: (Book) -> Unit,
    onSettingsClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onCancelSearch: () -> Unit,
    onRetrySearch: () -> Unit,
    onRetryDiscovery: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SearchField(
                query = uiState.query,
                onQueryChange = onQueryChange,
                onClear = onClearQuery,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.lg,
                    vertical = MaterialTheme.spacing.sm,
                ),
            )

            if (uiState.search == SearchState.Idle) {
                DiscoveryContent(
                    state = uiState.discovery,
                    onBookClick = onBookClick,
                    onRetry = onRetryDiscovery,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.md),
                )
            } else {
                SearchResultsContent(
                    state = uiState.search,
                    onBookClick = onBookClick,
                    onCancel = onCancelSearch,
                    onRetry = onRetrySearch,
                )
            }
        }
    }
}
