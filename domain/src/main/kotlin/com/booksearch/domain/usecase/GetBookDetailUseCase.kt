package com.booksearch.domain.usecase

import com.booksearch.domain.model.BookDetail
import com.booksearch.domain.repository.BookRepository
import javax.inject.Inject

class GetBookDetailUseCase @Inject constructor(
    private val repository: BookRepository,
) {
    suspend operator fun invoke(workId: String): BookDetail = repository.getBookDetail(workId)
}
