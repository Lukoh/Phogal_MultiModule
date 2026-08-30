package com.goforer.phogal.data.datasource.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.goforer.phogal.data.datasource.local.room.converter.PhogalTypeConverters
import com.goforer.phogal.data.datasource.local.room.dao.PhotoFeedDao
import com.goforer.phogal.data.datasource.local.room.dao.PictureDao
import com.goforer.phogal.data.datasource.local.room.dao.RemoteKeyDao
import com.goforer.phogal.data.datasource.local.room.entity.PhotoFeedEntity
import com.goforer.phogal.data.datasource.local.room.entity.PictureEntity
import com.goforer.phogal.data.datasource.local.room.entity.RemoteKeyEntity

/**
 * Phogal's local database — the Single Source of Truth (SSOT) for every
 * REST-fed dataset the UI renders.
 *
 * Data direction (Unidirectional Data Flow preserved end-to-end):
 *
 *   REST API (RestAPI.kt, suspend) ──▶ Room (this DB)
 *                                        │  observable queries (Flow / PagingSource)
 *                                        ▼
 *                       Repository ──▶ ViewModel (StateFlow) ──▶ View (Compose)
 *
 * The network layer only ever *writes into* this database; the presentation
 * layer only ever *reads from* it. Airplane-mode or server outages therefore
 * degrade to "stale but visible" rather than "blank screen".
 *
 * [PhogalTypeConverters] is a provided converter (constructor-injected Json),
 * registered in `DatabaseModule` via `addTypeConverter(...)`.
 */
@Database(
    entities = [
        PhotoFeedEntity::class,
        PictureEntity::class,
        RemoteKeyEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(PhogalTypeConverters::class)
abstract class PhogalDatabase : RoomDatabase() {
    abstract fun photoFeedDao(): PhotoFeedDao
    abstract fun pictureDao(): PictureDao
    abstract fun remoteKeyDao(): RemoteKeyDao

    companion object {
        const val NAME = "phogal.db"
    }
}
