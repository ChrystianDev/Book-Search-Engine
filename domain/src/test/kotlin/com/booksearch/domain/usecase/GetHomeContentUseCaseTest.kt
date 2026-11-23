package com.booksearch.domain.usecase

import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.fixtures.aBook
import com.booksearch.domain.model.BookSubject
import com.booksearch.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class GetHomeContentUseCaseTest {

    private val repository: BookRepository = mockk()
    private val useCase = GetHomeContentUseCase(repository)

    @Test
    fun `given every source succeeds, when invoked, then returns trending and one section per subject`() = runTest {
        // Given
        coEvery { repository.getTrendingBooks() } returns listOf(aBook("T1"))
        coEvery { repository.getBooksBySubject(any()) } returns listOf(aBook("S1"))

        // When
        val content = useCase()

        // Then
        assertEquals(listOf(aBook("T1")), content.trending)
        assertEquals(BookSubject.entries, content.sections.map { it.subject })
    }

    @Test
    fun `given one subject fails, when invoked, then the other blocks are still returned`() = runTest {
        // Given
        coEvery { repository.getTrendingBooks() } returns listOf(aBook("T1"))
        coEvery { repository.getBooksBySubject(any()) } returns listOf(aBook("S1"))
        coEvery { repository.getBooksBySubject(BookSubject.HISTORY) } throws DomainException(DomainError.Timeout)

        // When
        val content = useCase()

        // Then
        assertEquals(listOf(aBook("T1")), content.trending)
        assertEquals(
            listOf(BookSubject.SCIENCE_FICTION, BookSubject.PROGRAMMING),
            content.sections.map { it.subject },
        )
    }

    @Test
    fun `given every source fails, when invoked, then the domain error is propagated`() = runTest {
        // Given
        coEvery { repository.getTrendingBooks() } throws DomainException(DomainError.NoConnection)
        coEvery { repository.getBooksBySubject(any()) } throws DomainException(DomainError.NoConnection)

        // When
        try {
            useCase()
            fail("Expected DomainException")
        } catch (e: DomainException) {
            // Then
            assertEquals(DomainError.NoConnection, e.error)
        }
    }
}
