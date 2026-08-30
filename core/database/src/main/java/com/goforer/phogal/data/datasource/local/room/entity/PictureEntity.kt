package com.goforer.phogal.data.datasource.local.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.goforer.phogal.data.model.remote.response.gallery.photo.photoinfo.Picture

/**
 * Detail record of a single photo (the `GET photos/{id}` payload).
 *
 * Keyed by the Unsplash photo id. The row is upserted every time the network
 * detail call succeeds, and *observed* by the UI through
 * [com.goforer.phogal.data.datasource.local.room.dao.PictureDao.observePicture] —
 * a Room observable query returning a cold [kotlinx.coroutines.flow.Flow].
 *
 * Because every mutation (fresh network data, like/unlike patches, …) goes
 * through this table, the detail screen keeps working offline and always shows
 * one consistent version of the truth (SSOT).
 */
@Entity(tableName = "picture")
data class PictureEntity(
    @PrimaryKey
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "picture") val picture: Picture,
    @ColumnInfo(name = "cached_at") val cachedAt: Long
) {
    companion object {
        fun of(picture: Picture, cachedAt: Long = System.currentTimeMillis()) =
            PictureEntity(id = picture.id, picture = picture, cachedAt = cachedAt)
    }
}
