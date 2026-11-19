package com.booksearch.data.repository

import com.booksearch.data.cache.CacheConfig
import com.booksearch.data.fakes.FakeOpenLibraryApi
import com.booksearch.data.fakes.FakeTimeProvider
import com.booksearch.data.remote.dto.AuthorDto
import com.booksearch.data.remote.dto.KeyRefDto
import com.booksearch.data.remote.dto.WorkAuthorDto
import com.booksearch.data.remote.dto.WorkDto
import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class BookRepositoryDetailTest {

    private val api = FakeOpenLibraryApi()

    private fun TestScope.createRepository() = BookRepositoryImpl(
        api = api,
        timeProvider = FakeTimeProvider(),
        cacheConfig = CacheConfig(),
        ioDispatcher = StandardTestDispatcher(testScheduler),
    )

    private fun workWithAuthors(vararg ids: String) = WorkDto(
        title = "Good Omens",
        authors = ids.map { WorkAuthorDto(KeyRefDto("/authors/$it")) },
    )

    @Test
    fun `given a work with several authors, when loading the detail, then every author name is resolved`() = runTest {
        // Given
        api.works["OL1W"] = workWithAuthors("A1", "A2")
        api.authors["A1"] = AuthorDto(name = "Terry Pratchett")
        api.authors["A2"] = AuthorDto(personalName = "Neil Gaiman")

        // When
        val detail = createRepository().getBookDetail("OL1W")

        // Then
        assertEquals(listOf("Terry Pratchett", "Neil Gaiman"), detail.authors)
    }

    @Test
    fun `given a work without publish date, when loading the detail, then the year comes from the search index`() = runTest {
        // Given
        api.works["OL1W"] = workWithAuthors().copy(firstPublishDate = null)
        api.yearsByWorkKey["/works/OL1W"] = 1951

        // When
        val detail = createRepository().getBookDetail("OL1W")

        // Then
        assertEquals(1951, detail.firstPublishYear)
    }

    @Test
    fun `given one author fails, when loading the detail, then the other authors are still shown`() = runTest {
        // Given
        api.works["OL1W"] = workWithAuthors("A1", "MISSING")
        api.authors["A1"] = AuthorDto(name = "Terry Pratchett")

        // When
        val detail = createRepository().getBookDetail("OL1W")

        // Then
        assertEquals("Good Omens", detail.title)
        assertEquals(listOf("Terry Pratchett"), detail.authors)
    }

    @Test
    fun `given a complete detail was loaded, when loading it again, then it comes from the cache`() = runTest {
        // Given
        api.works["OL1W"] = workWithAuthors("A1")
        api.authors["A1"] = AuthorDto(name = "Terry Pratchett")
        val repository = createRepository()
        repository.getBookDetail("OL1W")

        // When
        repository.getBookDetail("OL1W")

        // Then
        assertEquals(1, api.workCalls)
    }

    @Test
    fun `given an author failed, when loading the detail again, then it is fetched again`() = runTest {
        // Given
        api.works["OL1W"] = workWithAuthors("MISSING")
        val repository = createRepository()
        repository.getBookDetail("OL1W")

        // When
        repository.getBookDetail("OL1W")

        // Then
        assertEquals(2, api.workCalls)
    }

    @Test
    fun `given the work request fails, when loading the detail, then a domain error is thrown`() = runTest {
        try {
            createRepository().getBookDetail("UNKNOWN")
            fail("Expected DomainException")
        } catch (e: DomainException) {
            assertEquals(DomainError.NoConnection, e.error)
        }
    }
}
