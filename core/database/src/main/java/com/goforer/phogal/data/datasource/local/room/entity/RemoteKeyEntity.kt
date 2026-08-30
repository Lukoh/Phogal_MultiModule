package com.goforer.phogal.data.datasource.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Pagination bookkeeping for a [PhotoFeedEntity] feed, one row per feedKey.
 *
 * The Unsplash list endpoints are positionally paged (page/per_page), so the
 * only state the [androidx.paging.RemoteMediator] must persist across process
 * death is "which page comes next" — plus a refresh timestamp used by
 * `RemoteMediator.initialize()` to decide whether the cache is still fresh
 * enough to skip the initial network refresh (true offline-first startup).
 */
@Entity(tableName = "remote_key")
data class RemoteKeyEntity(
    @PrimaryKey
    @ColumnInfo(name = "feed_key") val feedKey: String,
    /** Next page to request on APPEND; null = end of pagination reached. */
    @ColumnInfo(name = "next_page") val nextPage: Int?,
    /** Epoch millis of the last successful REFRESH for this feed. */
    @ColumnInfo(name = "last_refreshed_at") val lastRefreshedAt: Long
)
