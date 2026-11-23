package com.booksearch.app.feature.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.booksearch.app.R
import com.booksearch.app.ui.theme.spacing
import com.booksearch.domain.model.Book

@Composable
fun HomeScreen(
    onBookClick: (workId: String) -> Unit,
    onSettingsClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current
    var showExitDialog by rememberSaveable { mutableStateOf(false) }

    // Home is the root destination: by default back (and the predictive back gesture) would
    // close the app straight away. Ask first. Disabled while the dialog is open, so back
    // dismisses the dialog instead.
    BackHandler(enabled = !showExitDialog) { showExitDialog = true }

    if (showExitDialog) {
        ExitAppDialog(
            onConfirm = {
                showExitDialog = false
                activity?.finish()
            },
            onDismiss = { showExitDialog = false },
        )
    }

    HomeContent(
        uiState = uiState,
        onBookClick = { book -> onBookClick(book.workId) },
        onSettingsClick = onSettingsClick,
        onDiagnosticsClick = onDiagnosticsClick,
        onQueryChange = viewModel::onQueryChange,
        onClearQuery = viewModel::onClearQuery,
        onCancelSearch = viewModel::onCancelSearch,
        onRetrySearch = viewModel::onRetrySearch,
        onRetryDiscovery = viewModel::onRetryDiscovery,
    )
}

/** Tapping outside the dialog, pressing back or "No" all dismiss it (AlertDialog defaults). */
@Composable
private fun ExitAppDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.exit_dialog_title), textAlign = TextAlign.Center) },
        text = { Text(stringResource(R.string.exit_dialog_message), textAlign = TextAlign.Center) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.exit_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.exit_dialog_dismiss)) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onBookClick: (Book) -> Unit,
    onSettingsClick: () -> Unit,
    onDiagnosticsClick: () -> Unit,
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
                    IconButton(onClick = onDiagnosticsClick) {
                        Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.diagnostics_title))
                    }
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
