package com.booksearch.domain.fixtures

import com.booksearch.domain.model.Book
import com.booksearch.domain.model.BookDetail

fun aBook(id: String = "OL1W", title: String = "The Hobbit") = Book(
    workId = id,
    title = title,
    authors = listOf("J.R.R. Tolkien"),
    firstPublishYear = 1937,
    coverUrl = null,
)

fun aBookDetail(id: String = "OL1W") = BookDetail(
    workId = id,
    title = "The Hobbit",
    authors = listOf("J.R.R. Tolkien"),
    firstPublishYear = 1937,
    description = "A hobbit goes on an adventure.",
    subjects = listOf("Fantasy"),
    coverUrl = null,
)
