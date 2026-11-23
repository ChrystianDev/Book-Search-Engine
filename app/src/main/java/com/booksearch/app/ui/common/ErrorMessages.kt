package com.booksearch.app.ui.common

import androidx.annotation.StringRes
import com.booksearch.app.R
import com.booksearch.domain.error.DomainError

@StringRes
fun DomainError.messageRes(): Int = when (this) {
    DomainError.NoConnection -> R.string.error_no_connection
    DomainError.Timeout -> R.string.error_timeout
    is DomainError.Server -> R.string.error_server
    DomainError.Unknown -> R.string.error_unknown
}
