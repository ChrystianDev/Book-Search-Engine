package com.booksearch.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.booksearch.data.di.ImageHttpClient
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BookSearchApplication : Application(), SingletonImageLoader.Factory {

    /** Shares the User-Agent with the API client, without debug tools like demo latency. */
    @Inject
    @ImageHttpClient
    lateinit var imageHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        // Logs are a debug tool for following the search flow in Logcat; release builds stay silent.
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory(callFactory = { imageHttpClient })) }
            .crossfade(true)
            .build()
}
