package com.booksearch.app.di

/** Build-type facts injected into ViewModels, so they don't read BuildConfig directly (and stay testable). */
data class BuildInfo(
    val isDebug: Boolean,
)
