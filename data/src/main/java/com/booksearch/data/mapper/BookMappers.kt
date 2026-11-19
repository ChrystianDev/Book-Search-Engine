package com.booksearch.data.mapper

import com.booksearch.data.remote.dto.BookDocDto
import com.booksearch.data.remote.dto.SubjectWorkDto
import com.booksearch.data.remote.dto.WorkDto
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail

private const val MAX_SUBJECTS = 12
private val YEAR_REGEX = Regex("""\b(\d{4})\b""")

/** Section separator Open Library editors use before "Contains"/source notes. */
private val DESCRIPTION_FOOTER_REGEX = Regex("""\n\s*-{3,}""")

/** Returns null for documents that cannot be shown (no id or no title). */
fun BookDocDto.toDomain(): Book? {
    val workId = key?.toWorkId() ?: return null
    val title = title?.takeIf { it.isNotBlank() } ?: return null
    return Book(
        workId = workId,
        title = title,
        authors = authorNames,
        firstPublishYear = firstPublishYear,
        editionCount = editionCount,
        coverUrl = coverId?.let { CoverUrls.medium(it) },
    )
}

fun SubjectWorkDto.toDomain(): Book? {
    val workId = key?.toWorkId() ?: return null
    val title = title?.takeIf { it.isNotBlank() } ?: return null
    return Book(
        workId = workId,
        title = title,
        authors = authors.mapNotNull { it.name },
        firstPublishYear = firstPublishYear,
        editionCount = editionCount,
        coverUrl = coverId?.let { CoverUrls.medium(it) },
    )
}

/** Author ids referenced by the work, e.g. `OL26320A`. */
fun WorkDto.authorIds(): List<String> =
    authors.mapNotNull { it.author?.key?.substringAfterLast('/') }.distinct()

/** @param searchYear year from the search index, preferred over parsing the free-text date. */
fun WorkDto.toDomain(workId: String, authorNames: List<String>, searchYear: Int? = null): BookDetail = BookDetail(
    workId = workId,
    title = title.orEmpty(),
    authors = authorNames,
    firstPublishYear = searchYear ?: firstPublishDate?.let(::parseYear),
    description = description?.let(::cleanDescription),
    subjects = cleanSubjects(subjects),
    coverUrl = covers.firstOrNull { it > 0 }?.let { CoverUrls.large(it) },
)

/** "/works/OL27482W" -> "OL27482W" */
internal fun String.toWorkId(): String? = substringAfterLast('/').takeIf { it.isNotBlank() }

/** `first_publish_date` is free text ("January 1938", "1954"…): extract the first 4-digit year. */
internal fun parseYear(date: String): Int? = YEAR_REGEX.find(date)?.groupValues?.get(1)?.toInt()

internal fun cleanDescription(raw: String): String? =
    raw.replace("\r\n", "\n")
        .split(DESCRIPTION_FOOTER_REGEX)
        .first()
        .trim()
        .takeIf { it.isNotEmpty() }

/** Drops machine tags like "series:Harry_Potter" and case-insensitive duplicates. */
internal fun cleanSubjects(subjects: List<String>): List<String> =
    subjects
        .map { it.trim() }
        .filter { it.isNotEmpty() && ':' !in it }
        .distinctBy { it.lowercase() }
        .take(MAX_SUBJECTS)

object CoverUrls {
    private const val BASE = "https://covers.openlibrary.org/b/id"
    fun medium(coverId: Long) = "$BASE/$coverId-M.jpg"
    fun large(coverId: Long) = "$BASE/$coverId-L.jpg"
}
