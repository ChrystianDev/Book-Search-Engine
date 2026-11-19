package com.booksearch.domain.usecase

import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.fixtures.aBookDetail
import com.booksearch.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class GetBookDetailUseCaseTest {

    private val repository: BookRepository = mockk()
    private val useCase = GetBookDetailUseCase(repository)

    @Test
    fun `given a work id, when invoked, then returns the detail from the repository`() = runTest {
        // Given
        val expected = aBookDetail("OL27482W")
        coEvery { repository.getBookDetail("OL27482W") } returns expected

        // When
        val result = useCase("OL27482W")

        // Then
        assertSame(expected, result)
    }

    @Test
    fun `given the repository fails, when invoked, then the domain error is propagated`() = runTest {
        // Given
        coEvery { repository.getBookDetail(any()) } throws DomainException(DomainError.Server(500))

        // When
        try {
            useCase("OL27482W")
            fail("Expected DomainException")
        } catch (e: DomainException) {
            // Then
            assertEquals(DomainError.Server(500), e.error)
        }
    }
}
