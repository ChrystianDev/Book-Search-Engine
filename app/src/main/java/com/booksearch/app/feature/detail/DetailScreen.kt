package com.booksearch.app.feature.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.booksearch.app.R
import com.booksearch.app.ui.common.messageRes
import com.booksearch.app.ui.component.BookCover
import com.booksearch.app.ui.component.COVER_ASPECT_RATIO
import com.booksearch.app.ui.component.MessageWithAction
import com.booksearch.app.ui.theme.coverSizes
import com.booksearch.app.ui.theme.spacing
import com.booksearch.domain.model.BookDetail

@Composable
fun DetailScreen(
    workId: String,
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel<DetailViewModel, DetailViewModel.Factory>(
        creationCallback = { factory -> factory.create(workId) },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DetailContent(uiState = uiState, onBack = onBack, onRetry = viewModel::onRetry)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailContent(uiState: DetailUiState, onBack: () -> Unit, onRetry: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                DetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is DetailUiState.Error -> MessageWithAction(
                    title = stringResource(R.string.detail_error_title),
                    message = stringResource(uiState.error.messageRes()),
                    actionLabel = stringResource(R.string.action_retry),
                    onAction = onRetry,
                    modifier = Modifier.align(Alignment.Center),
                )
                is DetailUiState.Content -> BookDetailBody(uiState.book)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BookDetailBody(book: BookDetail) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.xl)
            .padding(bottom = MaterialTheme.spacing.xxl),
    ) {
        BookCover(
            coverUrl = book.coverUrl,
            contentDescription = stringResource(R.string.cover_of, book.title),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(MaterialTheme.coverSizes.detail)
                .aspectRatio(COVER_ASPECT_RATIO),
        )

        Text(
            text = book.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.xl)
                .semantics { heading() },
        )
        Text(
            text = book.authors.joinToString().ifEmpty { stringResource(R.string.detail_unknown_author) },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MaterialTheme.spacing.sm),
        )
        book.firstPublishYear?.let { year ->
            Text(
                text = stringResource(R.string.detail_first_published, year),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.xs),
            )
        }

        SectionHeader(stringResource(R.string.detail_description))
        Text(
            text = book.description ?: stringResource(R.string.detail_no_description),
            style = MaterialTheme.typography.bodyLarge,
            color = if (book.description != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
        )

        if (book.subjects.isNotEmpty()) {
            SectionHeader(stringResource(R.string.detail_subjects))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                book.subjects.forEach { SubjectTag(it) }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier
            .padding(top = MaterialTheme.spacing.xl, bottom = MaterialTheme.spacing.sm)
            .semantics { heading() },
    )
}

/** Informative, non-interactive tag (a chip without click semantics). */
@Composable
private fun SubjectTag(text: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.md, vertical = MaterialTheme.spacing.xs + MaterialTheme.spacing.xxs),
        )
    }
}
