package com.booksearch.data.cache

import org.junit.Assert.assertEquals
import org.junit.Test

class CacheConfigTest {

    @Test
    fun `given the production config, then the ttl is 3 minutes`() {
        // Given / When
        val config = CacheConfig()

        // Then: the test requires a TTL between 2 and 5 minutes; the README documents 3.
        assertEquals(3 * 60 * 1_000L, config.ttlMillis)
    }
}
