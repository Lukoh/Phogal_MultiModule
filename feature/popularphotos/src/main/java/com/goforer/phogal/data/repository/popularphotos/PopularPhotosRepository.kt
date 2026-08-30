package com.goforer.phogal.data.repository.popularphotos

import androidx.paging.PagingData
import com.goforer.phogal.data.model.remote.response.gallery.common.photo.Photo
import kotlinx.coroutines.flow.Flow

/**
 * Repository for loading the public "popular" feed from Unsplash, paged.
 */
interface PopularPhotosRepository {

    /**
     * @param orderBy   Unsplash order parameter (e.g. "popular", "latest")
     * @param pageSize  page size for [androidx.paging.PagingConfig]
     */
    fun popularPhotos(orderBy: String, pageSize: Int): Flow<PagingData<Photo>>
}