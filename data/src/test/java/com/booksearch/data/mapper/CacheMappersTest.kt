package com.booksearch.data.mapper

import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail
import org.junit.Assert.assertEquals
import org.junit.Test

class CacheMappersTest {

    @Test
    fun `given a book, when mapped to the cache model and back, then nothing is lost`() {
        // Given
        val book = Book("OL1W", "The Hobbit", listOf("J.R.R. Tolkien"), 1937, 42, null)

        // When / Then
        assertEquals(book, book.toCached().toDomain())
    }

    @Test
    fun `given a book detail, when mapped to the cache model and back, then nothing is lost`() {
        // Given
        val detail = BookDetail("OL1W", "The Hobbit", listOf("J.R.R. Tolkien"), null, "A hobbit.", listOf("Fantasy"), "https://covers/1.jpg")

        // When / Then
        assertEquals(detail, detail.toCached().toDomain())
    }
}
