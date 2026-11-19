package com.booksearch.data.remote

import com.booksearch.data.remote.dto.SearchResponseDto
import com.booksearch.data.remote.dto.SubjectResponseDto
import com.booksearch.data.remote.dto.WorkDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtoDeserializationTest {

    private val json = NetworkJson.instance

    @Test
    fun `given description as a plain string, when decoding a work, then description is the string`() {
        // Given
        val body = """{ "title": "Hungry Caterpillar", "description": "A very hungry caterpillar." }"""

        // When
        val work = json.decodeFromString<WorkDto>(body)

        // Then
        assertEquals("A very hungry caterpillar.", work.description)
    }

    @Test
    fun `given description as a typed object, when decoding a work, then description is its value`() {
        // Given
        val body = """
            { "title": "The Hobbit",
              "description": { "type": "/type/text", "value": "In a hole in the ground..." } }
        """.trimIndent()

        // When
        val work = json.decodeFromString<WorkDto>(body)

        // Then
        assertEquals("In a hole in the ground...", work.description)
    }

    @Test
    fun `given a work without optional fields, when decoding, then defaults are used`() {
        // Given
        val body = """{ "key": "/works/OL1W" }"""

        // When
        val work = json.decodeFromString<WorkDto>(body)

        // Then
        assertNull(work.title)
        assertNull(work.description)
        assertNull(work.firstPublishDate)
        assertTrue(work.authors.isEmpty())
        assertTrue(work.covers.isEmpty())
        assertTrue(work.subjects.isEmpty())
    }

    @Test
    fun `given a work with author references, when decoding, then author keys are read`() {
        // Given
        val body = """
            { "authors": [ { "author": { "key": "/authors/OL26320A" }, "type": { "key": "/type/author_role" } } ] }
        """.trimIndent()

        // When
        val work = json.decodeFromString<WorkDto>(body)

        // Then
        assertEquals("/authors/OL26320A", work.authors.single().author?.key)
    }

    @Test
    fun `given a search doc with missing fields and unknown keys, when decoding, then defaults are used`() {
        // Given
        val body = """{ "numFound": 1, "docs": [ { "key": "/works/OL1W", "title": "T", "extra": 1 } ] }"""

        // When
        val doc = json.decodeFromString<SearchResponseDto>(body).docs.single()

        // Then
        assertTrue(doc.authorNames.isEmpty())
        assertNull(doc.coverId)
        assertNull(doc.firstPublishYear)
        assertEquals(0, doc.editionCount)
    }

    @Test
    fun `given an empty search, when decoding, then docs is an empty list`() {
        // Given
        val body = """{ "numFound": 0, "docs": [] }"""

        // When / Then
        assertTrue(json.decodeFromString<SearchResponseDto>(body).docs.isEmpty())
    }

    @Test
    fun `given a subject response, when decoding, then cover_id and author names are read`() {
        // Given
        val body = """
            { "works": [ { "key": "/works/OL138052W", "title": "Alice", "cover_id": 10527843,
                           "authors": [ { "key": "/authors/OL22098A", "name": "Lewis Carroll" } ] } ] }
        """.trimIndent()

        // When
        val work = json.decodeFromString<SubjectResponseDto>(body).works.single()

        // Then
        assertEquals(10527843L, work.coverId)
        assertEquals("Lewis Carroll", work.authors.single().name)
    }
}
