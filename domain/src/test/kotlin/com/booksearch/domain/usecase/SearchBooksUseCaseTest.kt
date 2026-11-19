package com.booksearch.domain.usecase

import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.fixtures.aBook
import com.booksearch.domain.model.SearchResult
import com.booksearch.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SearchBooksUseCaseTest {

    private val repository: BookRepository = mockk()
    private val useCase = SearchBooksUseCase(repository)

    @Test
    fun `given a query, when invoked, then delegates to the repository and returns its result`() = runTest {
        // Given
        val expected = SearchResult("tolkien", listOf(aBook()), fromCache = false)
        coEvery { repository.searchBooks("tolkien") } returns expected

        // When
        val result = useCase("tolkien")

        // Then
        assertSame(expected, result)
    }

    @Test
    fun `given a query with surrounding spaces, when invoked, then the repository receives it trimmed`() = runTest {
        // Given
        coEvery { repository.searchBooks(any()) } returns SearchResult("tolkien", emptyList(), false)

        // When
        useCase("  tolkien  ")

        // Then
        coVerify(exactly = 1) { repository.searchBooks("tolkien") }
    }

    @Test
    fun `given a blank query, when invoked, then returns an empty result without calling the repository`() = runTest {
        // When
        val result = useCase("   ")

        // Then
        assertTrue(result.books.isEmpty())
        assertEquals("", result.query)
        coVerify(exactly = 0) { repository.searchBooks(any()) }
    }

    @Test
    fun `given the repository fails, when invoked, then the domain error is propagated`() = runTest {
        // Given
        coEvery { repository.searchBooks(any()) } throws DomainException(DomainError.NoConnection)

        // When
        try {
            useCase("tolkien")
            fail("Expected DomainException")
        } catch (e: DomainException) {
            // Then
            assertEquals(DomainError.NoConnection, e.error)
        }
    }
}
