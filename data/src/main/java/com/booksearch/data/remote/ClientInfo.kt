package com.booksearch.data.remote

/** Identifies the app to Open Library, as its API guidelines request. Provided by `:app`. */
data class ClientInfo(
    val appName: String,
    val appVersion: String,
    val contactUrl: String,
) {
    val userAgent: String get() = "$appName/$appVersion (+$contactUrl)"
}
