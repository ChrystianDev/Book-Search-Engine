package com.booksearch.data.fakes

import com.booksearch.data.remote.OpenLibraryApi
import com.booksearch.data.remote.dto.AuthorDto
import com.booksearch.data.remote.dto.BookDocDto
import com.booksearch.data.remote.dto.SearchResponseDto
import com.booksearch.data.remote.dto.SubjectResponseDto
import com.booksearch.data.remote.dto.TrendingResponseDto
import com.booksearch.data.remote.dto.WorkDto
import java.io.IOException

/** Hand-written fake that counts calls per endpoint, so tests can assert "no network call". */
class FakeOpenLibraryApi : OpenLibraryApi {

    val searchCalls = mutableListOf<String>()
    var searchFailure: Throwable? = null

    val works = mutableMapOf<String, WorkDto>()
    val authors = mutableMapOf<String, AuthorDto>()
    var workCalls = 0
        private set

    override suspend fun search(query: String, fields: String, limit: Int): SearchResponseDto {
        searchCalls += query
        searchFailure?.let { throw it }
        return SearchResponseDto(
            docs = listOf(BookDocDto(key = "/works/OL${searchCalls.size}W", title = "$query #${searchCalls.size}")),
        )
    }

    override suspend fun getTrending(fields: String, limit: Int) = TrendingResponseDto()

    override suspend fun getSubject(subject: String, limit: Int) = SubjectResponseDto()

    override suspend fun getWork(workId: String): WorkDto {
        workCalls++
        return works[workId] ?: throw IOException("unknown work $workId")
    }

    override suspend fun getAuthor(authorId: String): AuthorDto =
        authors[authorId] ?: throw IOException("unknown author $authorId")
}
