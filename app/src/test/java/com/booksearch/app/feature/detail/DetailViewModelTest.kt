package com.booksearch.app.feature.detail

import app.cash.turbine.test
import com.booksearch.app.testing.FakeBookRepository
import com.booksearch.app.testing.MainDispatcherRule
import com.booksearch.app.testing.aBookDetail
import com.booksearch.domain.error.DomainError
import com.booksearch.domain.error.DomainException
import com.booksearch.domain.usecase.GetBookDetailUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBookRepository()

    private fun createViewModel(workId: String = "OL1W") =
        DetailViewModel(workId, GetBookDetailUseCase(repository))

    @Test
    fun `given a work id, when the screen opens, then it shows loading and then the detail`() = runTest {
        // Given
        repository.detailLatencyMillis = 500

        // When
        val viewModel = createViewModel("OL1W")

        // Then
        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            advanceUntilIdle()
            assertEquals(DetailUiState.Content(aBookDetail("OL1W")), awaitItem())
        }
    }

    @Test
    fun `given the detail fails, when retry is pressed, then it loads again`() = runTest {
        // Given
        repository.detailResponse = { throw DomainException(DomainError.Timeout) }
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(DetailUiState.Error(DomainError.Timeout), viewModel.uiState.value)

        // When
        repository.detailResponse = { aBookDetail(it) }
        viewModel.onRetry()
        advanceUntilIdle()

        // Then
        assertEquals(DetailUiState.Content(aBookDetail("OL1W")), viewModel.uiState.value)
    }
}
