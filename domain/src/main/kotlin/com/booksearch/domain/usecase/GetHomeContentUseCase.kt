package com.booksearch.domain.usecase

import com.booksearch.domain.error.catchDomainError
import com.booksearch.domain.model.BookSubject
import com.booksearch.domain.model.HomeContent
import com.booksearch.domain.model.HomeSection
import com.booksearch.domain.repository.BookRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/**
 * Loads the trending carousel and every subject section in parallel.
 *
 * A failing block is simply left out so the home never fails because of a single row.
 * Only when every block fails the first error is thrown.
 */
class GetHomeContentUseCase @Inject constructor(
    private val repository: BookRepository,
) {
    suspend operator fun invoke(): HomeContent = coroutineScope {
        val trendingDeferred = async { catchDomainError { repository.getTrendingBooks() } }
        val sectionDeferreds = BookSubject.entries.map { subject ->
            async { catchDomainError { HomeSection(subject, repository.getBooksBySubject(subject)) } }
        }

        val trending = trendingDeferred.await()
        val sections = sectionDeferreds.awaitAll()

        val results = listOf(trending) + sections
        if (results.all { it.isFailure }) {
            throw results.first().exceptionOrNull()!!
        }

        HomeContent(
            trending = trending.getOrDefault(emptyList()),
            sections = sections.mapNotNull { it.getOrNull() }.filter { it.books.isNotEmpty() },
        )
    }
}
