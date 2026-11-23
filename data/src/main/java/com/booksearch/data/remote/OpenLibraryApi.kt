package com.booksearch.data.remote

import com.booksearch.data.remote.dto.AuthorDto
import com.booksearch.data.remote.dto.SearchResponseDto
import com.booksearch.data.remote.dto.SubjectResponseDto
import com.booksearch.data.remote.dto.TrendingResponseDto
import com.booksearch.data.remote.dto.WorkDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Open Library endpoints. Suspend functions: cancelling the coroutine cancels the OkHttp call. */
interface OpenLibraryApi {

    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("fields") fields: String = BOOK_LIST_FIELDS,
        @Query("limit") limit: Int = SEARCH_LIMIT,
    ): SearchResponseDto

    @GET("trending/daily.json")
    suspend fun getTrending(
        @Query("fields") fields: String = BOOK_LIST_FIELDS,
        @Query("limit") limit: Int = HOME_LIMIT,
    ): TrendingResponseDto

    @GET("subjects/{subject}.json")
    suspend fun getSubject(
        @Path("subject") subject: String,
        @Query("limit") limit: Int = HOME_LIMIT,
    ): SubjectResponseDto

    @GET("works/{workId}.json")
    suspend fun getWork(@Path("workId") workId: String): WorkDto

    @GET("authors/{authorId}.json")
    suspend fun getAuthor(@Path("authorId") authorId: String): AuthorDto

    companion object {
        const val BASE_URL = "https://openlibrary.org/"
        const val BOOK_LIST_FIELDS = "key,title,author_name,first_publish_year,cover_i,edition_count"
        const val SEARCH_LIMIT = 20
        const val HOME_LIMIT = 10
    }
}
