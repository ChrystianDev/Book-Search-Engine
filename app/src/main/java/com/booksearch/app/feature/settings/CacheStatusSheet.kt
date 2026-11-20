package com.booksearch.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.booksearch.app.R
import com.booksearch.app.ui.theme.spacing

/**
 * "Info estado": cached searches of the active backend with a live countdown.
 * The ticker runs only while this sheet is shown and the app is in the foreground.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CacheStatusSheet(
    onDismiss: () -> Unit,
    viewModel: CacheStatusViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        CacheStatusContent(uiState)
    }
}

@Composable
private fun CacheStatusContent(uiState: CacheStatusUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.lg)
            .padding(bottom = MaterialTheme.spacing.xl),
    ) {
        Text(stringResource(R.string.cache_status_title), style = MaterialTheme.typography.titleLarge)
        Text(
            text = stringResource(R.string.cache_status_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = MaterialTheme.spacing.xs, bottom = MaterialTheme.spacing.md),
        )
        when (uiState) {
            CacheStatusUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.lg),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is CacheStatusUiState.Content -> if (uiState.items.isEmpty()) {
                Text(
                    text = stringResource(R.string.cache_status_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.lg),
                )
            } else {
                LazyColumn {
                    items(uiState.items, key = { it.query }) { item ->
                        CachedSearchRow(item)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun CachedSearchRow(item: CachedSearchItem) {
    // Expired rows use the Material "disabled" treatment: no longer available, still listed.
    val color = if (item.isExpired) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val expiredLabel = stringResource(R.string.cache_status_expired)
    // No live region on purpose: announcing the countdown every second would flood TalkBack.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics(mergeDescendants = true) {
                if (item.isExpired) stateDescription = expiredLabel
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Text(
            text = item.query,
            style = MaterialTheme.typography.bodyLarge,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(
                R.string.cache_status_countdown,
                item.remainingSeconds / 60,
                item.remainingSeconds % 60,
            ),
            // Tabular figures keep the digits from shifting as the countdown changes.
            style = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum"),
            color = color,
        )
    }
}
