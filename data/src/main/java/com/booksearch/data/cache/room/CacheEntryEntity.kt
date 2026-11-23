package com.booksearch.data.cache.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

/**
 * A single cached value serialized as JSON.
 *
 * There is one table for everything; [scope] separates search results from home
 * sections and book details, mirroring the three [InMemoryCache][com.booksearch.data.cache.InMemoryCache]
 * instances used by the repository. The composite primary key is (scope, cache_key).
 */
@Entity(
    tableName = "cache_entries",
    primaryKeys = ["scope", "cache_key"],
    indices = [Index("stored_at_millis")],
)
data class CacheEntryEntity(
    val scope: String,
    @ColumnInfo(name = "cache_key") val cacheKey: String,
    val json: String,
    @ColumnInfo(name = "stored_at_millis") val storedAtMillis: Long,
)
