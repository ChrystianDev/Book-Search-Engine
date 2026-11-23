package com.booksearch.app.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.booksearch.app.R
import com.booksearch.app.ui.common.messageRes
import com.booksearch.app.ui.component.BookCard
import com.booksearch.app.ui.component.BookCover
import com.booksearch.app.ui.component.MessageWithAction
import com.booksearch.app.ui.theme.coverSizes
import com.booksearch.app.ui.theme.spacing
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookSubject
import com.booksearch.domain.model.HomeSection

@Composable
fun DiscoveryContent(
    state: DiscoveryState,
    onBookClick: (Book) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        DiscoveryState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is DiscoveryState.Error -> MessageWithAction(
            title = stringResource(R.string.discovery_error_title),
            message = stringResource(state.error.messageRes()),
            actionLabel = stringResource(R.string.action_retry),
            onAction = onRetry,
            modifier = modifier,
        )

        is DiscoveryState.Content -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = MaterialTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl),
        ) {
            if (state.trending.isNotEmpty()) {
                item(key = "trending") {
                    SectionTitle(stringResource(R.string.discovery_trending))
                    TrendingCarousel(state.trending, onBookClick)
                }
            }
            items(state.sections, key = { it.subject.name }) { section ->
                SubjectRow(section, onBookClick)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrendingCarousel(books: List<Book>, onBookClick: (Book) -> Unit) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { books.size },
        preferredItemWidth = 180.dp,
        itemSpacing = MaterialTheme.spacing.sm,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg),
        modifier = Modifier
            .fillMaxWidth()
            .height(270.dp),
    ) { index ->
        val book = books[index]
        BookCover(
            coverUrl = book.coverUrl,
            contentDescription = stringResource(R.string.cover_of, book.title),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxSize()
                .maskClip(MaterialTheme.shapes.large)
                .clickable(onClickLabel = stringResource(R.string.action_open_detail)) { onBookClick(book) },
        )
    }
}

@Composable
private fun SubjectRow(section: HomeSection, onBookClick: (Book) -> Unit) {
    SectionTitle(stringResource(section.subject.titleRes()))
    LazyRow(
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        items(section.books, key = { it.workId }) { book ->
            BookCard(book = book, width = MaterialTheme.coverSizes.medium, onClick = { onBookClick(book) })
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier
            .padding(horizontal = MaterialTheme.spacing.lg)
            .padding(bottom = MaterialTheme.spacing.md)
            .semantics { heading() },
    )
}

private fun BookSubject.titleRes(): Int = when (this) {
    BookSubject.SCIENCE_FICTION -> R.string.subject_science_fiction
    BookSubject.HISTORY -> R.string.subject_history
    BookSubject.PROGRAMMING -> R.string.subject_programming
}
