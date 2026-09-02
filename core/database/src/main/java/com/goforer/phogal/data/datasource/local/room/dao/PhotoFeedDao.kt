package com.goforer.phogal.data.datasource.local.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goforer.phogal.data.datasource.local.room.entity.PhotoFeedEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for the paged photo feeds (search / popular / user photos).
 *
 * SSOT wiring:
 *  - [pagingSource] is a *Room-generated observable PagingSource*. Paging 3 loads
 *    pages straight from the local DB; whenever the RemoteMediator writes fresh
 *    rows, Room invalidates the PagingSource and the UI re-renders automatically.
 *  - [observeFeed] exposes the same table as a plain observable [Flow] for any
 *    non-paged consumer — Room's Flow support ("Flow builder" at the DAO level)
 *    re-emits on every table change.
 *
 * The UI never talks to Retrofit; it only ever sees what is in this table.
 */
@Dao
interface PhotoFeedDao {

    /** Paged, DB-backed source of a feed — ordered exactly as the server returned it. */
    @Query("SELECT * FROM photo_feed WHERE feed_key = :feedKey ORDER BY local_id ASC")
    fun pagingSource(feedKey: String): PagingSource<Int, PhotoFeedEntity>

    /** Observable snapshot of a whole feed (non-paged consumers, widgets, tests). */
    @Query("SELECT * FROM photo_feed WHERE feed_key = :feedKey ORDER BY local_id ASC")
    fun observeFeed(feedKey: String): Flow<List<PhotoFeedEntity>>

    /**
     * REPLACE + unique(feed_key, photo_id): a photo that drifted between pages on the
     * server (a common Unsplash artifact) is not duplicated, and existing rows are
     * updated with the latest metadata.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<PhotoFeedEntity>)

    @Query("DELETE FROM photo_feed WHERE feed_key = :feedKey")
    suspend fun clearFeed(feedKey: String)

    /**
     * Clears all feeds starting with a certain prefix (e.g. 'search/').
     * Used to ensure only one search or one category's latest data is cached.
     */
    @Query("DELETE FROM photo_feed WHERE feed_key LIKE :prefix || '%'")
    suspend fun clearFeedsByPrefix(prefix: String)

    @Query("SELECT COUNT(local_id) FROM photo_feed WHERE feed_key = :feedKey")
    suspend fun countFeed(feedKey: String): Int

    @Query("DELETE FROM photo_feed")
    suspend fun clearAll()

    @Query("DELETE FROM photo_feed WHERE feed_key LIKE :prefix || '%' AND cached_at < :timestamp")
    suspend fun deleteOldFeedsByPrefix(prefix: String, timestamp: Long)
}
