package com.booksearch.data.fakes

import com.booksearch.domain.time.TimeProvider

class FakeTimeProvider(var nowMillis: Long = 0L) : TimeProvider {
    override fun currentTimeMillis(): Long = nowMillis

    fun advanceBy(millis: Long) {
        nowMillis += millis
    }
}
