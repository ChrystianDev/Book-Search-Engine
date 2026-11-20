package com.booksearch.data.mapper

import com.booksearch.data.cache.room.CachedBook
import com.booksearch.data.cache.room.CachedBookDetail
import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail

/** Domain ⇄ disk cache format. See [CachedBook] for why the two are separate. */

internal fun Book.toCached(): CachedBook = CachedBook(
    workId = workId,
    title = title,
    authors = authors,
    firstPublishYear = firstPublishYear,
    editionCount = editionCount,
    coverUrl = coverUrl,
)

internal fun CachedBook.toDomain(): Book = Book(
    workId = workId,
    title = title,
    authors = authors,
    firstPublishYear = firstPublishYear,
    editionCount = editionCount,
    coverUrl = coverUrl,
)

internal fun BookDetail.toCached(): CachedBookDetail = CachedBookDetail(
    workId = workId,
    title = title,
    authors = authors,
    firstPublishYear = firstPublishYear,
    description = description,
    subjects = subjects,
    coverUrl = coverUrl,
)

internal fun CachedBookDetail.toDomain(): BookDetail = BookDetail(
    workId = workId,
    title = title,
    authors = authors,
    firstPublishYear = firstPublishYear,
    description = description,
    subjects = subjects,
    coverUrl = coverUrl,
)
