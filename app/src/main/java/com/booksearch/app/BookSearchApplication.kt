package com.booksearch.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class BookSearchApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Logs are a debug tool for following the search flow in Logcat; release builds stay silent.
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
