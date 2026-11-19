package com.booksearch.data.mapper

import com.booksearch.data.remote.dto.BookDocDto
import com.booksearch.data.remote.dto.KeyRefDto
import com.booksearch.data.remote.dto.WorkAuthorDto
import com.booksearch.data.remote.dto.WorkDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BookMappersTest {

    @Test
    fun `given a complete search doc, when mapping, then builds a book with the medium cover url`() {
        // Given
        val dto = BookDocDto(
            key = "/works/OL27482W",
            title = "The Hobbit",
            authorNames = listOf("J.R.R. Tolkien"),
            firstPublishYear = 1937,
            coverId = 14627509,
            editionCount = 482,
        )

        // When
        val book = dto.toDomain()!!

        // Then
        assertEquals("OL27482W", book.workId)
        assertEquals("The Hobbit", book.title)
        assertEquals(listOf("J.R.R. Tolkien"), book.authors)
        assertEquals(1937, book.firstPublishYear)
        assertEquals(482, book.editionCount)
        assertEquals("https://covers.openlibrary.org/b/id/14627509-M.jpg", book.coverUrl)
    }

    @Test
    fun `given a search doc without cover, when mapping, then cover url is null`() {
        // Given
        val dto = BookDocDto(key = "/works/OL1W", title = "No cover")

        // When / Then
        assertNull(dto.toDomain()!!.coverUrl)
    }

    @Test
    fun `given a search doc without title or key, when mapping, then it is discarded`() {
        assertNull(BookDocDto(key = "/works/OL1W", title = " ").toDomain())
        assertNull(BookDocDto(key = null, title = "Title").toDomain())
    }

    @Test
    fun `given a work, when mapping to detail, then year is parsed and subjects are cleaned`() {
        // Given
        val dto = WorkDto(
            title = "The Hobbit",
            description = "Bilbo's journey.\r\n\r\n----------\r\nSource notes",
            covers = listOf(-1, 14627509),
            firstPublishDate = "January 1938",
            subjects = listOf("Fantasy", "fantasy", "series:Middle_Earth", " Dragons "),
            authors = listOf(WorkAuthorDto(KeyRefDto("/authors/OL26320A"))),
        )

        // When
        val detail = dto.toDomain(workId = "OL27482W", authorNames = listOf("J.R.R. Tolkien"))

        // Then
        assertEquals(1938, detail.firstPublishYear)
        assertEquals("Bilbo's journey.", detail.description)
        assertEquals(listOf("Fantasy", "Dragons"), detail.subjects)
        assertEquals("https://covers.openlibrary.org/b/id/14627509-L.jpg", detail.coverUrl)
        assertEquals(listOf("OL26320A"), dto.authorIds())
    }

    @Test
    fun `given a year from search and a publish date, when mapping, then the search year wins`() {
        val detail = WorkDto(firstPublishDate = "January 1938").toDomain("OL1W", emptyList(), searchYear = 1937)
        assertEquals(1937, detail.firstPublishYear)
    }

    @Test
    fun `given a work without optional fields, when mapping to detail, then nullable fields are null`() {
        // When
        val detail = WorkDto().toDomain(workId = "OL1W", authorNames = emptyList())

        // Then
        assertNull(detail.firstPublishYear)
        assertNull(detail.description)
        assertNull(detail.coverUrl)
        assertEquals(emptyList<String>(), detail.subjects)
    }
}
