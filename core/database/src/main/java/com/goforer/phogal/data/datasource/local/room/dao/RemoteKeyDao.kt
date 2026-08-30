package com.goforer.phogal.data.datasource.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goforer.phogal.data.datasource.local.room.entity.RemoteKeyEntity

/**
 * DAO for per-feed pagination bookkeeping used by the RemoteMediator.
 */
@Dao
interface RemoteKeyDao {

    @Query("SELECT * FROM remote_key WHERE feed_key = :feedKey")
    suspend fun remoteKey(feedKey: String): RemoteKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(key: RemoteKeyEntity)

    @Query("DELETE FROM remote_key WHERE feed_key = :feedKey")
    suspend fun delete(feedKey: String)

    @Query("DELETE FROM remote_key WHERE feed_key LIKE :prefix || '%'")
    suspend fun clearByPrefix(prefix: String)

    @Query("DELETE FROM remote_key")
    suspend fun clearAll()
}
