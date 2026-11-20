package com.booksearch.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.booksearch.app.R
import com.booksearch.app.ui.common.messageRes
import com.booksearch.app.ui.component.BookCover
import com.booksearch.app.ui.component.COVER_ASPECT_RATIO
import com.booksearch.app.ui.component.MessageWithAction
import com.booksearch.app.ui.theme.coverSizes
import com.booksearch.app.ui.theme.spacing
import com.booksearch.domain.model.Book
import com.booksearch.domain.usecase.SearchBooksUseCase.Companion.MIN_QUERY_LENGTH

/** Results section. Loading only takes this area: the search field above stays usable. */
@Composable
fun SearchResultsContent(
    state: SearchState,
    onBookClick: (Book) -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        SearchState.Idle -> Unit
        is SearchState.Loading -> SearchLoading(state.query, onCancel, modifier)
        is SearchState.Success -> SearchResultList(state, onBookClick, modifier)
        is SearchState.Empty -> MessageWithAction(
            title = stringResource(R.string.search_empty_title, state.query),
            message = stringResource(R.string.search_empty_message),
            modifier = modifier,
        )
        is SearchState.Error -> MessageWithAction(
            title = stringResource(R.string.search_error_title),
            message = stringResource(state.error.messageRes()),
            actionLabel = stringResource(R.string.action_retry),
            onAction = onRetry,
            modifier = modifier,
        )
        is SearchState.TooShort -> MessageWithAction(
            title = stringResource(R.string.search_too_short_title, MIN_QUERY_LENGTH),
            message = stringResource(R.string.search_too_short_message),
            modifier = modifier,
        )
        is SearchState.Cancelled -> MessageWithAction(
            title = stringResource(R.string.search_cancelled_title),
            message = stringResource(R.string.search_cancelled_message, state.query),
            actionLabel = stringResource(R.string.search_again),
            onAction = onRetry,
            modifier = modifier,
        )
    }
}

@Composable
private fun SearchLoading(query: String, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        CircularProgressIndicator()
        Text(
            text = stringResource(R.string.search_loading, query),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        OutlinedButton(onClick = onCancel, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.search_cancel))
        }
    }
}

@Composable
private fun SearchResultList(state: SearchState.Success, onBookClick: (Book) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = MaterialTheme.spacing.xl),
    ) {
        item(key = "header") {
            ResultsHeader(state)
        }
        items(state.books, key = { it.workId }) { book ->
            BookListItem(book, onClick = { onBookClick(book) })
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun ResultsHeader(state: SearchState.Success) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.search_results_for, state.query),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (state.fromCache) {
            // Discreet hint so any user notices that recent searches come back instantly.
            AssistChip(
                onClick = {},
                enabled = false,
                label = { Text(stringResource(R.string.search_recent_results)) },
                leadingIcon = {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                },
                colors = AssistChipDefaults.assistChipColors(
                    disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    disabledLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                border = null,
            )
        }
    }
}

@Composable
private fun BookListItem(book: Book, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = stringResource(R.string.action_open_detail), onClick = onClick)
            .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCover(
            coverUrl = book.coverUrl,
            contentDescription = stringResource(R.string.cover_of, book.title),
            modifier = Modifier
                .width(MaterialTheme.coverSizes.small)
                .heightIn(min = MaterialTheme.coverSizes.small / COVER_ASPECT_RATIO),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (book.authors.isNotEmpty()) {
                Text(
                    text = book.authors.joinToString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BookMetadata(book)
        }
    }
}

@Composable
private fun BookMetadata(book: Book) {
    val parts = buildList {
        book.firstPublishYear?.let { add(it.toString()) }
        if (book.editionCount > 0) {
            add(pluralStringResource(R.plurals.edition_count, book.editionCount, book.editionCount))
        }
    }
    if (parts.isNotEmpty()) {
        Text(
            text = parts.joinToString(" · "),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
