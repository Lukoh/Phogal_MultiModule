package com.goforer.phogal.data.datasource.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo

/**
 * One row = one photo belonging to one *feed*.
 *
 * A "feed" is any REST-backed, paged photo list the app renders:
 *  - keyword search      → feedKey = "search/<query>"
 *  - popular photos      → feedKey = "popular/<orderBy>"
 *  - a user's photos     → feedKey = "user/<username>"
 *
 * The same Unsplash photo may legitimately appear in several feeds, so the
 * primary key is a local auto-generated id and uniqueness is enforced per
 * (feedKey, photoId) pair. `localId` is monotonically increasing per insert,
 * which preserves the exact server-side ordering of each page — the paging
 * query simply orders by it.
 *
 * The full [Photo] payload is stored as a JSON column via
 * [com.goforer.phogal.data.datasource.local.room.converter.PhogalTypeConverters],
 * so the UI renders *exactly* what the API returned — Room being the
 * Single Source of Truth (SSOT).
 */
@Entity(
    tableName = "photo_feed",
    indices = [
        Index(value = ["feed_key", "photo_id"], unique = true),
        Index(value = ["feed_key"])
    ]
)
data class PhotoFeedEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id") val localId: Long = 0L,
    @ColumnInfo(name = "feed_key") val feedKey: String,
    @ColumnInfo(name = "photo_id") val photoId: String,
    @ColumnInfo(name = "photo") val photo: Photo,
    @ColumnInfo(name = "cached_at") val cachedAt: Long
) {
    companion object {
        fun searchFeedKey(query: String) = "search/${query.trim().lowercase()}"
        fun popularFeedKey(orderBy: String) = "popular/$orderBy"
        fun userFeedKey(username: String) = "user/$username"

        fun of(feedKey: String, photo: Photo, cachedAt: Long = System.currentTimeMillis()) =
            PhotoFeedEntity(
                feedKey = feedKey,
                photoId = photo.id,
                photo = photo,
                cachedAt = cachedAt
            )
    }
}
