package com.booksearch.data.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Long-lived scope for app-wide work (e.g. observing preferences). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

/** Extra OkHttp interceptors contributed per build type (debug tools, empty in release). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BuildTypeInterceptor

/** OkHttp client for cover images: same User-Agent, without debug tools such as demo latency. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ImageHttpClient
