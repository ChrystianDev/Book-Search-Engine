package com.booksearch.domain.model

/** Discovery content shown on the home screen when there is no active search. */
data class HomeContent(
    val trending: List<Book>,
    val sections: List<HomeSection>,
)

data class HomeSection(
    val subject: BookSubject,
    val books: List<Book>,
)

/** Curated subjects for the home screen. Open Library has no "best sellers", so we use subjects. */
enum class BookSubject {
    SCIENCE_FICTION,
    HISTORY,
    PROGRAMMING,
}
