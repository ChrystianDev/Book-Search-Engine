package com.booksearch.data.remote.interceptor

import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

/**
 * Logcat tag for the HTTP traffic. It is the tag OkHttp itself uses for `OkHttpClient` on
 * Android, so filtering Logcat by `okhttp.OkHttpClient` shows every request and response.
 */
internal const val HTTP_LOG_TAG = "okhttp.OkHttpClient"

/**
 * DEBUG ONLY: logs every API call with its headers and body: method and URL, request
 * headers/body, status code, timing, response headers and the full JSON response.
 * A cancelled call shows up as `<-- HTTP FAILED`.
 *
 * Messages go through Timber (only planted in debug), which splits lines longer than
 * Logcat's limit so big JSON responses are not truncated.
 */
internal fun httpLoggingInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor { message -> Timber.tag(HTTP_LOG_TAG).d(message) }
        .setLevel(HttpLoggingInterceptor.Level.BODY)
