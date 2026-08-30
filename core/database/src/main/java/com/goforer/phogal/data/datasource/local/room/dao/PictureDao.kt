package com.goforer.phogal.data.datasource.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.goforer.phogal.data.datasource.local.room.entity.PictureEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for single-photo detail records.
 *
 * [observePicture] is the SSOT stream for the picture-viewer screen: a Room
 * observable query returning a cold [Flow] that re-emits the row every time it
 * changes (network refresh, like/unlike patch, …). The repository merges the
 * network refresh into this stream with a `flow { }` builder, so the ViewModel
 * only ever collects one Flow whose origin is the local database.
 */
@Dao
interface PictureDao {

    /** Observable single row — emits `null` while nothing is cached yet. */
    @Query("SELECT * FROM picture WHERE id = :id")
    fun observePicture(id: String): Flow<PictureEntity?>

    /** One-shot read used for cache checks and read-modify-write patches. */
    @Query("SELECT * FROM picture WHERE id = :id")
    suspend fun getPicture(id: String): PictureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PictureEntity)

    @Query("DELETE FROM picture")
    suspend fun clearAll()
}
