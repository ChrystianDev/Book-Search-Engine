package com.booksearch.data.cache.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CacheEntryEntity::class], version = 1, exportSchema = false)
abstract class BookSearchDatabase : RoomDatabase() {
    abstract fun cacheEntryDao(): CacheEntryDao

    companion object {
        const val NAME = "booksearch_cache.db"
    }
}
