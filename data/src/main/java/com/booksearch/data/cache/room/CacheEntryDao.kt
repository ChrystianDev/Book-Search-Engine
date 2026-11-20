package com.booksearch.data.cache.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CacheEntryDao {

    @Query("SELECT * FROM cache_entries WHERE scope = :scope AND cache_key = :key")
    suspend fun get(scope: String, key: String): CacheEntryEntity?

    @Query("SELECT * FROM cache_entries WHERE scope = :scope")
    suspend fun getAll(scope: String): List<CacheEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: CacheEntryEntity)

    @Query("DELETE FROM cache_entries WHERE scope = :scope AND cache_key = :key")
    suspend fun delete(scope: String, key: String)

    @Query("SELECT COUNT(*) FROM cache_entries WHERE scope = :scope")
    suspend fun count(scope: String): Int

    /** Evicts the oldest entries of a scope, used to enforce the LRU limit. */
    @Query(
        """
        DELETE FROM cache_entries WHERE scope = :scope AND cache_key IN (
            SELECT cache_key FROM cache_entries WHERE scope = :scope
            ORDER BY stored_at_millis ASC LIMIT :count
        )
        """,
    )
    suspend fun deleteOldest(scope: String, count: Int)

    @Query("DELETE FROM cache_entries")
    suspend fun clearAll()
}
